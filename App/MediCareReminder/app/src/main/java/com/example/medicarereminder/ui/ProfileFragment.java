package com.example.medicarereminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.medicarereminder.R;
import com.example.medicarereminder.utils.SharedPrefManager;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvEmail;
    private Button btnLogout;
    private SharedPrefManager sharedPrefManager;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPrefManager = SharedPrefManager.getInstance(requireContext());

        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Display user info
        displayUserInfo();

        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void displayUserInfo() {
        String username = sharedPrefManager.getUsername();
        String email = sharedPrefManager.getEmail();

        if (username != null) {
            tvUsername.setText("Username: " + username);
        } else {
            tvUsername.setText("Username: Not available");
        }

        if (email != null) {
            tvEmail.setText("Email: " + email);
        } else {
            tvEmail.setText("Email: Not available");
        }
    }

    private void logoutUser() {
        sharedPrefManager.clearUserData();
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to login - let MainActivity handle the navigation
        requireActivity().finish();
        requireActivity().startActivity(requireActivity().getIntent());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh user info when fragment becomes visible
        displayUserInfo();
    }
}