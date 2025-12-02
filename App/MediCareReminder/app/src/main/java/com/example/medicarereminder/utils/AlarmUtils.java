package com.example.medicarereminder.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

public class AlarmUtils {

    public static void scheduleRepeatingAlarm(Context context,
                                              String medicationName,
                                              String dosage,
                                              int hour,
                                              int minute,
                                              List<Integer> daysOfWeek,
                                              int requestCode) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("medication_name", medicationName);
        alarmIntent.putExtra("dosage", dosage);
        alarmIntent.putExtra("request_code", requestCode);

        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context,
                    requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context,
                    requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }

        Log.d("AlarmUtils", "Alarm scheduled for " + hour + ":" + minute +
                " (Medication: " + medicationName + ")");
        saveAlarmToPreferences(context, medicationName, dosage, hour, minute, daysOfWeek, requestCode);
    }

    public static void cancelAlarm(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getBroadcast(context,
                    requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(context,
                    requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d("AlarmUtils", "Alarm cancelled with request code: " + requestCode);
        removeAlarmFromPreferences(context, requestCode);
    }

    private static void saveAlarmToPreferences(Context context,
                                               String medicationName,
                                               String dosage,
                                               int hour,
                                               int minute,
                                               List<Integer> daysOfWeek,
                                               int requestCode) {

        SharedPreferences prefs = context.getSharedPreferences("medication_alarms", Context.MODE_PRIVATE);
        String savedAlarms = prefs.getString("active_alarms", "[]");

        try {
            JSONArray alarmsArray = new JSONArray(savedAlarms);
            JSONObject alarmObj = new JSONObject();

            alarmObj.put("medication_name", medicationName);
            alarmObj.put("dosage", dosage);
            alarmObj.put("hour", hour);
            alarmObj.put("minute", minute);
            alarmObj.put("request_code", requestCode);

            JSONArray daysArray = new JSONArray();
            for (Integer day : daysOfWeek) {
                daysArray.put(day);
            }
            alarmObj.put("days", daysArray);

            alarmsArray.put(alarmObj);

            prefs.edit().putString("active_alarms", alarmsArray.toString()).apply();

        } catch (JSONException e) {
            Log.e("AlarmUtils", "Error saving alarm: " + e.getMessage());
        }
    }

    private static void removeAlarmFromPreferences(Context context, int requestCode) {
        SharedPreferences prefs = context.getSharedPreferences("medication_alarms", Context.MODE_PRIVATE);
        String savedAlarms = prefs.getString("active_alarms", "[]");

        try {
            JSONArray alarmsArray = new JSONArray(savedAlarms);
            JSONArray newAlarmsArray = new JSONArray();

            for (int i = 0; i < alarmsArray.length(); i++) {
                JSONObject alarmObj = alarmsArray.getJSONObject(i);
                int savedRequestCode = alarmObj.getInt("request_code");

                if (savedRequestCode != requestCode) {
                    newAlarmsArray.put(alarmObj);
                }
            }

            prefs.edit().putString("active_alarms", newAlarmsArray.toString()).apply();

        } catch (JSONException e) {
            Log.e("AlarmUtils", "Error removing alarm: " + e.getMessage());
        }
    }

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.app.NotificationManager notificationManager =
                    context.getSystemService(android.app.NotificationManager.class);
            return notificationManager.areNotificationsEnabled();
        }
        return true;
    }
}