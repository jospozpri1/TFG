package com.greenhouse.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "lecturas_sensores")
public class LecturasSensores {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "fecha_creacion", updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;
    
    @Column(name = "nivel_agua", nullable = false)
    private Integer nivelAgua;
    
    @Column(name = "luz_ambiente", nullable = false)
    private Integer luzAmbiente;
    
    @Column(name = "humedad_suelo", nullable = false)
    private Integer humedadSuelo;
    
    @Column(name = "humedad_ambiental", nullable = false)
    private Double humedadAmbiental;
    
    @Column(name = "temperatura_c", nullable = false)
    private Double temperaturaC;
    
    @Column(name = "temperatura_f", nullable = false)
    private Double temperaturaF;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getNivelAgua() {
        return nivelAgua;
    }

    public void setNivelAgua(Integer nivelAgua) {
        this.nivelAgua = nivelAgua;
    }

    public Integer getLuzAmbiente() {
        return luzAmbiente;
    }

    public void setLuzAmbiente(Integer luzAmbiente) {
        this.luzAmbiente = luzAmbiente;
    }

    public Integer getHumedadSuelo() {
        return humedadSuelo;
    }

    public void setHumedadSuelo(Integer humedadSuelo) {
        this.humedadSuelo = humedadSuelo;
    }

    public Double getHumedadAmbiental() {
        return humedadAmbiental;
    }

    public void setHumedadAmbiental(Double humedadAmbiental) {
        this.humedadAmbiental = humedadAmbiental;
    }

    public Double getTemperaturaC() {
        return temperaturaC;
    }

    public void setTemperaturaC(Double temperaturaC) {
        this.temperaturaC = temperaturaC;
    }

    public Double getTemperaturaF() {
        return temperaturaF;
    }

    public void setTemperaturaF(Double temperaturaF) {
        this.temperaturaF = temperaturaF;
    }
}
