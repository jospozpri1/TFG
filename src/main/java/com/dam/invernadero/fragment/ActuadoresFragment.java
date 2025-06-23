package com.dam.invernadero.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.dam.invernadero.R;
import com.dam.invernadero.api.ActuadoresService;
import com.dam.invernadero.model.EstadoMotorRequest;
import com.dam.invernadero.model.Motor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActuadoresFragment extends Fragment {

    private TextView textoEstadoMotor;
    private Switch switchMotor;
    private String baseUrl;
    private boolean isUserInteraction = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_actuadores, container, false);

        textoEstadoMotor = view.findViewById(R.id.textoEstadoMotor);
        switchMotor = view.findViewById(R.id.switch_motor);


        baseUrl = getString(R.string.IP);

        // Configuramos el listener primero para que no salte al entrar
        switchMotor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUserInteraction) {
                cambiarEstadoMotor(isChecked);
            }
        });

        // Luego obtenemos el estado y asi no se envia al inicio
        obtenerEstadoMotor();

        return view;
    }

    private void obtenerEstadoMotor() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ActuadoresService api = retrofit.create(ActuadoresService.class);
        Call<Motor> call = api.getEstadoMotor();

        call.enqueue(new Callback<Motor>() {
            @Override
            public void onResponse(Call<Motor> call, Response<Motor> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean estado = response.body().isEstado();
                    textoEstadoMotor.setText(estado ? "Encendido" : "Apagado");

                    isUserInteraction = false;
                    switchMotor.setChecked(estado);
                    isUserInteraction = true;
                } else {
                    Log.e("ActuadoresFragment", "Error al obtener estado: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Motor> call, Throwable t) {
                Log.e("ActuadoresFragment", "Fallo en la llamada", t);
                isUserInteraction = true;
            }
        });
    }

    private void cambiarEstadoMotor(boolean nuevoEstado) {
        // Creamos el objeto de estado correcto
        Map<String, Object> estado = new HashMap<>();
        estado.put("type", "Boolean");
        estado.put("value", nuevoEstado);

        // Creamos el objeto Motor
        Map<String, Object> motorData = new HashMap<>();
        motorData.put("id", "MotorData1");
        motorData.put("type", "Motor");
        motorData.put("estado", estado);

        // Creamos el payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", Collections.singletonList(motorData));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ActuadoresService api = retrofit.create(ActuadoresService.class);

        Call<Motor> call = api.cambiarEstadoMotor(payload, "app");
        call.enqueue(new Callback<Motor>() {
            @Override
            public void onResponse(Call<Motor> call, Response<Motor> response) {
                if (response.isSuccessful()) {
                    Motor estado = response.body();
                    textoEstadoMotor.setText(estado.isEstado() ? "Encendido" : "Apagado");
                    Toast.makeText(getContext(), "Motor " + (estado.isEstado() ? "encendido" : "apagado"), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ActuadoresFragment", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Motor> call, Throwable t) {
                Log.e("ActuadoresFragment", "Fallo al cambiar estado", t);
            }
        });
    }


}
