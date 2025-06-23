package com.dam.invernadero.api;

import com.dam.invernadero.model.Limites;
import com.dam.invernadero.model.ReeResponse;

import java.time.LocalTime;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReeApiService {
    @GET("es/datos/mercados/precios-mercados-tiempo-real")
    Call<ReeResponse> getPrecios(
            @Query("start_date") String startDate,
            @Query("end_date") String endDate,
            @Query("time_trunc") String timeTrunc,
            @Query("geo_trunc") String geoTrunc,
            @Query("geo_limit") String geoLimit,
            @Query("geo_ids") String geoIds
    );
    @POST("invernadero/limites/actualizar-horas-riego/{id}")
    Call<Void> actualizarHorasRiego(
            @Path("id") int userId,
            @Body List<String> horasPermitidas
    );

}
