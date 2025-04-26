package com.karoldm.bookstore.services;

import com.karoldm.bookstore.entities.Admin;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Employee;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("storeSecurityService")
@AllArgsConstructor
public class StoreSecurityService {
    public boolean canAccessStore(Object user, UUID storeId) {
        if (user instanceof AppUser appUser) {

            return appUser instanceof Admin admin && storeId.equals(admin.getStore().getId()) ||
                    appUser instanceof Employee emp && storeId.equals(emp.getStore().getId());
        }

        return false;
    }

    public boolean isStoreAdmin(Object user, UUID storeId) {
        if (user instanceof AppUser appUser) {

            return appUser instanceof Admin admin &&
                    storeId.equals(admin.getStore().getId());
        }

        return false;
    }
}
