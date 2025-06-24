package com.greenhouse.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "estado_motor")
public class EstadoMotor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean estado;

    @Column(name = "duracion_segundos")
    private Integer duracionSegundos;

    @Column(name = "consumo_agua")
    private Double consumoAgua;

    @Column(nullable = false)
    private String motivo;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Integer getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(Integer duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public Double getConsumoAgua() {
        return consumoAgua;
    }

    public void setConsumoAgua(Double consumoAgua) {
        this.consumoAgua = consumoAgua;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
