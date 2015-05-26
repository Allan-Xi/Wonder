package org.wondertech.wonder.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;

import org.wondertech.wonder.R;
import org.wondertech.wonder.VOIPActivity;

/**
 * Created by xiyu on 5/14/15.
 */
public class VOIPService extends Service {
    static public SinchClient sinchClient;
    private static SharedPreferences userInfo;
    static public Call call;
    static public MediaPlayer mediaPlayer;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        userInfo = getSharedPreferences("user_info", 0);
        if (sinchClient == null)
        {
            android.content.Context context = this.getApplicationContext();
            sinchClient = Sinch.getSinchClientBuilder().context(context)
                    .applicationKey("094ae94c-efd6-430f-be86-d12099865918")
                    .applicationSecret("4YvV643zzEOJpLG8SBqvqA==")
                    .environmentHost("clientapi.sinch.com")
                    .userId(userInfo.getString("phone", ""))
                    .build();
            sinchClient.setSupportCalling(true);
            sinchClient.setSupportActiveConnectionInBackground(true);
            sinchClient.setSupportPushNotifications(true);
            sinchClient.startListeningOnActiveConnection();
            sinchClient.addSinchClientListener(new SinchClientListener() {
                public void onClientStarted(SinchClient client) { }
                public void onClientStopped(SinchClient client) { }
                public void onClientFailed(SinchClient client, SinchError error) { }
                public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration registrationCallback) { }
                public void onLogMessage(int level, String area, String message) { }
            });
            sinchClient.start();
            sinchClient.registerPushNotificationData(userInfo.getString("token_id", "").getBytes());
            sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sinchClient.stopListeningOnActiveConnection();
        sinchClient.terminate();
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            //Pick up the call!
            call = incomingCall;
            mediaPlayer = MediaPlayer.create(VOIPService.this, R.raw.incoming);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            Intent callIntent = new Intent(VOIPService.this, VOIPActivity.class);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(callIntent);
        }
    }
}
