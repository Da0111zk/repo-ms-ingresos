package com.example.ingresos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "INGRESOS")
public class Ingreso {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_ingresos")
    @SequenceGenerator(name = "seq_ingresos", sequenceName = "SEQ_INGRESOS", allocationSize = 1)
    private Long id;

    @Column(name = "PRODUCTO_ID", nullable = false)
    private Long productoId;

    @Column(name = "PROVEEDOR_ID", nullable = false)
    private Long proveedorId;

    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

    @Column(name = "FECHA_RECEPCION", nullable = false)
    private LocalDate fechaRecepcion;

    @Column(name = "NUMERO_GUIA", nullable = false, unique = true, length = 50)
    private String numeroGuia;

    @Column(name = "OBSERVACIONES", length = 300)
    private String observaciones;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "BODEGA_ID", nullable = false)
    private Long bodegaId;
}