package com.example.medicarereminder.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.medicarereminder.R;
import com.example.medicarereminder.utils.SharedPrefManager;
import com.example.medicarereminder.utils.ToastUtils;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    // UI Elements
    private CircleImageView profileImage;
    private TextView tvUsername, tvEmail;
    private Button btnLogout, btnRemovePhoto;
    private View btnChangePhoto;
    private SharedPrefManager sharedPrefManager;

    // Activity result launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sharedPrefManager = SharedPrefManager.getInstance(requireContext());

        // Initialize UI
        profileImage = view.findViewById(R.id.profile_image);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnRemovePhoto = view.findViewById(R.id.btnRemovePhoto);
        btnChangePhoto = view.findViewById(R.id.btn_change_photo);

        // Display user info
        displayUserInfo();

        // Set up click listeners
        btnLogout.setOnClickListener(v -> logoutUser());
        btnRemovePhoto.setOnClickListener(v -> removeProfilePicture());
        btnChangePhoto.setOnClickListener(v -> showImagePickerOptions());

        // Initialize activity result launchers
        initializeLaunchers();

        return view;
    }

    private void initializeLaunchers() {
        // Permission launcher for gallery - supports multiple permissions
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    Boolean readMediaImagesGranted = permissions.get(getImagePermission());
                    if (readMediaImagesGranted != null && readMediaImagesGranted) {
                        openGallery();
                    } else {
                        showSafeToast("Storage permission is required to select photos");
                    }
                }
        );

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        handleGalleryResult(result.getData());
                    }
                }
        );
    }

    private String getImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            return Manifest.permission.READ_MEDIA_IMAGES;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 (API 29-32) - no permission needed for gallery if using ACTION_PICK
            return null;
        } else {
            // Android 9 and below (API 28 and below)
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }

    private void displayUserInfo() {
        // Display username and email (keep original)
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

        // Load profile picture
        loadProfilePicture();

        // Show/hide remove photo button
        btnRemovePhoto.setVisibility(sharedPrefManager.hasProfilePicture() ?
                View.VISIBLE : View.GONE);
    }

    private void loadProfilePicture() {
        String profilePictureUri = sharedPrefManager.getProfilePictureUri();
        if (profilePictureUri != null && !profilePictureUri.isEmpty()) {
            try {
                // Load image using Glide
                Glide.with(requireContext())
                        .load(Uri.parse(profilePictureUri))
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(profileImage);
            } catch (Exception e) {
                // If error, show default
                profileImage.setImageResource(R.drawable.default_profile);
            }
        } else {
            profileImage.setImageResource(R.drawable.default_profile);
        }
    }

    private void showImagePickerOptions() {
        // Simple: just open gallery for now
        checkGalleryPermission();
    }

    private void checkGalleryPermission() {
        String permission = getImagePermission();

        // Android 10+ (API 29+) doesn't need READ_EXTERNAL_STORAGE for gallery picker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openGallery();
        } else if (permission != null && ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted for Android 9 and below
            openGallery();
        } else if (permission != null) {
            // Request permission for Android 9 and below
            permissionLauncher.launch(new String[]{permission});
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        try {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickPhotoIntent.setType("image/*");
            galleryLauncher.launch(pickPhotoIntent);
        } catch (Exception e) {
            showSafeToast("Error opening gallery: " + e.getMessage());
        }
    }

    private void handleGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        if (selectedImageUri != null) {
            try {
                // Save the URI
                sharedPrefManager.saveProfilePictureUri(selectedImageUri.toString());

                // Display the photo
                Glide.with(requireContext())
                        .load(selectedImageUri)
                        .placeholder(R.drawable.default_profile)
                        .into(profileImage);

                // Show remove button
                btnRemovePhoto.setVisibility(View.VISIBLE);

                showSafeToast("Profile picture updated!");
            } catch (Exception e) {
                showSafeToast("Error loading image");
            }
        }
    }

    private void removeProfilePicture() {
        // Clear the saved URI
        sharedPrefManager.clearProfilePicture();

        // Reset to default image
        profileImage.setImageResource(R.drawable.default_profile);

        // Hide remove button
        btnRemovePhoto.setVisibility(View.GONE);

        showSafeToast("Profile picture removed");
    }

    private void logoutUser() {
        // Clear all user data including profile picture
        sharedPrefManager.clearUserData();
        sharedPrefManager.clearProfilePicture();
        sharedPrefManager.clearLocationData();

        showSafeToast("Logged out successfully");

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

    // Safe Toast method that won't crash
    private void showSafeToast(String message) {
        Context context = getContext();
        if (context == null || getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                ToastUtils.showToast(context, message);
            } catch (Exception e) {
                // Ignore Toast errors
            }
        });
    }
}