package ru.senla.scooterrental.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import ru.senla.scooterrental.common.exception.ConflictException;
import ru.senla.scooterrental.common.exception.ForbiddenException;
import ru.senla.scooterrental.common.exception.NotFoundException;
import ru.senla.scooterrental.common.exception.PaymentRequiredException;
import ru.senla.scooterrental.common.exception.ValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Authentication failed: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return build(
                HttpStatus.UNAUTHORIZED,
                "Неверный email или пароль",
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Access denied: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return build(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            ValidationException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Validation error: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Resource not found: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Conflict detected: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Forbidden operation: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(PaymentRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentRequired(
            PaymentRequiredException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Payment required: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return build(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        int statusCode = ex.getStatusCode().value();
        HttpStatus status = HttpStatus.valueOf(statusCode);

        String message = ex.getReason() != null
                ? ex.getReason()
                : ex.getMessage();

        log.warn(
                "Response status exception: method={}, uri={}, status={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                message
        );

        return build(status, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleBadBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Invalid request body: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return build(
                HttpStatus.BAD_REQUEST,
                "Некорректное тело запроса",
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = "Некорректный параметр '" + ex.getName()
                + "': " + ex.getValue();

        log.warn(
                "Method argument type mismatch: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                message
        );

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String message = "Отсутствует обязательный параметр: "
                + ex.getParameterName();

        log.warn(
                "Missing request parameter: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                message
        );

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        StringBuilder message = new StringBuilder();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            message.append(error.getField())
                    .append(": ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        }

        log.warn(
                "Method argument validation failed: method={}, uri={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                message
        );

        return build(
                HttpStatus.BAD_REQUEST,
                message.toString(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAny(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Unhandled server error: method={}, uri={}",
                request.getMethod(),
                request.getRequestURI(),
                ex
        );

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                requestPath(request)
        );

        return ResponseEntity.status(status).body(response);
    }

    private String requestPath(HttpServletRequest request) {
        String queryString = request.getQueryString();

        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }

        return request.getRequestURI() + "?" + queryString;
    }
}