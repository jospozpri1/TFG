package com.greenhouse.repository;

import com.greenhouse.model.EventosMotor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface EventosMotorRepository extends JpaRepository<EventosMotor, Integer> {
    List<EventosMotor> findAllByOrderByFechaCreacionDesc();
    List<EventosMotor> findTop5ByOrderByFechaCreacionDesc();
}
