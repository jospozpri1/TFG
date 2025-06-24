package com.greenhouse.service;

import com.greenhouse.model.EstadoMotor;
import java.util.List;

public interface EstadoMotorService {
	EstadoMotor obtenerUltimoEstado();
    EstadoMotor registrarCambioEstado(boolean nuevoEstado, String motivo);
}

