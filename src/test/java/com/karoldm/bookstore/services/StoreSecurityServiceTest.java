package com.karoldm.bookstore.services;

import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.enums.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class StoreSecurityServiceTest {
    @InjectMocks
    private StoreSecurityService storeSecurityService;

    private AppUser admin;
    private AppUser employee;

    private AppUser wrongAdmin;
    private AppUser wrongEmployee;

    private final UUID testStoreId = UUID.randomUUID();

    @BeforeEach
    void setup() {

        Store store = Store.builder()
                .id(testStoreId)
                .name("my store")
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
    }

    @Nested
    class CanAccessStoreTests{

        @Test
        void mustReturnFalseToWrongAdmin() {
            boolean result = storeSecurityService.canAccessStore(wrongAdmin, testStoreId);
            assertFalse(result);
        }

        @Test
        void mustReturnFalseToWrongEmployee() {
            boolean result = storeSecurityService.canAccessStore(wrongEmployee, testStoreId);
            assertFalse(result);
        }

        @Test
        void mustReturnFalseToCommonUser(){
            boolean result = storeSecurityService.canAccessStore(AppUser.builder().build(), testStoreId);
            assertFalse(result);
        }

        @Test
        void mustReturnTrueToStoreAdmin(){
            boolean result = storeSecurityService.canAccessStore(admin, testStoreId);
            assertTrue(result);
        }

        @Test
        void mustReturnTrueToStoreEmployee() {
            boolean result = storeSecurityService.canAccessStore(employee, testStoreId);
            assertTrue(result);
        }
    }

    @Nested
    class IsStoreAdminTests {
        @Test
        void mustReturnFalseToWrongAdmin() {
            boolean result = storeSecurityService.isStoreAdmin(wrongAdmin, testStoreId);
            assertFalse(result);
        }

        @Test
        void mustReturnFalseToWrongEmployee() {
            boolean result = storeSecurityService.isStoreAdmin(wrongEmployee, testStoreId);
            assertFalse(result);
        }

        @Test
        void mustReturnFalseToCommonUser(){
            boolean result = storeSecurityService.isStoreAdmin(AppUser.builder().build(), testStoreId);
            assertFalse(result);
        }

        @Test
        void mustReturnTrueToStoreAdmin(){
            boolean result = storeSecurityService.isStoreAdmin(admin, testStoreId);
            assertTrue(result);
        }

        @Test
        void mustReturnFalseToStoreEmployee() {
            boolean result = storeSecurityService.isStoreAdmin(employee, testStoreId);
            assertFalse(result);
        }
    }
}
