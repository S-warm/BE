package com.swarm.dashboard.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

/**
 * 모든 API 에러 응답의 공통 포맷
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "...",
 *   "timestamp": "2026-04-15T14:30:00+09:00",
 *   "path": "/api/simulations/..."
 * }
 */
public record ErrorResponse(
        int status,
        String error,
        String message,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime timestamp,

        String path
) {
    /** path 없이 생성하는 편의 메서드 */
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, OffsetDateTime.now(), null);
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, OffsetDateTime.now(), path);
    }
}
