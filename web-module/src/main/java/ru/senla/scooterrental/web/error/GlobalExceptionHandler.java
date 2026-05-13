package ru.senla.scooterrental.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
            HttpServletRequest req
    ) {

        log.warn(
                "Validation error: method={}, uri={}, message={}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getMessage()
        );

        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            NotFoundException ex,
            HttpServletRequest req
    ) {

        log.warn(
                "Resource not found: method={}, uri={}, message={}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getMessage()
        );

        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest req
    ) {

        log.warn(
                "Conflict detected: method={}, uri={}, message={}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getMessage()
        );

        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest req
    ) {

        log.warn(
                "Forbidden operation: method={}, uri={}, message={}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getMessage()
        );

        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(PaymentRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handlePaymentRequired(
            PaymentRequiredException ex,
            HttpServletRequest req
    ) {

        log.warn(
                "Payment required: method={}, uri={}, message={}",
                req.getMethod(),
                req.getRequestURI(),
                ex.getMessage()
        );

        return build(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), req);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest req
    ) {
        int statusCode = ex.getStatusCode().value();
        HttpStatus status = HttpStatus.valueOf(statusCode);

        String message = ex.getReason() != null
                ? ex.getReason()
                : ex.getMessage();

        log.warn(
                "Response status exception: method={}, uri={}, status={}, message={}",
                req.getMethod(),
                req.getRequestURI(),
                status.value(),
                message
        );

        return build(status, message, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleBadBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest req
    ) {

        log.warn(
                "Invalid request body: method={}, uri={}",
                req.getMethod(),
                req.getRequestURI()
        );

        return build(
                HttpStatus.BAD_REQUEST,
                "Некорректное тело запроса",
                req
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest req
    ) {
        String message = "Некорректный параметр '" + ex.getName()
                + "': " + ex.getValue();

        log.warn(
                "Method argument type mismatch: method={}, uri={}, message={}",
                req.getMethod(),
                req.getRequestURI(),
                message
        );

        return build(HttpStatus.BAD_REQUEST, message, req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
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
                req.getMethod(),
                req.getRequestURI(),
                message
        );

        return build(HttpStatus.BAD_REQUEST, message.toString(), req);
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
            HttpServletRequest req
    ) {
        ApiErrorResponse dto = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                requestPath(req)
        );

        return ResponseEntity.status(status).body(dto);
    }

    private String requestPath(HttpServletRequest req) {
        String queryString = req.getQueryString();

        if (queryString == null || queryString.isBlank()) {
            return req.getRequestURI();
        }

        return req.getRequestURI() + "?" + queryString;
    }
}