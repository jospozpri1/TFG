package com.dam.invernadero.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dam.invernadero.R;
import com.dam.invernadero.api.LimitesService;
import com.dam.invernadero.model.Limites;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LimitesFragment extends Fragment {

    private EditText etTempMin, etTempMax, etLuzMin,etHumedadAmbMin,etHumedadAmbMax,etHumedadSueloMin,
    etHumedadSueloMax,etLuzMax,etConsumoAguaMax,etVolumenAguaMin;
    private TextView tvListaHorasRiego;
    private Button btnGuardar;
    private String baseUrl;
    private int userId;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_limites, container, false);

        etTempMin = view.findViewById(R.id.etTempMin);
        etTempMax = view.findViewById(R.id.etTempMax);
        etHumedadAmbMin = view.findViewById(R.id.etHumedadAmbMin);
        etHumedadAmbMax = view.findViewById(R.id.etHumedadAmbMax);
        etHumedadSueloMin = view.findViewById(R.id.etHumedadSueloMin);
        etHumedadSueloMax = view.findViewById(R.id.etHumedadSueloMax);
        etLuzMin = view.findViewById(R.id.etLuzMin);
        etLuzMax = view.findViewById(R.id.etLuzMax);
        etConsumoAguaMax = view.findViewById(R.id.etConsumoAguaMax);
        etVolumenAguaMin = view.findViewById(R.id.etVolumenAguaMin);
        tvListaHorasRiego = view.findViewById(R.id.tvListaHorasRiego);
        btnGuardar = view.findViewById(R.id.btnGuardar);

        baseUrl = getString(R.string.IP);

        if (getArguments() != null) {
            String jsonString = getArguments().getString("userData");
            Log.d("Fragment", "Datos recibidos: " + jsonString);

            try {
                JSONObject userData = new JSONObject(jsonString);
                userId = userData.getInt("id");
                int nivelAcceso = userData.getInt("nivelAcceso");

                if (nivelAcceso == 3) {
                    btnGuardar.setVisibility(View.VISIBLE);
                    etTempMin.setEnabled(true);
                    etTempMax.setEnabled(true);
                    etHumedadAmbMin.setEnabled(true);
                    etHumedadAmbMax.setEnabled(true);
                    etHumedadSueloMin.setEnabled(true);
                    etHumedadSueloMax.setEnabled(true);
                    etLuzMin.setEnabled(true);
                    etLuzMax.setEnabled(true);
                    etConsumoAguaMax.setEnabled(true);
                    etVolumenAguaMin.setEnabled(true);
                    btnGuardar.setEnabled(true);
                }else{
                    btnGuardar.setVisibility(View.GONE);
                    userId=1;
                }

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                LimitesService service = retrofit.create(LimitesService.class);
                service.getLimites(userId).enqueue(new Callback<Limites>() {
                    @Override
                    public void onResponse(Call<Limites> call, Response<Limites> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            mostrarLimites(response.body());
                        } else {
                            Toast.makeText(getActivity(), "No se pudieron obtener los límites", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Limites> call, Throwable t) {
                        Toast.makeText(getActivity(), "Error al obtener los límites", Toast.LENGTH_SHORT).show();
                    }
                });

                btnGuardar.setOnClickListener(v -> {
                    Limites limitesActualizados = new Limites();
                    limitesActualizados.setTempMin(Double.valueOf(etTempMin.getText().toString()));
                    limitesActualizados.setTempMax(Double.valueOf(etTempMax.getText().toString()));
                    limitesActualizados.setHumedadAmbMin(Double.valueOf(etHumedadAmbMin.getText().toString()));
                    limitesActualizados.setHumedadAmbMax(Double.valueOf(etHumedadAmbMax.getText().toString()));
                    limitesActualizados.setHumedadSueloMin(Integer.valueOf(etHumedadSueloMin.getText().toString()));
                    limitesActualizados.setHumedadSueloMax(Integer.valueOf(etHumedadSueloMax.getText().toString()));
                    limitesActualizados.setLuzMin(Integer.valueOf(etLuzMin.getText().toString()));
                    limitesActualizados.setLuzMax(Integer.valueOf(etLuzMax.getText().toString()));
                    limitesActualizados.setConsumoAguaMax(Double.valueOf(etConsumoAguaMax.getText().toString()));
                    limitesActualizados.setVolumenAguaMin(Double.valueOf(etVolumenAguaMin.getText().toString()));
                    enviarLimitesActualizados(userId, limitesActualizados);
                });

            } catch (JSONException e) {
                Log.e("Fragment", "Error al parsear JSON", e);
            }
        }

        return view;
    }

    private void mostrarLimites(Limites limites) {
        etTempMin.setText(String.valueOf(limites.getTempMin()));
        etTempMax.setText(String.valueOf(limites.getTempMax()));
        etHumedadAmbMin.setText(String.valueOf(limites.getHumedadAmbMin()));
        etHumedadAmbMax.setText(String.valueOf(limites.getHumedadAmbMax()));
        etHumedadSueloMin.setText(String.valueOf(limites.getHumedadSueloMin()));
        etHumedadSueloMax.setText(String.valueOf(limites.getHumedadSueloMax()));
        etLuzMin.setText(String.valueOf(limites.getLuzMin()));
        etLuzMax.setText(String.valueOf(limites.getLuzMax()));
        etConsumoAguaMax.setText(String.valueOf(limites.getConsumoAguaMax()));
        etVolumenAguaMin.setText(String.valueOf(limites.getVolumenAguaMin()));
        tvListaHorasRiego.setText(String.join(", ", limites.getHorasRiego()));
    }


    private void enviarLimitesActualizados(int usuarioId, Limites limitesActualizados) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LimitesService service = retrofit.create(LimitesService.class);
        service.actualizarLimites(usuarioId, limitesActualizados).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Límites actualizados con éxito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Error al actualizar los límites", Toast.LENGTH_SHORT).show();
                    Log.d("Limites", String.valueOf(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getActivity(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
