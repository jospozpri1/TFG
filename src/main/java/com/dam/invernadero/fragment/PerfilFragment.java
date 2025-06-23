package com.dam.invernadero.fragment;

import static android.app.PendingIntent.getActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.InputType;
import android.app.AlertDialog;
import android.widget.EditText;
import com.dam.invernadero.R;
import com.dam.invernadero.api.UsuarioService;
import com.dam.invernadero.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PerfilFragment extends Fragment {
    private TextView txtNombreUsuario, txtEmail, txtEstado, txtNivelAcceso;
    private Button btnCambiarContrasena;
    private String baseUrl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtEstado = view.findViewById(R.id.txtEstado);
        txtNivelAcceso = view.findViewById(R.id.txtNivelAcceso);
        btnCambiarContrasena = view.findViewById(R.id.btnCambiarContrasena);
        baseUrl = getString(R.string.IP);

        Log.d("Fragment", String.valueOf(getArguments()));
        if (getArguments() != null) {
            String jsonString = getArguments().getString("userData");
            Log.d("Fragment", "Datos recibidos: " + jsonString);

            try {
                JSONObject userData = new JSONObject(jsonString);
                int userId = userData.getInt("id"); // Obtenemos el ID del JSON
                Log.d("Fragment", "ID del usuario: " + userId);
                // Configuramos Retrofit
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                UsuarioService service = retrofit.create(UsuarioService.class);

                service.getUsuarioById(userId).enqueue(new Callback<Usuario>() {
                    @Override
                    public void onResponse(Call<Usuario> call, Response<Usuario> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Usuario usuario = response.body();
                            txtNombreUsuario.setText(usuario.getNombreUsuario());
                            txtEmail.setText(usuario.getEmail());
                            txtEstado.setText(usuario.isActivo() ? "Activo" : "Inactivo");
                            txtNivelAcceso.setText("Nivel " + usuario.getNivelAcceso());
                        } else {
                            Toast.makeText(getActivity(), "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Usuario> call, Throwable t) {
                        Toast.makeText(getActivity(), "Error al obtener datos", Toast.LENGTH_SHORT).show();
                    }
                });
                btnCambiarContrasena.setOnClickListener(v -> mostrarDialogoCambioContrasena(userId));

            } catch (JSONException e) {
                Log.e("Fragment", "Error al parsear JSON", e);
            }
        }

        return view;
    }

    private void mostrarDialogoCambioContrasena(int userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Cambiar contraseña");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);



        builder.setPositiveButton("Cambiar", (dialog, which) -> {
            String nuevaContrasena = input.getText().toString();
            Map<String, String> body = new HashMap<>();
            body.put("nuevaContrasena", nuevaContrasena);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            UsuarioService service = retrofit.create(UsuarioService.class);
            service.cambiarContrasena(userId, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Error al cambiar la contraseña", Toast.LENGTH_SHORT).show();
                        Log.d("LOG",String.valueOf(response));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getActivity(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}

