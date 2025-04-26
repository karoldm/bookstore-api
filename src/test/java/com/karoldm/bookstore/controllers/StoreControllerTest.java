package com.karoldm.bookstore.controllers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karoldm.bookstore.dto.requests.UpdateStoreDTO;
import com.karoldm.bookstore.dto.responses.ResponseStoreDTO;
import com.karoldm.bookstore.entities.Admin;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Employee;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
// use the created security config and filter to test the roles and routes access
// inject the storesecurityservice
@Import({SecurityConfig.class, SecurityFilter.class, StoreSecurityService.class})
@AutoConfigureMockMvc(addFilters = true)
public class StoreControllerTest {

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

    private final UUID testStoreId = UUID.randomUUID();
    private ResponseStoreDTO responseStoreDTO;
    private Store store;
    private Admin admin;
    private Employee employee;
    private String validToken = "valid-token";
    private Store anotherStore;
    private Admin wrongAdmin;
    private Employee wrongEmployee;
    private AppUser commonUser;
    private UpdateStoreDTO updateStoreDTO;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        updateStoreDTO = UpdateStoreDTO.builder()
                .name("my store")
                .banner(null)
                .slogan("The best tech books")
                .build();

        commonUser = AppUser.builder()
                .id(UUID.randomUUID())
                .name("common user")
                .password("common_user")
                .username("common_user")
                .photo(null)
                .role(Roles.COMMON)
                .build();

        store = Store.builder()
                .id(testStoreId)
                .name("my store")
                .slogan("The best tech books")
                .banner(null)
                .build();

        admin = Admin.builder()
                .name("admin")
                .photo(null)
                .role(Roles.ADMIN)
                .username("admin")
                .password("admin")
                .store(store)
                .build();

        employee = Employee.builder()
                .name("employee")
                .photo(null)
                .role(Roles.EMPLOYEE)
                .username("employee")
                .password("employee")
                .store(store)
                .build();


        anotherStore = Store.builder()
                .id(UUID.randomUUID())
                .name("another store")
                .slogan("The best tech books")
                .banner(null)
                .build();

        wrongAdmin = Admin.builder()
                .name("wrong admin")
                .photo(null)
                .role(Roles.ADMIN)
                .username("wrong_admin")
                .password("wrong_admin")
                .store(anotherStore)
                .build();

        wrongEmployee = Employee.builder()
                .name("wrong employee")
                .photo(null)
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
            mockMvc.perform(put("/v1/store/" + testStoreId)
                            .content(objectMapper.writeValueAsString(updateStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(storeService, never()).updateStore(testStoreId, updateStoreDTO);
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(put("/v1/store/" + testStoreId)
                            .header("Authorization", "invalid-token")
                            .content(objectMapper.writeValueAsString(updateStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(storeService, never()).updateStore(testStoreId, updateStoreDTO);
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(put("/v1/store/" + testStoreId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).isStoreAdmin(any(), any());

            verify(storeService, never()).updateStore(testStoreId, updateStoreDTO);
        }

        @Test
        void mustReturnForbiddenWhenAdminUpdateWrongStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(put("/v1/store/" + testStoreId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(wrongAdmin, testStoreId);

            verify(storeService, never()).updateStore(testStoreId, updateStoreDTO);
        }

        @Test
        void mustReturnOkWhenAdminUpdateOwnStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            when(storeService.updateStore(testStoreId, updateStoreDTO))
                    .thenReturn(responseStoreDTO);

            mockMvc.perform(put("/v1/store/" + testStoreId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("banner").value(responseStoreDTO.getBanner()))
                    .andExpect(jsonPath("name").value(responseStoreDTO.getName()))
                    .andExpect(jsonPath("id").value(responseStoreDTO.getId().toString()))
                    .andExpect(jsonPath("slogan").value(responseStoreDTO.getSlogan()));

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(storeService, times(1)).updateStore(testStoreId, updateStoreDTO);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenWhenEmployeeUpdateWrongStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            mockMvc.perform(put("/v1/store/" + testStoreId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(wrongEmployee, testStoreId);

            verify(storeService, never()).updateStore(testStoreId, updateStoreDTO);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenWhenEmployeeUpdateOwnStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            mockMvc.perform(put("/v1/store/" + testStoreId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(employee, testStoreId);

            verify(storeService, never()).updateStore(testStoreId, updateStoreDTO);
        }

        @Test
        void mustReturnBadRequestWhenUpdateWithWrongBody() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            UpdateStoreDTO invalidUpdateStoreDTO = UpdateStoreDTO.builder().build();

            mockMvc.perform(put("/v1/store/" + testStoreId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(invalidUpdateStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(storeSecurityService, never()).isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(storeService, never()).updateStore(testStoreId, invalidUpdateStoreDTO);
        }
    }
}