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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/v1/auth/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/appstatus").permitAll()
                        .requestMatchers("/error", "/swagger-ui/*", "/v3/api-docs", "/v3/api-docs/*").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/v1/store/*").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/v1/store/*").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/v1/store/*").hasAnyRole(Roles.ADMIN.name(), Roles.EMPLOYEE.name())
                        .requestMatchers(HttpMethod.PUT, "/v1/store/*/book/*/available").hasAnyRole(Roles.EMPLOYEE.name(), Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/v1/store/*/book/*").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/v1/store/*/book").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/v1/store/*/book/*").hasRole(Roles.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/v1/store/*/book").hasAnyRole(Roles.ADMIN.name(), Roles.EMPLOYEE.name())
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
