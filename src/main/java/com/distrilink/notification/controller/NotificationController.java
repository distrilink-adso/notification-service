package com.distrilink.notification.controller;

import com.distrilink.notification.dto.ApiResponse;
import com.distrilink.notification.dto.NotificationRequest;
import com.distrilink.notification.dto.NotificationResponse;
import com.distrilink.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Capa de Controllers: recibe HTTP, valida el DTO y delega en el Service.
 * Solo escribe el camino feliz: los errores los convierte GlobalExceptionHandler.
 * Todas las respuestas van dentro de ApiResponse (success, data, error, meta).
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "CRUD de las notificaciones de DistriLink")
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    @Operation(summary = "Crear una notificación")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Notificación creada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> create(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/notifications/" + created.id()))
                .body(ApiResponse.ok(created));
    }

    @GetMapping
    @Operation(summary = "Listar notificaciones",
            description = "Si se envía recipientId, devuelve solo las de ese usuario, de la más reciente a la más antigua")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> findAll(
            @Parameter(description = "Id del usuario destinatario", example = "15")
            @RequestParam(required = false) Long recipientId) {
        return ResponseEntity.ok(ApiResponse.ok(service.findAll(recipientId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una notificación por id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notificación encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No existe",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una notificación")
    public ResponseEntity<ApiResponse<NotificationResponse>> update(@PathVariable Long id,
                                                                    @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, request)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar una notificación como leída")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.markAsRead(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una notificación", description = "Responde 200 con success=true y data=null")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok());
    }
}
