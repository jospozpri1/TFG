package com.dam.invernadero.model;

public class EstadoMotorRequest {
    private boolean accion;
    private String motivo;

    public EstadoMotorRequest(boolean accion, String motivo) {
        this.accion = accion;
        this.motivo = motivo;
    }

    public boolean getAccion() {
        return accion;
    }

    public void setAccion(boolean accion) {
        this.accion = accion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
