package com.distrilink.notification.controller;

import com.distrilink.notification.dto.NotificationRequest;
import com.distrilink.notification.dto.NotificationResponse;
import com.distrilink.notification.exception.NotificationNotFoundException;
import com.distrilink.notification.model.NotificationChannel;
import com.distrilink.notification.model.NotificationStatus;
import com.distrilink.notification.model.NotificationType;
import com.distrilink.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba del Controller: solo levanta la capa web; el Service es un mock.
 * Comprueba que el código HTTP y el JSON (success, data, error, meta) siempre coincidan.
 */
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private NotificationService service;

    private static final String VALID_JSON = """
            {
              "recipientId": 15,
              "orderId": 120,
              "type": "ORDER_CONFIRMED",
              "title": "Pedido aprobado",
              "message": "Tu pedido #120 fue aprobado."
            }
            """;

    private NotificationResponse response() {
        return new NotificationResponse(1L, 15L, 120L, NotificationType.ORDER_CONFIRMED,
                NotificationChannel.IN_APP, "Pedido aprobado", "Tu pedido #120 fue aprobado.",
                NotificationStatus.PENDING, LocalDateTime.now(), null);
    }

    @Test
    void post_valido_devuelve201YElWrapperConLosDatos() throws Exception {
        when(service.create(any(NotificationRequest.class))).thenReturn(response());

        mvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/notifications/1"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.error").value(nullValue()))
                .andExpect(jsonPath("$.meta.timestamp").exists())
                .andExpect(jsonPath("$.meta.apiVersion").value("v1.0"));
    }

    @Test
    void get_lista_devuelve200ConUnaListaEnData() throws Exception {
        when(service.findAll(null)).thenReturn(List.of(response()));

        mvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void post_conVariosCamposInvalidos_devuelve400ConCadaCampoEnDetails() throws Exception {
        String invalid = """
                { "recipientId": -1, "type": "ORDER_CONFIRMED", "title": "", "message": "" }
                """;

        mvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details.recipientId").exists())
                .andExpect(jsonPath("$.error.details.title").exists())
                .andExpect(jsonPath("$.error.details.message").exists());
    }

    @Test
    void post_conJsonMalEscrito_devuelve400MalformedRequest() throws Exception {
        mvc.perform(post("/api/notifications").contentType(MediaType.APPLICATION_JSON).content("{ esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void get_conIdQueNoEsNumero_devuelve400InvalidParameter() throws Exception {
        mvc.perform(get("/api/notifications/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.error.details.id").exists());
    }

    @Test
    void get_inexistente_devuelve404ResourceNotFound() throws Exception {
        when(service.findById(99L)).thenThrow(new NotificationNotFoundException(99L));

        mvc.perform(get("/api/notifications/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("No existe la notificación con id 99"));
    }

    @Test
    void errorInesperado_devuelve500SinDetallesTecnicos() throws Exception {
        when(service.findById(7L)).thenThrow(new IllegalStateException("conexión a la BD caída"));

        mvc.perform(get("/api/notifications/7"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Ocurrió un error inesperado. Intenta de nuevo más tarde"));
    }

    @Test
    void delete_devuelve200ConSuccessYDataNull() throws Exception {
        mvc.perform(delete("/api/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}
