package com.propellerng.thepropeller.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by ESIDEM jnr on 5/29/2017.
 */

public class NewsIntentService extends IntentService {

    public NewsIntentService() {
        super(NewsIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        NewsSyncJobs.loadNews(getApplicationContext());

    }
}
