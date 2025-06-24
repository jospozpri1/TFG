package com.greenhouse.repository;

import com.greenhouse.model.Limites;
import com.greenhouse.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LimitesRepository extends JpaRepository<Limites, Integer> {
    Optional<Limites> findTopByOrderByFechaActualizacionDesc();
	Limites findByUsuarioId(Integer usuarioId);
    Optional<Limites> findTopByUsuarioOrderByFechaActualizacionDesc(Usuarios usuario);
    List<Limites> findByUsuarioOrderByFechaActualizacionDesc(Usuarios usuario);
}
