package com.example.medicarereminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicarereminder.R;
import com.example.medicarereminder.adapter.MedicationAdapter;
import com.example.medicarereminder.api.ApiService;
import com.example.medicarereminder.api.MedicationRequest;
import com.example.medicarereminder.api.RetrofitClient;
import com.example.medicarereminder.model.Medication;
import com.example.medicarereminder.utils.SharedPrefManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicationsFragment extends Fragment {

    private RecyclerView recyclerViewMedications;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Button btnAddMedication;

    private MedicationAdapter adapter;
    private List<Medication> medications = new ArrayList<>();
    private ApiService apiService;
    private SharedPrefManager sharedPrefManager;

    public MedicationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_medications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize
        apiService = RetrofitClient.getInstance().create(ApiService.class);
        sharedPrefManager = SharedPrefManager.getInstance(requireContext());

        // Initialize views
        recyclerViewMedications = view.findViewById(R.id.recyclerViewMedications);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnAddMedication = view.findViewById(R.id.btnAddMedication);

        // Setup RecyclerView
        adapter = new MedicationAdapter(medications);
        recyclerViewMedications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMedications.setAdapter(adapter);

        // Set up click listeners
        btnAddMedication.setOnClickListener(v -> showAddMedicationDialog());

        // Set up item click listeners for adapter
        adapter.setOnItemClickListener(new MedicationAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                Medication medication = medications.get(position);
                showEditMedicationDialog(medication);
            }

            @Override
            public void onDeleteClick(int position) {
                Medication medication = medications.get(position);
                showDeleteConfirmationDialog(medication);
            }
        });

        // Load medications
        loadMedications();
    }

    private void loadMedications() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Check if user is logged in first
        if (!sharedPrefManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIXED: Add "Bearer " prefix to token
        String token = "Bearer " + sharedPrefManager.getToken();

        System.out.println("Loading medications with token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL"));

        Call<List<Medication>> call = apiService.getMedications(token);
        call.enqueue(new Callback<List<Medication>>() {
            @Override
            public void onResponse(Call<List<Medication>> call, Response<List<Medication>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    medications.clear();
                    medications.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    Toast.makeText(requireContext(), "Loaded " + medications.size() + " medications", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMessage = "Failed to load medications";
                    if (response.code() == 401) {
                        errorMessage = "Authentication failed. Please login again.";
                    } else if (response.code() == 403) {
                        errorMessage = "Access denied. Invalid token.";
                    }
                    Toast.makeText(requireContext(), errorMessage + " (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();
                    updateEmptyState();

                    // Debug: Print response error
                    if (response.errorBody() != null) {
                        try {
                            System.out.println("Error response: " + response.errorBody().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Medication>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                updateEmptyState();
            }
        });
    }

    private void showAddMedicationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medication, null);

        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etDosage = dialogView.findViewById(R.id.etDosage);
        TextInputEditText etTime = dialogView.findViewById(R.id.etTime);
        TextInputEditText etDuration = dialogView.findViewById(R.id.etDuration);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Medication")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String dosage = etDosage.getText().toString().trim();
                    String time = etTime.getText().toString().trim();
                    String durationStr = etDuration.getText().toString().trim();

                    // Validation
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Medication name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (time.isEmpty()) {
                        Toast.makeText(requireContext(), "Time is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Integer duration = null;
                    if (!durationStr.isEmpty()) {
                        try {
                            duration = Integer.parseInt(durationStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Please enter a valid number for duration", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    addMedication(name, dosage, time, duration);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addMedication(String name, String dosage, String time, Integer duration) {
        progressBar.setVisibility(View.VISIBLE);

        // Check if user is logged in first
        if (!sharedPrefManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIXED: Add "Bearer " prefix to token
        String token = "Bearer " + sharedPrefManager.getToken();
        MedicationRequest request = new MedicationRequest(name, dosage, time, duration);

        System.out.println("Adding medication with token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL"));

        Call<Medication> call = apiService.addMedication(token, request);
        call.enqueue(new Callback<Medication>() {
            @Override
            public void onResponse(Call<Medication> call, Response<Medication> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Medication addedMedication = response.body();
                    Toast.makeText(requireContext(), "Medication added successfully!", Toast.LENGTH_SHORT).show();
                    loadMedications(); // Refresh the list
                } else {
                    String errorMessage = "Failed to add medication";
                    if (response.code() == 401) {
                        errorMessage = "Authentication failed. Please login again.";
                    } else if (response.code() == 403) {
                        errorMessage = "Access denied. Invalid token.";
                    }
                    Toast.makeText(requireContext(), errorMessage + " (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();

                    // Debug: Print response error
                    if (response.errorBody() != null) {
                        try {
                            System.out.println("Error response: " + response.errorBody().string());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Medication> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEditMedicationDialog(Medication medication) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medication, null);

        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etDosage = dialogView.findViewById(R.id.etDosage);
        TextInputEditText etTime = dialogView.findViewById(R.id.etTime);
        TextInputEditText etDuration = dialogView.findViewById(R.id.etDuration);

        // Pre-fill with existing data
        etName.setText(medication.getName());
        etDosage.setText(medication.getDosage());
        etTime.setText(medication.getTime());
        if (medication.getDuration() != null && medication.getDuration() > 0) {
            etDuration.setText(String.valueOf(medication.getDuration()));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Medication")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    // For now, we'll just show a message since we don't have update endpoint
                    Toast.makeText(requireContext(), "Update functionality coming soon!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmationDialog(Medication medication) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Medication")
                .setMessage("Are you sure you want to delete " + medication.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // For now, we'll just show a message since we don't have delete endpoint
                    Toast.makeText(requireContext(), "Delete functionality coming soon!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState() {
        if (medications.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerViewMedications.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerViewMedications.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh medications when fragment becomes visible
        loadMedications();
    }
}