package com.distrilink.notification.exception;

/**
 * Códigos de error comunes de DistriLink. Son los mismos en los 4 microservicios,
 * para que el frontend los reconozca sin importar qué servicio respondió.
 */
public final class ErrorCode {

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";       // 400: campos inválidos
    public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";     // 400: JSON mal escrito o valor de enum inexistente
    public static final String INVALID_PARAMETER = "INVALID_PARAMETER";     // 400: parámetro de la URL con tipo incorrecto
    public static final String UNAUTHORIZED = "UNAUTHORIZED";               // 401: sin token o token vencido
    public static final String FORBIDDEN = "FORBIDDEN";                     // 403: sin permiso para la acción
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";   // 404: el recurso no existe
    public static final String ENDPOINT_NOT_FOUND = "ENDPOINT_NOT_FOUND";   // 404: la ruta no existe
    public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";   // 405: método HTTP no soportado
    public static final String CONFLICT = "CONFLICT";                       // 409: choca con datos existentes
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";           // 500: fallo no controlado

    private ErrorCode() {
    }
}
