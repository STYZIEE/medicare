package com.example.medicarereminder.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.medicarereminder.R;
import com.example.medicarereminder.utils.SharedPrefManager;
import com.example.medicarereminder.utils.ToastUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class HomeFragment extends Fragment implements LocationListener {

    // UI Elements
    private TextView tvWelcome;
    private TextView tvLocationStatus;
    private MapView mapView;
    private FloatingActionButton fabMyLocation;
    private ProgressBar progressBar;

    // Map Elements
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;

    // Location Variables
    private SharedPrefManager sharedPrefManager;
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Add these constants for better control
    private static final long MIN_TIME_BETWEEN_UPDATES = 5000; // 5 seconds
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // Current Location
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize
        sharedPrefManager = SharedPrefManager.getInstance(requireContext());
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        // Initialize osmdroid configuration - FIXED: Added this line
        Configuration.getInstance().load(requireContext(),
                requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        // Initialize UI
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvLocationStatus = view.findViewById(R.id.tvLocationStatus);
        mapView = view.findViewById(R.id.mapView);
        fabMyLocation = view.findViewById(R.id.fabMyLocation);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup map
        setupMap();

        // Display welcome message
        String username = sharedPrefManager.getUsername();
        if (username != null && !username.isEmpty()) {
            tvWelcome.setText("Welcome, " + username + "!");
        }

        // Load last saved location if available
        if (sharedPrefManager.hasSavedLocation()) {
            double lastLat = sharedPrefManager.getLastLatitude();
            double lastLon = sharedPrefManager.getLastLongitude();
            // Update status text
            tvLocationStatus.setText(String.format("Last location: %.4f, %.4f", lastLat, lastLon));
        }

        // Set click listener for My Location button
        fabMyLocation.setOnClickListener(v -> requestLocation());

        return view;
    }

    private void setupMap() {
        // Set tile source to OpenStreetMap
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Set zoom controls - FIXED: Added zoom controls setting
        mapView.getZoomController().setVisibility(
                org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
        mapView.setMinZoomLevel(3.0);
        mapView.setMaxZoomLevel(19.0);

        // Get map controller
        mapController = mapView.getController();
        mapController.setZoom(15.0);

        // Set default location (center of map) - FIXED: Check for saved location first
        if (sharedPrefManager.hasSavedLocation()) {
            double lastLat = sharedPrefManager.getLastLatitude();
            double lastLon = sharedPrefManager.getLastLongitude();
            GeoPoint defaultPoint = new GeoPoint(lastLat, lastLon);
            mapController.setCenter(defaultPoint);
        } else {
            GeoPoint defaultPoint = new GeoPoint(33.8938, 35.5018); // Default location
            mapController.setCenter(defaultPoint);
        }

        // Initialize location overlay - FIXED: Added proper initialization
        myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()), mapView);
        myLocationOverlay.setEnabled(false);
        mapView.getOverlays().add(myLocationOverlay);

        // Enable follow location - FIXED: Added this for better UX
        myLocationOverlay.setDrawAccuracyEnabled(true);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocation() {
        if (!checkLocationPermission()) {
            // Request permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Permission already granted, get location
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        // Check if location services are enabled
        if (!isLocationEnabled()) {
            showSafeToast("Please enable location services in your device settings");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvLocationStatus.setText("Getting your location...");

        try {
            // Enable location overlay on map - FIXED: This must come before requesting updates
            if (myLocationOverlay != null) {
                myLocationOverlay.setEnabled(true);
                myLocationOverlay.enableMyLocation();
                myLocationOverlay.enableFollowLocation();
                myLocationOverlay.setDrawAccuracyEnabled(true);
            }

            // Get last known location first (fastest)
            Location lastLocation = getLastKnownLocation();
            if (lastLocation != null) {
                updateLocation(lastLocation);
            }

            // Request fresh location updates
            if (checkLocationPermission()) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BETWEEN_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );
                }

                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BETWEEN_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );
                }
            }

        } catch (SecurityException e) {
            showSafeToast("Location permission denied");
            progressBar.setVisibility(View.GONE);
            tvLocationStatus.setText("Location: Permission required");
        } catch (Exception e) {
            showSafeToast("Error: " + e.getMessage());
            progressBar.setVisibility(View.GONE);
            tvLocationStatus.setText("Location: Error occurred");
        }
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private Location getLastKnownLocation() {
        Location bestLocation = null;

        try {
            if (checkLocationPermission()) {
                // Try GPS first
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                // Try network
                Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                // Use the most recent location
                if (gpsLocation != null && networkLocation != null) {
                    if (gpsLocation.getTime() > networkLocation.getTime()) {
                        bestLocation = gpsLocation;
                    } else {
                        bestLocation = networkLocation;
                    }
                } else if (gpsLocation != null) {
                    bestLocation = gpsLocation;
                } else if (networkLocation != null) {
                    bestLocation = networkLocation;
                }
            }
        } catch (Exception e) {
            // Ignore
        }

        return bestLocation;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location != null) {
            updateLocation(location);
        }
    }

    private void updateLocation(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        // Update UI on main thread
        requireActivity().runOnUiThread(() -> {
            String locationText = String.format("Location: %.6f, %.6f",
                    currentLatitude, currentLongitude);
            tvLocationStatus.setText(locationText);

            // Move map to location
            moveMapToLocation(currentLatitude, currentLongitude);

            // Save to shared preferences
            sharedPrefManager.saveLastLocation(currentLatitude, currentLongitude);

            progressBar.setVisibility(View.GONE);
        });
    }

    private void moveMapToLocation(double latitude, double longitude) {
        if (mapController != null) {
            GeoPoint geoPoint = new GeoPoint(latitude, longitude);
            mapController.animateTo(geoPoint);

            // Only zoom if we're at default zoom
            if (mapView.getZoomLevelDouble() < 16.0) {
                mapController.setZoom(16.0);
            }

            // Enable follow location on the overlay
            if (myLocationOverlay != null) {
                myLocationOverlay.enableFollowLocation();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                            (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED))) {
                // Permission granted
                getCurrentLocation();
            } else {
                // Permission denied
                showSafeToast("Location permission is required to show your location");
                tvLocationStatus.setText("Location: Permission denied");
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        stopLocationUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLocationUpdates();
        if (mapView != null) {
            mapView.onDetach();
        }
    }

    private void stopLocationUpdates() {
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
            if (myLocationOverlay != null) {
                myLocationOverlay.disableFollowLocation();
                myLocationOverlay.disableMyLocation();
                myLocationOverlay.setEnabled(false);
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    // Required LocationListener methods
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle provider status changes if needed
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        requireActivity().runOnUiThread(() -> showSafeToast(provider + " enabled"));
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        requireActivity().runOnUiThread(() -> showSafeToast(provider + " disabled"));
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