package com.propellerng.thepropeller.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.propellerng.thepropeller.AppController;

import com.propellerng.thepropeller.R;
import com.propellerng.thepropeller.database.NewsContract;
import com.propellerng.thepropeller.database.NewsDBHelper;
import com.propellerng.thepropeller.parser.JSONParser;
import com.propellerng.thepropeller.parser.Post;
import com.propellerng.thepropeller.util.Config;

import org.json.JSONObject;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static android.R.attr.id;
import static android.R.attr.order;
import static java.security.AccessController.getContext;


/**
 * Created by ESIDEM jnr on 5/29/2017.
 */

public class NewsSyncJobs {

    private NewsSyncJobs(){}

    private static final int ONE_OFF_ID = 2;
    private static final String ACTION_DATA_UPDATED = "com.propellerng.thepropeller.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    Context context;







    static void loadNews(final Context context){


        String RECENT_POST_URL = "http://propellerng.com/?json=get_posts&page=1";
       final List<Post> postItems = new ArrayList<Post>();
       final ContentResolver mContentResolver = context.getContentResolver();


try {


    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, RECENT_POST_URL, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {


                    // Parse JSON data
                    postItems.addAll(JSONParser.parsePosts(jsonObject));

                    // A temporary workaround to avoid downloading duplicate posts in some
                    // rare circumstances by converting ArrayList to a LinkedHashSet without
                    // losing its order

                    ArrayList<ContentValues> cvArray = new ArrayList<>();
                    Set<Post> set = new LinkedHashSet<>(postItems);
                    postItems.clear();
                    postItems.addAll(new ArrayList<>(set));

                    for (Post item : postItems) {


                        ContentValues contentValues = new ContentValues();
                        contentValues.put(NewsContract.Entry.COLUMN_NAME_TITLE, item.getTitle());
                        contentValues.put(NewsContract.Entry.COLUMN_NAME_LINK, item.getUrl());
                        contentValues.put(NewsContract.Entry.COLUMN_NAME_DESCRIPTION, item.getContent());
                        contentValues.put(NewsContract.Entry.COLUMN_NAME_FAV, 0);
                        contentValues.put(NewsContract.Entry.COLUMN_NAME_PUBLISHED, (item.getDate()));
                        contentValues.put(NewsContract.Entry.COLUMN_NAME_IMAGE_URL, item.getFeaturedImageUrl());


                        String select = "(" + NewsContract.Entry.COLUMN_NAME_TITLE + " = ? )";
                        Uri dirUri = NewsContract.Entry.buildDirUri();
                        Cursor check = mContentResolver.query(dirUri, new String[]{NewsContract.Entry.COLUMN_NAME_TITLE},
                                select, new String[]{item.getTitle()}, null, null);
                        check.moveToFirst();
                        if (check.getCount() > 0) {
                            int columIndex = check.getColumnIndex(NewsContract.Entry.COLUMN_NAME_TITLE);
                            if (item.getTitle().compareTo(check.getString(columIndex)) == 1) {
                                cvArray.add(contentValues);
                            }
                        } else {

                            cvArray.add(contentValues);
                        }

                        check.close();
                    }

                    ContentValues[] cc = new ContentValues[cvArray.size()];
                    cvArray.toArray(cc);

                    if (cc.length > 0) {

                        mContentResolver.bulkInsert(NewsContract.Entry.CONTENT_URI, cc);


                        //  deleteOldData(context);

                        int maxNumber = 10;

                        try {

                            String order = NewsContract.Entry.COLUMN_NAME_PUBLISHED + " ASC";
                            String select = NewsContract.Entry.COLUMN_NAME_FAV + " = ?";
                            Cursor c = mContentResolver.query(NewsContract.Entry.CONTENT_URI,
                                    new String[]{NewsContract.Entry._ID, NewsContract.Entry.COLUMN_NAME_FAV},
                                    select,
                                    new String[]{"0"},
                                    order);

                            int deletNum = 0;
                            if (c.getCount() > 0 && c.getCount() > maxNumber) {
                                deletNum = c.getCount() - maxNumber;
                            }
                            int[] deleID;
                            if (deletNum != 0) {
                                c.moveToFirst();
                                deleID = new int[deletNum];
                                for (int i = 0; i < deletNum; i++) {
                                    deleID[i] = Integer.valueOf(c.getString(c.getColumnIndex(NewsContract.Entry._ID)));
                                    c.moveToNext();
                                }
                                for (int i = 0; i < deletNum; i++) {
                                    mContentResolver.delete(NewsContract.Entry.buildItemUri((long) deleID[i]), null, null);
                                }
                            }
                            c.close();
                        }catch (NullPointerException e){

                            Log.d("Delete old data", e.getMessage());
                        }
                    }




                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {


                    volleyError.printStackTrace();
                    Log.d("NewsSyncJob", "----- Error: " + volleyError.getMessage());


                }
            });


    request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


    AppController.getInstance().addToRequestQueue(request, "NewsSyncJob");
    AppController.getInstance().getRequestQueue().getCache().clear();

    Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
    context.sendBroadcast(dataUpdatedIntent);


}catch (NullPointerException e){

    Log.d("NewsSyncJob",e.getMessage());
}

    }

    static void loadNewsPolitics(final Context context){


        NewsDBHelper dbHelper = new NewsDBHelper(context);
        final SQLiteDatabase mDb  = dbHelper.getWritableDatabase();

       String POLITICS_URL = "http://propellerng.com/?json=get_category_posts&category_id=2&page=1";
       final List<Post> postItems = new ArrayList<Post>();



        try {


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, POLITICS_URL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {


                            // Parse JSON data
                            postItems.addAll(JSONParser.parsePosts(jsonObject));

                            // A temporary workaround to avoid downloading duplicate posts in some
                            // rare circumstances by converting ArrayList to a LinkedHashSet without
                            // losing its order

                            ArrayList<ContentValues> cvArray = new ArrayList<>();
                            Set<Post> set = new LinkedHashSet<>(postItems);
                            postItems.clear();
                            postItems.addAll(new ArrayList<>(set));

                            for (Post item : postItems) {


                                ContentValues contentValues = new ContentValues();
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_TITLE, item.getTitle());
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_LINK, item.getUrl());
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_DESCRIPTION, item.getContent());
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_FAV, 0);
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_PUBLISHED, (item.getDate()));
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_IMAGE_URL, item.getFeaturedImageUrl());


                                String select = "(" + NewsContract.Entry.COLUMN_NAME_TITLE + " = ? )";
                                 Cursor check = mDb.query(
                                        NewsContract.Entry.TABLE_NAME_POLITICS,
                                        new String[]{NewsContract.Entry.COLUMN_NAME_TITLE} , // Column
                                        select, // Where clause
                                        new String[]{item.getTitle()}, // Arguments
                                        null, // Group by
                                        null, // having
                                        null // Sort_order
                                );
                                check.moveToFirst();
                                if (check.getCount() > 0) {
                                    int columIndex = check.getColumnIndex(NewsContract.Entry.COLUMN_NAME_TITLE);
                                    if (item.getTitle().compareTo(check.getString(columIndex)) == 1) {
                                        cvArray.add(contentValues);
                                    }
                                } else {

                                    cvArray.add(contentValues);
                                }

                                check.close();
                            }

                            ContentValues[] cc = new ContentValues[cvArray.size()];
                            cvArray.toArray(cc);

                            if (cc.length > 0) {
                                mDb.beginTransaction();

                                try {

                                    for (ContentValues contentValues : cc) {

                                        long newID = mDb.insertOrThrow(NewsContract.Entry.TABLE_NAME_POLITICS, null, contentValues);

                                        if (newID <= 0) {
                                            throw new SQLException("Failed to insert row into ");
                                        }
                                    }

                                    int maxNumber = 3;

                                    try {
                                        String where = NewsContract.Entry._ID + " = ?";
                                        String order = NewsContract.Entry.COLUMN_NAME_PUBLISHED + " ASC";
                                        String select = NewsContract.Entry.COLUMN_NAME_FAV + " = ?";
                                        Cursor c = mDb.query(
                                                NewsContract.Entry.TABLE_NAME_POLITICS,
                                                new String[]{NewsContract.Entry._ID,NewsContract.Entry.COLUMN_NAME_FAV} , // Column
                                                select, // Where clause
                                                new String[]{"0"}, // Arguments
                                                null, // Group by
                                                null, // having
                                                order // Sort_order
                                        );

                                        int deletNum = 0;
                                        if (c.getCount() > 0 && c.getCount() > maxNumber) {
                                            deletNum = c.getCount() - maxNumber;
                                        }
                                        int[] deleID;
                                        if (deletNum != 0) {
                                            c.moveToFirst();
                                            deleID = new int[deletNum];
                                            for (int i = 0; i < deletNum; i++) {
                                                deleID[i] = Integer.valueOf(c.getString(c.getColumnIndex(NewsContract.Entry._ID)));
                                                c.moveToNext();
                                            }
                                            for (int i = 0; i < deletNum; i++) {
                                                mDb.delete(NewsContract.Entry.TABLE_NAME_POLITICS,where, new String[] {Integer.toString(deleID[i])});
                                            }
                                        }
                                        c.close();
                                    }catch (NullPointerException e){

                                        Log.d("Delete old data", e.getMessage());
                                    }

                                   // deleteOldDataFromPolitcis(context);
                                    mDb.setTransactionSuccessful();

                                }finally {
                                    mDb.endTransaction();
                                }


                                }





                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {


                            volleyError.printStackTrace();
                            Log.d("NewsSyncJob", "----- Error: " + volleyError.getMessage());


                        }
                    });


            request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


            AppController.getInstance().addToRequestQueue(request, "NewsSyncJob");
            AppController.getInstance().getRequestQueue().getCache().clear();

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);


        }catch (NullPointerException e){

            Log.d("NewsSyncJob",e.getMessage());
        }

    }

    static void loadNewsBusiness(final Context context){


        NewsDBHelper dbHelper = new NewsDBHelper(context);
        final SQLiteDatabase mDb  = dbHelper.getWritableDatabase();

        String BUSINESS_URL = "http://propellerng.com/?json=get_category_posts&category_id=21&page=1";
        final List<Post> postItems = new ArrayList<Post>();



        try {


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, BUSINESS_URL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {


                            // Parse JSON data
                            postItems.addAll(JSONParser.parsePosts(jsonObject));

                            // A temporary workaround to avoid downloading duplicate posts in some
                            // rare circumstances by converting ArrayList to a LinkedHashSet without
                            // losing its order

                            ArrayList<ContentValues> cvArray = new ArrayList<>();
                            Set<Post> set = new LinkedHashSet<>(postItems);
                            postItems.clear();
                            postItems.addAll(new ArrayList<>(set));

                            for (Post item : postItems) {


                                ContentValues contentValues = new ContentValues();
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_TITLE, item.getTitle());
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_LINK, item.getUrl());
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_DESCRIPTION, item.getContent());
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_FAV, 0);
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_PUBLISHED, (item.getDate()));
                                contentValues.put(NewsContract.Entry.COLUMN_NAME_IMAGE_URL, item.getFeaturedImageUrl());


                                String select = "(" + NewsContract.Entry.COLUMN_NAME_TITLE + " = ? )";
                                Cursor check = mDb.query(
                                        NewsContract.Entry.TABLE_NAME_BUSINESS,
                                        new String[]{NewsContract.Entry.COLUMN_NAME_TITLE} , // Column
                                        select, // Where clause
                                        new String[]{item.getTitle()}, // Arguments
                                        null, // Group by
                                        null, // having
                                        null // Sort_order
                                );
                                check.moveToFirst();
                                if (check.getCount() > 0) {
                                    int columIndex = check.getColumnIndex(NewsContract.Entry.COLUMN_NAME_TITLE);
                                    if (item.getTitle().compareTo(check.getString(columIndex)) == 1) {
                                        cvArray.add(contentValues);
                                    }
                                } else {

                                    cvArray.add(contentValues);
                                }

                                check.close();
                            }

                            ContentValues[] cc = new ContentValues[cvArray.size()];
                            cvArray.toArray(cc);

                            if (cc.length > 0) {
                                mDb.beginTransaction();

                                try {

                                    for (ContentValues contentValues : cc) {

                                        long newID = mDb.insertOrThrow(NewsContract.Entry.TABLE_NAME_BUSINESS, null, contentValues);

                                        if (newID <= 0) {
                                            throw new SQLException("Failed to insert row into ");
                                        }
                                    }

                                    int maxNumber = 3;

                                    try {
                                        String where = NewsContract.Entry._ID + " = ?";
                                        String order = NewsContract.Entry.COLUMN_NAME_PUBLISHED + " ASC";
                                        String select = NewsContract.Entry.COLUMN_NAME_FAV + " = ?";
                                        Cursor c = mDb.query(
                                                NewsContract.Entry.TABLE_NAME_BUSINESS,
                                                new String[]{NewsContract.Entry._ID,NewsContract.Entry.COLUMN_NAME_FAV} , // Column
                                                select, // Where clause
                                                new String[]{"0"}, // Arguments
                                                null, // Group by
                                                null, // having
                                                order // Sort_order
                                        );

                                        int deletNum = 0;
                                        if (c.getCount() > 0 && c.getCount() > maxNumber) {
                                            deletNum = c.getCount() - maxNumber;
                                        }
                                        int[] deleID;
                                        if (deletNum != 0) {
                                            c.moveToFirst();
                                            deleID = new int[deletNum];
                                            for (int i = 0; i < deletNum; i++) {
                                                deleID[i] = Integer.valueOf(c.getString(c.getColumnIndex(NewsContract.Entry._ID)));
                                                c.moveToNext();
                                            }
                                            for (int i = 0; i < deletNum; i++) {
                                                mDb.delete(NewsContract.Entry.TABLE_NAME_BUSINESS,where, new String[] {Integer.toString(deleID[i])});
                                            }
                                        }
                                        c.close();
                                    }catch (NullPointerException e){

                                        Log.d("Delete old data", e.getMessage());
                                    }

                                    // deleteOldDataFromPolitcis(context);
                                    mDb.setTransactionSuccessful();

                                }finally {
                                    mDb.endTransaction();
                                }


                            }





                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {


                            volleyError.printStackTrace();
                            Log.d("NewsSyncJob", "----- Error: " + volleyError.getMessage());


                        }
                    });


            request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


            AppController.getInstance().addToRequestQueue(request, "NewsSyncJob");
            AppController.getInstance().getRequestQueue().getCache().clear();

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);


        }catch (NullPointerException e){

            Log.d("NewsSyncJob",e.getMessage());
        }

    }



    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }


    private static void schedulePeriodic(Context context) {



        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, NewsJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, NewsIntentService.class);
            context.startService(nowIntent);
        } else {

            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, NewsJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }

    static void deleteOldData(Context context)
    {

        ContentResolver contentResolver = context.getContentResolver();
        int maxNumber = 10;

        try {

            String order = NewsContract.Entry.COLUMN_NAME_PUBLISHED + " ASC";
            String select = NewsContract.Entry.COLUMN_NAME_FAV + " = ?";
            Cursor c = contentResolver.query(NewsContract.Entry.CONTENT_URI,
                    new String[]{NewsContract.Entry._ID, NewsContract.Entry.COLUMN_NAME_FAV},
                    select,
                    new String[]{"0"},
                    order);

            int deletNum = 0;
            if (c.getCount() > 0 && c.getCount() > maxNumber) {
                deletNum = c.getCount() - maxNumber;
            }
            int[] deleID;
            if (deletNum != 0) {
                c.moveToFirst();
                deleID = new int[deletNum];
                for (int i = 0; i < deletNum; i++) {
                    deleID[i] = Integer.valueOf(c.getString(c.getColumnIndex(NewsContract.Entry._ID)));
                    c.moveToNext();
                }
                for (int i = 0; i < deletNum; i++) {
                    contentResolver.delete(NewsContract.Entry.buildItemUri((long) deleID[i]), null, null);
                }
            }
            c.close();
        }catch (NullPointerException e){

            Log.d("Delete old data", e.getMessage());
        }
    }



}
