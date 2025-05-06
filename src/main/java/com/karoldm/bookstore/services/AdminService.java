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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class AdminService {
    private AppUserRepository userRepository;
    private StoreRepository storeRepository;

    @Transactional
    public ResponseUserDTO updateAccount(AppUser user, UpdateUserDTO updateUserDTO) {

        if (user.getRole() != Roles.ADMIN) {
            throw new InvalidRoleException(user.getRole());
        }

        if (updateUserDTO.getPassword() != null) {
            if (updateUserDTO.getPassword().length() < 6) {
                throw new InvalidPasswordException();
            }
            String encryptedPassword = new BCryptPasswordEncoder()
                    .encode(updateUserDTO.getPassword());
            user.setPassword(encryptedPassword);
        }

        if (updateUserDTO.getName() != null) {
            if (updateUserDTO.getName().trim().isEmpty()) {
                throw new InvalidNameException(updateUserDTO.getName());
            }
            user.setName(updateUserDTO.getName());
        }

        userRepository.save(user);

        return ResponseUserDTO.builder()
                .name(user.getName())
                .role(user.getRole().name())
                .username(user.getUsername())
                .build();
    }

    @Transactional
    public void deleteAccount(AppUser user) {
        if (user.getRole() != Roles.ADMIN) {
            throw new InvalidRoleException(user.getRole());
        }

        // if user is adm, delete your store and the store employees
        Store store = user.getStore();

        Set<AppUser> employees = userRepository.findByStoreAndRole(store, Roles.EMPLOYEE);
        userRepository.deleteAll(employees);

        storeRepository.delete(store);

        userRepository.delete(user);
    }

}
