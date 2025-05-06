package com.karoldm.bookstore.services;

import com.karoldm.bookstore.dto.requests.LoginRequestDTO;
import com.karoldm.bookstore.dto.requests.RegisterStoreDTO;
import com.karoldm.bookstore.dto.requests.RegisterUserDTO;
import com.karoldm.bookstore.dto.responses.ResponseAuthDTO;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.entities.Store;
import com.karoldm.bookstore.enums.Roles;
import com.karoldm.bookstore.exceptions.InvalidRoleException;
import com.karoldm.bookstore.exceptions.StoreAlreadyExist;
import com.karoldm.bookstore.exceptions.UserNotFoundException;
import com.karoldm.bookstore.exceptions.UsernameAlreadyExist;
import com.karoldm.bookstore.repositories.AppUserRepository;
import com.karoldm.bookstore.repositories.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    private AuthService authService;
    @Mock
    private AppUserRepository userRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private AuthenticationConfiguration authenticationConfiguration;
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetails userDetails;
    @Mock
    private TokenService tokenService;

    private RegisterUserDTO registerUserDTO;
    private RegisterStoreDTO registerStoreDTO;
    private LoginRequestDTO loginRequestDTO;

    @BeforeEach
    void setup() {

        registerUserDTO = RegisterUserDTO.builder()
                .name("karol marques")
                .username("karol.marques")
                .password("123456")
                .build();

        registerStoreDTO = RegisterStoreDTO.builder()
                .admin(registerUserDTO)
                .name("book store")
                .banner(null)
                .slogan("The best tech books")
                .build();

        loginRequestDTO = LoginRequestDTO.builder()
                .username("karol.marques")
                .password("123456")
                .build();
    }

    @Nested
    class RegisterTests {
        @Test
        void mustThrowUserAlreadyExist() {
            when(userRepository.findByUsername(registerUserDTO.getUsername()))
                    .thenReturn(Optional.of(AppUser.builder().build()));

            Exception exception = assertThrows(UsernameAlreadyExist.class, () -> {
                authService.register(registerStoreDTO);
            });

            assertEquals("Já existe um usuário com o username " + registerUserDTO.getUsername(),
                    exception.getMessage());

            verify(userRepository, times(1))
                    .findByUsername(registerUserDTO.getUsername());
        }


        @Test
        void mustThrowStoreAlreadyExist() {
            when(userRepository.findByUsername(registerUserDTO.getUsername()))
                    .thenReturn(Optional.empty());

            when(storeRepository.findByName(registerStoreDTO.getName())).thenReturn(
                    Optional.of(Store.builder().build()));

            Exception exception = assertThrows(StoreAlreadyExist.class, () -> {
                authService.register(registerStoreDTO);
            });

            assertEquals("Uma loja com o nome " + registerStoreDTO.getName() + " já existe.",
                    exception.getMessage());

            verify(userRepository, times(1))
                    .findByUsername(registerUserDTO.getUsername());

            verify(storeRepository, times(1))
                    .findByName(registerStoreDTO.getName());
        }


        @Test
        void mustRegister() throws Exception {
            when(userRepository.findByUsername(registerUserDTO.getUsername()))
                    .thenReturn(Optional.empty());

            when(storeRepository.findByName(registerStoreDTO.getName())).thenReturn(
                    Optional.empty());

            when(userRepository.save(any(AppUser.class)))
                    .thenReturn(AppUser.builder().id(UUID.randomUUID()).role(Roles.ADMIN).build());

            when(storeRepository.save(any(Store.class)))
                    .thenReturn(Store.builder().id(UUID.randomUUID()).build());

            when(authenticationConfiguration.getAuthenticationManager())
                    .thenReturn(authenticationManager);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);

            when(authentication.getPrincipal()).thenReturn(userDetails);

            when(userDetails.getUsername()).thenReturn(registerUserDTO.getUsername());

            when(tokenService.generateToken(any(String.class))).thenReturn("token");
            when(tokenService.generateRefreshToken(any(String.class))).thenReturn("refresh-token");

            ResponseAuthDTO responseAuthDTO = authService.register(registerStoreDTO);

            assertEquals("karol marques", responseAuthDTO.getUser().getName());
            assertEquals("karol.marques", responseAuthDTO.getUser().getUsername());
            assertEquals("ADMIN", responseAuthDTO.getUser().getRole());

            assertEquals("book store", responseAuthDTO.getStore().getName());
            assertEquals("The best tech books", responseAuthDTO.getStore().getSlogan());

            assertEquals("token", responseAuthDTO.getToken());
            assertEquals("refresh-token", responseAuthDTO.getRefreshToken());

            verify(userRepository, times(1))
                    .findByUsername(registerUserDTO.getUsername());

            verify(storeRepository, times(1))
                    .findByName(registerStoreDTO.getName());

            verify(userRepository, times(1))
                    .save(any(AppUser.class));

            verify(storeRepository, times(1))
                    .save(any(Store.class));

            verify(authenticationManager, times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            verify(tokenService, times(1)).generateToken(any(String.class));
            verify(tokenService, times(1)).generateRefreshToken(any(String.class));
        }
    }

    @Nested
    class LoginTests {
        @Test
        void mustThrowUserNotFound() {
            when(userRepository.findByUsername(loginRequestDTO.getUsername()))
                    .thenReturn(Optional.empty());

            Exception exception = assertThrows(UserNotFoundException.class, () -> {
                authService.login(loginRequestDTO);
            });

            assertEquals("Usuário com username " + loginRequestDTO.getUsername() + " não encontrado.",
                    exception.getMessage());

            verify(userRepository, times(1))
                    .findByUsername(loginRequestDTO.getUsername());
        }

        @Test
        void mustThrowBadCredentialsException() throws Exception {
            when(userRepository.findByUsername(loginRequestDTO.getUsername()))
                    .thenReturn(Optional.of(AppUser.builder().build()));

            when(authenticationConfiguration.getAuthenticationManager())
                    .thenReturn(authenticationManager);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Senha incorreta."));

            Exception exception = assertThrows(BadCredentialsException.class, () -> {
                authService.login(loginRequestDTO);
            });

            assertEquals("Senha incorreta.", exception.getMessage());

            verify(userRepository, times(1))
                    .findByUsername(loginRequestDTO.getUsername());

            verify(authenticationManager, times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        void mustThrowInvalidRoleException() throws Exception {
            AppUser user = AppUser.builder()
                    .role(Roles.COMMON)
                    .username("karol.marques")
                    .name("karol marques")
                    .id(UUID.randomUUID())
                    .build();

            when(userRepository.findByUsername(loginRequestDTO.getUsername()))
                    .thenReturn(Optional.of(user));

            when(authenticationConfiguration.getAuthenticationManager())
                    .thenReturn(authenticationManager);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);

            when(authentication.getPrincipal()).thenReturn(userDetails);

            Exception exception = assertThrows(InvalidRoleException.class, () -> {
                authService.login(loginRequestDTO);
            });

            assertEquals("Usuário com role " + user.getRole().name() + " não tem acesso a esse recurso.",
                    exception.getMessage());

            verify(userRepository, times(1))
                    .findByUsername(loginRequestDTO.getUsername());

            verify(authenticationManager, times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
        }


        @Test
        void mustLogin() throws Exception {
            Store store = Store.builder()
                    .id(UUID.randomUUID())
                    .name("book store")
                    .slogan("The best tech books")
                    .banner(null)
                    .build();

            AppUser user = AppUser.builder()
                    .role(Roles.ADMIN)
                    .username("karol.marques")
                    .name("karol marques")
                    .id(UUID.randomUUID())
                    .store(store)
                    .build();

            when(userRepository.findByUsername(loginRequestDTO.getUsername()))
                    .thenReturn(Optional.of(user));

            when(userDetails.getUsername()).thenReturn(loginRequestDTO.getUsername());

            when(tokenService.generateToken(any(String.class))).thenReturn("token");
            when(tokenService.generateRefreshToken(any(String.class))).thenReturn("refresh-token");

            when(authenticationConfiguration.getAuthenticationManager())
                    .thenReturn(authenticationManager);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);

            when(authentication.getPrincipal()).thenReturn(userDetails);

            ResponseAuthDTO responseAuthDTO = authService.login(loginRequestDTO);

            assertEquals("karol marques", responseAuthDTO.getUser().getName());
            assertEquals("karol.marques", responseAuthDTO.getUser().getUsername());
            assertEquals("ADMIN", responseAuthDTO.getUser().getRole());

            assertEquals("book store", responseAuthDTO.getStore().getName());
            assertEquals("The best tech books", responseAuthDTO.getStore().getSlogan());

            assertEquals("token", responseAuthDTO.getToken());
            assertEquals("refresh-token", responseAuthDTO.getRefreshToken());

            verify(userRepository, times(1))
                    .findByUsername(registerUserDTO.getUsername());

            verify(authenticationManager, times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));

            verify(tokenService, times(1)).generateToken(any(String.class));
            verify(tokenService, times(1)).generateRefreshToken(any(String.class));
        }
    }

}
