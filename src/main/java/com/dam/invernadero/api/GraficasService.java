package com.dam.invernadero.api;

import com.dam.invernadero.model.LecturaSensor;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GraficasService {
    @GET("invernadero/sensores/resumen-hoy")
    Call<List<Map<String, Object>>> getResumenHoy();

    @GET("invernadero/sensores/resumen-ultimos-dias")
    Call<List<Map<String, Object>>> getResumenUltimosDias(@Query("periodo") String periodo);

}