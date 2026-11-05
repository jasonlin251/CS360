package com.example.zhenqianglineventtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "event_tracker.db";

    // Increased to version 2 because the category column was added.
    private static final int DATABASE_VERSION = 2;

    // User table
    private static final String TABLE_USERS = "users";
    private static final String USER_ID = "id";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    // Event table
    private static final String TABLE_EVENTS = "events";

    public static final String EVENT_ID = "id";
    public static final String EVENT_USER = "username";
    public static final String EVENT_TITLE = "title";
    public static final String EVENT_DATE = "date";
    public static final String EVENT_TIME = "time";
    public static final String EVENT_CATEGORY = "category";
    public static final String EVENT_DESCRIPTION = "description";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Stores usernames and passwords.
        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        USERNAME + " TEXT UNIQUE NOT NULL, " +
                        PASSWORD + " TEXT NOT NULL)";

        db.execSQL(createUsersTable);

        // Stores all event information.
        String createEventsTable =
                "CREATE TABLE " + TABLE_EVENTS + " (" +
                        EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        EVENT_USER + " TEXT NOT NULL, " +
                        EVENT_TITLE + " TEXT NOT NULL, " +
                        EVENT_DATE + " TEXT, " +
                        EVENT_TIME + " TEXT, " +
                        EVENT_CATEGORY + " TEXT, " +
                        EVENT_DESCRIPTION + " TEXT)";

        db.execSQL(createEventsTable);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        // Version 2 adds the category field without deleting existing data.
        if (oldVersion < 2) {
            db.execSQL(
                    "ALTER TABLE " + TABLE_EVENTS +
                            " ADD COLUMN " + EVENT_CATEGORY + " TEXT"
            );
        }
    }

    // Creates a new user account.
    public boolean createUser(String username, String password) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);

        return result != -1;
    }

    // Checks the username and password during login.
    public boolean checkUser(String username, String password) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{USER_ID},
                USERNAME + "=? AND " + PASSWORD + "=?",
                new String[]{username, password},
                null,
                null,
                null
        );

        boolean userExists = cursor.getCount() > 0;

        cursor.close();

        return userExists;
    }

    // Checks whether the username is already in the database.
    public boolean usernameExists(String username) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{USER_ID},
                USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;

        cursor.close();

        return exists;
    }

    // Adds a new event to the database.
    public boolean addEvent(
            String username,
            String title,
            String date,
            String time,
            String category,
            String description) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(EVENT_USER, username);
        values.put(EVENT_TITLE, title);
        values.put(EVENT_DATE, date);
        values.put(EVENT_TIME, time);
        values.put(EVENT_CATEGORY, category);
        values.put(EVENT_DESCRIPTION, description);

        long result = db.insert(
                TABLE_EVENTS,
                null,
                values
        );

        return result != -1;
    }

    // Reads all events belonging to the logged-in user.
    public Cursor getEvents(String username) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_EVENTS,
                null,
                EVENT_USER + "=?",
                new String[]{username},
                null,
                null,
                EVENT_DATE + " ASC"
        );
    }

    // Updates an existing event.
    public boolean updateEvent(
            int eventId,
            String title,
            String date,
            String time,
            String category,
            String description) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(EVENT_TITLE, title);
        values.put(EVENT_DATE, date);
        values.put(EVENT_TIME, time);
        values.put(EVENT_CATEGORY, category);
        values.put(EVENT_DESCRIPTION, description);

        int result = db.update(
                TABLE_EVENTS,
                values,
                EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}
        );

        return result > 0;
    }

    // Deletes an event from the database.
    public boolean deleteEvent(int eventId) {

        SQLiteDatabase db = getWritableDatabase();

        int result = db.delete(
                TABLE_EVENTS,
                EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}
        );

        return result > 0;
    }
}