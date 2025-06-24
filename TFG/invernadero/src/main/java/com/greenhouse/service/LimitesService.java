package com.greenhouse.service;

import com.greenhouse.model.Limites;
import java.util.List;
import java.time.LocalTime;
public interface LimitesService {
    Limites findCurrentLimits();
    Limites findCurrentLimitsByUsuarioId(Integer usuarioId);
    Limites save(Limites limites, Integer usuarioId);
    Limites updateLimits(Limites nuevosLimites, Integer usuarioId);
    List<Limites> findAll();
    List<Limites> findByUsuarioId(Integer usuarioId);
	void notificarCambioAFIWARE(Limites limites, Integer usuarioId);
	void actualizarHorasRiego(List<LocalTime> horasRiego, Integer usuarioId);
	void validateLimits(Limites limites);
}
