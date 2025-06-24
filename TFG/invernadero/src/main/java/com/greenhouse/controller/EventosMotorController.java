package com.greenhouse.controller;

import com.greenhouse.model.EventosMotor;
import com.greenhouse.service.EventosMotorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invernadero/motor/eventos")
@Tag(name = "Eventos del Motor", description = "API para consultar eventos del motor")
public class EventosMotorController {

    @Autowired
    private EventosMotorService service;

    @GetMapping
    @Operation(summary = "Obtiene todos los eventos del motor")
    public List<EventosMotor> obtenerEventos() {
        return service.obtenerEventos();
    }
}
