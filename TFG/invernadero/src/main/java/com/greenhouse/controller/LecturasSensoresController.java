package com.greenhouse.controller;

import com.greenhouse.model.LecturasSensores;
import com.greenhouse.service.LecturasSensoresService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Lecturas de sensores", description = "Operaciones relacionadas con lecturas de sensores desde FIWARE")
@RestController
@RequestMapping("/invernadero/sensores")
public class LecturasSensoresController {

    @Autowired
    private LecturasSensoresService service;

    @Operation(summary = "Recibir notificación desde FIWARE",
            description = "Recibe los datos de la entidad SensorData1 y guarda una nueva lectura en la base de datos")
    @ApiResponse(responseCode = "200", description = "Datos guardados correctamente")
    @PostMapping("/fiware-callback")
    public void recibirNotificacion(@RequestBody Map<String, Object> payload) {
		System.out.println("Payload recibido: " + payload);
        service.guardarDesdeNotificacion(payload);
    }

    @Operation(summary = "Obtener la última lectura registrada")
    @ApiResponse(responseCode = "200", description = "Última lectura recuperada")
    @GetMapping("/ultima")
    public LecturasSensores obtenerUltimaLectura() {
        return service.obtenerUltimaLectura();
    }

    @Operation(summary = "Obtener el historial completo de lecturas")
    @ApiResponse(responseCode = "200", description = "Historial de lecturas recuperado")
    @GetMapping("/historial")
    public List<LecturasSensores> obtenerHistorial() {
        return service.obtenerHistorial();
    }
	@Operation(summary = "Obtener 12 lecturas del dia actual",
           description = "Devuelve 12 lecturas 1 cada 2 horas para el dia en que se pide")
	@ApiResponse(responseCode = "200", description = "12 lecturas recuperadas")
	@GetMapping("/resumen-hoy")
	public List<Map<String, Object>> obtenerResumenHoy() {
		return service.obtenerResumenHoy();
	}
	@Operation(summary = "Obtener resumen de lecturas (periodo de días)",
           description = "Devuelve la media diaria por sensor para el número de días con datos que se pidan")
	@ApiResponse(responseCode = "200", description = "Resumen diario obtenido correctamente")
	@GetMapping("/resumen-ultimos-dias")
	public List<Map<String, Object>> obtenerResumen(@RequestParam("periodo") String periodo) {
		return service.obtenerResumenUltimosDias(periodo);
	}

}

