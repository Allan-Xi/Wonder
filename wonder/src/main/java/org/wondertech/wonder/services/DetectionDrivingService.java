package org.wondertech.wonder.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.zendrive.sdk.DriveInfo;
import com.zendrive.sdk.DriveStartInfo;
import com.zendrive.sdk.Zendrive;
import com.zendrive.sdk.ZendriveConfiguration;
import com.zendrive.sdk.ZendriveDriveDetectionMode;
import com.zendrive.sdk.ZendriveListener;

import org.wondertech.wonder.AsyncTasks.SetDrivingTask;
import org.wondertech.wonder.SetDrivingActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xiyu on 5/14/15.
 */
public class DetectionDrivingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    static public GoogleApiClient mGoogleApiClient;
    private static SharedPreferences userInfo;
    private PendingIntent mActivityRecognitionPendingIntent;
    private static long ACTIVITYRECOGNITIONDURATION = 10000;
    private static long SETDRIVINGDURATION = 18000;
    public static TimerTask ttask;
    private Timer timer;


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        userInfo = getSharedPreferences("user_info", 0);
        mGoogleApiClient =
                new GoogleApiClient.Builder(this)
                        .addApi(ActivityRecognition.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
        mGoogleApiClient.connect();
        startService(new Intent(this, CheckMotionDetectionService.class));

        // Zendrive SDK setup
        String zendriveApplicationKey = "mTudUAta3fHPkaGvAOd69AednG1Ezpa8";   // Your Zendrive SDK Key
        String driveID = userInfo.getString("phone", "");
        ZendriveListener zendriveListener = new ZendriveListener() {
            @Override
            public void onDriveStart(DriveStartInfo driveStartInfo) {
                Log.v("start driving", "123");
                userInfo.edit().putBoolean("isDriving", true).apply();
                userInfo.edit().putBoolean("autoDriving", true).apply();
                timer = new Timer();
                ttask = new TimerTask() {
                    public void run() {
                        new SetDrivingTask(DetectionDrivingService.this).execute(true);
                    }
                };
                timer.schedule(ttask, 0, SETDRIVINGDURATION);
                Intent intent1 = new Intent(DetectionDrivingService.this, SetDrivingActivity.class);
                intent1.putExtra("auto", true);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            }


            @Override
            public void onDriveEnd(DriveInfo driveInfo) {
                Log.v("stop driving", "123");
            }
        };


        ZendriveConfiguration zendriveConfiguration = new ZendriveConfiguration(
                zendriveApplicationKey, driveID, ZendriveDriveDetectionMode.AUTO_OFF);

        if (PreferenceManager.getDefaultSharedPreferences(DetectionDrivingService.this)
                .getBoolean("detectDriving", false))
        {
            Zendrive.setup(
                    this.getApplicationContext(),
                    zendriveConfiguration,
                    zendriveListener,                 // can be null.
                    new Zendrive.SetupCallback() {
                        @Override
                        public void onSetup(boolean success) {
                            if (success) {
                                Log.v("zen", "success");
                            } else {
                                Log.v("zen", "fail");
                            }
                        }
                    }
            );
        }

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
        //Log.v("motion detection", "connected");
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
