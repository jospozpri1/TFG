package com.greenhouse.repository;

import com.greenhouse.model.EstadoMotor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstadoMotorRepository extends JpaRepository<EstadoMotor, Integer> {
    EstadoMotor findTopByOrderByFechaCreacionDesc();
}
