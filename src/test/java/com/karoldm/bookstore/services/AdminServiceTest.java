package com.karoldm.bookstore.services;

import com.karoldm.bookstore.dto.requests.UpdateUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseUserDTO;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.enums.Roles;
import com.karoldm.bookstore.exceptions.InvalidNameException;
import com.karoldm.bookstore.exceptions.InvalidPasswordException;
import com.karoldm.bookstore.exceptions.InvalidRoleException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {
    @InjectMocks
    private AdminService adminService;

    @Mock
    private AppUserRepository userRepository;
    @Mock
    private StoreRepository storeRepository;

    private AppUser admin;
    private AppUser employee;
    private Store store;
    private UpdateUserDTO updateUserDTO;

    final private UUID testStoreId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        updateUserDTO = UpdateUserDTO.builder()
                .name("updated name")
                .password("updated password")
                .build();

        store = Store.builder()
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
                .id(UUID.randomUUID())
                .build();

        employee = AppUser.builder()
                .name("employee")
                .role(Roles.EMPLOYEE)
                .username("employee")
                .password("employee")
                .store(store)
                .id(UUID.randomUUID())
                .build();
    }

    @Nested
    class UpdateAdminTests {
        @Test
        void mustThrowExceptionWhenPasswordIsInvalid() {
            updateUserDTO.setPassword("");

            Exception ex = assertThrows(InvalidPasswordException.class, () ->
                    adminService.updateAccount(admin, updateUserDTO)
            );

            assertEquals("A senha deve possuir pelo menos 6 caracteres.", ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        void mustThrowInvalidNameExceptionToEmptyName() {
            updateUserDTO.setName(" ");

            Exception ex = assertThrows(InvalidNameException.class, () ->
                    adminService.updateAccount(admin, updateUserDTO)
            );

            assertEquals(updateUserDTO.getName() + " não é um nome válido. Insira ao menos 1 caractere.",
                    ex.getMessage());

            verify(userRepository, never()).save(any());
        }

        @Test
        void mustUpdateAdmin() {
            ResponseUserDTO response = adminService.updateAccount(admin, updateUserDTO);

            assertEquals(updateUserDTO.getName(), response.getName());
            assertEquals(admin.getRole().name(), response.getRole());
            assertEquals(admin.getUsername(), response.getUsername());
            assertEquals(admin.getName(), response.getName());

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String encryptedPassword = encoder.encode(updateUserDTO.getPassword());

            assertTrue(encoder.matches(updateUserDTO.getPassword(), encryptedPassword));

            verify(userRepository, times(1)).save(admin);
        }

        @Test
        void mustThrowInvalidRoleIfNonAdminTryUpdateAccount() {
            Exception ex = assertThrows(InvalidRoleException.class, () ->
                    adminService.updateAccount(employee, updateUserDTO)
            );

            assertEquals("Usuário com role " + employee.getRole().name() + " não tem acesso a esse recurso.",
                    ex.getMessage());

            verify(userRepository, never()).delete(any());
            verify(storeRepository, never()).delete(any());
        }
    }

    @Nested
    class DeleteAdminTests {
        @Test
        void mustDeleteUserAndStoreAndEmployeesIfAdmin() {
            HashSet<AppUser> employees = new HashSet<>();
            employees.add(employee);
            when(userRepository.findByStoreAndRole(store, Roles.EMPLOYEE)).thenReturn(employees);

            adminService.deleteAccount(admin);

            verify(storeRepository, times(1)).delete(store);
            verify(userRepository, times(1)).deleteAll(employees);
            verify(userRepository, times(1)).delete(admin);
        }

        @Test
        void mustThrowInvalidRoleIfNonAdminTryDeleteAccount() {
            Exception ex = assertThrows(InvalidRoleException.class, () ->
                    adminService.deleteAccount(employee));

            assertEquals("Usuário com role " + employee.getRole().name() + " não tem acesso a esse recurso.",
                    ex.getMessage());

            verify(userRepository, never()).delete(any());
            verify(storeRepository, never()).delete(any());
        }
    }
}
