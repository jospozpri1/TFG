package com.dam.invernadero;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText txtUsuario;
    private EditText txtContrasena;
    private EditText txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtUsuario = findViewById(R.id.txtUsuario);
        txtContrasena = findViewById(R.id.txtContrasena);

    }

    public void Hacer_peticion_acceso(View view) {
        String usuario = txtUsuario.getText().toString().trim();
        String contrasena = txtContrasena.getText().toString().trim();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Usuario y contraseña son requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        hacerPeticionAcceso(usuario, contrasena);
    }
    public void Hacer_registro(View view) {
        Intent intent = new Intent(this, RegistroActivity.class);
        startActivity(intent);
    }

    private void hacerPeticionAcceso(String usuario, String contrasena) {
        String url = getString(R.string.IP)+"invernadero/usuarios/login?nombreUsuario=" +
                usuario + "&contraseña=" + contrasena;

        Log.d("URL", url); // Verificamos la URL en Logcat

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response != null && response.has("usuario")) {
                            String username = response.getString("usuario");
                            Toast.makeText(getApplicationContext(),
                                    "Inicio de sesión exitoso",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("LOG", String.valueOf(response));
                            // Verificamos permisos del usuario de la aplicacion
                            if (response.has("nivelAcceso")) {
                                String nivelAcceso  = response.getString("nivelAcceso");

                                Intent intent = new Intent(MainActivity.this, MainUsuarios.class);
                                Log.d("LOGIN","URL peticion" + response.toString());
                                intent.putExtra("userData", response.toString());
                                startActivity(intent);

                            }
                        }
                    } catch (Exception e) {
                        Log.e("JSON Error", e.getMessage());
                    }
                },
                error -> {
                    Log.e("VOLLEY_ERROR", error.toString());
                    if (error.networkResponse != null) {
                        Log.e("STATUS_CODE", String.valueOf(error.networkResponse.statusCode));
                    }
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
        );

        ServiciosWeb.getInstance(this).addToRequestQueue(request);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiciosWeb.getInstance(this).getRequestQueue().cancelAll(tag -> true);
    }
}