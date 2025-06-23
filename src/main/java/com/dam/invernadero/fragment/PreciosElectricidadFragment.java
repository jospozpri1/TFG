package com.dam.invernadero.fragment;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dam.invernadero.R;
import com.dam.invernadero.api.ReeApiService;
import com.dam.invernadero.model.Limites;
import com.dam.invernadero.model.Precio;
import com.dam.invernadero.model.ReeResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PreciosElectricidadFragment extends Fragment {

    private TextView textoPrecios;
    private List<String> horasSeleccionadas = new ArrayList<>();
    private TextView textoHorasSeleccionadas;
    private String baseUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_precios_electricidad, container, false);
        textoPrecios = view.findViewById(R.id.textoPrecios);
        obtenerPrecios("peninsular");
        textoHorasSeleccionadas = view.findViewById(R.id.textoHorasSeleccionadas);

        baseUrl= getString(R.string.IP);
        Log.d("Ele", "Datos recibidos: "+baseUrl);

        Button btnAgregarHora = view.findViewById(R.id.btnAgregarHora);
        Button btnEnviarHoras = view.findViewById(R.id.btnEnviarHoras);

        Bundle arguments = getArguments();
        if (arguments != null) {
            String jsonString = arguments.getString("userData");
            if (jsonString != null) {
                Log.d("PreciosElectricidadFragment", "Datos recibidos: " + jsonString);

                try {
                    JSONObject userData = new JSONObject(jsonString);
                    int userId = userData.getInt("id");
                    int nivelAcceso = userData.getInt("nivelAcceso");
                    if (nivelAcceso==3) {

                        btnAgregarHora.setVisibility(View.VISIBLE);
                        btnEnviarHoras.setVisibility(View.VISIBLE);
                        textoHorasSeleccionadas.setVisibility(View.VISIBLE);

                    } else {
                        btnAgregarHora.setVisibility(View.GONE);
                        btnEnviarHoras.setVisibility(View.GONE);
                        textoHorasSeleccionadas.setVisibility(View.GONE);

                    }
                    btnAgregarHora.setOnClickListener(v -> mostrarTimePicker());

                    btnEnviarHoras.setOnClickListener(v -> enviarHorasAlServidor(userId));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return view;
    }

    private void obtenerPrecios(String zona) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://apidatos.ree.es/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ReeApiService service = retrofit.create(ReeApiService.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String hoy = sdf.format(new Date());

        textoPrecios.setText("Cargando datos...\n");

        Call<ReeResponse> callHoy = service.getPrecios(
                hoy + "T00:00",
                hoy + "T23:59",
                "hour",
                "electric_system",
                zona,
                "8741"
        );

        callHoy.enqueue(new Callback<ReeResponse>() {
            @Override
            public void onResponse(@NonNull Call<ReeResponse> call, @NonNull Response<ReeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Precio> precios = response.body().getIncluded().get(0).getAttributes().getValues();

                    // Creamos una copia independiente para ordenarla por precio sin modificar la lista original
                    List<Precio> copiaOrdenada = new ArrayList<>(precios);
                    Collections.sort(copiaOrdenada, Comparator.comparingDouble(Precio::getValue));

                    // Guardamos los datetimes de los 5 precios más baratos
                    Set<String> fechasMasBaratas = new HashSet<>();
                    for (int i = 0; i < Math.min(5, copiaOrdenada.size()); i++) {
                        fechasMasBaratas.add(copiaOrdenada.get(i).getDatetime());
                    }

                    // Construimos el resultado en orden cronológico
                    SpannableString resultado = new SpannableString("Precios de hoy:\n");
                    for (Precio p : precios) {
                        String hora = p.getDatetime().substring(11, 16);
                        String linea = hora + " - " + p.getValue() + " €/MWh\n";
                        SpannableString lineaSpan = new SpannableString(linea);

                        // Si la hora es de los 5 precios más baratos, se pinta en verde
                        if (fechasMasBaratas.contains(p.getDatetime())) {
                            lineaSpan.setSpan(
                                    new ForegroundColorSpan(Color.MAGENTA),
                                    0, hora.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            );
                        }

                        resultado = new SpannableString(TextUtils.concat(resultado, lineaSpan));
                    }

                    textoPrecios.setText(resultado);
                } else {
                    textoPrecios.setText("Error cargando precios de hoy\n");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReeResponse> call, @NonNull Throwable t) {
                textoPrecios.setText("Fallo en la carga de hoy: " + t.getMessage() + "\n");
            }
        });
    }
    private void mostrarTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            String hora = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            horasSeleccionadas.add(hora);
            actualizarTextoHoras();
        }, 12, 0, true);
        timePicker.show();
    }

    private void actualizarTextoHoras() {
        textoHorasSeleccionadas.setText("Horas seleccionadas: " + TextUtils.join(", ", horasSeleccionadas));
    }
    private void enviarHorasAlServidor(Integer userId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        List<String> horasRiego = new ArrayList<>(horasSeleccionadas);

        // Creamos el servicio Retrofit
        ReeApiService service = retrofit.create(ReeApiService.class);

        Call<Void> call = service.actualizarHorasRiego(userId, horasRiego);

        // Realizamos la solicitud
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Horas enviadas correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al enviar", Toast.LENGTH_SHORT).show();
                    Log.d("PreciosElectricidadFragment", "Código de error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo en la red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



}
