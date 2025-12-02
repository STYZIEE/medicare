package com.example.medicarereminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.medicarereminder.R;
import com.example.medicarereminder.api.ApiService;
import com.example.medicarereminder.api.AuthResponse;
import com.example.medicarereminder.api.RegisterRequest;
import com.example.medicarereminder.api.RetrofitClient;
import com.example.medicarereminder.databinding.FragmentRegisterBinding;
import com.example.medicarereminder.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private SharedPrefManager sharedPrefManager;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize
        apiService = RetrofitClient.getInstance().create(ApiService.class);
        sharedPrefManager = SharedPrefManager.getInstance(requireContext());

        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void registerUser() {
        String username = binding.etUsername.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String dob = binding.etDateOfBirth.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validation
        if (username.isEmpty()) {
            binding.etUsername.setError("Username is required");
            binding.etUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return;
        }

        if (dob.isEmpty()) {
            binding.etDateOfBirth.setError("Date of birth is required");
            binding.etDateOfBirth.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        RegisterRequest request = new RegisterRequest(username, email, dob, password);

        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    if (authResponse.isSuccess()) {
                        // Save user data - FIXED: Added debug logging
                        String token = authResponse.getToken();
                        Long userId = authResponse.getUser().getId();
                        String savedUsername = authResponse.getUser().getUsername();
                        String userEmail = authResponse.getUser().getEmail();

                        System.out.println("=== REGISTRATION SUCCESS ===");
                        System.out.println("Token: " + token);
                        System.out.println("Token length: " + (token != null ? token.length() : "NULL"));
                        System.out.println("User ID: " + userId);
                        System.out.println("Username: " + savedUsername);
                        System.out.println("Email: " + userEmail);
                        System.out.println("=== END REGISTRATION DEBUG ===");

                        sharedPrefManager.saveUserData(token, userId, savedUsername, userEmail);

                        Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainApp();
                    } else {
                        Toast.makeText(getContext(), authResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMessage = "Registration failed!";
                    if (response.code() == 400) {
                        errorMessage = "User already exists or invalid data";
                    } else if (response.code() == 500) {
                        errorMessage = "Server error. Please try again.";
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnRegister.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLogin() {
        Navigation.findNavController(requireView()).navigate(R.id.action_registerFragment_to_loginFragment);
    }

    private void navigateToMainApp() {
        Navigation.findNavController(requireView()).navigate(R.id.action_registerFragment_to_mainNavigationFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}