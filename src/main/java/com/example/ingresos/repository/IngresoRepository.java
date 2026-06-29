package com.example.ingresos.repository;

import com.example.ingresos.model.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IngresoRepository extends JpaRepository<Ingreso, Long> {
    Optional<Ingreso> findByNumeroGuia(String numeroGuia);
    List<Ingreso> findByProductoId(Long productoId);
    List<Ingreso> findByEstado(String estado);
}