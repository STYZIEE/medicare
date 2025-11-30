package com.example.medicarereminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.medicarereminder.R;
import com.example.medicarereminder.api.ApiService;
import com.example.medicarereminder.api.AuthResponse;
import com.example.medicarereminder.api.LoginRequest;
import com.example.medicarereminder.api.RetrofitClient;
import com.example.medicarereminder.utils.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    private ApiService apiService;
    private SharedPrefManager sharedPrefManager;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize
        apiService = RetrofitClient.getInstance().create(ApiService.class);
        sharedPrefManager = SharedPrefManager.getInstance(requireContext());

        // Initialize views
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegister = view.findViewById(R.id.tvRegister);
        progressBar = view.findViewById(R.id.progressBar);

        // Set up click listeners
        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> navigateToRegister());

        return view;
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Create login request
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Make API call
        Call<AuthResponse> call = apiService.login(loginRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    if (authResponse.isSuccess()) {
                        // Save user data - FIXED: Added debug logging
                        String token = authResponse.getToken();
                        Long userId = authResponse.getUser().getId();
                        String username = authResponse.getUser().getUsername();
                        String userEmail = authResponse.getUser().getEmail();

                        System.out.println("=== LOGIN SUCCESS ===");
                        System.out.println("Token: " + token);
                        System.out.println("Token length: " + (token != null ? token.length() : "NULL"));
                        System.out.println("User ID: " + userId);
                        System.out.println("Username: " + username);
                        System.out.println("Email: " + userEmail);
                        System.out.println("=== END LOGIN DEBUG ===");

                        sharedPrefManager.saveUserData(token, userId, username, userEmail);

                        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainApp();
                    } else {
                        Toast.makeText(requireContext(), authResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMessage = "Login failed. Please try again.";
                    if (response.code() == 401) {
                        errorMessage = "Invalid email or password";
                    } else if (response.code() == 400) {
                        errorMessage = "Invalid request format";
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToRegister() {
        // Navigate to RegisterFragment using Navigation Component
        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_registerFragment);
    }

    private void navigateToMainApp() {
        // Navigate to Main Navigation Fragment
        Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_mainNavigationFragment);
    }
}