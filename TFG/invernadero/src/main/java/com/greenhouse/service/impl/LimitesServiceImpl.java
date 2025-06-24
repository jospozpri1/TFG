package com.greenhouse.service.impl;

import com.greenhouse.model.Limites;
import com.greenhouse.model.Usuarios;
import com.greenhouse.repository.LimitesRepository;
import com.greenhouse.repository.UsuariosRepository;
import com.greenhouse.service.LimitesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.util.stream.Collectors;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LimitesServiceImpl implements LimitesService {

    @Autowired
    private LimitesRepository limitesRepository;
    
    @Autowired
    private UsuariosRepository usuariosRepository;

    @Override
    public Limites findCurrentLimits() {
        return limitesRepository.findTopByOrderByFechaActualizacionDesc()
               .orElseThrow(() -> new RuntimeException("No hay límites configurados"));
    }

    @Override
    public Limites findCurrentLimitsByUsuarioId(Integer usuarioId) {
        Usuarios usuario = usuariosRepository.findById(usuarioId)
               .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return limitesRepository.findTopByUsuarioOrderByFechaActualizacionDesc(usuario)
               .orElseThrow(() -> new RuntimeException("No hay límites configurados para este usuario"));
    }

    @Override
    public Limites save(Limites limites, Integer usuarioId) {
        validateLimits(limites); // Validamos antes de guardar
        
        Usuarios usuario = usuariosRepository.findById(usuarioId)
               .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        limites.setUsuario(usuario);
        limites.setUsuarioActualizacion(usuario.getNombreUsuario());
        limites.setFechaActualizacion(LocalDateTime.now());
        notificarCambioAFIWARE(limites, usuarioId);
        return limitesRepository.save(limites);
    }

	@Transactional
    @Override
    public Limites updateLimits(Limites nuevosLimites, Integer usuarioId) {
        // Obtenemos los límites actuales del usuario
        Limites limitesActuales = findCurrentLimitsByUsuarioId(usuarioId);
        
        // Actualizamos solo los campos permitidos
        limitesActuales.setTempMin(nuevosLimites.getTempMin());
        limitesActuales.setTempMax(nuevosLimites.getTempMax());
        limitesActuales.setHumedadAmbMin(nuevosLimites.getHumedadAmbMin());
        limitesActuales.setHumedadAmbMax(nuevosLimites.getHumedadAmbMax());
        limitesActuales.setHumedadSueloMin(nuevosLimites.getHumedadSueloMin());
        limitesActuales.setHumedadSueloMax(nuevosLimites.getHumedadSueloMax());
        limitesActuales.setLuzMin(nuevosLimites.getLuzMin());
        limitesActuales.setLuzMax(nuevosLimites.getLuzMax());
        limitesActuales.setConsumoAguaMax(nuevosLimites.getConsumoAguaMax());
        limitesActuales.setVolumenAguaMin(nuevosLimites.getVolumenAguaMin());
        //limitesActuales.setHorasRiego(nuevosLimites.getHorasRiego());
        limitesActuales.setFechaActualizacion(LocalDateTime.now());
        
        validateLimits(limitesActuales); // Validamos antes de actualizar
		notificarCambioAFIWARE(limitesActuales, usuarioId);
        
        return limitesRepository.save(limitesActuales);
    }

    @Override
    public List<Limites> findAll() {
        return limitesRepository.findAll();
    }

    @Override
    public List<Limites> findByUsuarioId(Integer usuarioId) {
        Usuarios usuario = usuariosRepository.findById(usuarioId)
               .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return limitesRepository.findByUsuarioOrderByFechaActualizacionDesc(usuario);
    }

	@Transactional
	@Override
	public void actualizarHorasRiego(List<LocalTime> horasRiego, Integer usuarioId) {
		// Buscamos los límites actuales del usuario de forma segura
		Limites limites = limitesRepository.findByUsuarioId(usuarioId);

		// Actualizamos horas de riego
		limites.setHorasRiego(horasRiego);
		limites.setFechaActualizacion(LocalDateTime.now());
		
		notificarCambioAFIWARE(limites, usuarioId);
		limitesRepository.save(limites);
	}

    public void validateLimits(Limites limites) {
		if (limites.getTempMin() >= limites.getTempMax()) {
		    throw new IllegalArgumentException("La temperatura mínima debe ser menor que la máxima");
		}

		if (limites.getHumedadAmbMin() >= limites.getHumedadAmbMax()) {
		    throw new IllegalArgumentException("La humedad ambiental mínima debe ser menor que la máxima");
		}
		if (limites.getHumedadAmbMin() < 0 || limites.getHumedadAmbMax() > 100) {
		    throw new IllegalArgumentException("La humedad ambiental debe estar entre 0 y 100%");
		}

		if (limites.getHumedadSueloMin() >= limites.getHumedadSueloMax()) {
		    throw new IllegalArgumentException("La humedad del suelo mínima debe ser menor que la máxima");
		}
		if (limites.getHumedadSueloMin() < 0 || limites.getHumedadSueloMax() > 100) {
		    throw new IllegalArgumentException("La humedad del suelo debe estar entre 0 y 100%");
		}

		if (limites.getLuzMin() >= limites.getLuzMax()) {
		    throw new IllegalArgumentException("El nivel de luz mínimo debe ser menor que el máximo");
		}
		if (limites.getLuzMin() < 0 || limites.getLuzMax() > 100) {
		    throw new IllegalArgumentException("El nivel de luz debe estar entre 0 y 100%");
		}

		if (limites.getConsumoAguaMax() <= 0) {
		    throw new IllegalArgumentException("El consumo máximo de agua debe ser mayor que cero");
		}

		if (limites.getHorasRiego() == null || limites.getHorasRiego().isEmpty()) {
		    throw new IllegalArgumentException("Debe especificar al menos una hora de riego permitida");
		}

		if (limites.getVolumenAguaMin() == null || limites.getVolumenAguaMin() < 0 || limites.getVolumenAguaMin() > 100) {
		    throw new IllegalArgumentException("El volumen mínimo de agua debe estar entre 0 y 100%");
		}
	}

	
	public void notificarCambioAFIWARE(Limites limites, Integer usuarioId) {
		String fiwareUrl = "http://192.168.1.112:1026/v2/entities/Limites:" + usuarioId + "/attrs";

		CloseableHttpClient httpClient = HttpClients.createDefault();
		RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Function<Object, Map<String, Object>> createAttr = value -> Map.of(
		    "value", value,
		    "metadata", Map.of("origen", Map.of("value", "backend"))
		);
		Map<String, Object> attrs = new HashMap<>();
		attrs.put("temp_min", createAttr.apply(limites.getTempMin()));
		attrs.put("temp_max", createAttr.apply(limites.getTempMax()));
		attrs.put("humedad_amb_min", createAttr.apply(limites.getHumedadAmbMin()));
		attrs.put("humedad_amb_max", createAttr.apply(limites.getHumedadAmbMax()));
		attrs.put("humedad_suelo_min", createAttr.apply(limites.getHumedadSueloMin()));
		attrs.put("humedad_suelo_max", createAttr.apply(limites.getHumedadSueloMax()));
		attrs.put("luz_min", createAttr.apply(limites.getLuzMin()));
		attrs.put("luz_max", createAttr.apply(limites.getLuzMax()));
		attrs.put("consumo_agua_max", createAttr.apply(limites.getConsumoAguaMax()));
		attrs.put("volumen_agua_min", createAttr.apply(limites.getVolumenAguaMin()));
		attrs.put("horas_riego", createAttr.apply(limites.getHorasRiego()));

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(attrs, headers);

		try {
		    restTemplate.exchange(fiwareUrl, HttpMethod.PATCH, entity, String.class);
		} catch (Exception ex) {
		    throw new RuntimeException("Error al notificar a FIWARE", ex);
		}
	}


}
