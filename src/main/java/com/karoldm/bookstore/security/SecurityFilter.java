package com.karoldm.bookstore.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karoldm.bookstore.entities.AppUser;
import com.karoldm.bookstore.repositories.AppUserRepository;
import com.karoldm.bookstore.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.hibernate.query.IllegalQueryOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@AllArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private AppUserRepository repository;
    private TokenService tokenService;

    @Override
    public void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IllegalQueryOperationException, IOException {
        try {
            Optional<String> token = recoverToken(request);

            if (token.isPresent()) {
                String username = tokenService.validateToken(token.get());
                Optional<AppUser> user = repository.findByUsername(username);

                if (user.isPresent()) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user.get(), null, user.get().getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);

        } catch (JWTVerificationException ex) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(
                    "{\"error\":\"Invalid token\",\"message\":\"" + ex.getMessage() + "\"}"
            );
        } catch (IOException ioEx) {
            throw new IOException(ioEx.getMessage());
        }
    }

    private Optional<String> recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return Optional.empty();
        return Optional.of(authHeader.replace("Bearer ", ""));
    }
}
