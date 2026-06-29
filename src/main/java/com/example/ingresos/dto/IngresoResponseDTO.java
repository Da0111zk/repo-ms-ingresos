package com.example.ingresos.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class IngresoResponseDTO {
    private Long id;
    private Long productoId;
    private Long proveedorId;
    private Integer cantidad;
    private LocalDate fechaRecepcion;
    private String numeroGuia;
    private String observaciones;
    private String estado;
    private Long bodegaId;
}