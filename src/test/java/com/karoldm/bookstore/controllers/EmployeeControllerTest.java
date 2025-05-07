package com.karoldm.bookstore.controllers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.karoldm.bookstore.dto.requests.RegisterUserDTO;
import com.karoldm.bookstore.dto.requests.UpdateUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseUserDTO;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.enums.Roles;
import com.karoldm.bookstore.repositories.AppUserRepository;
import com.karoldm.bookstore.security.SecurityConfig;
import com.karoldm.bookstore.security.SecurityFilter;
import com.karoldm.bookstore.services.EmployeeService;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import({SecurityConfig.class, SecurityFilter.class, StoreSecurityService.class})
@AutoConfigureMockMvc(addFilters = true)
class EmployeeControllerTest {
    @MockitoBean
    private EmployeeService employeeService;
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
    final private String validToken = "valid-token";
    private AppUser wrongAdmin;
    private AppUser wrongEmployee;
    private AppUser commonUser;

    private Set<ResponseUserDTO> listEmployees;
    private RegisterUserDTO registerUserDTO;
    private ResponseUserDTO responseUserDTO;
    private UpdateUserDTO updateUserDTO;
    final private UUID testEmployeeId = UUID.randomUUID();

    private ObjectMapper objectMapper;

    final private String baseURL = "/v1/store/" + testStoreId + "/employee";

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        updateUserDTO = UpdateUserDTO.builder()
                .name("employee name")
                .password("updated-password")
                .build();

        responseUserDTO = ResponseUserDTO.builder()
                .id(testEmployeeId)
                .name("employee name")
                .username("employee.user")
                .role(Roles.EMPLOYEE.name())
                .build();

        listEmployees = new HashSet<>();
        listEmployees.add(responseUserDTO);

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

        registerUserDTO = RegisterUserDTO.builder()
                .username("employee.new")
                .name("employee")
                .password("123456")
                .build();
    }

    @Nested
    class ListEmployeesTests {

        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(get(baseURL))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any(String.class));
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(employeeService, never()).listEmployees(testStoreId);
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken(any(String.class)))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(get(baseURL)
                            .header("Authorization", "invalid-token"))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(employeeService, never()).listEmployees(testStoreId);
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

            verify(storeSecurityService, never()).isStoreAdmin(any(), any());

            verify(employeeService, never()).listEmployees(testStoreId);
        }

        @Test
        void mustReturnForbiddenWhenAdminAccessesWrongStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(get(baseURL)
                            .header("Authorization", validToken))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(wrongAdmin, testStoreId);

            verify(employeeService, never()).listEmployees(testStoreId);
        }

        @Test
        void mustReturnOkWhenAdminAccessesOwnStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            when(employeeService.listEmployees(testStoreId))
                    .thenReturn(listEmployees);

            mockMvc.perform(get(baseURL)
                            .header("Authorization", validToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));

            verify(employeeService, times(1))
                    .listEmployees(testStoreId);

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenToEmployeeRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            when(storeSecurityService.isStoreAdmin(wrongEmployee, testStoreId))
                    .thenReturn(false);

            mockMvc.perform(get(baseURL)
                            .header("Authorization", validToken))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(wrongEmployee, testStoreId);

            verify(employeeService, never()).listEmployees(testStoreId);
        }
    }

    @Nested
    class UpdateEmployeeTests {
        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(put(baseURL + "/" + testEmployeeId)
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(employeeService, never()).updateEmployee(testStoreId, testEmployeeId, updateUserDTO);
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(put(baseURL + "/" + testEmployeeId)
                            .header("Authorization", "invalid-token")
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(employeeService, never()).updateEmployee(testStoreId, testEmployeeId, updateUserDTO);
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(put(baseURL + "/" + testEmployeeId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).isStoreAdmin(any(), any());

            verify(employeeService, never()).updateEmployee(testStoreId, testEmployeeId, updateUserDTO);
        }

        @Test
        void mustReturnForbiddenWhenAdminUpdateEmployeeInWrongStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(put(baseURL + "/" + testEmployeeId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(wrongAdmin, testStoreId);

            verify(employeeService, never()).updateEmployee(testStoreId, testEmployeeId, updateUserDTO);
        }

        @Test
        void mustReturnOkWhenAdminUpdateEmployeeInOwnStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            when(employeeService.updateEmployee(testStoreId, testEmployeeId, updateUserDTO))
                    .thenReturn(responseUserDTO);

            mockMvc.perform(put(baseURL + "/" + testEmployeeId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("name").value(responseUserDTO.getName()))
                    .andExpect(jsonPath("role").value(responseUserDTO.getRole()))
                    .andExpect(jsonPath("id").value(responseUserDTO.getId().toString()))
                    .andExpect(jsonPath("username").value(responseUserDTO.getUsername()));


            verify(storeSecurityService, times(1))
                    .isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(employeeService, times(1))
                    .updateEmployee(testStoreId, testEmployeeId, updateUserDTO);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenToEmployeeRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            mockMvc.perform(put(baseURL + "/" + testEmployeeId)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(updateUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(wrongEmployee, testStoreId);

            verify(employeeService, never()).updateEmployee(testStoreId, testEmployeeId, updateUserDTO);
        }
    }

    @Nested
    class DeleteEmployeeTests {
        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(delete(baseURL + "/" + testEmployeeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(employeeService, never()).deleteEmployee(testStoreId, testEmployeeId);
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(delete(baseURL + "/" + testEmployeeId)
                            .header("Authorization", "invalid-token")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(employeeService, never()).deleteEmployee(testStoreId, testEmployeeId);
        }

        @Test
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(delete(baseURL + "/" + testEmployeeId)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).isStoreAdmin(any(), any());

            verify(employeeService, never()).deleteEmployee(testStoreId, testEmployeeId);
        }

        @Test
        void mustReturnForbiddenWhenAdminDeleteEmployeeInWrongStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(delete(baseURL + "/" + testEmployeeId)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(wrongAdmin, testStoreId);

            verify(employeeService, never()).deleteEmployee(testStoreId, testEmployeeId);
        }

        @Test
        void mustReturnOkWhenAdminDeleteEmplyeeInOwnStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            mockMvc.perform(delete(baseURL + "/" + testEmployeeId)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(employeeService, times(1))
                    .deleteEmployee(testStoreId, testEmployeeId);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenToEmployeeRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongEmployee.getUsername());

            when(appUserRepository.findByUsername(wrongEmployee.getUsername())).thenReturn(
                    Optional.of(wrongEmployee)
            );

            mockMvc.perform(delete(baseURL + "/" + testEmployeeId)
                            .header("Authorization", validToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongEmployee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(wrongEmployee, testStoreId);

            verify(employeeService, never()).deleteEmployee(testStoreId, testEmployeeId);
        }
    }

    @Nested
    class CreateEmployeeTests {
        @Test
        void mustReturnForbiddenWhenNoTokenProvided() throws Exception {
            mockMvc.perform(post(baseURL)
                            .content(objectMapper.writeValueAsString(registerUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(employeeService, never()).createEmployee(testStoreId, registerUserDTO);
        }

        @Test
        void mustReturnForbiddenForInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token"))
                    .thenThrow(new JWTVerificationException("Invalid token"));

            mockMvc.perform(post(baseURL)
                            .header("Authorization", "invalid-token")
                            .content(objectMapper.writeValueAsString(registerUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, never()).findByUsername(any());
            verify(storeSecurityService, never()).isStoreAdmin(any(), any());
            verify(employeeService, never()).createEmployee(testStoreId, registerUserDTO);
        }

        @Test
        @WithMockUser(roles = "COMMON")
        void mustReturnForbiddenForUserCommonRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(commonUser.getUsername());

            when(appUserRepository.findByUsername(commonUser.getUsername())).thenReturn(
                    Optional.of(commonUser)
            );

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(registerUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(commonUser.getUsername());

            verify(storeSecurityService, never()).isStoreAdmin(any(), any());

            verify(employeeService, never()).createEmployee(testStoreId, registerUserDTO);
        }

        @Test
        void mustReturnForbiddenWhenAdminCreateEmployeeInWrongStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(wrongAdmin.getUsername());

            when(appUserRepository.findByUsername(wrongAdmin.getUsername())).thenReturn(
                    Optional.of(wrongAdmin)
            );

            when(storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId)).thenReturn(false);

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(registerUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(wrongAdmin.getUsername());

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(wrongAdmin, testStoreId);

            verify(employeeService, never()).createEmployee(testStoreId, registerUserDTO);
        }

        @Test
        void mustReturnOkWhenAdminCreateEmployeeInOwnStore() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            when(employeeService.createEmployee(testStoreId, registerUserDTO))
                    .thenReturn(responseUserDTO);

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(registerUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("username").value(responseUserDTO.getUsername()))
                    .andExpect(jsonPath("name").value(responseUserDTO.getName()))
                    .andExpect(jsonPath("id").value(responseUserDTO.getId().toString()))
                    .andExpect(jsonPath("role").value(responseUserDTO.getRole()));

            verify(storeSecurityService, times(1))
                    .isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(employeeService, times(1)).createEmployee(testStoreId, registerUserDTO);
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void mustReturnForbiddenToEmployeeRole() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(employee.getUsername());

            when(appUserRepository.findByUsername(employee.getUsername())).thenReturn(
                    Optional.of(employee)
            );

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(registerUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(appUserRepository, times(1))
                    .findByUsername(employee.getUsername());

            verify(storeSecurityService, never())
                    .isStoreAdmin(employee, testStoreId);

            verify(employeeService, never()).createEmployee(testStoreId, registerUserDTO);
        }

        @Test
        void mustReturnBadRequestWhenCreateWithWrongBody() throws Exception {
            when(tokenService.validateToken(validToken)).thenReturn(admin.getUsername());

            when(appUserRepository.findByUsername(admin.getUsername())).thenReturn(
                    Optional.of(admin)
            );

            when(storeSecurityService.isStoreAdmin(admin, testStoreId)).thenReturn(true);

            RegisterUserDTO invalidRegisterUserDTO = RegisterUserDTO.builder().build();

            mockMvc.perform(post(baseURL)
                            .header("Authorization", validToken)
                            .content(objectMapper.writeValueAsString(invalidRegisterUserDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(storeSecurityService, never()).isStoreAdmin(admin, testStoreId);

            verify(appUserRepository, times(1))
                    .findByUsername(admin.getUsername());

            verify(employeeService, never()).createEmployee(testStoreId, registerUserDTO);
        }
    }
}
