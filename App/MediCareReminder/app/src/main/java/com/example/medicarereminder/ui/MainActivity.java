package com.example.medicarereminder.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.medicarereminder.R;
import com.example.medicarereminder.utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    SharedPrefManager prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = SharedPrefManager.getInstance(this);

        // FIXED: Proper way to get NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            return;
        }

        NavController navController = navHostFragment.getNavController();

        // IMPORTANT: delay navigation until UI is ready
        navHostFragment.getViewLifecycleOwnerLiveData()
                .observe(this, owner -> {
                    if (owner != null) {
                        if (prefs.isLoggedIn()) {
                            navController.navigate(R.id.mainNavigationFragment);
                        } else {
                            navController.navigate(R.id.loginFragment);
                        }
                    }
                });
    }
}