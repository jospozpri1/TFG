package com.dam.invernadero.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.dam.invernadero.R;
import com.dam.invernadero.api.OpenWeatherService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TiempoFragment extends Fragment {

    private EditText etCiudad, etPais;
    private TextView textoTiempo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tiempo, container, false);

        etCiudad = view.findViewById(R.id.etCiudad);
        etPais = view.findViewById(R.id.etPais);
        textoTiempo = view.findViewById(R.id.textoTiempo);
        Button btnBuscar = view.findViewById(R.id.btnBuscar);

        btnBuscar.setOnClickListener(v -> {
            String ciudad = etCiudad.getText().toString().trim();
            String pais = etPais.getText().toString().trim();
            if (!ciudad.isEmpty() && !pais.isEmpty()) {
                obtenerDatosTiempo(ciudad + "," + pais);
            } else {
                textoTiempo.setText("Introduce ciudad y país.");
                textoTiempo.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void obtenerDatosTiempo(String ubicacion) {
        Log.d("TiempoFragment", "Obteniendo datos para: " + ubicacion);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OpenWeatherService service = retrofit.create(OpenWeatherService.class);
        String API_KEY = "10e1bfa429a87a1fcecec68de568a942";
        Call<JsonObject> call = service.getForecast(ubicacion, "metric", "es", API_KEY);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    textoTiempo.setVisibility(View.VISIBLE);
                    boolean previsionMañana = false;
                    boolean previsionPasadoMañana = false;
                    int cont=0;
                    Log.d("TiempoFragment", "Respuesta exitosa. Procesando datos.");
                    JsonObject weatherData = response.body();
                    JsonArray hourlyArray = weatherData.getAsJsonArray("list");

                    // Obtenemos la fecha actual
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String fechaActual = sdf.format(new Date());
                    Log.d("TiempoFragment", "Fecha actual: " + fechaActual);

                    // Obtenemos la primera hora de la previsión (hora 0)
                    long timestamp = hourlyArray.get(0).getAsJsonObject().get("dt").getAsLong();
                    String fechaPrevision = sdf.format(new Date(timestamp * 1000L));
                    Log.d("TiempoFragment", "Fecha de previsión: " + fechaPrevision);

                    // Comprobamos la primera hora para determinar si es 00:00
                    String mensaje = "Previsión para hoy:\n";

                    long firstHourTimestamp = hourlyArray.get(0).getAsJsonObject().get("dt").getAsLong();
                    String firstHour = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date(firstHourTimestamp * 1000L));
                    Log.d("TiempoFragment", "Hora: " + firstHour);

                    if ("00".equals(firstHour)) {
                        mensaje = "Previsión para mañana:\n";
                        previsionMañana=true;
                    }


                    // Procesamos los datos de la previsión por horas
                    StringBuilder resultado = new StringBuilder();
                    resultado.append(mensaje);
                    for (int i = 0; i < Math.min(hourlyArray.size(), 12); i++) {
                        JsonObject hourData = hourlyArray.get(i).getAsJsonObject();
                        long hourTimestamp = hourData.get("dt").getAsLong();
                        double temp = hourData.getAsJsonObject("main").get("temp").getAsDouble();
                        String descripcion = hourData.getAsJsonArray("weather").get(0).getAsJsonObject().get("description").getAsString();
                        String icono = obtenerIconoTiempo(hourData.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString());

                        // Convertimos el timestamp a hora
                        SimpleDateFormat horaFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        String horaStr = horaFormat.format(new Date(hourTimestamp * 1000L));
                        if (horaStr.equals("00:00") ) {
                            cont=cont+1;
                            if (!previsionMañana) {
                                resultado.append("\nPrevisión para mañana:\n");
                                previsionMañana = true;
                                previsionPasadoMañana = true;
                            }
                            if(previsionPasadoMañana && cont==2){
                                resultado.append("\nPrevisión para pasado mañana:\n");
                                previsionPasadoMañana = false;
                            }
                        }


                        resultado.append(horaStr)
                                .append(": ").append(descripcion)
                                .append(" ").append(icono)
                                .append(", ").append(temp).append("°C\n");
                    }

                    textoTiempo.setText(resultado.toString());
                    Log.d("TiempoFragment", "Datos procesados y mostrados.");
                } else {
                    textoTiempo.setText("No se pudo obtener el clima. Verifica ciudad y país.");
                    Log.d("TiempoFragment", "Error en la respuesta o datos nulos.");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                textoTiempo.setText("Error de red: " + t.getMessage());
                Log.d("TiempoFragment", "Error en la solicitud: " + t.getMessage());
            }
        });
    }

    private String obtenerIconoTiempo(String main) {
        switch (main.toLowerCase()) {
            case "rain":
            case "drizzle":
            case "thunderstorm":
                return "🌧️";  // Lluvia
            case "clear":
                return "☀️";  // Sol
            case "clouds":
                return "☁️";  // Nubes
            default:
                return "🌫️";  // Desconocido
        }
    }
}
