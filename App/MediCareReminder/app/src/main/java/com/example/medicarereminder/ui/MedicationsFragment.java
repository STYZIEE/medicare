package com.example.medicarereminder.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.example.medicarereminder.utils.NotificationHelper;
import com.example.medicarereminder.utils.NotificationUtils;
import com.example.medicarereminder.utils.SharedPrefManager;
import com.example.medicarereminder.utils.ToastUtils;
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

    // Notification time
    private int notificationHour = 8;
    private int notificationMinute = 0;

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

            @Override
            public void onToggleNotificationClick(int position) {
                Medication medication = medications.get(position);
                NotificationHelper.toggleMedicationNotification(requireContext(), medication);
                ToastUtils.showToast(requireContext(),
                        medication.isNotificationEnabled() ?
                                "Notifications ON for " + medication.getName() :
                                "Notifications OFF for " + medication.getName());
                adapter.notifyItemChanged(position);
            }
        });

        // Check notification permission for Android 13+
        if (!NotificationUtils.checkNotificationPermission(requireContext())) {
            NotificationUtils.requestNotificationPermission(requireActivity());
        }

        // Load medications
        loadMedications();
    }

    private void loadMedications() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        if (!sharedPrefManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.showToast(requireContext(), "Please login first");
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
                    ToastUtils.showToast(requireContext(), "Loaded " + medications.size() + " medications");

                    // Schedule notifications for all loaded medications
                    NotificationHelper.scheduleAllMedicationNotifications(requireContext(), medications);
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

                    ToastUtils.showToast(requireContext(), errorMessage);
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Medication>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Load medications network error", t);
                ToastUtils.showToast(requireContext(), "Network error: " + t.getMessage());
                updateEmptyState();
            }
        });
    }

    private void showAddMedicationDialog() {
        // Use existing dialog layout but with enhanced options
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medication, null);

        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etDosage = dialogView.findViewById(R.id.etDosage);
        TextInputEditText etTime = dialogView.findViewById(R.id.etTime);
        TextInputEditText etDuration = dialogView.findViewById(R.id.etDuration);
        Button btnSetNotificationTime = dialogView.findViewById(R.id.btnSetNotificationTime);
        LinearLayout daysContainer = dialogView.findViewById(R.id.daysContainer);

        // Initialize days checkboxes (default all checked)
        CheckBox[] dayCheckboxes = new CheckBox[7];
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (int i = 0; i < dayCheckboxes.length; i++) {
            dayCheckboxes[i] = new CheckBox(requireContext());
            dayCheckboxes[i].setText(dayNames[i]);
            dayCheckboxes[i].setChecked(true); // Default all days
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
            );
            params.setMargins(2, 2, 2, 2);
            dayCheckboxes[i].setLayoutParams(params);
            daysContainer.addView(dayCheckboxes[i]);
        }

        // Set notification time button
        btnSetNotificationTime.setText(String.format("Notification: %02d:%02d", notificationHour, notificationMinute));
        btnSetNotificationTime.setOnClickListener(v -> showTimePicker());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Medication")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String dosage = etDosage.getText().toString().trim();
                    String time = etTime.getText().toString().trim();
                    String durationStr = etDuration.getText().toString().trim();

                    if (name.isEmpty()) {
                        ToastUtils.showToast(requireContext(), "Medication name is required");
                        return;
                    }
                    if (time.isEmpty()) {
                        ToastUtils.showToast(requireContext(), "Time is required");
                        return;
                    }

                    Integer duration = null;
                    if (!durationStr.isEmpty()) {
                        try {
                            duration = Integer.parseInt(durationStr);
                        } catch (NumberFormatException e) {
                            ToastUtils.showToast(requireContext(), "Please enter a valid number for duration");
                            return;
                        }
                    }

                    addMedication(name, dosage, time, duration, dayCheckboxes);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    notificationHour = hourOfDay;
                    notificationMinute = minute;
                },
                notificationHour,
                notificationMinute,
                true
        );
        timePickerDialog.setTitle("Set Notification Time");
        timePickerDialog.show();
    }

    private void addMedication(String name, String dosage, String time, Integer duration, CheckBox[] dayCheckboxes) {
        progressBar.setVisibility(View.VISIBLE);

        if (!sharedPrefManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.showToast(requireContext(), "Please login first");
            return;
        }

        String token = "Bearer " + sharedPrefManager.getToken();
        MedicationRequest request = new MedicationRequest(name, dosage, time, duration);

        Log.d(TAG, "Adding medication: " + name);

        Call<Medication> call = apiService.addMedication(token, request);
        call.enqueue(new Callback<Medication>() {
            @Override
            public void onResponse(Call<Medication> call, Response<Medication> response) {
                progressBar.setVisibility(View.GONE);

                Log.d(TAG, "Add medication response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Medication addedMedication = response.body();
                    Log.d(TAG, "Medication added successfully: " + addedMedication.getName());

                    // Set local notification settings (not sent to backend)
                    addedMedication.setNotificationEnabled(true);
                    addedMedication.setNotificationHour(notificationHour);
                    addedMedication.setNotificationMinute(notificationMinute);

                    // Set days from checkboxes
                    for (int i = 0; i < dayCheckboxes.length; i++) {
                        addedMedication.setNotificationDay(i, dayCheckboxes[i].isChecked());
                    }

                    // Schedule notification locally
                    NotificationHelper.scheduleMedicationNotification(requireContext(), addedMedication);

                    ToastUtils.showToast(requireContext(), "Medication added with notifications!");
                    loadMedications(); // Reload to get fresh list
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

                    ToastUtils.showToast(requireContext(), errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Medication> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Add medication network error", t);
                ToastUtils.showToast(requireContext(), "Network error: " + t.getMessage());
            }
        });
    }

    private void showEditMedicationDialog(Medication medication) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_medication, null);

        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etDosage = dialogView.findViewById(R.id.etDosage);
        TextInputEditText etTime = dialogView.findViewById(R.id.etTime);
        TextInputEditText etDuration = dialogView.findViewById(R.id.etDuration);
        Button btnSetNotificationTime = dialogView.findViewById(R.id.btnSetNotificationTime);
        LinearLayout daysContainer = dialogView.findViewById(R.id.daysContainer);

        // Pre-fill with existing data
        etName.setText(medication.getName());
        etDosage.setText(medication.getDosage());
        etTime.setText(medication.getTime());
        if (medication.getDuration() != null && medication.getDuration() > 0) {
            etDuration.setText(String.valueOf(medication.getDuration()));
        }

        // Set notification time from medication
        notificationHour = medication.getNotificationHour();
        notificationMinute = medication.getNotificationMinute();
        btnSetNotificationTime.setText(String.format("Notification: %02d:%02d", notificationHour, notificationMinute));
        btnSetNotificationTime.setOnClickListener(v -> showTimePicker());

        // Initialize days checkboxes with current selection
        CheckBox[] dayCheckboxes = new CheckBox[7];
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (int i = 0; i < dayCheckboxes.length; i++) {
            dayCheckboxes[i] = new CheckBox(requireContext());
            dayCheckboxes[i].setText(dayNames[i]);
            dayCheckboxes[i].setChecked(medication.getNotificationDays()[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
            );
            params.setMargins(2, 2, 2, 2);
            dayCheckboxes[i].setLayoutParams(params);
            daysContainer.addView(dayCheckboxes[i]);
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
                        ToastUtils.showToast(requireContext(), "Medication name is required");
                        return;
                    }
                    if (time.isEmpty()) {
                        ToastUtils.showToast(requireContext(), "Time is required");
                        return;
                    }

                    Integer duration = null;
                    if (!durationStr.isEmpty()) {
                        try {
                            duration = Integer.parseInt(durationStr);
                        } catch (NumberFormatException e) {
                            ToastUtils.showToast(requireContext(), "Please enter a valid number for duration");
                            return;
                        }
                    }

                    // Cancel old notification
                    NotificationHelper.cancelMedicationNotification(requireContext(), medication);

                    // Update medication on backend
                    updateMedication(medication.getId(), name, dosage, time, duration, dayCheckboxes);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateMedication(Long medicationId, String name, String dosage, String time, Integer duration, CheckBox[] dayCheckboxes) {
        progressBar.setVisibility(View.VISIBLE);

        if (!sharedPrefManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.showToast(requireContext(), "Please login first");
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
                    Medication updatedMedication = response.body();

                    // Update local notification settings
                    updatedMedication.setNotificationEnabled(true);
                    updatedMedication.setNotificationHour(notificationHour);
                    updatedMedication.setNotificationMinute(notificationMinute);

                    // Set days from checkboxes
                    for (int i = 0; i < dayCheckboxes.length; i++) {
                        updatedMedication.setNotificationDay(i, dayCheckboxes[i].isChecked());
                    }

                    // Schedule new notification
                    NotificationHelper.scheduleMedicationNotification(requireContext(), updatedMedication);

                    ToastUtils.showToast(requireContext(), "Medication updated with notifications!");
                    loadMedications();
                } else {
                    ToastUtils.showToast(requireContext(), "Failed to update medication");
                }
            }

            @Override
            public void onFailure(Call<Medication> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtils.showToast(requireContext(), "Network error: " + t.getMessage());
            }
        });
    }

    private void showDeleteConfirmationDialog(Medication medication) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Medication")
                .setMessage("Are you sure you want to delete " + medication.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Cancel notification first
                    NotificationHelper.cancelMedicationNotification(requireContext(), medication);
                    deleteMedication(medication.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMedication(Long medicationId) {
        progressBar.setVisibility(View.VISIBLE);

        if (!sharedPrefManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            ToastUtils.showToast(requireContext(), "Please login first");
            return;
        }

        String token = "Bearer " + sharedPrefManager.getToken();

        Call<Void> call = apiService.deleteMedication(token, medicationId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    ToastUtils.showToast(requireContext(), "Medication deleted successfully!");
                    loadMedications();
                } else {
                    ToastUtils.showToast(requireContext(), "Failed to delete medication");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                ToastUtils.showToast(requireContext(), "Network error: " + t.getMessage());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
}
}