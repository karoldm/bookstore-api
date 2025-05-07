package com.karoldm.bookstore.security;


import com.karoldm.bookstore.enums.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final SecurityFilter securityFilter;

    private static final String STORE_PATH = "/v1/store/*";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // CSRF disabled because we're stateless JWT API
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/v1/auth/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/appstatus").permitAll()
                        .requestMatchers(
                                "/error",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/swagger-resources",
                                "/swagger-resources/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.PUT, STORE_PATH).hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, STORE_PATH).hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, STORE_PATH).hasAnyRole(Roles.ADMIN.name(), Roles.EMPLOYEE.name())
                        .requestMatchers(HttpMethod.PUT, STORE_PATH+"/book/*/available").hasAnyRole(Roles.EMPLOYEE.name(), Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, STORE_PATH+"/book/*").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, STORE_PATH+"/book").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, STORE_PATH+"/book/*").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, STORE_PATH+"/book").hasAnyRole(Roles.ADMIN.name(), Roles.EMPLOYEE.name())
                        .requestMatchers(HttpMethod.GET, STORE_PATH+"/employee").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, STORE_PATH+"/employee").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, STORE_PATH+"/employee/*").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, STORE_PATH+"/employee/*").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/v1/admin").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/v1/admin").hasRole(Roles.ADMIN.name())
                        .anyRequest().authenticated()// Authenticated for all other endpoints
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
