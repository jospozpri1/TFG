package com.greenhouse.service.impl;

import com.greenhouse.model.LecturasSensores;
import com.greenhouse.repository.LecturasSensoresRepository;
import com.greenhouse.service.LecturasSensoresService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LecturasSensoresServiceImpl implements LecturasSensoresService {

    @Autowired
    private LecturasSensoresRepository repository;

    @Override
	public void guardarDesdeNotificacion(Map<String, Object> payload) {
		try {
		    System.out.println("Payload en servicio: " + payload);

		    List<Map<String, Object>> data = (List<Map<String, Object>>) payload.get("data");
		    if (data == null || data.isEmpty()) {
		        System.err.println("El campo 'data' está vacío o no existe");
		        return;
		    }

		    Map<String, Object> entidad = data.get(0);

		    Map<String, Object> humedadAmb = (Map<String, Object>) entidad.get("humedad_ambiental");
		    Map<String, Object> humedadSuelo = (Map<String, Object>) entidad.get("humedad_suelo");
		    Map<String, Object> luz = (Map<String, Object>) entidad.get("luz");
		    Map<String, Object> nivel = (Map<String, Object>) entidad.get("nivel");
		    Map<String, Object> tempC = (Map<String, Object>) entidad.get("temperatura_C");
		    Map<String, Object> tempF = (Map<String, Object>) entidad.get("temperatura_F");

		    LecturasSensores lectura = new LecturasSensores();
		    lectura.setHumedadAmbiental(Double.parseDouble(humedadAmb.get("value").toString()));
		    lectura.setHumedadSuelo(Integer.parseInt(humedadSuelo.get("value").toString()));
		    lectura.setLuzAmbiente(Integer.parseInt(luz.get("value").toString()));
		    lectura.setNivelAgua(Integer.parseInt(nivel.get("value").toString()));
		    lectura.setTemperaturaC(Double.parseDouble(tempC.get("value").toString()));
		    lectura.setTemperaturaF(Double.parseDouble(tempF.get("value").toString()));

		    repository.save(lectura);

		    System.out.println("Lectura guardada en la base de datos.");
		} catch (Exception e) {
		    System.err.println("Error al procesar notificación FIWARE:");
		    e.printStackTrace();
		}
	}

    @Override
    public LecturasSensores obtenerUltimaLectura() {
        return repository.findUltimaLectura();
    }

    @Override
    public List<LecturasSensores> obtenerHistorial() {
        return repository.findAll();
    }
	@Override
	public List<Map<String, Object>> obtenerResumenHoy() {
		return repository.obtenerResumenHoy();
	}
	@Override
	public List<Map<String, Object>> obtenerResumenUltimosDias(String periodo) {
		List<Map<String, Object>> resultado;
		if ("7".equals(periodo)) {
		    resultado= repository.obtenerResumen7Dias();
		} else if ("30".equals(periodo)) {
		    resultado= repository.obtenerResumen30Dias();
		} else {
		    throw new IllegalArgumentException("Periodo no válido. Solo se acepta 7 o 30");
		    
		}
		return resultado;
	}

}

