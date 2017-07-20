package com.propellerng.thepropeller.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.propellerng.thepropeller.AppController;

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
    private SQLiteDatabase mDb;






    static void loadNews(Context context){


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


                        //  deleteOldData();
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

    static void loadNewsBusiness(Context context){

        NewsDBHelper dbHelper = new NewsDBHelper(context);


        String POLITICS_URL = "http://propellerng.com/?json=get_category_posts&category_id=2&page=1";
        final List<Post> postItems = new ArrayList<Post>();
        final ContentResolver mContentResolver = context.getContentResolver();


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


                                //  deleteOldData();
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

}
