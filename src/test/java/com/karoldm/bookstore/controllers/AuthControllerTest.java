package com.karoldm.bookstore.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karoldm.bookstore.dto.requests.LoginRequestDTO;
import com.karoldm.bookstore.dto.requests.RegisterStoreDTO;
import com.karoldm.bookstore.dto.requests.RegisterUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseAuthDTO;
import com.karoldm.bookstore.dto.responses.ResponseStoreDTO;
import com.karoldm.bookstore.dto.responses.ResponseUserDTO;
import com.karoldm.bookstore.exceptions.StoreAlreadyExist;
import com.karoldm.bookstore.exceptions.UserNotFoundException;
import com.karoldm.bookstore.exceptions.UsernameAlreadyExist;
import com.karoldm.bookstore.repositories.AppUserRepository;
import com.karoldm.bookstore.security.SecurityFilter;
import com.karoldm.bookstore.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @MockitoBean
    private AuthService authService;
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @MockitoBean
    private SecurityFilter securityFilter;
    @MockitoBean
    private AppUserRepository appUserRepository;

    private RegisterStoreDTO registerStoreDTO;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();

        RegisterUserDTO registerUserDTO = RegisterUserDTO.builder()
                .name("karol marques")
                .username("karol.marques")
                .password("123456")
                .build();

        registerStoreDTO = RegisterStoreDTO.builder()
                .admin(registerUserDTO)
                .name("book store")
                .banner(null)
                .slogan("The best tech books")
                .build();

        loginRequestDTO = LoginRequestDTO.builder()
                .username("karol.marques")
                .password("123456")
                .build();
    }

    @Nested
    class RegisterTests {
        @Test
        void mustCrateRegister() throws Exception {
            when(authService.register(registerStoreDTO)).thenReturn(
                    ResponseAuthDTO.builder()
                            .token("token")
                            .refreshToken("refresh-token")
                            .user(ResponseUserDTO.builder().build())
                            .store(ResponseStoreDTO.builder().build())
                            .build()
            );

            mockMvc.perform(post("/v1/auth/register")
                            .content(objectMapper.writeValueAsString(registerStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("token").value("token"))
                    .andExpect(jsonPath("refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("user").exists())
                    .andExpect(jsonPath("store").exists())
                    .andExpect(status().isCreated());

            verify(authService, times(1)).register(registerStoreDTO);
        }

        @Test
        void mustReturnConflictIfUserAlreadyExist() throws Exception {
            when(authService.register(registerStoreDTO)).thenThrow(
                    new UsernameAlreadyExist(registerStoreDTO.getAdmin().getUsername()));

            mockMvc.perform(post("/v1/auth/register")
                            .content(objectMapper.writeValueAsString(registerStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());

            verify(authService, times(1)).register(registerStoreDTO);
        }

        @Test
        void mustReturnConflictIfStoreAlreadyExist() throws Exception {
            when(authService.register(registerStoreDTO)).thenThrow(
                    new StoreAlreadyExist(registerStoreDTO.getName()));

            mockMvc.perform(post("/v1/auth/register")
                            .content(objectMapper.writeValueAsString(registerStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());

            verify(authService, times(1)).register(registerStoreDTO);
        }

        @Test
        void shouldReturnBadRequestWhenRequestBodyIsIncorrect() throws Exception {
            registerStoreDTO = RegisterStoreDTO.builder().build();

            mockMvc.perform(post("/v1/auth/register")
                            .content(objectMapper.writeValueAsString(registerStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(authService, times(0)).register(registerStoreDTO);
        }

        @Test
        void shouldReturnBadRequestWhenPasswordIsLessThanSix() throws Exception {
            registerStoreDTO.getAdmin().setPassword("123");

            mockMvc.perform(post("/v1/auth/register")
                            .content(objectMapper.writeValueAsString(registerStoreDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(authService, times(0)).register(registerStoreDTO);
        }
    }

    @Nested
    class LoginTests {
        @Test
        void mustLogin() throws Exception {
            when(authService.login(loginRequestDTO)).thenReturn(
                    ResponseAuthDTO.builder()
                            .token("token")
                            .refreshToken("refresh-token")
                            .user(ResponseUserDTO.builder().build())
                            .store(ResponseStoreDTO.builder().build())
                            .build()
            );

            mockMvc.perform(post("/v1/auth/login")
                            .content(objectMapper.writeValueAsString(loginRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("token").value("token"))
                    .andExpect(jsonPath("refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("user").exists())
                    .andExpect(jsonPath("store").exists())
                    .andExpect(status().isOk());

            verify(authService, times(1)).login(loginRequestDTO);
        }

        @Test
        void mustReturnNotFoundWhenLoginWithNonExistingUsername() throws Exception {
            when(authService.login(loginRequestDTO)).thenThrow(
                    new UserNotFoundException(loginRequestDTO.getUsername()));

            mockMvc.perform(post("/v1/auth/login")
                            .content(objectMapper.writeValueAsString(loginRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(authService, times(1)).login(loginRequestDTO);
        }

        @Test
        void mustReturnUnauthorizedWhenLoginWithWrongPassword() throws Exception {
            when(authService.login(loginRequestDTO)).thenThrow(
                    new BadCredentialsException("Password incorrect"));

            mockMvc.perform(post("/v1/auth/login")
                            .content(objectMapper.writeValueAsString(loginRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());

            verify(authService, times(1)).login(loginRequestDTO);
        }

        @Test
        void shouldReturnBadRequestWhenRequestBodyIsIncorrect() throws Exception {
            loginRequestDTO = LoginRequestDTO.builder().build();

            mockMvc.perform(post("/v1/auth/login")
                            .content(objectMapper.writeValueAsString(loginRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(authService, times(0)).login(loginRequestDTO);
        }
    }
}

