package com.example.medicarereminder.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON") ||
                intent.getAction().equals("com.htc.intent.action.QUICKBOOT_POWERON")) {

            Log.d("BootReceiver", "Device rebooted, restoring alarms");
            restoreAlarms(context);
        }
    }

    private void restoreAlarms(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("medication_alarms", Context.MODE_PRIVATE);
        String savedAlarms = prefs.getString("active_alarms", "[]");

        try {
            JSONArray alarmsArray = new JSONArray(savedAlarms);
            for (int i = 0; i < alarmsArray.length(); i++) {
                JSONObject alarmObj = alarmsArray.getJSONObject(i);

                String medicationName = alarmObj.getString("medication_name");
                String dosage = alarmObj.getString("dosage");
                int hour = alarmObj.getInt("hour");
                int minute = alarmObj.getInt("minute");
                List<Integer> days = new ArrayList<>();

                JSONArray daysArray = alarmObj.getJSONArray("days");
                for (int j = 0; j < daysArray.length(); j++) {
                    days.add(daysArray.getInt(j));
                }

                int requestCode = alarmObj.getInt("request_code");

                // Reschedule the alarm
                AlarmUtils.scheduleRepeatingAlarm(
                        context,
                        medicationName,
                        dosage,
                        hour,
                        minute,
                        days,
                        requestCode
                );
            }

            Log.d("BootReceiver", "Restored " + alarmsArray.length() + " alarms");

        } catch (JSONException e) {
            Log.e("BootReceiver", "Error restoring alarms: " + e.getMessage());
        }
    }
}