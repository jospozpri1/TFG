package com.dam.invernadero.model;

import com.google.gson.annotations.SerializedName;

public class Motor {
    private boolean estado;


    private String accion;
    private String motivo;
    private int duracionSegundos;
    private double consumoAgua;

    @SerializedName("fechaCreacion")
    private String fecha;

    public Motor() {
    }

    public Motor(String accion, String motivo, int duracionSegundos, double consumoAgua, String fecha) {
        this.accion = accion;
        this.motivo = motivo;
        this.duracionSegundos = duracionSegundos;
        this.consumoAgua = consumoAgua;
        this.fecha = fecha;
    }
    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public int getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(int duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public double getConsumoAgua() {
        return consumoAgua;
    }

    public void setConsumoAgua(double consumoAgua) {
        this.consumoAgua = consumoAgua;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}

