package com.example.softdevoluciones.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import io.jsonwebtoken.JwtException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        return status(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return status(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiError> handleExpiredJwt(JwtException ex) {
        return status(HttpStatus.UNAUTHORIZED,
                "Tu sesión ha expirado o el acceso no es válido. Por favor, inicia sesión nuevamente para continuar.");
    }

    @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
    public ResponseEntity<ApiError> handleBadCredentials(RuntimeException ex) {
        return status(HttpStatus.UNAUTHORIZED,
                "El correo electrónico o la contraseña son incorrectos. Por favor, verifica los datos ingresados e inténtalo de nuevo.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex) {
        return status(HttpStatus.UNAUTHORIZED,
                "No has iniciado sesión o tu sesión no es válida. Por favor, inicia sesión para acceder a este recurso.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex) {
        return status(HttpStatus.FORBIDDEN,
                "No tienes permiso para realizar esta acción. Si crees que deberías tener acceso, contacta al administrador.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        ApiError body = new ApiError(
                "La información enviada no es válida. Por favor, revisa los campos indicados e inténtalo de nuevo.",
                HttpStatus.BAD_REQUEST.value(), errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBind(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        ApiError body = new ApiError(
                "La información enviada no es válida. Por favor, revisa los campos indicados e inténtalo de nuevo.",
                HttpStatus.BAD_REQUEST.value(), errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiError> handleMultipart(MultipartException ex) {
        return status(HttpStatus.BAD_REQUEST,
                "No fue posible procesar el archivo enviado. Por favor, verifica el tamaño y el formato de la imagen e inténtalo de nuevo.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return status(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado: " + ex.getMessage()
                        + ". Por favor, inténtalo de nuevo más tarde y contacta al soporte si el problema persiste.");
    }

    private ResponseEntity<ApiError> status(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(message, status.value()));
    }
}
