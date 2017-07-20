package com.propellerng.thepropeller.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by ESIDEM jnr on 5/28/2017.
 */

public class NewsProvider extends ContentProvider {



    private static final int ENTRIES = 100;
    private static final int ENTRIES_WITH_ID = 101;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private NewsDBHelper dbHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(NewsContract.AUTHORITY, NewsContract.PATH_ENTRIES, ENTRIES);
        matcher.addURI(NewsContract.AUTHORITY, NewsContract.PATH_ENTRIES_WITH_ID, ENTRIES_WITH_ID);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        dbHelper = new NewsDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor;
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        switch (uriMatcher.match(uri)) {
            case ENTRIES:
                returnCursor = db.query(
                        NewsContract.Entry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case ENTRIES_WITH_ID:
                returnCursor = db.query(
                        NewsContract.Entry.TABLE_NAME,
                        projection,
                        NewsContract.Entry._ID + " = ?",
                        new String[]{NewsContract.Entry.getEntryFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );

                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        Context context = getContext();
        if (context != null){
            returnCursor.setNotificationUri(context.getContentResolver(), uri);
        }

        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri returnUri;

        switch (uriMatcher.match(uri)) {
            case ENTRIES:
                db.insert(
                       NewsContract.Entry.TABLE_NAME,
                        null,
                        values
                );
                returnUri = NewsContract.Entry.CONTENT_URI;
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        Context context = getContext();
        if (context != null){
            context.getContentResolver().notifyChange(uri, null);
        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;

        if (null == selection) {
            selection = "1";
        }
        switch (uriMatcher.match(uri)) {
            case ENTRIES:
                rowsDeleted = db.delete(
                        NewsContract.Entry.TABLE_NAME,
                        selection,
                        selectionArgs
                );

                break;

            case ENTRIES_WITH_ID:
                String symbol = NewsContract.Entry.getEntryFromUri(uri);
                rowsDeleted = db.delete(
                        NewsContract.Entry.TABLE_NAME,
                        '"' + symbol + '"' + " =" + NewsContract.Entry._ID,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        if (rowsDeleted != 0) {
            Context context = getContext();
            if (context != null){
                context.getContentResolver().notifyChange(uri, null);
            }
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case ENTRIES:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        db.insert(
                                NewsContract.Entry.TABLE_NAME,
                                null,
                                value
                        );
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                Context context = getContext();
                if (context != null) {
                    context.getContentResolver().notifyChange(uri, null);
                }

                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }


    }
}
