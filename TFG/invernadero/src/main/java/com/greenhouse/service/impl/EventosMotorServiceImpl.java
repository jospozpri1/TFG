package com.greenhouse.service.impl;

import com.greenhouse.model.EventosMotor;
import com.greenhouse.repository.EventosMotorRepository;
import com.greenhouse.service.EventosMotorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventosMotorServiceImpl implements EventosMotorService {

    @Autowired
    private EventosMotorRepository repository;

    @Override
    public List<EventosMotor> obtenerEventos() {
        return repository.findAll();
    }
}
