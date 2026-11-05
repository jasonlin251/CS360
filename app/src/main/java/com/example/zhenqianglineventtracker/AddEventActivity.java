package com.example.zhenqianglineventtracker;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddEventActivity extends Activity {

    private EditText eventTitleField;
    private EditText eventDateField;
    private EditText eventTimeField;
    private EditText eventDetailsField;
    private Spinner categorySpinner;

    private DatabaseHelper databaseHelper;

    private String username;

    // -1 means that a new event is being created.
    private int eventId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_event);

        // Connect Java variables to the screen.
        eventTitleField = findViewById(R.id.eventTitleField);
        eventDateField = findViewById(R.id.eventDateField);
        eventTimeField = findViewById(R.id.eventTimeField);
        eventDetailsField = findViewById(R.id.eventDetailsField);
        categorySpinner = findViewById(R.id.categorySpinner);

        Button saveButton = findViewById(R.id.saveEventButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        databaseHelper = new DatabaseHelper(this);

        // Get information passed from EventsActivity.
        username = getIntent().getStringExtra("username");

        eventId = getIntent().getIntExtra(
                "eventId",
                -1
        );

        // If an event ID was received, the user is editing an event.
        if (eventId != -1) {
            loadEventForEditing();
            saveButton.setText("Update Event");
        }

        // Save or update the event.
        saveButton.setOnClickListener(view -> saveEvent());

        // Return without saving.
        cancelButton.setOnClickListener(view -> {
            finish();

            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        });
    }

    /**
     * Loads existing event information into the form
     * when the user presses Edit.
     */
    private void loadEventForEditing() {

        String title =
                getIntent().getStringExtra("title");

        String date =
                getIntent().getStringExtra("date");

        String time =
                getIntent().getStringExtra("time");

        String category =
                getIntent().getStringExtra("category");

        String description =
                getIntent().getStringExtra("description");

        eventTitleField.setText(title);
        eventDateField.setText(date);
        eventTimeField.setText(time);
        eventDetailsField.setText(description);

        // Select the saved category in the spinner.
        if (category != null) {

            ArrayAdapter adapter =
                    (ArrayAdapter) categorySpinner.getAdapter();

            for (int i = 0; i < adapter.getCount(); i++) {

                Object item = adapter.getItem(i);

                if (item != null &&
                        category.equalsIgnoreCase(
                                item.toString())) {

                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    /**
     * Validates the form and either creates a new event
     * or updates an existing event.
     */
    private void saveEvent() {

        String title =
                eventTitleField.getText()
                        .toString()
                        .trim();

        String date =
                eventDateField.getText()
                        .toString()
                        .trim();

        String time =
                eventTimeField.getText()
                        .toString()
                        .trim();

        String category =
                categorySpinner.getSelectedItem()
                        .toString();

        String description =
                eventDetailsField.getText()
                        .toString()
                        .trim();

        // Title, date, and time are required.
        if (TextUtils.isEmpty(title)) {

            eventTitleField.setError(
                    "Please enter an event title."
            );

            eventTitleField.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(date)) {

            eventDateField.setError(
                    "Please enter a date."
            );

            eventDateField.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(time)) {

            eventTimeField.setError(
                    "Please enter a time."
            );

            eventTimeField.requestFocus();
            return;
        }

        // A new event is being created.
        if (eventId == -1) {

            boolean added =
                    databaseHelper.addEvent(
                            username,
                            title,
                            date,
                            time,
                            category,
                            description
                    );

            if (added) {

                Toast.makeText(
                        this,
                        "Event saved successfully.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                Toast.makeText(
                        this,
                        "Unable to save event.",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } else {

            // Existing event is being updated.
            boolean updated =
                    databaseHelper.updateEvent(
                            eventId,
                            title,
                            date,
                            time,
                            category,
                            description
                    );

            if (updated) {

                Toast.makeText(
                        this,
                        "Event updated successfully.",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                Toast.makeText(
                        this,
                        "Unable to update event.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}