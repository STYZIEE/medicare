package com.example.medicarereminder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicarereminder.R;
import com.example.medicarereminder.model.Medication;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    private List<Medication> medications;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
        void onToggleNotificationClick(int position); // ADD THIS
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public MedicationAdapter(List<Medication> medications) {
        this.medications = medications;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false); // CHANGED LAYOUT
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medications.get(position);

        holder.tvMedName.setText(medication.getName());
        holder.tvDosage.setText("Dosage: " + medication.getDosage());
        holder.tvTime.setText("Time: " + medication.getTime());

        if (medication.getDuration() != null && medication.getDuration() > 0) {
            holder.tvDuration.setText("Duration: " + medication.getDuration() + " days");
        } else {
            holder.tvDuration.setText("Duration: Not specified");
        }

        // Notification status
        if (medication.isNotificationEnabled()) {
            holder.btnNotification.setText("🔔 ON");
            holder.btnNotification.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
        } else {
            holder.btnNotification.setText("🔕 OFF");
            holder.btnNotification.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
        }

        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onEditClick(position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onDeleteClick(position);
            }
        });

        holder.btnNotification.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onToggleNotificationClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    public void updateData(List<Medication> newMedications) {
        this.medications = newMedications;
        notifyDataSetChanged();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvDosage, tvTime, tvDuration;
        Button btnEdit, btnDelete, btnNotification;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnNotification = itemView.findViewById(R.id.btnNotification);
        }
    }
}