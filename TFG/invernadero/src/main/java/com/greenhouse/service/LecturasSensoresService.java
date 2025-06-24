package com.greenhouse.service;

import com.greenhouse.model.LecturasSensores;

import java.util.List;
import java.util.Map;

public interface LecturasSensoresService {
    void guardarDesdeNotificacion(Map<String, Object> payload);
    LecturasSensores obtenerUltimaLectura();
    List<LecturasSensores> obtenerHistorial();
	List<Map<String, Object>> obtenerResumenHoy();
	List<Map<String, Object>> obtenerResumenUltimosDias(String periodo);

}

