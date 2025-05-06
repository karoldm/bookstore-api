package com.karoldm.bookstore.services;

import com.karoldm.bookstore.dto.requests.LoginRequestDTO;
import com.karoldm.bookstore.dto.requests.RegisterStoreDTO;
import com.karoldm.bookstore.dto.requests.RegisterUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseAuthDTO;
import com.karoldm.bookstore.dto.responses.ResponseStoreDTO;
import com.karoldm.bookstore.dto.responses.ResponseUserDTO;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.enums.Roles;
import com.karoldm.bookstore.exceptions.InvalidRoleException;
import com.karoldm.bookstore.exceptions.StoreAlreadyExist;
import com.karoldm.bookstore.exceptions.UserNotFoundException;
import com.karoldm.bookstore.exceptions.UsernameAlreadyExist;
import com.karoldm.bookstore.repositories.AppUserRepository;
import com.karoldm.bookstore.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService implements UserDetailsService {
    private AppUserRepository userRepository;
    private StoreRepository storeRepository;
    private TokenService tokenService;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Transactional
    public ResponseAuthDTO register(@NotNull RegisterStoreDTO registerDTO) throws Exception {
        Optional<AppUser> existingUser = userRepository.findByUsername(registerDTO.getAdmin().getUsername());

        if (existingUser.isPresent()) {
            throw new UsernameAlreadyExist(registerDTO.getAdmin().getUsername());
        }

        Optional<Store> existingStore = storeRepository.findByName(registerDTO.getName());

        if (existingStore.isPresent()) {
            throw new StoreAlreadyExist(registerDTO.getName());
        }

        String encryptedPassword = new BCryptPasswordEncoder()
                .encode(registerDTO.getAdmin().getPassword());

        Store newStore = Store.builder()
                .name(registerDTO.getName())
                .banner(registerDTO.getBanner())
                .slogan(registerDTO.getSlogan())
                .build();

        Store savedStore = storeRepository.save(newStore);

        newStore.setId(savedStore.getId());

        RegisterUserDTO registerUserDTO = registerDTO.getAdmin();

        AppUser newAdmin = AppUser.builder()
                .username(registerUserDTO.getUsername())
                .password(encryptedPassword)
                .name(registerUserDTO.getName())
                .role(Roles.ADMIN)
                .store(newStore)
                .build();

        AppUser savedAdmin = userRepository.save(newAdmin);

        newAdmin.setId(savedAdmin.getId());

        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(
                registerUserDTO.getUsername(), registerUserDTO.getPassword());

        var auth = authenticationConfiguration.getAuthenticationManager()
                .authenticate(usernamePassword);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        String token = tokenService.generateToken(userDetails.getUsername());
        String refreshToken = tokenService.generateRefreshToken(userDetails.getUsername());

        ResponseUserDTO responseUserDTO = ResponseUserDTO.builder()
                .id(newAdmin.getId())
                .username(newAdmin.getUsername())
                .role(newAdmin.getRole().name())
                .name(newAdmin.getName())
                .build();

        ResponseStoreDTO responseStoreDTO = ResponseStoreDTO.builder()
                .id(newStore.getId())
                .slogan(newStore.getSlogan())
                .name(newStore.getName())
                .banner(newStore.getBanner())
                .build();

        return ResponseAuthDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(responseUserDTO)
                .store(responseStoreDTO)
                .build();
    }

    @Transactional
    public ResponseAuthDTO login(LoginRequestDTO loginRequestDTO) throws Exception {
        Optional<AppUser> existingUser = userRepository.findByUsername(loginRequestDTO.getUsername());

        if (existingUser.isEmpty()) {
            throw new UserNotFoundException(loginRequestDTO.getUsername());
        }

        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(
                loginRequestDTO.getUsername(), loginRequestDTO.getPassword());

        Authentication auth = authenticationConfiguration.getAuthenticationManager()
                .authenticate(usernamePassword);
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        String token = tokenService.generateToken(userDetails.getUsername());
        String refreshToken = tokenService.generateRefreshToken(userDetails.getUsername());

        AppUser appUser = existingUser.get();

        ResponseUserDTO responseUserDTO = ResponseUserDTO.builder()
                .username(appUser.getUsername())
                .name(appUser.getName())
                .role(appUser.getRole().name())
                .build();

        Store store;

        if (appUser.getRole() == Roles.ADMIN || appUser.getRole() == Roles.EMPLOYEE) {
            store = appUser.getStore();
        } else {
            throw new InvalidRoleException(appUser.getRole());
        }

        ResponseStoreDTO responseStoreDTO = ResponseStoreDTO.builder()
                .id(store.getId())
                .slogan(store.getSlogan())
                .name(store.getName())
                .banner(store.getBanner())
                .build();

        return ResponseAuthDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(responseUserDTO)
                .store(responseStoreDTO)
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AppUser> appUser = userRepository.findByUsername(username);
        return (UserDetails) appUser.orElse(null);
    }
}
