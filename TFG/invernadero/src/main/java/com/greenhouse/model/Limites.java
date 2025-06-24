package com.greenhouse.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "limites")
public class Limites {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
    
    @Column(name = "temp_min", nullable = false)
    private Double tempMin;
    
    @Column(name = "temp_max", nullable = false)
    private Double tempMax;
    
    @Column(name = "humedad_amb_min", nullable = false)
    private Double humedadAmbMin;
    
    @Column(name = "humedad_amb_max", nullable = false)
    private Double humedadAmbMax;
    
    @Column(name = "humedad_suelo_min", nullable = false)
    private Integer humedadSueloMin;
    
    @Column(name = "humedad_suelo_max", nullable = false)
    private Integer humedadSueloMax;
    
    @Column(name = "luz_min", nullable = false)
    private Integer luzMin;
    
    @Column(name = "luz_max", nullable = false)
    private Integer luzMax;
    
    @Column(name = "consumo_agua_max", nullable = false)
    private Double consumoAguaMax = 10.0;
    
    @Column(name = "volumen_agua_min", nullable = false)
    private Double volumenAguaMin = 20.0;
    
    @ElementCollection
    @CollectionTable(name = "limites_horas_riego", joinColumns = @JoinColumn(name = "limite_id"))
    @Column(name = "hora_riego")
    private List<LocalTime> horasRiego;
    
    @Column(name = "usuario_actualizacion", nullable = false)
    private String usuarioActualizacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuarios usuario;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Double getTempMin() {
        return tempMin;
    }

    public void setTempMin(Double tempMin) {
        this.tempMin = tempMin;
    }

    public Double getTempMax() {
        return tempMax;
    }

    public void setTempMax(Double tempMax) {
        this.tempMax = tempMax;
    }

    public Double getHumedadAmbMin() {
        return humedadAmbMin;
    }

    public void setHumedadAmbMin(Double humedadAmbMin) {
        this.humedadAmbMin = humedadAmbMin;
    }

    public Double getHumedadAmbMax() {
        return humedadAmbMax;
    }

    public void setHumedadAmbMax(Double humedadAmbMax) {
        this.humedadAmbMax = humedadAmbMax;
    }

    public Integer getHumedadSueloMin() {
        return humedadSueloMin;
    }

    public void setHumedadSueloMin(Integer humedadSueloMin) {
        this.humedadSueloMin = humedadSueloMin;
    }

    public Integer getHumedadSueloMax() {
        return humedadSueloMax;
    }

    public void setHumedadSueloMax(Integer humedadSueloMax) {
        this.humedadSueloMax = humedadSueloMax;
    }

    public Integer getLuzMin() {
        return luzMin;
    }

    public void setLuzMin(Integer luzMin) {
        this.luzMin = luzMin;
    }

    public Integer getLuzMax() {
        return luzMax;
    }

    public void setLuzMax(Integer luzMax) {
        this.luzMax = luzMax;
    }

    public Double getConsumoAguaMax() {
        return consumoAguaMax;
    }

    public void setConsumoAguaMax(Double consumoAguaMax) {
        this.consumoAguaMax = consumoAguaMax;
    }

    public Double getVolumenAguaMin() {
        return volumenAguaMin;
    }

    public void setVolumenAguaMin(Double volumenAguaMin) {
        this.volumenAguaMin = volumenAguaMin;
    }

    public List<LocalTime> getHorasRiego() {
        return horasRiego;
    }

    public void setHorasRiego(List<LocalTime> horasRiego) {
        this.horasRiego = horasRiego;
    }

    public String getUsuarioActualizacion() {
        return usuarioActualizacion;
    }

    public void setUsuarioActualizacion(String usuarioActualizacion) {
        this.usuarioActualizacion = usuarioActualizacion;
    }

    public Usuarios getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }
}
