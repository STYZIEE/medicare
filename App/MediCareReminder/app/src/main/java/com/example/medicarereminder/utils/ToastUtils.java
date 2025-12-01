package com.example.medicarereminder.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtils {

    private static Toast currentToast;

    // Safe way to show Toast (handles null context and uses application context)
    public static void showToast(Context context, String message) {
        if (context == null) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                // Cancel previous toast if exists
                if (currentToast != null) {
                    currentToast.cancel();
                }

                // Use application context to avoid memory leaks
                currentToast = Toast.makeText(context.getApplicationContext(),
                        message, Toast.LENGTH_SHORT);
                currentToast.show();
            } catch (Exception e) {
                // Ignore Toast errors - don't crash the app
                e.printStackTrace();
            }
        });
    }

    // Show long toast
    public static void showLongToast(Context context, String message) {
        if (context == null) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                if (currentToast != null) {
                    currentToast.cancel();
                }

                currentToast = Toast.makeText(context.getApplicationContext(),
                        message, Toast.LENGTH_LONG);
                currentToast.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}