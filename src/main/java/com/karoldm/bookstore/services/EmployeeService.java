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
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeService {
    private AppUserRepository userRepository;
    private StoreRepository storeRepository;

    public Set<ResponseUserDTO> listEmployees(Long storeId) {
        Optional<Store> optionalStore = storeRepository.findById(storeId);
        if (optionalStore.isEmpty()) {
            throw new StoreNotFoundException(storeId);
        }

        Store store = optionalStore.get();

        Set<AppUser> employees = userRepository.findByStoreAndRole(store, Roles.EMPLOYEE);

        return employees.stream().map(
                        employee -> ResponseUserDTO.builder()
                                .role(employee.getRole().name())
                                .name(employee.getName())
                                .username(employee.getUsername())
                                .id(employee.getId())
                                .build())
                .collect(Collectors.toSet());
    }

    public ResponseUserDTO createEmployee(Long storeId, RegisterUserDTO registerUserDTO) {
        Optional<Store> optionalStore = storeRepository.findById(storeId);

        if (optionalStore.isEmpty()) {
            throw new StoreNotFoundException(storeId);
        }

        Store store = optionalStore.get();

        Optional<AppUser> existingUser = userRepository.findByUsername(registerUserDTO.getUsername());

        if (existingUser.isPresent()) {
            throw new UsernameAlreadyExist(registerUserDTO.getUsername());
        }

        String encryptedPassword = new BCryptPasswordEncoder()
                .encode(registerUserDTO.getPassword());

        AppUser newEmployee = AppUser.builder()
                .username(registerUserDTO.getUsername())
                .password(encryptedPassword)
                .name(registerUserDTO.getName())
                .role(Roles.EMPLOYEE)
                .store(store)
                .build();

        AppUser savedEmployee = userRepository.save(newEmployee);

        newEmployee.setId(savedEmployee.getId());

        return ResponseUserDTO.builder()
                .username(newEmployee.getUsername())
                .role(newEmployee.getRole().name())
                .name(newEmployee.getName())
                .build();
    }

    public ResponseUserDTO updateEmployee(Long storeId, Long employeeId, UpdateUserDTO updateUserDTO) {
        Optional<Store> optionalStore = storeRepository.findById(storeId);

        if (optionalStore.isEmpty()) {
            throw new StoreNotFoundException(storeId);
        }

        Store store = optionalStore.get();

        Optional<AppUser> optionalUser = userRepository.findByIdAndStoreAndRole(employeeId, store, Roles.EMPLOYEE);

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(employeeId);
        }

        AppUser user = optionalUser.get();

        if(updateUserDTO.getPassword() != null) {
            if(updateUserDTO.getPassword().length()<6) {
                throw new InvalidPasswordException();
            }
            String encryptedPassword = new BCryptPasswordEncoder()
                    .encode(updateUserDTO.getPassword());
            user.setPassword(encryptedPassword);
        }

        if(updateUserDTO.getName() != null){
            if(updateUserDTO.getName().trim().isEmpty()){
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

    public void deleteEmployee(Long storeId, Long employeeId){
        Optional<Store> optionalStore = storeRepository.findById(storeId);

        if (optionalStore.isEmpty()) {
            throw new StoreNotFoundException(storeId);
        }

        Store store = optionalStore.get();

        Optional<AppUser> optionalUser = userRepository.findByIdAndStoreAndRole(employeeId, store, Roles.EMPLOYEE);

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(employeeId);
        }

        AppUser user = optionalUser.get();

        userRepository.delete(user);
    }
}
