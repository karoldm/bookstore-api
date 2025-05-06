package com.karoldm.bookstore.services;

import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.enums.Roles;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("storeSecurityService")
@AllArgsConstructor
public class StoreSecurityService {
    public boolean canAccessStore(Object user, UUID storeId) {
        if (user instanceof AppUser appUser) {

            return appUser.getRole() == Roles.ADMIN && storeId.equals(appUser.getStore().getId()) ||
                    appUser.getRole() == Roles.EMPLOYEE && storeId.equals(appUser.getStore().getId());
        }

        return false;
    }

    public boolean isStoreAdmin(Object user, UUID storeId) {
        if (user instanceof AppUser appUser) {

            return appUser.getRole() == Roles.ADMIN &&
                    storeId.equals(appUser.getStore().getId());
        }

        return false;
    }
}
