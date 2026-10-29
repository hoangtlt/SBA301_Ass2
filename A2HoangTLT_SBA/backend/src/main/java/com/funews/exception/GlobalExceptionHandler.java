/**
 * @author HoangTLT
 */

package com.funews.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    public record Error(String message, Map<String, String> errors, String path) { }
    @ExceptionHandler(ApiException.class)
    ResponseEntity<Error> business(ApiException e, HttpServletRequest request) {
        return ResponseEntity.status(e.getStatus()).body(new Error(e.getMessage(), Map.of(), request.getRequestURI()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Error> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new Error("Dữ liệu chưa hợp lệ. Vui lòng kiểm tra các trường.", fields, request.getRequestURI()));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<Error> invalidType(Exception e, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new Error("Sai kiểu dữ liệu hoặc nội dung yêu cầu.", Map.of(), request.getRequestURI()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Error> integrity(DataIntegrityViolationException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Error("Dữ liệu bị trùng hoặc đang được sử dụng. Vui lòng tải lại và kiểm tra.", Map.of(), request.getRequestURI()));
    }
}
