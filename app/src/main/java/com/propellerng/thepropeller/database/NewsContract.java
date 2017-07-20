package com.propellerng.thepropeller.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ESIDEM jnr on 5/28/2017.
 */

public final class NewsContract {

    static final String AUTHORITY = "com.propellerng.thepropeller";
    static final String PATH_ENTRIES = "entries";
    static final String PATH_ENTRIES_WITH_ID = "entries/*";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    private NewsContract() {
    }


    /**
     * Columns supported by "entries" records.
     */
    public static class Entry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_URI.buildUpon().appendPath(PATH_ENTRIES).build();


        /**
         * Table name where records are stored for "entry" resources.
         */
        public static final String TABLE_NAME = "entry";
        public static final String TABLE_NAME_BUSINESS ="business";
        public static final String TABLE_NAME_POLITICS ="politics";
        public static final String TABLE_NAME_ENTERTAINMENT ="entertainment";
        public static final String TABLE_NAME_RELIGION ="religion";
        public static final String TABLE_NAME_SPORTS ="sport";


        public static final String COLUMN_NAME_TITLE = "title";


        public static final String COLUMN_NAME_LINK = "link";
        /**
         * Date article was published.
         */
        public static final String COLUMN_NAME_PUBLISHED = "published";

        public static final String COLUMN_NAME_FAV = "fav";

        public static final String COLUMN_NAME_DESCRIPTION = "description";

        public static final String COLUMN_NAME_IMAGE_URL = "image_url";


        public static Uri buildDirUri() {
            return BASE_URI.buildUpon().appendPath("entries").build();
        }

        /**
         * Matches: /items/[_id]/
         */
        public static Uri buildItemUri(long _id) {
            return BASE_URI.buildUpon().appendPath("entries").appendPath(Long.toString(_id)).build();
        }

        static String getEntryFromUri(Uri queryUri) {
            return queryUri.getLastPathSegment();
        }


    }
}