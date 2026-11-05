package com.example.zhenqianglineventtracker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class EventsActivity extends Activity {

    private DatabaseHelper databaseHelper;
    private TableLayout eventsTable;
    private TextView eventCountText;
    private TextView eventCountBadge;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        // Connect to the SQLite database
        databaseHelper = new DatabaseHelper(this);

        // Get the username passed from LoginActivity
        username = getIntent().getStringExtra("username");

        if (username == null) {
            username = "";
        }

        // Connect Java variables to XML views
        TextView welcomeText = findViewById(R.id.welcomeText);
        eventCountText = findViewById(R.id.eventCountText);
        eventCountBadge = findViewById(R.id.eventCountBadge);
        eventsTable = findViewById(R.id.eventsTable);

        Button addEventButton = findViewById(R.id.addEventButton);
        Button smsSettingsButton = findViewById(R.id.smsSettingsButton);

        // Display the logged-in username
        if (!username.trim().isEmpty()) {
            welcomeText.setText(
                    getString(R.string.welcome_user, username)
            );
        }

        // Open the Add Event screen
        addEventButton.setOnClickListener(view -> {

            Intent intent = new Intent(
                    EventsActivity.this,
                    AddEventActivity.class
            );

            // Pass username so the event belongs to this user
            intent.putExtra("username", username);

            startActivity(intent);

            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        });

        // Open the SMS notification permission screen
        smsSettingsButton.setOnClickListener(view -> {

            Intent intent = new Intent(
                    EventsActivity.this,
                    SmsPermissionActivity.class
            );

            startActivity(intent);

            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload events whenever the user returns to this screen
        loadEvents();
    }

    /**
     * Reads all events for the logged-in user
     * and displays them in the table.
     */
    private void loadEvents() {

        // Remove old event rows.
        // Child 0 is the header, so we keep it.
        while (eventsTable.getChildCount() > 1) {
            eventsTable.removeViewAt(1);
        }

        Cursor cursor = databaseHelper.getEvents(username);

        int eventCount = cursor.getCount();

        // Update event count text
        if (eventCount == 1) {
            eventCountText.setText("1 event coming up");
        } else {
            eventCountText.setText(
                    eventCount + " events coming up"
            );
        }

        // Update the number badge
        eventCountBadge.setText(
                String.valueOf(eventCount)
        );

        // Read every event from SQLite
        while (cursor.moveToNext()) {

            int eventId = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.EVENT_ID
                    )
            );

            String title = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.EVENT_TITLE
                    )
            );

            String date = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.EVENT_DATE
                    )
            );

            String time = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.EVENT_TIME
                    )
            );

            String category = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.EVENT_CATEGORY
                    )
            );

            String description = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.EVENT_DESCRIPTION
                    )
            );

            // Create a row for this event
            addEventRow(
                    eventId,
                    title,
                    date,
                    time,
                    category,
                    description
            );
        }

        cursor.close();
    }

    /**
     * Creates one event row and adds Edit/Delete buttons.
     */
    private void addEventRow(
            int eventId,
            String title,
            String date,
            String time,
            String category,
            String description) {

        TableRow row = new TableRow(this);

        row.setLayoutParams(
                new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(66)
                )
        );

        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_grid_row);

        // Event title
        TextView titleText = createTextCell(
                title,
                160
        );

        titleText.setTypeface(
                null,
                Typeface.BOLD
        );

        row.addView(titleText);

        // Event date
        row.addView(
                createTextCell(
                        date,
                        110
                )
        );

        // Event time
        row.addView(
                createTextCell(
                        time,
                        100
                )
        );

        // Event details
        row.addView(
                createTextCell(
                        description,
                        170
                )
        );

        // Container for Edit and Delete buttons
        LinearLayout actions = new LinearLayout(this);

        actions.setOrientation(
                LinearLayout.HORIZONTAL
        );

        actions.setGravity(
                Gravity.CENTER
        );

        TableRow.LayoutParams actionParams =
                new TableRow.LayoutParams(
                        dpToPx(170),
                        dpToPx(60)
                );

        actions.setLayoutParams(actionParams);

        // -------------------------
        // EDIT BUTTON
        // -------------------------

        Button editButton = new Button(this);

        editButton.setText("Edit");
        editButton.setTextSize(12);
        editButton.setAllCaps(false);

        editButton.setTextColor(
                getResources().getColor(
                        R.color.brand_purple,
                        getTheme()
                )
        );

        editButton.setBackgroundResource(
                R.drawable.bg_secondary_button
        );

        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(
                        dpToPx(75),
                        dpToPx(40)
                );

        editParams.setMargins(
                dpToPx(2),
                0,
                dpToPx(2),
                0
        );

        editButton.setLayoutParams(editParams);

        // When Edit is clicked, send existing event data
        // to AddEventActivity.
        editButton.setOnClickListener(view -> {

            Intent intent = new Intent(
                    EventsActivity.this,
                    AddEventActivity.class
            );

            intent.putExtra(
                    "username",
                    username
            );

            intent.putExtra(
                    "eventId",
                    eventId
            );

            intent.putExtra(
                    "title",
                    title
            );

            intent.putExtra(
                    "date",
                    date
            );

            intent.putExtra(
                    "time",
                    time
            );

            intent.putExtra(
                    "category",
                    category
            );

            intent.putExtra(
                    "description",
                    description
            );

            startActivity(intent);

            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        });

        // -------------------------
        // DELETE BUTTON
        // -------------------------

        Button deleteButton = new Button(this);

        deleteButton.setText("Delete");
        deleteButton.setTextSize(11);
        deleteButton.setAllCaps(false);

        deleteButton.setTextColor(
                getResources().getColor(
                        R.color.danger,
                        getTheme()
                )
        );

        deleteButton.setBackgroundResource(
                R.drawable.bg_delete_button
        );

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        dpToPx(82),
                        dpToPx(40)
                );

        deleteParams.setMargins(
                dpToPx(2),
                0,
                dpToPx(2),
                0
        );

        deleteButton.setLayoutParams(deleteParams);

        // Delete event from SQLite
        deleteButton.setOnClickListener(view -> {

            boolean deleted =
                    databaseHelper.deleteEvent(eventId);

            if (deleted) {

                Toast.makeText(
                        EventsActivity.this,
                        "Event deleted.",
                        Toast.LENGTH_SHORT
                ).show();

                // Refresh the event table
                loadEvents();

            } else {

                Toast.makeText(
                        EventsActivity.this,
                        "Unable to delete event.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Add both buttons
        actions.addView(editButton);
        actions.addView(deleteButton);

        // Add actions to the row
        row.addView(actions);

        // Add completed row to the table
        eventsTable.addView(row);
    }

    /**
     * Creates a TextView used as a table cell.
     */
    private TextView createTextCell(
            String text,
            int widthDp) {

        TextView textView = new TextView(this);

        if (text == null) {
            text = "";
        }

        textView.setText(text);
        textView.setTextSize(13);

        textView.setTextColor(
                getResources().getColor(
                        R.color.text_primary,
                        getTheme()
                )
        );

        textView.setGravity(
                Gravity.CENTER_VERTICAL
        );

        textView.setPadding(
                dpToPx(10),
                0,
                dpToPx(6),
                0
        );

        TableRow.LayoutParams params =
                new TableRow.LayoutParams(
                        dpToPx(widthDp),
                        dpToPx(66)
                );

        textView.setLayoutParams(params);

        return textView;
    }

    /**
     * Converts dp values into pixels.
     */
    private int dpToPx(int dp) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                dp * density
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the database connection
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}