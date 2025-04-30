package com.karoldm.bookstore.handlers;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.karoldm.bookstore.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;

@ControllerAdvice
public class GlobalExceptionHandler {

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
    private ProblemDetail errorJWTVerification(JWTVerificationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                ex.getMessage());
        problemDetail.setTitle("Token invalid error");
        problemDetail.setType(URI.create("http://localhost/9000/doc/token-errors"));
        return problemDetail;
    }

    @ExceptionHandler(JWTDecodeException.class)
    private ProblemDetail errorJWTDecode(JWTDecodeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                ex.getMessage());
        problemDetail.setTitle("Token invalid error");
        problemDetail.setType(URI.create("http://localhost/9000/doc/token-errors"));
        return problemDetail;
    }

    @ExceptionHandler(StoreNotFoundException.class)
    private ProblemDetail errorStoreNotFound(StoreNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                ex.getMessage());
        problemDetail.setTitle("Store not found error");
        problemDetail.setType(URI.create("http://localhost:9000/doc/not-found-errors"));
        return problemDetail;
    }

    @ExceptionHandler(BookNotFoundException.class)
    private ProblemDetail errorBookNotFound(BookNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                ex.getMessage());
        problemDetail.setTitle("Book not found error");
        problemDetail.setType(URI.create("http://localhost:9000/doc/not-found-errors"));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ProblemDetail errorMehtodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                errorMessage);
        problemDetail.setTitle("Invalid request body");
        problemDetail.setType(URI.create("http://localhost:9000/doc/bad-request-errors"));
        return problemDetail;
    }
}
