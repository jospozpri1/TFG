package com.greenhouse.service;

import com.greenhouse.model.Usuarios;
import java.util.List;
import java.util.Optional;

public interface UsuariosService {
    Usuarios registrarUsuario(Usuarios usuario);
    Optional<Usuarios> login(String nombreUsuario, String contraseña);
    List<Usuarios> findAll();
    Optional<Usuarios> findById(Integer id);
    Usuarios save(Usuarios usuario);
    void delete(Integer id);
    boolean existeNombreUsuario(String nombreUsuario);
    boolean existeEmail(String email);
	boolean actualizarContrasena(int id, String nuevaContrasena);
}
