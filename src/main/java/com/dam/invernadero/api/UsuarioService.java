package com.dam.invernadero.api;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

import com.dam.invernadero.model.Usuario;

import java.util.Map;

public interface UsuarioService {
    @GET("invernadero/usuarios/{id}")
    Call<Usuario> getUsuarioById(@Path("id") int id);
    @PATCH("invernadero/usuarios/{id}/contrasena")
    Call<Void> cambiarContrasena(@Path("id") int id, @Body Map<String, String> body);

}
