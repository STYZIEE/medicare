package com.example.medicarereminder.utils;

import android.content.Context;
import android.util.Log;

import com.example.medicarereminder.model.Medication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    public static void scheduleAllMedicationNotifications(Context context, List<Medication> medications) {
        if (medications == null || medications.isEmpty()) {
            Log.d(TAG, "No medications to schedule");
            return;
        }

        // First, cancel all existing alarms to avoid duplicates
        cancelAllMedicationNotifications(context, medications);

        // Schedule notifications for each medication
        for (Medication medication : medications) {
            if (medication.isNotificationEnabled()) {
                scheduleMedicationNotification(context, medication);
            }
        }

        Log.d(TAG, "Scheduled notifications for " + medications.size() + " medications");
    }

    public static void scheduleMedicationNotification(Context context, Medication medication) {
        if (medication == null || !medication.isNotificationEnabled()) {
            return;
        }

        // Generate unique request code based on medication ID
        int requestCode = medication.getId() != null ?
                medication.getId().intValue() + 1000 :
                (int) (System.currentTimeMillis() % 100000);

        // Convert days array to Calendar format
        boolean[] days = medication.getNotificationDays();
        List<Integer> calendarDays = new ArrayList<>();

        if (days[0]) calendarDays.add(Calendar.SUNDAY);    // Sunday
        if (days[1]) calendarDays.add(Calendar.MONDAY);
        if (days[2]) calendarDays.add(Calendar.TUESDAY);   // Tuesday
        if (days[3]) calendarDays.add(Calendar.WEDNESDAY); // Wednesday
        if (days[4]) calendarDays.add(Calendar.THURSDAY);  // Thursday
        if (days[5]) calendarDays.add(Calendar.FRIDAY);    // Friday
        if (days[6]) calendarDays.add(Calendar.SATURDAY);
        if (calendarDays.isEmpty()) {
            for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
                calendarDays.add(i);
            }
        }
        AlarmUtils.scheduleRepeatingAlarm(
                context,
                medication.getName(),
                medication.getDosage(),
                medication.getNotificationHour(),
                medication.getNotificationMinute(),
                calendarDays,
                requestCode
        );

        Log.d(TAG, "Scheduled notification for: " + medication.getName() +
                " at " + medication.getNotificationHour() + ":" + medication.getNotificationMinute() +
                " (ID: " + requestCode + ")");
    }

    public static void cancelMedicationNotification(Context context, Medication medication) {
        if (medication == null || medication.getId() == null) {
            return;
        }

        int requestCode = medication.getId().intValue() + 1000;
        AlarmUtils.cancelAlarm(context, requestCode);

        Log.d(TAG, "Cancelled notification for: " + medication.getName() + " (ID: " + requestCode + ")");
    }

    public static void cancelAllMedicationNotifications(Context context, List<Medication> medications) {
        if (medications == null) {
            return;
        }

        for (Medication medication : medications) {
            if (medication.getId() != null) {
                int requestCode = medication.getId().intValue() + 1000;
                AlarmUtils.cancelAlarm(context, requestCode);
            }
        }

        Log.d(TAG, "Cancelled all medication notifications");
    }

    public static void toggleMedicationNotification(Context context, Medication medication) {
        if (medication.isNotificationEnabled()) {
            cancelMedicationNotification(context, medication);
            medication.setNotificationEnabled(false);
            Log.d(TAG, "Disabled notifications for: " + medication.getName());
        } else {
            medication.setNotificationEnabled(true);
            scheduleMedicationNotification(context, medication);
            Log.d(TAG, "Enabled notifications for: " + medication.getName());
        }
    }
}