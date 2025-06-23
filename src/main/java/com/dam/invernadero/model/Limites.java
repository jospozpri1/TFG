package com.dam.invernadero.model;

import java.util.List;

public class Limites {
    private Double tempMin;
    private Double tempMax;
    private Double humedadAmbMin;
    private Double humedadAmbMax;
    private Integer humedadSueloMin;
    private Integer humedadSueloMax;
    private Integer luzMin;
    private Integer luzMax;
    private Double consumoAguaMax;
    private Double volumenAguaMin;
    private List<String> horasRiego;
    public Limites() {}

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

    public List<String> getHorasRiego() {
        return horasRiego;
    }

    public void setHorasRiego(List<String> horasRiego) {
        this.horasRiego = horasRiego;
    }
}
