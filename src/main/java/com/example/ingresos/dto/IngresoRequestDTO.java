package com.example.ingresos.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class IngresoRequestDTO {
    @Schema(description = "ID del producto a ingresar", example = "1")
    @NotNull(message = "El productoId es obligatorio")
    @Positive(message = "El productoId debe ser positivo")
    private Long productoId;

    @Schema(description = "ID del proveedor que entrega la mercancía", example = "1")
    @NotNull(message = "El proveedorId es obligatorio")
    @Positive(message = "El proveedorId debe ser positivo")
    private Long proveedorId;

    @Schema(description = "Cantidad de unidades a ingresar", example = "50")
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Schema(description = "Número de guía de despacho asociado", example = "GUIA-2025-001")
    @NotBlank(message = "El numero de guia es obligatorio")
    @Size(max = 50, message = "El numero de guia no puede exceder 50 caracteres")
    private String numeroGuia;

    @Schema(description = "Observaciones adicionales del ingreso", example = "Mercancía en buen estado")
    @Size(max = 300, message = "Las observaciones no pueden exceder 300 caracteres")
    private String observaciones;

    @Schema(description = "ID de la bodega donde se almacenará el producto", example = "1")
    @NotNull(message = "El ID de bodega es obligatorio")
    @Positive(message = "El ID de bodega debe ser positivo")
    private Long bodegaId;
}