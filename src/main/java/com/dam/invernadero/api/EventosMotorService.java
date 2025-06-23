package com.dam.invernadero.api;

import com.dam.invernadero.model.Motor;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EventosMotorService {
    @GET("/invernadero/motor/eventos")
    Call<List<Motor>> getEventosPaginados(@Query("page") int page, @Query("size") int size);
}
