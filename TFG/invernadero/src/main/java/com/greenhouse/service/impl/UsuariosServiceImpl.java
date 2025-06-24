package com.greenhouse.service.impl;

import com.greenhouse.model.Usuarios;
import com.greenhouse.repository.UsuariosRepository;
import com.greenhouse.service.UsuariosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuariosServiceImpl implements UsuariosService {

    @Autowired
    private UsuariosRepository repository;

    @Override
    public Usuarios registrarUsuario(Usuarios usuario) {
        usuario.setContraseña(usuario.getContraseña());
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setActivo(true);
        usuario.setNivelAcceso(1);
        return repository.save(usuario);
    }

    @Override
    public Optional<Usuarios> login(String nombreUsuario, String contraseña) {
        Optional<Usuarios> usuarioOpt = repository.findByNombreUsuario(nombreUsuario);
    	Optional<Usuarios> resultado = Optional.empty();

        if (usuarioOpt.isPresent()) {
            Usuarios usuario = usuarioOpt.get();
            if (contraseña.equals(usuario.getContraseña()) && usuario.getActivo()) {
                usuario.setUltimoAcceso(LocalDateTime.now());
                repository.save(usuario);
                resultado = Optional.of(usuario);
            }
        }
        return resultado;
    }

    @Override
    public List<Usuarios> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Usuarios> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Usuarios save(Usuarios usuario) {
        return repository.save(usuario);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existeNombreUsuario(String nombreUsuario) {
        return repository.existsByNombreUsuario(nombreUsuario);
    }

    @Override
    public boolean existeEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean actualizarContrasena(int id, String nuevaContrasena) {
		boolean resultado=false;
        Optional<Usuarios> usuarioOpt = repository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuarios usuario = usuarioOpt.get();
            usuario.setContraseña(nuevaContrasena);
            repository.save(usuario);
            resultado= true;
        }
        return resultado;
    }
}

