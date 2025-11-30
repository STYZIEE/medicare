package com.example.medicarereminder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.medicarereminder.R;
import com.example.medicarereminder.databinding.FragmentMainNavigationBinding;
import com.example.medicarereminder.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainNavigationFragment extends Fragment {

    private FragmentMainNavigationBinding binding;
    private SharedPrefManager sharedPrefManager;

    public MainNavigationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainNavigationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPrefManager = SharedPrefManager.getInstance(requireContext());

        // 🔥 FIX: Delay login check to avoid NavController crash
        view.post(() -> {
            if (!sharedPrefManager.isLoggedIn()) {
                NavController parentNavController =
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

                parentNavController.navigate(R.id.loginFragment);
                return;
            }

            // Only setup bottom navigation AFTER login check
            setupBottomNavigation();
        });
    }

    private void setupBottomNavigation() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.nav_host_fragment_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNav = binding.bottomNavigation;

            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
