package com.dam.invernadero.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.dam.invernadero.R;
import com.dam.invernadero.api.GraficasService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GraficaSensoresFragment extends Fragment {

    private Spinner spinnerPeriodo, spinnerSensor;
    private Button btnCargar;
    private LineChart lineChart;
    private BarChart barChart;

    private final String[] sensores = {"temperaturac", "temperaturaf", "humedadambiental",
            "humedadsuelo", "luzambiente", "nivelagua"
    };

    private String baseUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grafica_sensores, container, false);

        spinnerPeriodo = view.findViewById(R.id.spinnerPeriodo);
        spinnerSensor = view.findViewById(R.id.spinnerSensor);
        btnCargar = view.findViewById(R.id.btnCargar);
        lineChart = view.findViewById(R.id.lineChart);
        barChart = view.findViewById(R.id.barChart);
        baseUrl = getString(R.string.IP);

        ArrayAdapter<String> adapterSensor = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sensores);
        adapterSensor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSensor.setAdapter(adapterSensor);

        btnCargar.setOnClickListener(v -> obtenerDatosYMostrar());

        return view;
    }

    private void obtenerDatosYMostrar() {
        String sensor = spinnerSensor.getSelectedItem().toString();
        String periodo = spinnerPeriodo.getSelectedItem().toString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GraficasService service = retrofit.create(GraficasService.class);

        Call<List<Map<String, Object>>> call;

        if (periodo.equals("Hoy")) {
            call = service.getResumenHoy();
        } else {
            Log.d("Graficas",periodo);
            int per = 0;
            if (periodo.equals("Últimos 7 días"))
                per=7;
            else
                per=30;
            Log.d("Graficas", String.valueOf(per));
            call = service.getResumenUltimosDias(String.valueOf(per));
        }

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> datos = response.body();
                    Log.d("Graficas", String.valueOf(datos));
                    mostrarGraficaDesdeMap(sensor, datos, periodo);
                } else {
                    Toast.makeText(getContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                Log.e("Retrofit", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void mostrarGraficaDesdeMap(String sensor, List<Map<String, Object>> datos, String periodo) {
        lineChart.setVisibility(View.GONE);
        barChart.setVisibility(View.GONE);
        datos.sort((map1, map2) -> {
            String fecha1 = (String) map1.get("fecha");
            String fecha2 = (String) map2.get("fecha");

            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            SimpleDateFormat parserSoloFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            try {
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = parser.parse(fecha1);  //  Intentamos parsear con fecha y hora
                } catch (ParseException e) {
                    date1 = parserSoloFecha.parse(fecha1 + "T00:00:00.000+00:00");  // Si falla, lo parseamos como solo fecha
                }
                try {
                    date2 = parser.parse(fecha2);  // Intenta parsear con fecha y hora
                } catch (ParseException e) {
                    date2 = parserSoloFecha.parse(fecha2 + "T00:00:00.000+00:00");  // Si falla, lo parseamos como solo fecha
                }

                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        });

        List<Entry> entriesLinea = new ArrayList<>();
        List<BarEntry> entriesBarras = new ArrayList<>();
        List<String> fechas = new ArrayList<>();

        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
        SimpleDateFormat parserSoloFecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM", Locale.getDefault());

        Log.d("DataProcessing", "Total de datos: " + datos.size());

        for (int i = 0; i < datos.size(); i++) {
            Map<String, Object> lectura = datos.get(i);
            float valor = 0f;
            try {
                Object raw = lectura.get(sensor.toLowerCase());
                if (raw instanceof Number) {
                    valor = ((Number) raw).floatValue();
                }
            } catch (Exception e) {
                Log.e("ParseError", "Error parsing valor", e);
            }
            entriesLinea.add(new Entry(i, valor));
            entriesBarras.add(new BarEntry(i, valor));

            String fechaCruda = (String) lectura.get("fecha");
            String etiqueta = "";
            try {
                Date date;
                try {
                    date = parser.parse(fechaCruda);
                    if (periodo.equals("Hoy")) {
                        etiqueta = formatoHora.format(date); //Ponemos solo la hora en la grafica
                    } else {
                        etiqueta = formatoFecha.format(date); //Ponemos solo la hora en la grafica
                    }
                } catch (ParseException e) {
                    date = parserSoloFecha.parse(fechaCruda);
                    etiqueta = formatoFecha.format(date); // Ponemos solo la hora en la grafica
                }
            } catch (ParseException e) {
                Log.e("FechaError", "No se pudo parsear la fecha", e);
            }
            fechas.add(etiqueta);
        }


        XAxis xAxis;
        if (sensor.contains("humedad") || sensor.contains("nivel")) {
            barChart.setVisibility(View.VISIBLE);
            BarDataSet dataSet = new BarDataSet(entriesBarras, sensor);
            dataSet.setColor(Color.BLUE);
            barChart.setData(new BarData(dataSet));
            barChart.getDescription().setEnabled(false);
            final String periodoFinal = periodo;
            final int totalDatos = fechas.size();

            Log.d("DataProcessing", "Total de fechas: " + fechas.size());

            int etiquetasVisibles;
            if (periodoFinal.equals("Hoy")) {
                etiquetasVisibles = totalDatos;
            } else if (periodoFinal.equals("7 días")) {
                etiquetasVisibles = Math.min(totalDatos, 7);
            } else {
                etiquetasVisibles = Math.min(totalDatos, 6);
            }

            final int intervalo = totalDatos / etiquetasVisibles;

            Log.d("DataProcessing", "Etiquetas visibles: " + etiquetasVisibles);
            Log.d("DataProcessing", "Intervalo: " + intervalo);

            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = Math.round(value);

                    if (index >= 0 && index < fechas.size()) {
                        Log.d("DataProcessing", "Index: " + index + ", Fecha: " + fechas.get(index));

                        if (periodoFinal.equals("Hoy")) {
                            return fechas.get(index);
                        } else {
                            return index % intervalo == 0 ? fechas.get(index) : "";
                        }
                    }
                    return "";
                }
            };

            xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(formatter);
            barChart.invalidate();
        } else {
            lineChart.setVisibility(View.VISIBLE);
            LineDataSet dataSet = new LineDataSet(entriesLinea, sensor);
            dataSet.setColor(Color.GREEN);
            dataSet.setCircleColor(Color.RED);
            lineChart.setData(new LineData(dataSet));
            lineChart.getDescription().setEnabled(false);
            final String periodoFinal = periodo;
            final int totalDatos = fechas.size();

            Log.d("DataProcessing", "Total de fechas: " + fechas.size());

            int etiquetasVisibles;
            if (periodoFinal.equals("Hoy")) {
                etiquetasVisibles = totalDatos;
            } else if (periodoFinal.equals("7 días")) {
                etiquetasVisibles = Math.min(totalDatos, 7);
            } else {
                etiquetasVisibles = Math.min(totalDatos, 6);
            }
            final int intervalo = totalDatos / etiquetasVisibles;

            Log.d("DataProcessing", "Etiquetas visibles: " + etiquetasVisibles);
            Log.d("DataProcessing", "Intervalo: " + intervalo);

            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = Math.round(value);

                    if (index >= 0 && index < fechas.size()) {
                        Log.d("DataProcessing", "Index: " + index + ", Fecha: " + fechas.get(index));

                        if (periodoFinal.equals("Hoy")) {
                            return fechas.get(index);
                        } else {
                            return index % intervalo == 0 ? fechas.get(index) : "";
                        }
                    }
                    return "";
                }
            };

            xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(formatter);

            lineChart.invalidate();
        }

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setLabelCount(Math.min(fechas.size(), 6));
    }
}
