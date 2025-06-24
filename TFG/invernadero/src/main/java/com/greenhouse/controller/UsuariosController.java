package com.greenhouse.controller;

import com.greenhouse.model.Usuarios;
import com.greenhouse.service.UsuariosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import org.json.JSONObject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/invernadero/usuarios")
@Tag(name = "Usuarios", description = "Operaciones para la gestión de usuarios")
public class UsuariosController {

    @Autowired
    private UsuariosService usuariosService;

    @Autowired
    private UsuariosService service;
	
    
    @GetMapping
    @Operation(summary = "Obtener todos los usuarios", description = "Obtiene una lista de todos los usuarios registrados")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida")
    public ResponseEntity<List<Usuarios>> getAllUsuarios() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene los detalles de un usuario específico")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<?> getUsuarioById(
            @Parameter(description = "ID del usuario a buscar") @PathVariable Integer id) {
        Optional<Usuarios> usuario = usuariosService.findById(id);
		ResponseEntity<?> respuesta;
        if (usuario.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            Usuarios u = usuario.get();
            
            response.put("nombreUsuario", u.getNombreUsuario());
            response.put("email", u.getEmail());
            response.put("activo", u.getActivo());
            response.put("nivelAcceso", u.getNivelAcceso());
            response.put("ultimoAcceso", u.getUltimoAcceso());
            response.put("fechaCreacion", u.getFechaCreacion());
            
            respuesta= ResponseEntity.ok(response);
        } else {
            respuesta= ResponseEntity.notFound().build();
        }
		return respuesta;
    }
    
    @GetMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con nombre de usuario y contraseña")
    @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    public ResponseEntity<?> login(
            @Parameter(description = "Nombre de usuario") @RequestParam String nombreUsuario,
            @Parameter(description = "Contraseña") @RequestParam String contraseña) {
       
        Optional<Usuarios> usuarioOpt = usuariosService.login(nombreUsuario, contraseña);
		ResponseEntity<?> respuesta;
        if (usuarioOpt.isPresent()) {
            Usuarios usuario = usuarioOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("usuario", usuario.getNombreUsuario());
            response.put("email", usuario.getEmail());
            response.put("nivelAcceso", usuario.getNivelAcceso());
            respuesta= ResponseEntity.ok(response);
        } else {
            respuesta= ResponseEntity.status(401).body("Credenciales inválidas");
        }
		return respuesta;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario")
    @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Nombre de usuario o email ya existen")
    public ResponseEntity<?> registrarUsuario(
            @Parameter(description = "Datos del nuevo usuario") @RequestBody Usuarios usuario) {
		ResponseEntity<?> respuesta;
        if (usuariosService.existeNombreUsuario(usuario.getNombreUsuario())) {
            respuesta= ResponseEntity.badRequest().body("El nombre de usuario ya existe");
        }else{
		    if (usuariosService.existeEmail(usuario.getEmail())) {
		        respuesta= ResponseEntity.badRequest().body("El email ya está registrado");
		    }else{
		    
				Usuarios nuevoUsuario = usuariosService.registrarUsuario(usuario);
				respuesta= ResponseEntity.ok(new JSONObject().put("mensaje", "Registro exitoso").toString());
			}		
		}
		return respuesta;
    }

    @GetMapping("/{nombreUsuario}/existe")
    @Operation(summary = "Verificar nombre de usuario", description = "Comprueba si un nombre de usuario ya está en uso")
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    public ResponseEntity<?> verificarNombreUsuario(
            @Parameter(description = "Nombre de usuario a verificar") @PathVariable String nombreUsuario) {
        boolean existe = usuariosService.existeNombreUsuario(nombreUsuario);
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/email/{email}/existe")
    @Operation(summary = "Verificar email", description = "Comprueba si un email ya está registrado")
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    public ResponseEntity<?> verificarEmail(
            @Parameter(description = "Email a verificar") @PathVariable String email) {
        boolean existe = usuariosService.existeEmail(email);
        return ResponseEntity.ok(existe);
    }

    @PatchMapping("/{id}/contrasena")
    @Operation(summary = "Cambiar contraseña", description = "Actualiza la contraseña de un usuario")
    @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente")
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    public ResponseEntity<?> cambiarContrasena(
            @Parameter(description = "ID del usuario") @PathVariable int id,
            @Parameter(description = "Nueva contraseña") @RequestBody Map<String, String> body) {
		ResponseEntity<?> respuesta;
        String nueva = body.get("nuevaContrasena");
        if (usuariosService.actualizarContrasena(id, nueva)) {
            respuesta= ResponseEntity.ok().build();
        } else {
            respuesta= ResponseEntity.notFound().build();
        }
		return respuesta;
    }
}
