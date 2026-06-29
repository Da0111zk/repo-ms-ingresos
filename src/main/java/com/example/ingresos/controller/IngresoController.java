package com.example.ingresos.controller;

import com.example.ingresos.dto.IngresoRequestDTO;
import com.example.ingresos.dto.IngresoResponseDTO;
import com.example.ingresos.service.IngresoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
@Tag(name = "Ingresos", description = "Registro de ingresos de mercancía al sistema de bodega")
@RestController
@RequestMapping("/api/ingresos")
public class IngresoController {

    @Autowired
    private IngresoService service;

    @Operation(summary = "Registrar un nuevo ingreso", description = "Valida producto, proveedor y bodega antes de registrar el ingreso y actualizar el kardex")
    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Ingreso registrado correctamente"),
    @ApiResponse(responseCode = "400", description = "Datos inválidos o entidades relacionadas no existen")
    })
    @PostMapping
    public ResponseEntity<IngresoResponseDTO> crear(@Valid @RequestBody IngresoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }


    @Operation(summary = "Listar todos los ingresos", description = "Retorna el historial completo de ingresos registrados")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<IngresoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }


    @Operation(summary = "Buscar ingreso por ID")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Ingreso encontrado"),
    @ApiResponse(responseCode = "404", description = "Ingreso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<IngresoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Listar ingresos de un producto específico")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<IngresoResponseDTO>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(service.listarPorProducto(productoId));
    }

    @Operation(summary = "Listar ingresos en estado PENDIENTE")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping("/pendientes")
    public ResponseEntity<List<IngresoResponseDTO>> listarPendientes() {
        return ResponseEntity.ok(service.listarPendientes());
    }

    @Operation(summary = "Confirmar un ingreso pendiente", description = "Solo se puede confirmar un ingreso en estado PENDIENTE")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Ingreso confirmado correctamente"),
    @ApiResponse(responseCode = "400", description = "El ingreso no está en estado PENDIENTE"),
    @ApiResponse(responseCode = "404", description = "Ingreso no encontrado")
    })
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<IngresoResponseDTO> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(service.confirmar(id));
    }

    @Operation(summary = "Anular un ingreso")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Ingreso anulado correctamente"),
    @ApiResponse(responseCode = "404", description = "Ingreso no encontrado")
    })
    @PutMapping("/{id}/anular")
    public ResponseEntity<IngresoResponseDTO> anular(@PathVariable Long id) {
        return ResponseEntity.ok(service.anular(id));
    }
}