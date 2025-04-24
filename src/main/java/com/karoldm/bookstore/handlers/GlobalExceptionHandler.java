package com.karoldm.bookstore.handlers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karoldm.bookstore.exceptions.InvalidRoleException;
import com.karoldm.bookstore.exceptions.StoreAlreadyExist;
import com.karoldm.bookstore.exceptions.UserNotFoundException;
import com.karoldm.bookstore.exceptions.UsernameAlreadyExist;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidRoleException.class)
    private ProblemDetail errorInvalidRole(InvalidRoleException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                ex.getMessage());
        problemDetail.setTitle("Unauthorized role error");
        problemDetail.setType(URI.create("http://localhost/9000/doc/role-errors"));
        return problemDetail;
    }

    @ExceptionHandler(StoreAlreadyExist.class)
    private ProblemDetail errorStoreAlreadyExist(StoreAlreadyExist ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                ex.getMessage());
        problemDetail.setTitle("Store already exist error");
        problemDetail.setType(URI.create("http://localhost/9000/doc/conflict-errors"));
        return problemDetail;
    }

    @ExceptionHandler(UsernameAlreadyExist.class)
    private ProblemDetail errorUsernameAlreadyExist(UsernameAlreadyExist ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                ex.getMessage());
        problemDetail.setTitle("Username already exist error");
        problemDetail.setType(URI.create("http://localhost/9000/doc/conflict-errors"));
        return problemDetail;
    }

    @ExceptionHandler(UserNotFoundException.class)
    private ProblemDetail errorUserNotFound(UserNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                ex.getMessage());
        problemDetail.setTitle("User not found error");
        problemDetail.setType(URI.create("http://localhost/9000/doc/not-found-errors"));
        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    private ProblemDetail errorBadCredentials(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                ex.getMessage());
        problemDetail.setTitle("Bad credentials error");
        problemDetail.setType(URI.create("http://localhost/9000/doc/bad-credentials-errors"));
        return problemDetail;
    }

    @ExceptionHandler(JWTVerificationException.class)
    private ProblemDetail errorBadCredentials(JWTVerificationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                ex.getMessage());
        problemDetail.setTitle("Token invalid error");
        problemDetail.setType(URI.create("http://localhost/9000/doc/token-errors"));
        return problemDetail;
    }
}
