package com.karoldm.bookstore.controllers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karoldm.bookstore.dto.responses.ResponseStoreDTO;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.enums.Roles;
import com.karoldm.bookstore.repositories.AppUserRepository;
import com.karoldm.bookstore.security.SecurityConfig;
import com.karoldm.bookstore.security.SecurityFilter;
import com.karoldm.bookstore.services.StoreSecurityService;
import com.karoldm.bookstore.services.StoreService;
import com.karoldm.bookstore.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
// use the created security config and filter to test the roles and routes access
// inject the storesecurityservice
@Import({SecurityConfig.class, SecurityFilter.class, StoreSecurityService.class})
@AutoConfigureMockMvc(addFilters = true)
class StoreControllerTest {

    @MockitoBean
    private StoreService storeService;
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

    private final Long testStoreId = 1L;
    private ResponseStoreDTO responseStoreDTO;
    private AppUser admin;
    private AppUser employee;
    final private String validToken = "valid-token";
    private AppUser wrongAdmin;
    private AppUser wrongEmployee;
    private AppUser commonUser;

    final private MockMultipartFile coverFile = new MockMultipartFile(
            "banner",
            "test-image.jpg",
            "image/jpeg",
            new byte[0]
    );
    final private MockPart namePart = new MockPart("name", "bookstore".getBytes());
    final private MockPart sloganPart = new MockPart("slogan", "An amazing bookstore".getBytes());


    @BeforeEach
    void setup() {
        commonUser = AppUser.builder()
                .id(2L)
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
                .id(3L)
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

        responseStoreDTO = ResponseStoreDTO.builder()
                .id(testStoreId)
                .name("my store")
                .banner(null)
                .slogan("The best tech books")
                .build();
    }

    @Nested
    class GetStoreTests {

        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(get("/v1/store/" + testStoreId))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).canAccessStore(any(), any());
            verify(storeService, never()).getStore(any());
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken(any(String.class)))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(get("/v1/store/" + testStoreId)
                            .header("Authorization", "invalid-token"))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).canAccessStore(any(), any());
            verify(storeService, never()).getStore(any());
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(get("/v1/store/" + testStoreId)
                            .header("Authorization", validToken)
                    )
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).canAccessStore(any(), any());

            verify(storeService, never()).getStore(testStoreId);
        }

        @Test
        void mustReturnForbiddenWhenAdminAccessesWrongStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.canAccessStore(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(get("/v1/store/" + testStoreId)
                            .header("Authorization", validToken))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .canAccessStore(wrongAdmin, testStoreId);

            verify(storeService, never()).getStore(testStoreId);
        }

        @Test
        void mustReturnOkWhenAdminAccessesOwnStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.canAccessStore(admin, testStoreId)).thenReturn(true);

            when(storeService.getStore(testStoreId)).thenReturn(responseStoreDTO);

            mockMvc.perform(get("/v1/store/" + testStoreId)
                            .header("Authorization", validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("banner").value(responseStoreDTO.getBanner()))
                    .andExpect(jsonPath("name").value(responseStoreDTO.getName()))
                    .andExpect(jsonPath("id").value(responseStoreDTO.getId().toString()))
                    .andExpect(jsonPath("slogan").value(responseStoreDTO.getSlogan()));

            verify(storeService, times(1)).getStore(testStoreId);

            verify(storeSecurityService, times(1))
                    .canAccessStore(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenWhenEmployeeAccessesWrongStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            when(storeSecurityService.canAccessStore(wrongEmployee, testStoreId))
                    .thenReturn(false);

            mockMvc.perform(get("/v1/store/" + testStoreId)
                            .header("Authorization", validToken))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, times(1))
                    .canAccessStore(wrongEmployee, testStoreId);

            verify(storeService, never()).getStore(testStoreId);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnOkWhenEmployeeAccessesOwnStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            when(storeSecurityService.canAccessStore(employee, testStoreId))
                    .thenReturn(true);

            when(storeService.getStore(testStoreId)).thenReturn(responseStoreDTO);

            mockMvc.perform(get("/v1/store/" + testStoreId)
                            .header("Authorization", validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("banner").value(responseStoreDTO.getBanner()))
                    .andExpect(jsonPath("name").value(responseStoreDTO.getName()))
                    .andExpect(jsonPath("id").value(responseStoreDTO.getId().toString()))
                    .andExpect(jsonPath("slogan").value(responseStoreDTO.getSlogan()));

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());

            verify(storeSecurityService, times(1))
                    .canAccessStore(employee, testStoreId);

            verify(storeService, times(1)).getStore(testStoreId);
        }
    }

    @Nested
    class UpdateStoreTests {
        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.multipart(
                    HttpMethod.PUT, "/v1/store/" + testStoreId)
                            .file(coverFile)
                            .part(namePart)
                            .part(sloganPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(storeService, never()).updateStore(any(), any());
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(MockMvcRequestBuilders.multipart(
                                    HttpMethod.PUT, "/v1/store/" + testStoreId)
                            .file(coverFile)
                            .part(namePart)
                            .part(sloganPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header("Authorization", "invalid-token"))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(storeService, never()).updateStore(any(), any());
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(MockMvcRequestBuilders.multipart(
                                    HttpMethod.PUT, "/v1/store/" + testStoreId)
                            .file(coverFile)
                            .part(namePart)
                            .part(sloganPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header("Authorization", validToken))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).isStoreAdmin(any(), any());

            verify(storeService, never()).updateStore(any(), any());
        }

        @Test
        void mustReturnForbiddenWhenAdminUpdateWrongStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(MockMvcRequestBuilders.multipart(
                                    HttpMethod.PUT, "/v1/store/" + testStoreId)
                            .file(coverFile)
                            .part(namePart)
                            .part(sloganPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header("Authorization", validToken))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(wrongAdmin, testStoreId);

            verify(storeService, never()).updateStore(any(), any());
        }

        @Test
        void mustReturnOkWhenAdminUpdateOwnStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            when(storeService.updateStore(any(), any()))
                    .thenReturn(responseStoreDTO);

            mockMvc.perform(MockMvcRequestBuilders.multipart(
                                    HttpMethod.PUT, "/v1/store/" + testStoreId)
                            .file(coverFile)
                            .part(namePart)
                            .part(sloganPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header("Authorization", validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("banner").value(responseStoreDTO.getBanner()))
                    .andExpect(jsonPath("name").value(responseStoreDTO.getName()))
                    .andExpect(jsonPath("id").value(responseStoreDTO.getId().toString()))
                    .andExpect(jsonPath("slogan").value(responseStoreDTO.getSlogan()));

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(storeService, times(1)).updateStore(any(), any());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenWhenEmployeeUpdateWrongStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            mockMvc.perform(MockMvcRequestBuilders.multipart(
                                    HttpMethod.PUT, "/v1/store/" + testStoreId)
                            .file(coverFile)
                            .part(namePart)
                            .part(sloganPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header("Authorization", validToken))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(wrongEmployee, testStoreId);

            verify(storeService, never()).updateStore(any(), any());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenWhenEmployeeUpdateOwnStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            mockMvc.perform(MockMvcRequestBuilders.multipart(
                                    HttpMethod.PUT, "/v1/store/" + testStoreId)
                            .file(coverFile)
                            .part(namePart)
                            .part(sloganPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header("Authorization", validToken))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(employee, testStoreId);

            verify(storeService, never()).updateStore(any(), any());
        }

        @Test
        void mustReturnBadRequestWhenUpdateWithWrongBody() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            MockPart invalidName = new MockPart("name", "".getBytes());

            mockMvc.perform(MockMvcRequestBuilders.multipart(
                                    HttpMethod.PUT, "/v1/store/" + testStoreId)
                            .file(coverFile)
                            .part(invalidName)
                            .part(sloganPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .header("Authorization", validToken))
                    .andExpect(status().isBadRequest());

            verify(storeSecurityService, never()).isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(storeService, never()).updateStore(any(), any());
        }
    }
}