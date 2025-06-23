package com.dam.invernadero.api;

import com.dam.invernadero.model.Limites;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface LimitesService {
    @GET("invernadero/limites/usuario/{usuarioid}")
    Call<Limites> getLimites(@Path("usuarioid") int usuarioId);

    @PUT("invernadero/limites/usuario/{usuarioid}")
    Call<Void> actualizarLimites(@Path("usuarioid") int usuarioId, @Body Limites limites);
}
