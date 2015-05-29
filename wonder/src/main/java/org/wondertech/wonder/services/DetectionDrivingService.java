package org.wondertech.wonder.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

/**
 * Created by xiyu on 5/14/15.
 */
public class DetectionDrivingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    static public GoogleApiClient mGoogleApiClient;
    private PendingIntent mActivityRecognitionPendingIntent;
    private static long ACTIVITYRECOGNITIONDURATION = 10000;


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient =
                new GoogleApiClient.Builder(this)
                        .addApi(ActivityRecognition.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
        mGoogleApiClient.connect();
        startService(new Intent(this, CheckMotionDetectionService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                mActivityRecognitionPendingIntent);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Intent intent = new Intent(
                this, ActivityRecognitionIntentService.class);
        mActivityRecognitionPendingIntent =
                PendingIntent.getService(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        PendingResult<Status> result= ActivityRecognition.
        ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,
                ACTIVITYRECOGNITIONDURATION,
                mActivityRecognitionPendingIntent);
        result.setResultCallback(new ResultCallback<Status>() {

            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    // Successfully registered
                    Log.v("motion detection", "success");
                } else {
                    // No recovery. Weep softly or inform the user.
                    Log.v("motion detection", "failed");
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("motion detection", "ConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("motion detection", "Failed");
    }
}
