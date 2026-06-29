package com.example.ingresos.service;

import com.example.ingresos.dto.IngresoRequestDTO;
import com.example.ingresos.dto.IngresoResponseDTO;
import com.example.ingresos.model.Ingreso;
import com.example.ingresos.repository.IngresoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class IngresoServiceTest {

    @Mock IngresoRepository repository;
    @Mock(name = "webClientProductos") WebClient webClientProductos;
    @Mock(name = "webClientProveedores") WebClient webClientProveedores;
    @Mock(name = "webClientKardex") WebClient webClientKardex;
    @Mock(name = "webClientBodega") WebClient webClientBodega;
    @InjectMocks IngresoService service;

    @Mock WebClient.RequestHeadersUriSpec getUriSpec;
    @Mock WebClient.RequestHeadersSpec headersSpec;
    @Mock WebClient.ResponseSpec responseSpec;
    @Mock WebClient.RequestBodyUriSpec postUriSpec;
    @Mock WebClient.RequestBodySpec bodySpec;

    private void mockGetSuccess() {
        when(webClientProductos.get()).thenReturn(getUriSpec);
        when(webClientProveedores.get()).thenReturn(getUriSpec);
        when(webClientBodega.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Object.class)).thenReturn(Mono.just(new Object()));
    }

    private void mockPostKardex(boolean success) {
        when(webClientKardex.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        Mono<Object> mono = success ? Mono.just(new Object()) : Mono.error(new RuntimeException());
        when(responseSpec.bodyToMono(Object.class)).thenReturn(mono);
    }

    private IngresoRequestDTO dtoBase() {
        IngresoRequestDTO dto = new IngresoRequestDTO();
        dto.setNumeroGuia("G001");
        dto.setProductoId(1L);
        dto.setProveedorId(2L);
        dto.setBodegaId(3L);
        dto.setCantidad(10);
        return dto;
    }

    private Ingreso ingresoMock(Long id, String estado) {
        Ingreso i = new Ingreso();
        i.setId(id);
        i.setProductoId(1L);
        i.setProveedorId(2L);
        i.setBodegaId(3L);
        i.setCantidad(10);
        i.setFechaRecepcion(LocalDate.now());
        i.setNumeroGuia("G" + id);
        i.setEstado(estado);
        return i;
    }

    @Test
    void crear_exito() {
        mockGetSuccess();
        when(repository.findByNumeroGuia("G001")).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(ingresoMock(100L, "PENDIENTE"));

        IngresoResponseDTO resp = service.crear(dtoBase());

        assertThat(resp.getId()).isEqualTo(100L);
        assertThat(resp.getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    void crear_guiaDuplicada_lanzaExcepcion() {
        when(repository.findByNumeroGuia("G001")).thenReturn(Optional.of(new Ingreso()));

        assertThatThrownBy(() -> service.crear(dtoBase()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un ingreso");
    }

    @Test
    void crear_productoNoEncontrado_lanzaExcepcion() {
        when(repository.findByNumeroGuia("G001")).thenReturn(Optional.empty());
        when(webClientProductos.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri(anyString())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Object.class)).thenReturn(Mono.error(new RuntimeException()));

        assertThatThrownBy(() -> service.crear(dtoBase()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Producto no encontrado");
    }

    @Test
    void confirmar_exito() {
        Ingreso pendiente = ingresoMock(10L, "PENDIENTE");
        when(repository.findById(10L)).thenReturn(Optional.of(pendiente));
        when(repository.save(any())).thenReturn(pendiente);
        mockPostKardex(true);

        IngresoResponseDTO resp = service.confirmar(10L);

        assertThat(resp.getEstado()).isEqualTo("CONFIRMADO");
        verify(webClientKardex).post();
    }

    @Test
    void confirmar_falloKardex_noBloquea() {
        Ingreso pendiente = ingresoMock(11L, "PENDIENTE");
        when(repository.findById(11L)).thenReturn(Optional.of(pendiente));
        when(repository.save(any())).thenReturn(pendiente);
        mockPostKardex(false);

        IngresoResponseDTO resp = service.confirmar(11L);

        assertThat(resp.getEstado()).isEqualTo("CONFIRMADO");
    }

    @Test
    void confirmar_estadoNoPendiente_lanzaExcepcion() {
        when(repository.findById(12L)).thenReturn(Optional.of(ingresoMock(12L, "CONFIRMADO")));

        assertThatThrownBy(() -> service.confirmar(12L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Solo se puede confirmar");
    }

    @Test
    void anular_desdePendiente_exito() {
        when(repository.findById(20L)).thenReturn(Optional.of(ingresoMock(20L, "PENDIENTE")));
        when(repository.save(any())).thenReturn(ingresoMock(20L, "ANULADO"));

        assertThat(service.anular(20L).getEstado()).isEqualTo("ANULADO");
    }

    @Test
    void anular_confirmado_lanzaExcepcion() {
        when(repository.findById(21L)).thenReturn(Optional.of(ingresoMock(21L, "CONFIRMADO")));

        assertThatThrownBy(() -> service.anular(21L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se puede anular");
    }

    @Test
    void listarTodos_retornaLista() {
        when(repository.findAll()).thenReturn(List.of(ingresoMock(1L, "PENDIENTE"), ingresoMock(2L, "CONFIRMADO")));

        assertThat(service.listarTodos()).hasSize(2);
    }

    @Test
    void buscarPorId_noEncontrado_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ingreso no encontrado");
    }

    @Test
    void listarPendientes_soloPendientes() {
        when(repository.findByEstado("PENDIENTE")).thenReturn(List.of(ingresoMock(1L, "PENDIENTE")));

        assertThat(service.listarPendientes()).hasSize(1);
    }
}