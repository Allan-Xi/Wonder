package org.wondertech.wonder.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.SetDrivingActivity;
import org.wondertech.wonder.Utils.Utilities;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
        //mGoogleApiClient.connect();



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
                        new SetDrivngTask().execute(true);
                    }
                };
                timer.schedule(ttask, 0, SETDRIVINGDURATION);
                new SetDrivngTask().execute(true);
                Intent intent1 = new Intent(DetectionDrivingService.this, SetDrivingActivity.class);
                intent1.putExtra("auto", true);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            }


            @Override
            public void onDriveEnd(DriveInfo driveInfo) {
                Log.v("stop driving", "123");
                /*ttask.cancel();
                userInfo.edit().putBoolean("autoDriving", false).apply();
                userInfo.edit().putBoolean("isDriving", false).apply();
                new SetDrivngTask().execute(false);
                Intent intent1 = new Intent(DetectionDrivingService.this, MainActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);*/
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

    class SetDrivngTask extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(Boolean... params) {
            if (params[0])
            {
                Log.v("driving", "server");
            }
            else
                Log.v("stop driving", "server");
            String TargetURL = userInfo.getString("URL", "") + "isDriving";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(TargetURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setUseCaches(false);
                urlConnection.setConnectTimeout(Utilities.CONNECTTIMEOUT);
                urlConnection.setReadTimeout(Utilities.READTIMEOUT);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                JSONObject json = new JSONObject();
                json.put("app_version", userInfo.getString("app_version", ""));
                json.put("uuid", userInfo.getString("uuid", ""));
                json.put("client_id", userInfo.getString("client_id", ""));
                json.put("auto_mode", true);
                json.put("is_driving", params[0]);
                json.put("secs_remaining", 240);

                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                try {
                    out.write(json.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    out.close();
                }

                int HttpResult = urlConnection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                } else {
                    System.out.println(urlConnection.getResponseMessage());
                }
            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();

            } catch (JSONException e) {

                e.printStackTrace();

            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return null;
        }
    }
}
