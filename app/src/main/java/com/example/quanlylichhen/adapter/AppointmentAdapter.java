package com.example.quanlylichhen.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlylichhen.R;
import com.example.quanlylichhen.model.Appointment;
import java.util.ArrayList;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private ArrayList<Appointment> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int id);
    }

    public AppointmentAdapter(ArrayList<Appointment> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void setList(ArrayList<Appointment> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appt = list.get(position);
        holder.tvTitle.setText(appt.getTitle());
        holder.tvLocation.setText(appt.getLocation());
        
        String date = appt.getDate();
        try {
            String[] parts = date.split("-");
            date = parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) {}
        
        holder.tvDateTime.setText(date + " · " + appt.getTime());
        
        if (appt.isReminder()) {
            holder.tvReminder.setText("Có nhắc");
            holder.tvReminder.setTextColor(0xFF087F76);
        } else {
            holder.tvReminder.setText("Không nhắc");
            holder.tvReminder.setTextColor(0xFF78908B);
        }
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(appt.getId()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLocation, tvDateTime, tvReminder;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvReminder = itemView.findViewById(R.id.tvReminder);
        }
    }
}