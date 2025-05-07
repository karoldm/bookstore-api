package com.karoldm.bookstore.controllers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.karoldm.bookstore.dto.requests.BooksFilterDTO;
import com.karoldm.bookstore.dto.requests.RequestBookDTO;
import com.karoldm.bookstore.dto.requests.UpdateBookAvailableDTO;
import com.karoldm.bookstore.dto.responses.ResponseBookDTO;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.enums.Roles;
import com.karoldm.bookstore.mocks.BooksMock;
import com.karoldm.bookstore.repositories.AppUserRepository;
import com.karoldm.bookstore.security.SecurityConfig;
import com.karoldm.bookstore.security.SecurityFilter;
import com.karoldm.bookstore.services.BookService;
import com.karoldm.bookstore.services.StoreSecurityService;
import com.karoldm.bookstore.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, SecurityFilter.class, StoreSecurityService.class})
@AutoConfigureMockMvc(addFilters = true)
class BookControllerTest {
    @MockitoBean
    private BookService bookService;
    @MockitoBean
    private StoreSecurityService storeSecurityService;
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private AppUserRepository appUserRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SecurityFilter securityFilter;

    private final UUID testStoreId = UUID.randomUUID();
    private AppUser admin;
    private AppUser employee;
    private final String validToken = "valid-token";
    private AppUser wrongAdmin;
    private AppUser wrongEmployee;
    private AppUser commonUser;
    private Set<ResponseBookDTO> listBooks;
    private RequestBookDTO requestBookDTO;
    private ResponseBookDTO responseBookDTO;
    final private UUID testBookId = UUID.randomUUID();
    final private UpdateBookAvailableDTO updateBookAvailableDTO = UpdateBookAvailableDTO.builder()
            .available(false)
            .build();

    private ObjectMapper objectMapper;

    final private String baseURL = "/v1/store/" + testStoreId + "/book";

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        responseBookDTO = ResponseBookDTO.builder()
                .id(testBookId)
                .createdAt(LocalDate.of(2025, 1, 1))
                .releasedAt(LocalDate.of(1889, 5, 5))
                .author("author test")
                .title("book test")
                .summary("...")
                .rating(4)
                .available(false)
                .cover("")
                .build();

        listBooks = BooksMock.books.stream().map(book ->
                ResponseBookDTO.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .summary(book.getSummary())
                        .releasedAt(book.getReleasedAt())
                        .rating(book.getRating())
                        .available(book.isAvailable())
                        .cover(book.getCover())
                        .author(book.getAuthor())
                        .createdAt(book.getCreatedAt())
                        .build()
        ).collect(Collectors.toSet());

        commonUser = AppUser.builder()
                .id(UUID.randomUUID())
                .name("common user")
                .password("common_user")
                .username("common_user")
                .role(Roles.COMMON)
                .build();

        Store store = Store.builder()
                .id(testStoreId)
                .name("my store")
                .slogan("The best tech books")
                .banner(null)
                .build();

        admin = AppUser.builder()
                .name("admin")
                .role(Roles.ADMIN)
                .username("admin")
                .password("admin")
                .store(store)
                .build();

        employee = AppUser.builder()
                .name("employee")
                .role(Roles.EMPLOYEE)
                .username("employee")
                .password("employee")
                .store(store)
                .build();


        Store anotherStore = Store.builder()
                .id(UUID.randomUUID())
                .name("another store")
                .slogan("The best tech books")
                .banner(null)
                .build();

        wrongAdmin = AppUser.builder()
                .name("wrong admin")
                .role(Roles.ADMIN)
                .username("wrong_admin")
                .password("wrong_admin")
                .store(anotherStore)
                .build();

        wrongEmployee = AppUser.builder()
                .name("wrong employee")
                .role(Roles.EMPLOYEE)
                .username("wrong_employee")
                .password("wrong_employee")
                .store(anotherStore)
                .build();

        requestBookDTO = RequestBookDTO.builder()
                .title("book test")
                .summary("...")
                .cover("")
                .releasedAt(LocalDate.of(2025, 1, 1))
                .rating(4)
                .author("author test")
                .available(false)
                .build();
    }

    @Nested
    class ListBooksTest {

        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(get(baseURL))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any(String.class));
            verify(storeSecurityService, never()).canAccessStore(any(), any());
            verify(bookService, never()).listAll(
                    any(UUID.class),
                    any(Integer.class),
                    any(Integer.class),
                    any(BooksFilterDTO.class)
            );
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken(any(String.class)))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(get(baseURL)
                            .header("Authorization", "invalid-token"))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).canAccessStore(any(), any());
            verify(bookService, never()).listAll(
                    any(UUID.class),
                    any(Integer.class),
                    any(Integer.class),
                    any(BooksFilterDTO.class)
            );
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(get(baseURL)
                            .header("Authorization", validToken)
                    )
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).canAccessStore(any(), any());

            verify(bookService, never()).listAll(
                    any(UUID.class),
                    any(Integer.class),
                    any(Integer.class),
                    any(BooksFilterDTO.class)
            );
        }

        @Test
        void mustReturnForbiddenWhenAdminAccessesWrongBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.canAccessStore(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(get(baseURL)
                            .header("Authorization", validToken))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .canAccessStore(wrongAdmin, testStoreId);

            verify(bookService, never()).listAll(
                    any(UUID.class),
                    any(Integer.class),
                    any(Integer.class),
                    any(BooksFilterDTO.class)
            );
        }

        @Test
        void mustReturnOkWhenAdminAccessesOwnBook() throws Exception {
            BooksFilterDTO booksFilterDTO = BooksFilterDTO.builder().build();

            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.canAccessStore(admin, testStoreId)).thenReturn(true);

            when(bookService.listAll(testStoreId, 0, 10, booksFilterDTO))
                    .thenReturn(new HashSet<>(listBooks.stream().toList().subList(0, 10)));

            mockMvc.perform(get(baseURL)
                            .header("Authorization", validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(10));

            verify(bookService, times(1))
                    .listAll(testStoreId, 0, 10, booksFilterDTO);

            verify(storeSecurityService, times(1))
                    .canAccessStore(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());
        }

        @Test
        void mustReturnOkWhenAdminAccessesOwnBookWithPageAndSize() throws Exception {
            BooksFilterDTO booksFilterDTO = BooksFilterDTO.builder().build();

            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.canAccessStore(admin, testStoreId)).thenReturn(true);

            when(bookService.listAll(testStoreId, 0, 5, booksFilterDTO))
                    .thenReturn(new HashSet<>(listBooks.stream().toList().subList(0, 5)));

            mockMvc.perform(get(baseURL + "?size=5&page=0")
                            .header("Authorization", validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(5));

            verify(bookService, times(1))
                    .listAll(testStoreId, 0, 5, booksFilterDTO);

            verify(storeSecurityService, times(1))
                    .canAccessStore(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());
        }

        @Test
        void mustReturnForbiddenWhenEmployeeAccessesWrongBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            when(storeSecurityService.canAccessStore(wrongEmployee, testStoreId))
                    .thenReturn(false);

            mockMvc.perform(get(baseURL)
                            .header("Authorization", validToken))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, times(1))
                    .canAccessStore(wrongEmployee, testStoreId);

            verify(bookService, never()).listAll(
                    any(UUID.class),
                    any(Integer.class),
                    any(Integer.class),
                    any(BooksFilterDTO.class)
            );
        }

        @Test
        void mustReturnOkWhenEmployeeAccessesOwnBook() throws Exception {
            BooksFilterDTO booksFilterDTO = BooksFilterDTO.builder().build();

            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            when(storeSecurityService.canAccessStore(employee, testStoreId))
                    .thenReturn(true);

            when(bookService.listAll(testStoreId, 0, 5, booksFilterDTO))
                    .thenReturn(new HashSet<>(listBooks.stream().toList().subList(0, 5)));

            mockMvc.perform(get(baseURL + "?size=5&page=0")
                            .header("Authorization", validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(5));

            verify(bookService, times(1))
                    .listAll(testStoreId, 0, 5, booksFilterDTO);

            verify(storeSecurityService, times(1))
                    .canAccessStore(employee, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());
        }
    }

    @Nested
    class UpdateBookTests {
        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(put(baseURL + "/" + testBookId)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(bookService, never()).updateBook(testBookId, requestBookDTO);
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(put(baseURL + "/" + testBookId)
                            .header("Authorization", "invalid-token")
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(bookService, never()).updateBook(testBookId, requestBookDTO);
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(put(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).isStoreAdmin(any(), any());

            verify(bookService, never()).updateBook(testBookId, requestBookDTO);
        }

        @Test
        void mustReturnForbiddenWhenAdminUpdateWrongBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(put(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(wrongAdmin, testStoreId);

            verify(bookService, never()).updateBook(testBookId, requestBookDTO);
        }

        @Test
        void mustReturnOkWhenAdminUpdateOwnBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            when(bookService.updateBook(testBookId, requestBookDTO))
                    .thenReturn(responseBookDTO);

            mockMvc.perform(put(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("cover").value(responseBookDTO.getCover()))
                    .andExpect(jsonPath("title").value(responseBookDTO.getTitle()))
                    .andExpect(jsonPath("id").value(responseBookDTO.getId().toString()))
                    .andExpect(jsonPath("summary").value(responseBookDTO.getSummary()))
                    .andExpect(jsonPath("available").value(responseBookDTO.isAvailable()))
                    .andExpect(jsonPath("rating").value(responseBookDTO.getRating()))
                    .andExpect(jsonPath("releasedAt").value("05/05/1889"))
                    .andExpect(jsonPath("createdAt").value("01/01/2025"))
                    .andExpect(jsonPath("author").value(responseBookDTO.getAuthor()));

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(bookService, times(1))
                    .updateBook(testBookId, requestBookDTO);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenWhenEmployeeUpdateWrongBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            mockMvc.perform(put(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(wrongEmployee, testStoreId);

            verify(bookService, never()).updateBook(testBookId, requestBookDTO);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenWhenEmployeeUpdateOwnBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            mockMvc.perform(put(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(employee, testStoreId);

            verify(bookService, never()).updateBook(testBookId, requestBookDTO);
        }

        @Test
        void mustReturnBadRequestWhenUpdateWithWrongBody() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            RequestBookDTO invalidRequestBookDTO = RequestBookDTO.builder().build();

            mockMvc.perform(put(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(invalidRequestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(storeSecurityService, never()).isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(bookService, never()).updateBook(testBookId, invalidRequestBookDTO);
        }
    }

    @Nested
    class DeleteBookTests {
        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(delete(baseURL + "/" + testBookId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(bookService, never()).deleteBook(testBookId);
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(delete(baseURL + "/" + testBookId)
                            .header("Authorization", "invalid-token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(bookService, never()).deleteBook(testBookId);
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(delete(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).isStoreAdmin(any(), any());

            verify(bookService, never()).deleteBook(testBookId);
        }

        @Test
        void mustReturnForbiddenWhenAdminDeleteWrongBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(delete(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(wrongAdmin, testStoreId);

            verify(bookService, never()).deleteBook(testBookId);
        }

        @Test
        void mustReturnOkWhenAdminDeleteOwnBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            mockMvc.perform(delete(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(bookService, times(1))
                    .deleteBook(testBookId);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenWhenEmployeeDeleteWrongBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            mockMvc.perform(delete(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(wrongEmployee, testStoreId);

            verify(bookService, never()).deleteBook(testBookId);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenWhenEmployeeDeleteOwnBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            mockMvc.perform(delete(baseURL + "/" + testBookId)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(employee, testStoreId);

            verify(bookService, never()).deleteBook(testBookId);
        }
    }

    @Nested
    class ChangeBookAvailableTests {
        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(put(baseURL + "/" + testBookId + "/available")
                            .content(objectMapper.writeValueAsString(updateBookAvailableDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).canAccessStore(any(), any());
            verify(bookService, never()).changeAvailable(testBookId, updateBookAvailableDTO);
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(put(baseURL + "/" + testBookId + "/available")
                            .header("Authorization", "invalid-token")
                            .content(objectMapper.writeValueAsString(updateBookAvailableDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).canAccessStore(any(), any());
            verify(bookService, never()).changeAvailable(testBookId, updateBookAvailableDTO);
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(put(baseURL + "/" + testBookId + "/available")
                            .content(objectMapper.writeValueAsString(updateBookAvailableDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", validToken)
                    )
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).canAccessStore(any(), any());

            verify(bookService, never()).changeAvailable(testBookId, updateBookAvailableDTO);
        }

        @Test
        void mustReturnForbiddenWhenAdminChangeAvailableWrongBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(put(baseURL + "/" + testBookId + "/available")
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateBookAvailableDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .canAccessStore(wrongAdmin, testStoreId);

            verify(bookService, never()).changeAvailable(testBookId, updateBookAvailableDTO);
        }

        @Test
        void mustReturnOkWhenAdminChangeAvailableOwnBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            responseBookDTO.setAvailable(updateBookAvailableDTO.getAvailable());

            when(bookService.changeAvailable(testBookId, updateBookAvailableDTO))
                    .thenReturn(responseBookDTO);

            when(storeSecurityService.canAccessStore(admin, testStoreId)).thenReturn(true);

            mockMvc.perform(put(baseURL + "/" + testBookId + "/available")
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateBookAvailableDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("cover").value(responseBookDTO.getCover()))
                    .andExpect(jsonPath("title").value(responseBookDTO.getTitle()))
                    .andExpect(jsonPath("id").value(responseBookDTO.getId().toString()))
                    .andExpect(jsonPath("summary").value(responseBookDTO.getSummary()))
                    .andExpect(jsonPath("available").value(responseBookDTO.isAvailable()))
                    .andExpect(jsonPath("rating").value(responseBookDTO.getRating()))
                    .andExpect(jsonPath("releasedAt").value("05/05/1889"))
                    .andExpect(jsonPath("createdAt").value("01/01/2025"))
                    .andExpect(jsonPath("author").value(responseBookDTO.getAuthor()));

            verify(storeSecurityService, times(1))
                    .canAccessStore(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(bookService, times(1))
                    .changeAvailable(testBookId, updateBookAvailableDTO);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenWhenEmployeeChangeAvailableWrongBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            mockMvc.perform(put(baseURL + "/" + testBookId + "/available")
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateBookAvailableDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, times(1))
                    .canAccessStore(wrongEmployee, testStoreId);

            verify(bookService, never()).changeAvailable(testBookId, updateBookAvailableDTO);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnOkWhenEmployeeChangeAvailableOwnBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            responseBookDTO.setAvailable(updateBookAvailableDTO.getAvailable());

            when(bookService.changeAvailable(testBookId, updateBookAvailableDTO))
                    .thenReturn(responseBookDTO);

            when(storeSecurityService.canAccessStore(employee, testStoreId)).thenReturn(true);

            mockMvc.perform(put(baseURL + "/" + testBookId + "/available")
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateBookAvailableDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("cover").value(responseBookDTO.getCover()))
                    .andExpect(jsonPath("title").value(responseBookDTO.getTitle()))
                    .andExpect(jsonPath("id").value(responseBookDTO.getId().toString()))
                    .andExpect(jsonPath("summary").value(responseBookDTO.getSummary()))
                    .andExpect(jsonPath("available").value(responseBookDTO.isAvailable()))
                    .andExpect(jsonPath("rating").value(responseBookDTO.getRating()))
                    .andExpect(jsonPath("releasedAt").value("05/05/1889"))
                    .andExpect(jsonPath("createdAt").value("01/01/2025"))
                    .andExpect(jsonPath("author").value(responseBookDTO.getAuthor()));

            verify(storeSecurityService, times(1))
                    .canAccessStore(employee, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());

            verify(bookService, times(1))
                    .changeAvailable(testBookId, updateBookAvailableDTO);
        }

        @Test
        void mustReturnBadRequestWhenChangeAvailableWithWrongBody() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.canAccessStore(admin, testStoreId)).thenReturn(true);

            UpdateBookAvailableDTO invalidUpdateBookAvailableDTO1 =
                    UpdateBookAvailableDTO.builder().build();

            mockMvc.perform(put(baseURL + "/" + testBookId + "/available")
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(invalidUpdateBookAvailableDTO1))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(storeSecurityService, never()).canAccessStore(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(bookService, never()).changeAvailable(testBookId, invalidUpdateBookAvailableDTO1);
        }
    }

    @Nested
    class CreateBookTests {
        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(post(baseURL)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(bookService, never()).createBook(testStoreId, requestBookDTO);
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(post(baseURL)
                            .header("Authorization", "invalid-token")
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(bookService, never()).createBook(testStoreId, requestBookDTO);
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).isStoreAdmin(any(), any());

            verify(bookService, never()).createBook(testStoreId, requestBookDTO);
        }

        @Test
        void mustReturnForbiddenWhenAdminCreateWrongBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(wrongAdmin, testStoreId);

            verify(bookService, never()).createBook(testStoreId, requestBookDTO);
        }

        @Test
        void mustReturnOkWhenAdminCreateOwnBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            when(bookService.createBook(testStoreId, requestBookDTO))
                    .thenReturn(responseBookDTO);

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("cover").value(responseBookDTO.getCover()))
                    .andExpect(jsonPath("title").value(responseBookDTO.getTitle()))
                    .andExpect(jsonPath("id").value(responseBookDTO.getId().toString()))
                    .andExpect(jsonPath("summary").value(responseBookDTO.getSummary()))
                    .andExpect(jsonPath("available").value(responseBookDTO.isAvailable()))
                    .andExpect(jsonPath("rating").value(responseBookDTO.getRating()))
                    .andExpect(jsonPath("releasedAt").value("05/05/1889"))
                    .andExpect(jsonPath("createdAt").value("01/01/2025"))
                    .andExpect(jsonPath("author").value(responseBookDTO.getAuthor()));

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(bookService, times(1))
                    .createBook(testStoreId, requestBookDTO);
        }

        @Test
        void mustReturnForbiddenWhenEmployeeCreateWrongBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(wrongEmployee, testStoreId);

            verify(bookService, never()).createBook(testStoreId, requestBookDTO);
        }

        @Test
        void mustReturnForbiddenWhenEmployeeCreateOwnBook() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(requestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(employee, testStoreId);

            verify(bookService, never()).createBook(testStoreId, requestBookDTO);
        }

        @Test
        void mustReturnBadRequestWhenCreateWithWrongBody() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            RequestBookDTO invalidRequestBookDTO = RequestBookDTO.builder().build();

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(invalidRequestBookDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(storeSecurityService, never()).isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(bookService, never()).createBook(testStoreId, invalidRequestBookDTO);
        }
    }
}