package com.example.zhenqianglineventtracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SmsPermissionActivity extends Activity {

    private static final int SMS_PERMISSION_REQUEST = 1001;

    private static final String PREFS_NAME = "sms_preferences";
    private static final String PHONE_KEY = "phone_number";

    private TextView permissionStatus;
    private EditText phoneNumberField;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        // Connect Java variables to the XML layout.
        permissionStatus = findViewById(R.id.permissionStatus);
        phoneNumberField = findViewById(R.id.phoneNumberField);

        Button allowButton = findViewById(R.id.allowSmsButton);
        Button sendTestSmsButton = findViewById(R.id.sendTestSmsButton);
        Button notNowButton = findViewById(R.id.notNowButton);

        // SharedPreferences keeps the phone number saved
        // after the application is closed.
        preferences = getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        String savedPhoneNumber =
                preferences.getString(PHONE_KEY, "");

        phoneNumberField.setText(savedPhoneNumber);

        // Show the current permission state.
        updatePermissionStatus();

        // Ask Android for SMS permission.
        allowButton.setOnClickListener(view ->
                requestSmsPermission()
        );

        // Send a test event reminder.
        sendTestSmsButton.setOnClickListener(view ->
                sendTestSms()
        );

        // The user can deny SMS permission and continue
        // using the rest of the application.
        notNowButton.setOnClickListener(view -> {

            permissionStatus.setText(
                    R.string.sms_denied_status
            );

            permissionStatus.setContentDescription(
                    getString(R.string.sms_denied_status)
            );

            Toast.makeText(
                    SmsPermissionActivity.this,
                    "SMS notifications are disabled. The rest of the app will still work.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
        });
    }

    /**
     * Requests SEND_SMS permission from the user.
     */
    private void requestSmsPermission() {

        if (checkSelfPermission(Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {

            updatePermissionStatus();

            Toast.makeText(
                    this,
                    "SMS permission is already enabled.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        requestPermissions(
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_REQUEST
        );
    }

    /**
     * Updates the permission message displayed on screen.
     */
    private void updatePermissionStatus() {

        boolean permissionGranted =
                checkSelfPermission(Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED;

        int statusText;

        if (permissionGranted) {

            statusText = R.string.sms_allowed_status;

        } else {

            statusText = R.string.sms_off_status;
        }

        permissionStatus.setText(statusText);

        permissionStatus.setContentDescription(
                getString(statusText)
        );
    }

    /**
     * Saves the phone number and sends a test
     * event reminder by SMS.
     */
    private void sendTestSms() {

        String phoneNumber =
                phoneNumberField.getText()
                        .toString()
                        .trim();

        // Make sure a phone number was entered.
        if (TextUtils.isEmpty(phoneNumber)) {

            phoneNumberField.setError(
                    "Please enter a phone number."
            );

            phoneNumberField.requestFocus();

            return;
        }

        // SMS cannot be sent unless permission was granted.
        if (checkSelfPermission(Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(
                    this,
                    "Please allow SMS permission first.",
                    Toast.LENGTH_SHORT
            ).show();

            requestSmsPermission();

            return;
        }

        // Save the user's phone number.
        preferences.edit()
                .putString(PHONE_KEY, phoneNumber)
                .apply();

        // Check whether this device supports SMS messaging.
        boolean smsSupported =
                getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_TELEPHONY_MESSAGING
                );

        if (!smsSupported) {

            Toast.makeText(
                    this,
                    "SMS messaging is not supported on this emulator or device.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String message =
                "Event Tracker reminder: You have an upcoming event. " +
                        "Open the Event Tracking app to review your schedule.";

        try {

            // Get Android's SMS service.
            SmsManager smsManager =
                    getSystemService(SmsManager.class);

            if (smsManager == null) {

                Toast.makeText(
                        this,
                        "SMS service is unavailable.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Send the event reminder.
            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
            );

            Toast.makeText(
                    this,
                    "Event reminder SMS sent.",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception exception) {

            Toast.makeText(
                    this,
                    "Unable to send SMS: " +
                            exception.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    /**
     * Handles the user's response to the
     * Android SMS permission dialog.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == SMS_PERMISSION_REQUEST) {

            boolean permissionGranted =
                    grantResults.length > 0
                            && grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

            if (permissionGranted) {

                permissionStatus.setText(
                        R.string.sms_allowed_status
                );

                permissionStatus.setContentDescription(
                        getString(R.string.sms_allowed_status)
                );

                Toast.makeText(
                        this,
                        "SMS notifications enabled.",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                permissionStatus.setText(
                        R.string.sms_denied_status
                );

                permissionStatus.setContentDescription(
                        getString(R.string.sms_denied_status)
                );

                Toast.makeText(
                        this,
                        "SMS permission denied. The rest of the app will continue to work.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }
}