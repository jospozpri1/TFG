package com.dam.invernadero.api;

import com.dam.invernadero.model.EstadoMotorRequest;
import com.dam.invernadero.model.Motor;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ActuadoresService {

    // Ruta para obtener el estado actual del motor
    @GET("invernadero/motor/estado/")
    Call<Motor> getEstadoMotor();

    // Ruta para cambiar el estado del motor
    @POST("invernadero/motor/estado/cambiar")
    Call<Motor> cambiarEstadoMotor(@Body Map<String, Object> payload, @Query("motivo") String motivo);
}
