package com.swarm.dashboard.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ────────────────────────────────────────
    // 400 — 잘못된 파라미터 (ageGroup 등 validateAgeGroup에서 발생)
    // ────────────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request", e.getMessage(), request.getRequestURI()));
    }

    // ────────────────────────────────────────
    // 400 — @Valid 검증 실패 (Request Body 필드 누락 등)
    // ────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request", message, request.getRequestURI()));
    }

    // ────────────────────────────────────────
    // 400 — 필수 QueryParam 누락 (userId 등)
    // ────────────────────────────────────────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request",
                        "필수 파라미터가 누락되었습니다: " + e.getParameterName(), request.getRequestURI()));
    }

    // ────────────────────────────────────────
    // 404 — 리소스 없음 (시뮬레이션, 사용자 등)
    // ────────────────────────────────────────
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(
            RuntimeException e, HttpServletRequest request) {
        String msg = e.getMessage() != null ? e.getMessage() : "요청을 처리할 수 없습니다.";
        if (msg.contains("찾을 수 없습니다")) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of(404, "Not Found", msg, request.getRequestURI()));
        }
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Internal Server Error", "서버 오류가 발생했습니다.", request.getRequestURI()));
    }
}
