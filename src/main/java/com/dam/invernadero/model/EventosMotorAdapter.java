package com.dam.invernadero.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.dam.invernadero.R;

import java.util.List;

public class EventosMotorAdapter extends RecyclerView.Adapter<EventosMotorAdapter.ViewHolder> {
    private List<Motor> eventos;

    public EventosMotorAdapter(List<Motor> eventos) {
        this.eventos = eventos;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtAccion, txtMotivo, txtDuracion, txtConsumo, txtFecha;

        public ViewHolder(View view) {
            super(view);
            txtAccion = view.findViewById(R.id.txtAccion);
            txtMotivo = view.findViewById(R.id.txtMotivo);
            txtDuracion = view.findViewById(R.id.txtDuracion);
            txtConsumo = view.findViewById(R.id.txtConsumo);
            txtFecha=view.findViewById(R.id.txtFecha);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evento_motor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Motor evento = eventos.get(position);
        holder.txtAccion.setText("Acción: " + evento.getAccion());
        holder.txtMotivo.setText("Motivo: " + evento.getMotivo());
        holder.txtDuracion.setText("Duración: " + evento.getDuracionSegundos() + " s");
        holder.txtConsumo.setText("Consumo: " + evento.getConsumoAgua() + " ml");
        holder.txtFecha.setText("Fecha de registro: "+evento.getFecha());
    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }
}
