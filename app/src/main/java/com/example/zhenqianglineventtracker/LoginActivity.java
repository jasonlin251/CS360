package com.example.zhenqianglineventtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private EditText usernameField;
    private EditText passwordField;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Connect the Java code to the login screen
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);

        Button loginButton = findViewById(R.id.loginButton);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        // Create access to the SQLite database
        databaseHelper = new DatabaseHelper(this);

        // Attempt to log in when the Login button is pressed
        loginButton.setOnClickListener(view -> loginUser());

        // Create a new account when the Create Account button is pressed
        createAccountButton.setOnClickListener(view -> createAccount());
    }

    // Checks whether the username and password are correct
    private void loginUser() {

        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Make sure both fields contain information
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(
                    this,
                    "Please enter a username and password.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Check the username and password against the database
        if (databaseHelper.checkUser(username, password)) {

            Toast.makeText(
                    this,
                    "Login successful.",
                    Toast.LENGTH_SHORT
            ).show();

            openEventsScreen(username);

        } else {

            Toast.makeText(
                    this,
                    "Incorrect username or password.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Creates and saves a new user account
    private void createAccount() {

        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Make sure both fields contain information
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(
                    this,
                    "Please enter a username and password.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Do not allow duplicate usernames
        if (databaseHelper.usernameExists(username)) {

            Toast.makeText(
                    this,
                    "That username already exists.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Save the new account to SQLite
        boolean accountCreated =
                databaseHelper.createUser(username, password);

        if (accountCreated) {

            Toast.makeText(
                    this,
                    "Account created successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            openEventsScreen(username);

        } else {

            Toast.makeText(
                    this,
                    "Unable to create account.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Opens the main event screen
    private void openEventsScreen(String username) {

        Intent intent = new Intent(
                LoginActivity.this,
                EventsActivity.class
        );

        // Pass the logged-in username to the event screen
        intent.putExtra("username", username);

        startActivity(intent);

        overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the database when the activity is destroyed
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}