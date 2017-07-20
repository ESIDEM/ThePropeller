package com.propellerng.thepropeller.sync;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

/**
 * Created by ESIDEM jnr on 5/29/2017.
 */

public class NewsJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Intent nowIntent = new Intent(getApplicationContext(), NewsIntentService.class);
        getApplicationContext().startService(nowIntent);
        return true;

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
