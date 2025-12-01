package com.example.medicarereminder.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "medicare_shared_pref";
    private static final String KEY_TOKEN = "key_token";
    private static final String KEY_USER_ID = "key_user_id";
    private static final String KEY_USERNAME = "key_username";
    private static final String KEY_EMAIL = "key_email";

    // Location keys
    private static final String KEY_LAST_LATITUDE = "last_latitude";
    private static final String KEY_LAST_LONGITUDE = "last_longitude";
    private static final String KEY_LAST_LOCATION_TIME = "last_location_time";

    // Profile picture key
    private static final String KEY_PROFILE_PICTURE_URI = "profile_picture_uri";

    private static SharedPrefManager instance;
    private Context context;

    private SharedPrefManager(Context ctx) {
        this.context = ctx.getApplicationContext(); // Use application context to avoid memory leaks
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    // Save user data after login
    public void saveUserData(String token, Long userId, String username, String email) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_TOKEN, token);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);

        editor.apply();
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null) != null;
    }

    // Get stored token
    public String getToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    // Get user ID
    public Long getUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }

    // Get username
    public String getUsername() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    // Get email
    public String getEmail() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    // Clear user data on logout
    public void clearUserData() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // ============ PROFILE PICTURE METHODS ============

    // Save profile picture URI
    public void saveProfilePictureUri(String uriString) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PROFILE_PICTURE_URI, uriString);
        editor.apply();
    }

    // Get profile picture URI
    public String getProfilePictureUri() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_PROFILE_PICTURE_URI, null);
    }

    // Check if profile picture exists
    public boolean hasProfilePicture() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_PROFILE_PICTURE_URI, null) != null;
    }

    // Clear profile picture
    public void clearProfilePicture() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_PROFILE_PICTURE_URI);
        editor.apply();
    }

    // ============ LOCATION METHODS ============

    // Save last known location
    public void saveLastLocation(double latitude, double longitude) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat(KEY_LAST_LATITUDE, (float) latitude);
        editor.putFloat(KEY_LAST_LONGITUDE, (float) longitude);
        editor.putLong(KEY_LAST_LOCATION_TIME, System.currentTimeMillis());

        editor.apply();
    }

    // Get last known location as float array [latitude, longitude]
    public float[] getLastLocation() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        float latitude = sharedPreferences.getFloat(KEY_LAST_LATITUDE, 0);
        float longitude = sharedPreferences.getFloat(KEY_LAST_LONGITUDE, 0);
        return new float[]{latitude, longitude};
    }

    // Get last location timestamp
    public long getLastLocationTime() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(KEY_LAST_LOCATION_TIME, 0);
    }

    // Check if we have a saved location
    public boolean hasSavedLocation() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        float latitude = sharedPreferences.getFloat(KEY_LAST_LATITUDE, 0);
        float longitude = sharedPreferences.getFloat(KEY_LAST_LONGITUDE, 0);
        return latitude != 0 && longitude != 0;
    }

    // Get last latitude
    public double getLastLatitude() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(KEY_LAST_LATITUDE, 0);
    }

    // Get last longitude
    public double getLastLongitude() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(KEY_LAST_LONGITUDE, 0);
    }

    // Clear location data
    public void clearLocationData() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(KEY_LAST_LATITUDE);
        editor.remove(KEY_LAST_LONGITUDE);
        editor.remove(KEY_LAST_LOCATION_TIME);

        editor.apply();
    }
}