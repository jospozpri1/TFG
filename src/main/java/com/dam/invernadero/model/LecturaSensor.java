package com.dam.invernadero.model;

public class LecturaSensor {
    private String fecha;
    private Double temperaturaC, temperaturaF, humedadAmbiental, humedadSuelo, luzAmbiente, nivelAgua;

    public String getFecha() { return fecha; }

    public float getValorPorSensor(String sensor) {
        switch (sensor) {
            case "temperaturaC": return temperaturaC != null ? temperaturaC.floatValue() : 0;
            case "temperaturaF": return temperaturaF != null ? temperaturaF.floatValue() : 0;
            case "humedadAmbiental": return humedadAmbiental != null ? humedadAmbiental.floatValue() : 0;
            case "humedadSuelo": return humedadSuelo != null ? humedadSuelo.floatValue() : 0;
            case "luzAmbiente": return luzAmbiente != null ? luzAmbiente.floatValue() : 0;
            case "nivelAgua": return nivelAgua != null ? nivelAgua.floatValue() : 0;
            default: return 0;
        }
    }
}
