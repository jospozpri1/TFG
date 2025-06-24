package com.greenhouse.service.impl;

import com.greenhouse.model.EstadoMotor;
import com.greenhouse.repository.EstadoMotorRepository;
import com.greenhouse.service.EstadoMotorService;
import com.greenhouse.service.LimitesService;
import com.greenhouse.model.EventosMotor;
import com.greenhouse.model.Limites;
import com.greenhouse.repository.EventosMotorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.transaction.annotation.Transactional;
@Service
public class EstadoMotorServiceImpl implements EstadoMotorService {

    @Autowired
    private EstadoMotorRepository estadoMotorRepository;

    @Autowired
    private LimitesService limitesService;

    @Autowired
    private EventosMotorRepository eventosMotorRepository;

    private EstadoMotor ultimoEstado = null;
	@Override
	public EstadoMotor obtenerUltimoEstado() {
		EstadoMotor resultado = ultimoEstado;
		if (resultado == null) {
		    resultado = estadoMotorRepository.findTopByOrderByFechaCreacionDesc();
		}
		return resultado;
	}

	@Transactional
    @Override
    public EstadoMotor registrarCambioEstado(boolean nuevoEstado, String motivo) {
        LocalDateTime ahora = LocalDateTime.now();
        EstadoMotor nuevo = new EstadoMotor();
        nuevo.setEstado(nuevoEstado);
        nuevo.setMotivo(motivo);
        nuevo.setFechaCreacion(ahora);

        boolean notificarFiware = false;

        if (ultimoEstado != null && !ultimoEstado.getEstado().equals(nuevoEstado)) {
            Duration duracion = Duration.between(ultimoEstado.getFechaCreacion(), ahora);
            int duracionSegundos = (int) duracion.getSeconds();
            double consumo = 0.0;

            if (ultimoEstado.getEstado()) {
                consumo = duracionSegundos * 25.0;
            }

            // Guardamos el estado anterior
            ultimoEstado.setDuracionSegundos(duracionSegundos);
            ultimoEstado.setConsumoAgua(consumo);
            estadoMotorRepository.save(ultimoEstado);

            // Creamos el evento a guardar
            EventosMotor evento = new EventosMotor();
            evento.setAccion(nuevoEstado ? "ENCENDIDO" : "APAGADO");
            evento.setMotivo(motivo);
            evento.setDuracionSegundos(duracionSegundos);
            evento.setConsumoAgua(consumo);
            eventosMotorRepository.save(evento);
        }

        ultimoEstado = estadoMotorRepository.save(nuevo);

   
        if (!"fiware".equalsIgnoreCase(motivo)) {
            boolean notificado = notificarAFiware(nuevoEstado);
            if (!notificado) {
                throw new RuntimeException("Error notificando a FIWARE, se cancela la transacción");
            }
        }

        return ultimoEstado;
    }

    @Scheduled(fixedRate = 5000)
    public void comprobarConsumoAgua() {
        if (ultimoEstado != null && ultimoEstado.getEstado()) {
            LocalDateTime ahora = LocalDateTime.now();
            Duration duracion = Duration.between(ultimoEstado.getFechaCreacion(), ahora);
            int segundos = (int) duracion.getSeconds();
            double consumoActual = segundos * 25.0;

            double limite = 1000*limitesService.findCurrentLimits().getConsumoAguaMax();

            if (consumoActual >= limite) {
                System.out.println("Límite de consumo de agua superado (" + consumoActual + "L). Apagando motor...");
                registrarCambioEstado(false, "limite_superado");
            }
        }
    }

    public boolean notificarAFiware(boolean estado) {
		String url = "http://192.168.1.112:1026/v2/entities/MotorData1/attrs";
		boolean result;
		Function<Boolean, Map<String, Object>> createAttr = value -> Map.of(
		    "value", value,
		    "metadata", Map.of("origen", Map.of("value", "backend"))
		);

		Map<String, Object> data = new HashMap<>();
		data.put("estado", createAttr.apply(estado));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);

		try {
		    CloseableHttpClient httpClient = HttpClients.createDefault();
		    HttpComponentsClientHttpRequestFactory requestFactory =
		            new HttpComponentsClientHttpRequestFactory(httpClient);
		    RestTemplate restTemplate = new RestTemplate(requestFactory);

		    restTemplate.exchange(url, HttpMethod.PATCH, entity, String.class);
		    System.out.println("Estado del motor notificado a FIWARE: " + estado);
		    result= true;
		} catch (Exception e) {
		    System.err.println("Error notificando a FIWARE: " + e.getMessage());
		    result= false;
		}
		return estado;
	}

}

