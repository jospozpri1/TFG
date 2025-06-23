package com.dam.invernadero.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.dam.invernadero.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class InicioFragment extends Fragment {
    private TextView txtLuz, txtTemperatura, txtHumedadAmbiente, txtAgua, txtHumedadSuelo;
    private ImageView imgLuz, imgTemperatura, imgHumedadAmbiente, imgAgua, imgHumedadSuelo;
    private TextView txtWelcome;
    private JSONObject userData;
    private String jsonString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        txtWelcome = view.findViewById(R.id.txtInicio);
        txtLuz = view.findViewById(R.id.txtLuz);
        imgLuz = view.findViewById(R.id.imgLuz);
        txtTemperatura = view.findViewById(R.id.txtTemperatura);
        imgTemperatura = view.findViewById(R.id.imgTemperatura);
        txtHumedadAmbiente = view.findViewById(R.id.txtHumedadAmbiente);
        txtHumedadSuelo = view.findViewById(R.id.txtHumedadSuelo);
        imgHumedadAmbiente = view.findViewById(R.id.imgHumedadAmbiente);

        imgHumedadSuelo = view.findViewById(R.id.imgHumedadSuelo);

        txtAgua = view.findViewById(R.id.txtAgua);
        imgAgua = view.findViewById(R.id.imgAgua);

        obtenerUltimaLectura();

        Log.d("Fragment_INICIO", String.valueOf(getArguments()));
        if (getArguments() != null) {
            jsonString = getArguments().getString("userData");
            Log.d("Fragment", "Datos recibidos: " + jsonString);

            try {
                JSONObject userData = new JSONObject(jsonString);
                String user = userData.getString("usuario");
                txtWelcome.setText("Bienvenido, " + user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return view;
    }
    private void obtenerUltimaLectura() {
        String url = getString(R.string.IP)+"invernadero/sensores/ultima";

        new Thread(() -> {
            try {
                URL endpoint = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) result.append(line);
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                Log.d("Inicio", String.valueOf(json));
                int luz = json.getInt("luzAmbiente");
                double temp = json.getDouble("temperaturaC");
                double humedadAmbiental = json.getDouble("humedadAmbiental");
                int humedadSuelo = json.getInt("humedadSuelo");
                int nivelAgua = json.getInt("nivelAgua");
                requireActivity().runOnUiThread(() -> {
                    txtLuz.setText("Luz: " + luz+ " %");
                    txtTemperatura.setText("Temp: " + temp + "°C");
                    txtHumedadAmbiente.setText("Humedad del ambiente: " + humedadAmbiental + " %");
                    txtAgua.setText("Nivel Agua: " + nivelAgua);
                    txtHumedadSuelo.setText("Humedad del suelo: "+ humedadSuelo+ " %");
                    imgLuz.setImageResource(luz > 30 ? R.drawable.ic_sol_brillante : R.drawable.ic_sol_apagado);
                    if (temp<0){
                        imgTemperatura.setImageResource( R.drawable.ic_temperatura_frio);
                    } else if (temp>25) {
                        imgTemperatura.setImageResource( R.drawable.ic_temperatura_calor);
                    }else{
                        imgTemperatura.setImageResource( R.drawable.ic_temperatura_templada);
                    }
                    if (humedadAmbiental <25) {
                        imgHumedadAmbiente.setImageResource( R.drawable.ic_humedad_baja);
                    }else if (humedadAmbiental>75){
                        imgHumedadAmbiente.setImageResource( R.drawable.ic_humedad_alta);
                    }else{
                        imgHumedadAmbiente.setImageResource( R.drawable.ic_humedad_media);
                    }
                    if (humedadSuelo <25) {
                        imgHumedadSuelo.setImageResource( R.drawable.ic_humedad_baja);
                    }else if (humedadSuelo>75){
                        imgHumedadSuelo.setImageResource( R.drawable.ic_humedad_alta);
                    }else{
                        imgHumedadSuelo.setImageResource( R.drawable.ic_humedad_media);
                    }
                    if (nivelAgua<25){
                        imgAgua.setImageResource( R.drawable.ic_nivel_bajo);
                    } else if (nivelAgua<50) {
                        imgAgua.setImageResource( R.drawable.ic_nivel_medio);
                    } else if (nivelAgua<75) {
                        imgAgua.setImageResource( R.drawable.ic_nivel_alto);
                    }else{
                        imgAgua.setImageResource( R.drawable.ic_nivel_maximo);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
