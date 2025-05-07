package com.karoldm.bookstore.services;

import com.karoldm.bookstore.dto.requests.RegisterUserDTO;
import com.karoldm.bookstore.dto.requests.UpdateUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseUserDTO;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.enums.Roles;
import com.karoldm.bookstore.exceptions.*;
import com.karoldm.bookstore.repositories.AppUserRepository;
import com.karoldm.bookstore.repositories.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    private AppUser employee;
    private Store store;
    private RegisterUserDTO registerUserDTO;
    private UpdateUserDTO updateUserDTO;

    final private Long testStoreId = 1L;
    final private Long testEmployeeId = 2L;

    @BeforeEach
    void setup() {
        updateUserDTO = UpdateUserDTO.builder()
                .password("new password")
                .name("new name")
                .build();

        registerUserDTO = RegisterUserDTO.builder()
                .name("new user")
                .password("123456")
                .username("new.user")
                .build();

        store = Store.builder()
                .id(testStoreId)
                .name("my store")
                .slogan("The best tech books")
                .banner(null)
                .build();

        employee = AppUser.builder()
                .name("employee")
                .role(Roles.EMPLOYEE)
                .username("employee")
                .password("employee")
                .store(store)
                .id(testEmployeeId)
                .build();
    }

    @Nested
    class CreateEmployeeTests {
        @Test
        void mustThrowStoreNotFoundToNonExistentStore() {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.empty());

            Exception ex = assertThrows(StoreNotFoundException.class, () ->
                    employeeService.createEmployee(testStoreId, registerUserDTO)
            );

            assertEquals("Loja com id " + testStoreId + " não encontrada.", ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        void mustThrowUserAlreadyExistToExistentUsername() {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(store));

            when(userRepository.findByUsername(registerUserDTO.getUsername()))
                    .thenReturn(Optional.of(employee));

            Exception ex = assertThrows(UsernameAlreadyExist.class, () ->
                    employeeService.createEmployee(testStoreId, registerUserDTO)
            );

            assertEquals("Já existe um usuário com o username " + registerUserDTO.getUsername(),
                    ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        void mustCreateEmployee() throws Exception {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(store));

            when(userRepository.findByUsername(registerUserDTO.getUsername()))
                    .thenReturn(Optional.empty());

            when(userRepository.save(any(AppUser.class))).thenReturn(AppUser.builder().build());

            ResponseUserDTO response = employeeService.createEmployee(testStoreId, registerUserDTO);

            assertEquals(registerUserDTO.getName(), response.getName());
            assertEquals(registerUserDTO.getUsername(), response.getUsername());

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String encryptedPassword = encoder.encode(registerUserDTO.getPassword());

            assertTrue(encoder.matches(registerUserDTO.getPassword(), encryptedPassword));

            verify(userRepository, times(1)).save(any(AppUser.class));
        }
    }

    @Nested
    class ListEmployeeTests {
        @Test
        void mustThrowStoreNotFoundToNonExistentStore() {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.empty());

            Exception ex = assertThrows(StoreNotFoundException.class, () ->
                    employeeService.listEmployees(testStoreId)
            );

            assertEquals("Loja com id " + testStoreId + " não encontrada.", ex.getMessage());

            verify(userRepository, never()).findByStoreAndRole(store, Roles.EMPLOYEE);
        }

        @Test
        void mustListEmployees() throws Exception {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(store));

            HashSet<AppUser> employees = new HashSet<>();
            employees.add(employee);

            when(userRepository.findByStoreAndRole(store, Roles.EMPLOYEE))
                    .thenReturn(employees);

            Set<ResponseUserDTO> response = employeeService.listEmployees(testStoreId);

            assertEquals(1, response.size());

            ResponseUserDTO first = response.stream().toList().get(0);

            assertEquals(employee.getId(), first.getId());
            assertEquals(employee.getName(), first.getName());
            assertEquals(employee.getUsername(), first.getUsername());
            assertEquals(employee.getRole().name(), first.getRole());

            verify(userRepository, times(1))
                    .findByStoreAndRole(store, Roles.EMPLOYEE);
        }
    }

    @Nested
    class UpdateEmployeeTests {
        @Test
        void mustThrowStoreNotFoundToNonExistentStore() {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.empty());

            Exception ex = assertThrows(StoreNotFoundException.class, () ->
                    employeeService.updateEmployee(testStoreId, testEmployeeId, updateUserDTO)
            );

            assertEquals("Loja com id " + testStoreId + " não encontrada.", ex.getMessage());

            verify(userRepository, never()).save(any(AppUser.class));
        }

        @Test
        void mustThrowNotFoundWhenUserDoesntExist() throws Exception {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(store));

            when(userRepository.findByIdAndStoreAndRole(testEmployeeId, store, Roles.EMPLOYEE))
                    .thenReturn(Optional.empty());

            Exception ex = assertThrows(UserNotFoundException.class, () ->
                    employeeService.updateEmployee(testStoreId, testEmployeeId, updateUserDTO)
            );

            assertEquals("Usuário com ID " + testEmployeeId + " não encontrado.", ex.getMessage());

            verify(userRepository, never()).save(any(AppUser.class));
        }

        @Test
        void mustThrowInvalidPassword() {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(store));

            when(userRepository.findByIdAndStoreAndRole(testEmployeeId, store, Roles.EMPLOYEE))
                    .thenReturn(Optional.of(employee));

            updateUserDTO.setPassword("");

            Exception ex = assertThrows(InvalidPasswordException.class, () ->
                    employeeService.updateEmployee(testStoreId, testEmployeeId, updateUserDTO)
            );

            assertEquals("A senha deve possuir pelo menos 6 caracteres.", ex.getMessage());

            verify(userRepository, never()).save(any(AppUser.class));
        }

        @Test
        void mustThrowInvalidName() {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(store));

            when(userRepository.findByIdAndStoreAndRole(testEmployeeId, store, Roles.EMPLOYEE))
                    .thenReturn(Optional.of(employee));

            updateUserDTO.setName(" ");

            Exception ex = assertThrows(InvalidNameException.class, () ->
                    employeeService.updateEmployee(testStoreId, testEmployeeId, updateUserDTO)
            );

            assertEquals(updateUserDTO.getName()+" não é um nome válido. Insira ao menos 1 caractere.", ex.getMessage());

            verify(userRepository, never()).save(any(AppUser.class));
        }

        @Test
        void mustUpdateEmployee() {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(store));

            when(userRepository.findByIdAndStoreAndRole(testEmployeeId, store, Roles.EMPLOYEE))
                    .thenReturn(Optional.of(employee));

            ResponseUserDTO response = employeeService.updateEmployee(
                    testStoreId, testEmployeeId, updateUserDTO
            );

            assertEquals(updateUserDTO.getName(), response.getName());
            assertEquals(employee.getRole().name(), response.getRole());
            assertEquals(employee.getUsername(), response.getUsername());
            assertEquals(employee.getName(), response.getName());

            verify(userRepository, times(1)).save(employee);
        }
    }

    @Nested
    class DeleteEmployeeTests {
        @Test
        void mustThrowStoreNotFoundToNonExistentStore() {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.empty());

            Exception ex = assertThrows(StoreNotFoundException.class, () ->
                    employeeService.deleteEmployee(testStoreId, testEmployeeId)
            );

            assertEquals("Loja com id " + testStoreId + " não encontrada.", ex.getMessage());

            verify(userRepository, never()).delete(any(AppUser.class));
        }

        @Test
        void mustThrowNotFoundWhenUserDoesntExist() throws Exception {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(store));

            when(userRepository.findByIdAndStoreAndRole(testEmployeeId, store, Roles.EMPLOYEE))
                    .thenReturn(Optional.empty());

            Exception ex = assertThrows(UserNotFoundException.class, () ->
                    employeeService.deleteEmployee(testStoreId, testEmployeeId)
            );

            assertEquals("Usuário com ID " + testEmployeeId + " não encontrado.", ex.getMessage());

            verify(userRepository, never()).delete(any(AppUser.class));
        }

        @Test
        void mustDeleteEmployee() {
            when(storeRepository.findById(testStoreId)).thenReturn(Optional.of(store));

            when(userRepository.findByIdAndStoreAndRole(testEmployeeId, store, Roles.EMPLOYEE))
                    .thenReturn(Optional.of(employee));

            employeeService.deleteEmployee(testStoreId, testEmployeeId);

            verify(userRepository, times(1)).delete(employee);
        }
    }
}
