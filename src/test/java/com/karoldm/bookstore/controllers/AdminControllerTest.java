package com.karoldm.bookstore.controllers;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.karoldm.bookstore.dto.requests.UpdateUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseUserDTO;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.enums.Roles;
import com.karoldm.bookstore.repositories.AppUserRepository;
import com.karoldm.bookstore.security.SecurityConfig;
import com.karoldm.bookstore.security.SecurityFilter;
import com.karoldm.bookstore.services.AdminService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, SecurityFilter.class, StoreSecurityService.class})
@AutoConfigureMockMvc(addFilters = true)
class AdminControllerTest {
    @MockitoBean
    private AdminService adminService;
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private AppUserRepository appUserRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SecurityFilter securityFilter;

    private AppUser admin;
    private AppUser employee;
    final private String validToken = "valid-token";
    private AppUser commonUser;

    private ResponseUserDTO responseUserDTO;
    private UpdateUserDTO updateUserDTO;

    private ObjectMapper objectMapper;

    final private String baseURL = "/v1/admin";

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        updateUserDTO = UpdateUserDTO.builder()
                .name("employee name")
                .password("updated-password")
                .build();

        Long testEmployeeId = 2L;
        responseUserDTO = ResponseUserDTO.builder()
                .id(testEmployeeId)
                .name("employee name")
                .username("employee.user")
                .role(Roles.EMPLOYEE.name())
                .build();

        commonUser = AppUser.builder()
                .id(3L)
                .name("common user")
                .password("common_user")
                .username("common_user")
                .role(Roles.COMMON)
                .build();

        Long testStoreId = 1L;
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
    }

    @Nested
    class UpdateAccountTests {
        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(put(baseURL)
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(adminService, never()).updateAccount(any(), any());
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(put(baseURL)
                            .header("Authorization", "invalid-token")
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(adminService, never()).updateAccount(any(), any());
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(put(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(adminService, never()).updateAccount(any(), any());
        }

        @Test
        void mustReturnForbiddenForEmployeeRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            mockMvc.perform(put(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());

            verify(adminService, never()).updateAccount(any(), any());
        }

        @Test
        void mustReturnOkWhenAdminUpdateYourOwnAccount() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(adminService.updateAccount(admin, updateUserDTO))
                    .thenReturn(responseUserDTO);

            mockMvc.perform(put(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("name").value(responseUserDTO.getName()))
                    .andExpect(jsonPath("role").value(responseUserDTO.getRole()))
                    .andExpect(jsonPath("id").value(responseUserDTO.getId().toString()))
                    .andExpect(jsonPath("username").value(responseUserDTO.getUsername()));

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(adminService, times(1))
                    .updateAccount(admin, updateUserDTO);
        }
    }

    @Nested
    class DeleteAccountTests {
        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(delete(baseURL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(adminService, never()).deleteAccount(any());
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(delete(baseURL)
                            .header("Authorization", "invalid-token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(adminService, never()).deleteAccount(any());
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(delete(baseURL)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(adminService, never()).deleteAccount(any());
        }

        @Test
        void mustReturnForbiddenForEmployeeRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            mockMvc.perform(delete(baseURL)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());

            verify(adminService, never()).deleteAccount(any());
        }

        @Test
        void mustReturnOkWhenAdminDeleteYourOwnAccount() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            mockMvc.perform(delete(baseURL)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(adminService, times(1))
                    .deleteAccount(admin);
        }
    }
}
