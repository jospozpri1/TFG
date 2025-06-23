package com.dam.invernadero;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class RegistroActivity extends AppCompatActivity {
    private EditText txtUsuario;
    private EditText txtContrasena;
    private EditText txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Inicializamos vistas
        txtUsuario = findViewById(R.id.txtUsuario);
        txtContrasena = findViewById(R.id.txtContrasena);
        txtEmail = findViewById(R.id.txtEmail);
    }

    public void onConfirmRegisterClick(View view) {
        String usuario = txtUsuario.getText().toString().trim();
        String contrasena = txtContrasena.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();

        if (usuario.isEmpty() || contrasena.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        hacerRegistro(usuario, contrasena, email);
    }

    public void onCancelRegisterClick(View view) {
        finish();
    }

    private void hacerRegistro(String usuario, String contrasena, String email) {
        String url = getString(R.string.IP) + "invernadero/usuarios/registro";

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registrando usuario...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("nombreUsuario", usuario);
            jsonBody.put("contraseña", contrasena);
            jsonBody.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    progressDialog.dismiss();
                    manejarRespuestaRegistro(response);
                },
                error -> {
                    progressDialog.dismiss();
                    manejarErrorRegistro(error);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        ServiciosWeb.getInstance(this).addToRequestQueue(request);
    }

    private void manejarRespuestaRegistro(JSONObject response) {
        try {
            Log.d("LOG", response.getString("mensaje"));
            if (response.has("mensaje")) {
                String mensaje = response.getString("mensaje");
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();

                if (mensaje.equals("Registro exitoso")) {
                    // Cerramos este activity y volver al login
                    finish();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
        }
    }

    private void manejarErrorRegistro(VolleyError error) {
        String mensajeError = "Error en el registro";

        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                String responseBody = new String(error.networkResponse.data);
                JSONObject jsonResponse = new JSONObject(responseBody);
                if (jsonResponse.has("error")) {
                    mensajeError = jsonResponse.getString("error");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show();
    }
}