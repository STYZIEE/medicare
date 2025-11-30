package com.example.medicarereminder.ui;

import android.os.Bundle;
import android.util.Log;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicationsFragment extends Fragment {

    private static final String TAG = "MedicationsFragment";

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

        // Debug token
        debugTokenInfo();

        // Load medications
        loadMedications();
    }

    private void debugTokenInfo() {
        String token = sharedPrefManager.getToken();
        boolean isLoggedIn = sharedPrefManager.isLoggedIn();

        Log.d(TAG, "=== TOKEN DEBUG ===");
        Log.d(TAG, "Is logged in: " + isLoggedIn);
        Log.d(TAG, "Token exists: " + (token != null));
        Log.d(TAG, "Token length: " + (token != null ? token.length() : "NULL"));
        if (token != null) {
            Log.d(TAG, "Token preview: " + token.substring(0, Math.min(20, token.length())) + "...");
        }
        Log.d(TAG, "User ID: " + sharedPrefManager.getUserId());
        Log.d(TAG, "Username: " + sharedPrefManager.getUsername());
        Log.d(TAG, "Email: " + sharedPrefManager.getEmail());
        Log.d(TAG, "=== END DEBUG ===");
    }

    private void loadMedications() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        if (!sharedPrefManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + sharedPrefManager.getToken();
        Log.d(TAG, "Loading medications with token: " + token);

        Call<List<Medication>> call = apiService.getMedications(token);
        call.enqueue(new Callback<List<Medication>>() {
            @Override
            public void onResponse(Call<List<Medication>> call, Response<List<Medication>> response) {
                progressBar.setVisibility(View.GONE);

                Log.d(TAG, "Load medications response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    medications.clear();
                    medications.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    Log.d(TAG, "Successfully loaded " + medications.size() + " medications");
                    Toast.makeText(requireContext(), "Loaded " + medications.size() + " medications", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMessage = "Failed to load medications. Code: " + response.code();
                    Log.e(TAG, errorMessage);

                    // Try to read error body
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);
                            errorMessage += "\n" + errorBody;
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Medication>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Load medications network error", t);
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

        if (!sharedPrefManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + sharedPrefManager.getToken();
        MedicationRequest request = new MedicationRequest(name, dosage, time, duration);

        Log.d(TAG, "Adding medication: " + name);
        Log.d(TAG, "Request: " + request.getName() + ", " + request.getDosage() + ", " + request.getTime() + ", " + request.getDuration());

        Call<Medication> call = apiService.addMedication(token, request);
        call.enqueue(new Callback<Medication>() {
            @Override
            public void onResponse(Call<Medication> call, Response<Medication> response) {
                progressBar.setVisibility(View.GONE);

                Log.d(TAG, "Add medication response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Medication addedMedication = response.body();
                    Log.d(TAG, "Medication added successfully: " + addedMedication.getName());
                    Toast.makeText(requireContext(), "Medication added successfully!", Toast.LENGTH_SHORT).show();
                    loadMedications();
                } else {
                    String errorMessage = "Failed to add medication. Code: " + response.code();
                    Log.e(TAG, errorMessage);

                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error response body: " + errorBody);
                            errorMessage += "\n" + errorBody;
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Medication> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Add medication network error", t);
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
                    String name = etName.getText().toString().trim();
                    String dosage = etDosage.getText().toString().trim();
                    String time = etTime.getText().toString().trim();
                    String durationStr = etDuration.getText().toString().trim();

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

                    updateMedication(medication.getId(), name, dosage, time, duration);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateMedication(Long medicationId, String name, String dosage, String time, Integer duration) {
        progressBar.setVisibility(View.VISIBLE);

        if (!sharedPrefManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + sharedPrefManager.getToken();
        MedicationRequest request = new MedicationRequest(name, dosage, time, duration);

        Call<Medication> call = apiService.updateMedication(token, medicationId, request);
        call.enqueue(new Callback<Medication>() {
            @Override
            public void onResponse(Call<Medication> call, Response<Medication> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Medication updated successfully!", Toast.LENGTH_SHORT).show();
                    loadMedications();
                } else {
                    Toast.makeText(requireContext(), "Failed to update medication", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Medication> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(Medication medication) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Medication")
                .setMessage("Are you sure you want to delete " + medication.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteMedication(medication.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMedication(Long medicationId) {
        progressBar.setVisibility(View.VISIBLE);

        if (!sharedPrefManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = "Bearer " + sharedPrefManager.getToken();

        Call<Void> call = apiService.deleteMedication(token, medicationId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Medication deleted successfully!", Toast.LENGTH_SHORT).show();
                    loadMedications();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete medication", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
        loadMedications();
    }
}