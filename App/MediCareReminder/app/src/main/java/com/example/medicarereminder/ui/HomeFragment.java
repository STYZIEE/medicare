package com.example.medicarereminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.medicarereminder.R;
import com.example.medicarereminder.utils.SharedPrefManager;

public class HomeFragment extends Fragment {

    private TextView tvWelcome;
    private SharedPrefManager sharedPrefManager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPrefManager = SharedPrefManager.getInstance(requireContext());

        tvWelcome = view.findViewById(R.id.tvWelcome);

        // Display welcome message with username
        String username = sharedPrefManager.getUsername();
        if (username != null) {
            tvWelcome.setText("Welcome, " + username + "!");
        }

        return view;
    }
}