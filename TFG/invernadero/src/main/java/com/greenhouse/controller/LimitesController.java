package com.greenhouse.controller;

import com.greenhouse.model.Limites;
import com.greenhouse.service.LimitesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.greenhouse.repository.LimitesRepository;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;





@RestController
@RequestMapping("/invernadero/limites")
@Tag(name = "Límites", description = "Operaciones relacionadas con los límites de los parámetros configurados por el usuario")
public class LimitesController {

    @Autowired
    private LimitesService service;
	
	@Autowired
	private LimitesRepository limitesRepository;

    @GetMapping
    @Operation(summary = "Obtener límites actuales", description = "Obtiene los límites configurados actualmente")
    @ApiResponse(responseCode = "200", description = "Límites obtenidos exitosamente")
    public ResponseEntity<Limites> getLimitesActuales() {
        return ResponseEntity.ok(service.findCurrentLimits());
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener límites del usuario", description = "Obtiene los límites configurados para un usuario específico")
    @ApiResponse(responseCode = "200", description = "Límites del usuario obtenidos exitosamente")
    public ResponseEntity<Limites> getLimitesUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Integer usuarioId) {
        return ResponseEntity.ok(service.findCurrentLimitsByUsuarioId(usuarioId));
    }

    @PostMapping("/usuario/{usuarioId}")
    @Operation(summary = "Crear límites", description = "Crea una nueva configuración de límites para un usuario")
    @ApiResponse(responseCode = "200", description = "Límites creados exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de límites inválidos")
    public ResponseEntity<Limites> crearLimites(
            @Parameter(description = "Nuevos valores de límites") @RequestBody Limites nuevosLimites,
            @Parameter(description = "ID del usuario") @PathVariable Integer usuarioId) {
        return ResponseEntity.ok(service.save(nuevosLimites, usuarioId));
    }

    @PutMapping("/usuario/{usuarioId}")
    @Operation(summary = "Actualizar límites", description = "Actualiza los límites para un usuario específico")
    @ApiResponse(responseCode = "200", description = "Límites actualizados exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos de límites inválidos")
    public ResponseEntity<Limites> actualizarLimites(
            @Parameter(description = "Nuevos valores de límites") @RequestBody Limites nuevosLimites,
            @Parameter(description = "ID del usuario") @PathVariable Integer usuarioId) {
        return ResponseEntity.ok(service.updateLimits(nuevosLimites, usuarioId));
    }

	@PostMapping("/fiware-callback")
	@Operation(hidden = true)
	public ResponseEntity<Void> recibirNotificacionFiware(@RequestBody Map<String, Object> payload) {
		ResponseEntity<Void> respuesta;
		try {
			List<Map<String, Object>> data = (List<Map<String, Object>>) payload.get("data");
			Map<String, Object> entity = data.get(0);

			Double consumoAguaMax = getValueFromAttribute(entity, "consumo_agua_max");
			Double humedadAmbMax = getValueFromAttribute(entity, "humedad_amb_max");
			Double humedadAmbMin = getValueFromAttribute(entity, "humedad_amb_min");
			Double humedadSueloMax = getValueFromAttribute(entity, "humedad_suelo_max");
			Double humedadSueloMin = getValueFromAttribute(entity, "humedad_suelo_min");
			Double luzMax = getValueFromAttribute(entity, "luz_max");
			Double luzMin = getValueFromAttribute(entity, "luz_min");
			Double tempMax = getValueFromAttribute(entity, "temp_max");
			Double tempMin = getValueFromAttribute(entity, "temp_min");

			List<LocalTime> horasRiego = new ArrayList<>();
			Object horasRiegoObj = entity.get("horas_riego");
			if (horasRiegoObj instanceof Map) {
				Map<String, Object> horasRiegoMap = (Map<String, Object>) horasRiegoObj;
				Object valueObj = horasRiegoMap.get("value");
				if (valueObj instanceof List) {
					List<List<Integer>> horasRiegoRaw = (List<List<Integer>>) valueObj;
					horasRiego = horasRiegoRaw.stream()
						.map(par -> LocalTime.of(par.get(0), par.get(1)))
						.collect(Collectors.toList());
				}
			}

			Optional<Limites> optionalLimites = limitesRepository.findById(1);
			if (optionalLimites.isPresent()) {
				Limites limites = optionalLimites.get();
				limites.setConsumoAguaMax(consumoAguaMax);
				limites.setHumedadAmbMax(humedadAmbMax);
				limites.setHumedadAmbMin(humedadAmbMin);
				limites.setHumedadSueloMax(humedadSueloMax != null ? humedadSueloMax.intValue() : null);
				limites.setHumedadSueloMin(humedadSueloMin != null ? humedadSueloMin.intValue() : null);
				limites.setLuzMax(luzMax != null ? luzMax.intValue() : null);
				limites.setLuzMin(luzMin != null ? luzMin.intValue() : null);

				limites.setTempMax(tempMax);
				limites.setTempMin(tempMin);
				limites.setHorasRiego(horasRiego);
				limites.setFechaActualizacion(LocalDateTime.now());
				service.validateLimits(limites);
				limitesRepository.save(limites);
				System.out.println("Limites actualizados correctamente para ID 1");
			} else {
				System.out.println("No se encontró el registro con ID 1. No se pudo actualizar.");
			}
			respuesta= ResponseEntity.ok().build();
		} catch (Exception e) {
			e.printStackTrace();
			respuesta= ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		return respuesta;
	}



	private Double getValueFromAttribute(Map<String, Object> payload, String attributeName) {
		Object attrObj = payload.get(attributeName);
 		System.out.println("Obj: " + attrObj);
 		Double result = null;

		if (attrObj instanceof Map) {
    		Map<?, ?> attrMap = (Map<?, ?>) attrObj;
		    Object value = attrMap.get("value");
		    if (value instanceof Number) {
		        result= ((Number) value).doubleValue();
		    } else if (value instanceof String) {
		        try {
		            result= Double.parseDouble((String) value);
		        } catch (NumberFormatException e) {
		            e.printStackTrace();
		        }
		    }
		}
		return result;
	}


	@PostMapping("/actualizar-horas-riego/{usuarioId}")
    @Operation(
        summary = "Actualizar las horas de riego",
        description = "Este endpoint permite actualizar las horas en las que se permite el riego para un usuario específico",
        parameters = {
            @Parameter(name = "usuarioId", description = "ID del usuario para actualizar las horas de riego", required = true)
        }
    )
   
   @ApiResponse(responseCode = "200", description = "Horas de riego actualizadas correctamente")
   @ApiResponse(responseCode = "400", description = "Error al actualizar las horas de riego")

	public ResponseEntity<String> actualizarHorasRiego(
		    @RequestBody List<LocalTime> horasRiego,
		    @PathVariable  Integer usuarioId) {
		ResponseEntity<String> respuesta;
		try {
		    service.actualizarHorasRiego(horasRiego, usuarioId);
		    respuesta= ResponseEntity.ok("Horas de riego actualizadas correctamente.");
		} catch (Exception e) {
		    e.printStackTrace();
			System.out.println("ERROR al actualizar horas: " + e.getMessage());
		    respuesta= ResponseEntity.badRequest().body("Error al actualizar las horas de riego.");
		}
		return respuesta;
	}




}
