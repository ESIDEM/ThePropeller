package com.propellerng.thepropeller.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ESIDEM jnr on 5/28/2017.
 */

public class NewsDBHelper extends SQLiteOpenHelper {

    /** Schema version. */
    public static final int DATABASE_VERSION = 1;
    /** Filename for SQLite file. */
    public static final String DATABASE_NAME = "feed.db";

    public NewsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String COMMA_SEP = ",";
    /** SQL statement to create "entry" table. */
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NewsContract.Entry.TABLE_NAME + " (" +
                    NewsContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    NewsContract.Entry.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_LINK + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_FAV + TYPE_INTEGER + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_PUBLISHED + TYPE_INTEGER +COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +

                    NewsContract.Entry.COLUMN_NAME_IMAGE_URL + TYPE_TEXT + ");";
    private static final String SQL_CREATE_POLITICS =
            "CREATE TABLE " + NewsContract.Entry.TABLE_NAME_POLITICS + " (" +
                    NewsContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    NewsContract.Entry.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_LINK + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_FAV + TYPE_INTEGER + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_PUBLISHED + TYPE_INTEGER +COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +

                    NewsContract.Entry.COLUMN_NAME_IMAGE_URL + TYPE_TEXT + ");";

    private static final String SQL_CREATE_BUSINESS =
            "CREATE TABLE " + NewsContract.Entry.TABLE_NAME_BUSINESS + " (" +
                    NewsContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    NewsContract.Entry.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_LINK + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_FAV + TYPE_INTEGER + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_PUBLISHED + TYPE_INTEGER +COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +

                    NewsContract.Entry.COLUMN_NAME_IMAGE_URL + TYPE_TEXT + ");";
    private static final String SQL_CREATE_SPORT =
            "CREATE TABLE " + NewsContract.Entry.TABLE_NAME_SPORTS + " (" +
                    NewsContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    NewsContract.Entry.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_LINK + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_FAV + TYPE_INTEGER + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_PUBLISHED + TYPE_INTEGER +COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +

                    NewsContract.Entry.COLUMN_NAME_IMAGE_URL + TYPE_TEXT + ");";

    private static final String SQL_CREATE_ENTERTAINMENT =
            "CREATE TABLE " + NewsContract.Entry.TABLE_NAME_ENTERTAINMENT + " (" +
                    NewsContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    NewsContract.Entry.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_LINK + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_FAV + TYPE_INTEGER + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_PUBLISHED + TYPE_INTEGER +COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +

                    NewsContract.Entry.COLUMN_NAME_IMAGE_URL + TYPE_TEXT + ");";
    private static final String SQL_CREATE_RELIGION =
            "CREATE TABLE " + NewsContract.Entry.TABLE_NAME_RELIGION + " (" +
                    NewsContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    NewsContract.Entry.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_LINK + TYPE_TEXT + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_FAV + TYPE_INTEGER + COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_PUBLISHED + TYPE_INTEGER +COMMA_SEP +
                    NewsContract.Entry.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +

                    NewsContract.Entry.COLUMN_NAME_IMAGE_URL + TYPE_TEXT + ");";


    /** SQL statement to drop "entry" table. */
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NewsContract.Entry.TABLE_NAME;
    private static final String SQL_DELETE_B =
            "DROP TABLE IF EXISTS " + NewsContract.Entry.TABLE_NAME_BUSINESS;
    private static final String SQL_DELETE_E =
            "DROP TABLE IF EXISTS " + NewsContract.Entry.TABLE_NAME_ENTERTAINMENT;
    private static final String SQL_DELETE_P =
            "DROP TABLE IF EXISTS " + NewsContract.Entry.TABLE_NAME_POLITICS;
    private static final String SQL_DELETE_R =
            "DROP TABLE IF EXISTS " + NewsContract.Entry.TABLE_NAME_RELIGION;
    private static final String SQL_DELETE_S =
            "DROP TABLE IF EXISTS " + NewsContract.Entry.TABLE_NAME_SPORTS;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_BUSINESS);
        db.execSQL(SQL_CREATE_ENTERTAINMENT);
        db.execSQL(SQL_CREATE_POLITICS);
        db.execSQL(SQL_CREATE_RELIGION);
        db.execSQL(SQL_CREATE_SPORT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_E);
        db.execSQL(SQL_DELETE_B);
        db.execSQL(SQL_DELETE_P);
        db.execSQL(SQL_DELETE_R);
        db.execSQL(SQL_DELETE_S);
        onCreate(db);
    }
}
