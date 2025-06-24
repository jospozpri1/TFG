package com.greenhouse.controller;

import com.greenhouse.model.EstadoMotor;
import com.greenhouse.service.EstadoMotorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
@RestController
@RequestMapping("/invernadero/motor/estado")
@Tag(name = "Estado del Motor", description = "API para registrar y consultar el estado del motor")
public class EstadoMotorController {

    @Autowired
    private EstadoMotorService service;

	@GetMapping("/")
	@Operation(summary = "Obtiene el último estado del motor", description = "Devuelve el último estado registrado del motor, incluyendo si está encendido o apagado.")
	public EstadoMotor obtenerUltimoEstado() {
		return service.obtenerUltimoEstado();
	}

    @PostMapping("/cambiar")
	@Operation(summary = "Cambia el estado del motor y registra la transición")
	public EstadoMotor cambiarEstado(@RequestBody Map<String, Object> payload,
		                             @RequestParam(defaultValue = "fiware") String motivo) {

		List<Map<String, Object>> data = (List<Map<String, Object>>) payload.get("data");

		if (data == null || data.isEmpty()) {
		    throw new IllegalArgumentException("No se recibió el atributo 'data' con información del motor.");
		}

		Map<String, Object> motorData = data.get(0);

		Map<String, Object> estadoMap = (Map<String, Object>) motorData.get("estado");
		if (estadoMap == null || !estadoMap.containsKey("value")) {
		    throw new IllegalArgumentException("No se encontró el campo 'estado.value'.");
		}

		boolean estado = Boolean.parseBoolean(estadoMap.get("value").toString());
		System.out.println("Motivo: " + motivo);
		return service.registrarCambioEstado(estado, motivo);
	}
	
}
