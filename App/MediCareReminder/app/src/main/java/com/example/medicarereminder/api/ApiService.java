package com.example.medicarereminder.api;

import com.example.medicarereminder.model.Medication;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // Auth endpoints
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    // Medication endpoints
    @GET("medications")
    Call<List<Medication>> getMedications(@Header("Authorization") String token);

    @POST("medications")
    Call<Medication> addMedication(@Header("Authorization") String token, @Body MedicationRequest request);

    // ADD UPDATE AND DELETE ENDPOINTS
    @PUT("medications/{medicationId}")
    Call<Medication> updateMedication(
            @Header("Authorization") String token,
            @Path("medicationId") Long medicationId,
            @Body MedicationRequest request);

    @DELETE("medications/{medicationId}")
    Call<Void> deleteMedication(
            @Header("Authorization") String token,
            @Path("medicationId") Long medicationId);
}