package com.karoldm.bookstore.services;

import com.karoldm.bookstore.entities.Admin;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Employee;
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

    private Store store;
    private Admin admin;
    private Employee employee;

    private Store anotherStore;
    private Admin wrongAdmin;
    private Employee wrongEmployee;

    private UUID testStoreId = UUID.randomUUID();

    @BeforeEach
    void setup() {

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
