package vn.zaloppay.couponservice.presenter.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.zaloppay.couponservice.core.exceptions.BadRequestException;
import vn.zaloppay.couponservice.core.exceptions.ConflictException;
import vn.zaloppay.couponservice.core.exceptions.InternalServerErrorException;
import vn.zaloppay.couponservice.core.exceptions.ResourceNotFoundException;
import vn.zaloppay.couponservice.core.exceptions.TooManyRequestsException;
import vn.zaloppay.couponservice.presenter.entities.response.ApiResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new ApiResponse(ex.getMessage(), false, null), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(new ApiResponse(ex.getMessage(), false, null), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse> handleConflictException(ConflictException ex) {
        return new ResponseEntity<>(new ApiResponse(ex.getMessage(), false, null), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ApiResponse> handleInternalServerErrorException(InternalServerErrorException ex) {
        return new ResponseEntity<>(new ApiResponse(ex.getMessage(), false, null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse> handleTooManyRequestsException(TooManyRequestsException ex) {
        return new ResponseEntity<>(new ApiResponse(ex.getMessage(), false, null), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        
        ex.getBindingResult().getGlobalErrors().forEach(error -> errors.put(error.getObjectName(), error.getDefaultMessage()));
        
        return new ResponseEntity<>(
                new ApiResponse("Validation failed", false, errors), 
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        }
        
        return new ResponseEntity<>(
                new ApiResponse("Validation failed", false, errors), 
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "Invalid JSON format or unrecognized field in request body";
        
        // Extract more specific error information if available
        if (ex.getCause() != null) {
            String causeMessage = ex.getCause().getMessage();
            if (causeMessage != null && causeMessage.contains("Unrecognized field")) {
                message = "Invalid request: " + causeMessage.split("\\(")[0].trim();
            } else if (causeMessage != null && causeMessage.contains("JSON parse error")) {
                message = "Invalid JSON format in request body";
            }
        }
        
        return new ResponseEntity<>(
                new ApiResponse(message, false, null), 
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        ex.printStackTrace();
        return new ResponseEntity<>(new ApiResponse("Internal Server Error", false, null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
