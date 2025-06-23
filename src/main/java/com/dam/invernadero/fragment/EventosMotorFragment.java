package com.dam.invernadero.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.invernadero.R;
import com.dam.invernadero.api.EventosMotorService;
import com.dam.invernadero.model.Motor;
import com.dam.invernadero.model.EventosMotorAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventosMotorFragment extends Fragment {

    private RecyclerView recyclerEventos;
    private EventosMotorAdapter adapter;
    private List<Motor> listaEventos = new ArrayList<>();
    private int paginaActual = 0;

    private BarChart barChart;

    private String baseUrl;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eventos_motor, container, false);

        recyclerEventos = view.findViewById(R.id.recyclerEventos);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventosMotorAdapter(listaEventos);
        recyclerEventos.setAdapter(adapter);

        barChart = view.findViewById(R.id.barChart);
        baseUrl = getString(R.string.IP);
        obtenerEventos();

        return view;
    }

    private void obtenerEventos() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        EventosMotorService api = retrofit.create(EventosMotorService.class);
        Call<List<Motor>> call = api.getEventosPaginados(paginaActual, 5);

        call.enqueue(new Callback<List<Motor>>() {
            @Override
            public void onResponse(Call<List<Motor>> call, Response<List<Motor>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("EventosMotorFragment", "Respuesta API: " + response.body().toString());
                    listaEventos.clear();
                    listaEventos.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    cargarGrafica(listaEventos);
                }else {
                    Log.e("EventosMotorFragment", "Error en la respuesta de la API: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Motor>> call, Throwable t) {
                Log.e("EventosMotorFragment", "Error cargando eventos", t);
            }
        });
    }

    private void cargarGrafica(List<Motor> eventos) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();

        for (int i = 0; i < eventos.size(); i++) {
            Motor evento = eventos.get(i);
            entries.add(new BarEntry(i, (float) evento.getConsumoAgua()));

            // Extraemos solo la fecha
            String fechaCompleta = evento.getFecha();
            if (fechaCompleta != null) {
                try {
                    SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
                    Date fecha = formatoFecha.parse(fechaCompleta);
                    SimpleDateFormat formatoHora = new SimpleDateFormat("dd/MM HH:mm");
                    String etiqueta = formatoHora.format(fecha);
                    etiquetas.add(etiqueta);
                } catch (Exception e) {
                    Log.e("EventosMotorFragment", "Error al parsear la fecha: " + e.getMessage());
                    etiquetas.add("Fecha no válida");
                }
            } else {
                etiquetas.add("Fecha no válida");
            }
        }


        BarDataSet dataSet = new BarDataSet(entries, "Consumo de agua (ml)");
        dataSet.setColor(Color.BLUE);
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Etiquetas del eje X
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);

                if (index >= 0 && index < etiquetas.size()) {
                    return etiquetas.get(index);
                } else {
                    return "";
                }
            }
        });


        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.invalidate();
    }



}
