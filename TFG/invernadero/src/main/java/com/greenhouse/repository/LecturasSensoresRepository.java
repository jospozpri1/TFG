package com.greenhouse.repository;

import com.greenhouse.model.LecturasSensores;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import java.util.Map;
public interface LecturasSensoresRepository extends JpaRepository<LecturasSensores, Integer> {
    Optional<LecturasSensores> findTopByOrderByFechaCreacionDesc();
    List<LecturasSensores> findByFechaCreacionBetween(Date inicio, Date fin);
    @Query(value = "SELECT * FROM lecturas_sensores ORDER BY fecha_creacion DESC LIMIT 1", nativeQuery = true)
    LecturasSensores findUltimaLectura();
	@Query(value = "SELECT DATE_TRUNC('hour', fecha_creacion) AS fecha,AVG(temperatura_c) AS temperaturaC,AVG(temperatura_f) AS temperaturaF, AVG(humedad_ambiental) AS humedadAmbiental, AVG(humedad_suelo) AS humedadSuelo, AVG(luz_ambiente) AS luzAmbiente, AVG(nivel_agua) AS nivelAgua     FROM lecturas_sensores    WHERE fecha_creacion >= CURRENT_DATE    GROUP BY DATE_TRUNC('hour', fecha_creacion)    ORDER BY fecha DESC    LIMIT 12", nativeQuery = true)
	List<Map<String, Object>> obtenerResumenHoy();
	@Query(value = "SELECT  DATE(fecha_creacion) AS fecha,  AVG(temperatura_c) AS temperaturaC,   AVG(temperatura_f) AS temperaturaF,   AVG(humedad_ambiental) AS humedadAmbiental,   AVG(humedad_suelo) AS humedadSuelo,    AVG(luz_ambiente) AS luzAmbiente,   AVG(nivel_agua) AS nivelAgua 		FROM lecturas_sensores		WHERE fecha_creacion >= CURRENT_DATE - INTERVAL '30 days'		GROUP BY DATE(fecha_creacion) 		ORDER BY fecha DESC		LIMIT 7	", nativeQuery = true)
	List<Map<String, Object>> obtenerResumen7Dias();
	@Query(value = "SELECT  DATE(fecha_creacion) AS fecha,  AVG(temperatura_c) AS temperaturaC,   AVG(temperatura_f) AS temperaturaF,   AVG(humedad_ambiental) AS humedadAmbiental,   AVG(humedad_suelo) AS humedadSuelo,    AVG(luz_ambiente) AS luzAmbiente,   AVG(nivel_agua) AS nivelAgua 		FROM lecturas_sensores		WHERE fecha_creacion >= CURRENT_DATE - INTERVAL '30 days'		GROUP BY DATE(fecha_creacion) 		ORDER BY fecha DESC		LIMIT 30	", nativeQuery = true)
	List<Map<String, Object>> obtenerResumen30Dias();
}
