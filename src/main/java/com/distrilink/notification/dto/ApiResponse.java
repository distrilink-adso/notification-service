package com.distrilink.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Wrapper estándar de DistriLink: TODAS las respuestas de la API tienen esta forma,
 * sin importar si es un objeto, una lista o un error.
 *
 * <pre>
 * { "success": true|false, "data": ..., "error": {...}|null, "meta": {...} }
 * </pre>
 *
 * Mismo archivo en los 4 microservicios: solo cambia el paquete.
 *
 * @param <T> tipo de lo que va en "data" (un DTO, una lista de DTOs o Void)
 */
@Getter
@Schema(description = "Respuesta estándar de la API")
public class ApiResponse<T> {

    public static final String API_VERSION = "v1.0";

    @Schema(description = "true si la operación terminó con éxito")
    private final boolean success;

    @Schema(description = "El recurso pedido. Es null cuando hay error")
    private final T data;

    @Schema(description = "Detalle del fallo. Es null cuando todo salió bien")
    private final ApiError error;

    @Schema(description = "Datos de contexto de la respuesta")
    private final ApiMeta meta = new ApiMeta();

    private ApiResponse(boolean success, T data, ApiError error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    /** Respuesta exitosa con datos (un objeto o una lista). */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /** Respuesta exitosa sin datos (por ejemplo, después de eliminar). */
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    /** Respuesta de error. details puede ser null cuando no hay campos que señalar. */
    public static ApiResponse<Void> error(String code, String message, Map<String, String> details) {
        return new ApiResponse<>(false, null, new ApiError(code, message, details));
    }

    /** Detalle del error: un código para el programador y un mensaje para el usuario. */
    @Getter
    @Schema(description = "Detalle del error")
    public static class ApiError {

        @Schema(description = "Código técnico del error", example = "VALIDATION_ERROR")
        private final String code;

        @Schema(description = "Mensaje para el usuario", example = "Hay datos inválidos en la petición")
        private final String message;

        @Schema(description = "Campo → motivo, cuando el error es de validación")
        private final Map<String, String> details;

        public ApiError(String code, String message, Map<String, String> details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }
    }

    /** Datos de contexto que se llenan solos. */
    @Getter
    @Schema(description = "Datos de contexto")
    public static class ApiMeta {

        @Schema(description = "Momento de la respuesta (UTC, ISO-8601)", example = "2026-11-13T21:05:43.125Z")
        private final String timestamp = Instant.now().toString();

        @Schema(description = "Versión de la API", example = API_VERSION)
        private final String apiVersion = API_VERSION;
    }
}
