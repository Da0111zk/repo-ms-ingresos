package com.example.ingresos.service;

import com.example.ingresos.dto.IngresoRequestDTO;
import com.example.ingresos.dto.IngresoResponseDTO;
import com.example.ingresos.model.Ingreso;
import com.example.ingresos.repository.IngresoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IngresoService {

    @Autowired private IngresoRepository repository;
    @Autowired @Qualifier("webClientProductos") private WebClient webClientProductos;
    @Autowired @Qualifier("webClientProveedores") private WebClient webClientProveedores;
    @Autowired @Qualifier("webClientKardex") private WebClient webClientKardex;
    @Autowired @Qualifier("webClientBodega") private WebClient webClientBodega;

    public IngresoResponseDTO crear(IngresoRequestDTO dto) {
        if (repository.findByNumeroGuia(dto.getNumeroGuia()).isPresent())
            throw new RuntimeException("Ya existe un ingreso con el numero de guia: " + dto.getNumeroGuia());

        log.info("Validando productoId={} en ms-productos", dto.getProductoId());
        validarExterno(webClientProductos, "/api/productos/" + dto.getProductoId(),
                "Producto no encontrado con ID: " + dto.getProductoId());

        log.info("Validando proveedorId={} en ms-proveedores", dto.getProveedorId());
        validarExterno(webClientProveedores, "/api/proveedores/" + dto.getProveedorId(),
                "Proveedor no encontrado con ID: " + dto.getProveedorId());

        log.info("Validando bodegaId={} en ms-bodega", dto.getBodegaId());
        validarExterno(webClientBodega, "/api/ubicaciones/" + dto.getBodegaId(),
        "Bodega no encontrada con ID: " + dto.getBodegaId());

        Ingreso ingreso = new Ingreso();
        ingreso.setProductoId(dto.getProductoId());
        ingreso.setProveedorId(dto.getProveedorId());
        ingreso.setBodegaId(dto.getBodegaId());
        ingreso.setCantidad(dto.getCantidad());
        ingreso.setFechaRecepcion(LocalDate.now());
        ingreso.setNumeroGuia(dto.getNumeroGuia());
        ingreso.setObservaciones(dto.getObservaciones());
        ingreso.setEstado("PENDIENTE");

        Ingreso guardado = repository.save(ingreso);
        log.info("Ingreso creado con ID={}, numeroGuia={}", guardado.getId(), guardado.getNumeroGuia());
        return toResponse(guardado);
    }

    public IngresoResponseDTO confirmar(Long id) {
        Ingreso ingreso = buscarEntidad(id);
        if (!ingreso.getEstado().equals("PENDIENTE"))
            throw new RuntimeException("Solo se puede confirmar un ingreso en estado PENDIENTE");

        try {
            Map<String, Object> movimiento = new HashMap<>();
            movimiento.put("productoId", ingreso.getProductoId());
            movimiento.put("ubicacionId", ingreso.getBodegaId());
            movimiento.put("cantidad", ingreso.getCantidad());
            movimiento.put("tipoMovimiento", "INGRESO");

            if (ingreso.getNumeroGuia() != null) {
                movimiento.put("referencia", ingreso.getNumeroGuia());
            }

            webClientKardex.post()
                    .uri("/api/kardex/movimiento")
                    .bodyValue(movimiento)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            log.info("Movimiento INGRESO registrado en Kardex para productoId={}", ingreso.getProductoId());
        } catch (Exception e) {
            log.warn("No se pudo registrar en Kardex: {}", e.getMessage());
        }

        ingreso.setEstado("CONFIRMADO");
        log.info("Ingreso ID={} CONFIRMADO", id);
        return toResponse(repository.save(ingreso));
    }

    public IngresoResponseDTO anular(Long id) {
        Ingreso ingreso = buscarEntidad(id);
        if (ingreso.getEstado().equals("CONFIRMADO"))
            throw new RuntimeException("No se puede anular un ingreso ya CONFIRMADO");
        ingreso.setEstado("ANULADO");
        log.info("Ingreso ID={} ANULADO", id);
        return toResponse(repository.save(ingreso));
    }

    public List<IngresoResponseDTO> listarTodos() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public IngresoResponseDTO buscarPorId(Long id) {
        return toResponse(buscarEntidad(id));
    }

    public List<IngresoResponseDTO> listarPorProducto(Long productoId) {
        return repository.findByProductoId(productoId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<IngresoResponseDTO> listarPendientes() {
        return repository.findByEstado("PENDIENTE").stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Ingreso buscarEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado con ID: " + id));
    }

    private void validarExterno(WebClient client, String uri, String mensaje) {
        try {
            client.get().uri(uri).retrieve()
                    .bodyToMono(Object.class)
                    .timeout(Duration.ofSeconds(3)).block();
        } catch (Exception e) {
            log.error("Error al validar externo {}: {}", uri, e.getMessage());
            throw new RuntimeException(mensaje);
        }
    }

    private IngresoResponseDTO toResponse(Ingreso i) {
        IngresoResponseDTO r = new IngresoResponseDTO();
        r.setId(i.getId());
        r.setProductoId(i.getProductoId());
        r.setProveedorId(i.getProveedorId());
        r.setBodegaId(i.getBodegaId());  
        r.setCantidad(i.getCantidad());
        r.setFechaRecepcion(i.getFechaRecepcion());
        r.setNumeroGuia(i.getNumeroGuia());
        r.setObservaciones(i.getObservaciones());
        r.setEstado(i.getEstado());
        r.setBodegaId(i.getBodegaId());
        return r;
    }
}