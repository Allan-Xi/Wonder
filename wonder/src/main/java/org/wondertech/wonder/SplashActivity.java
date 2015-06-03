package org.wondertech.wonder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.amplitude.api.Amplitude;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import org.wondertech.wonder.Utils.Utilities;

import java.util.Timer;
import java.util.TimerTask;


public class SplashActivity extends Activity{
	private static SharedPreferences userInfo;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private Timer timer;
    private final static String serverURL = "https://prod.wonderdev.com/v1/";
    private final static String appVersion = "A1.52";
    private final static int delayTime = 1300;
    static final String TAG = "SplashActivity";

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        if (checkPlayServices()) {
            //set the server url and the app version, need to be manually updated
            userInfo = getSharedPreferences("user_info", 0);
            userInfo.edit().putString("URL", serverURL).apply();
            userInfo.edit().putString("app_version", appVersion).apply();

            //dev tools: crashlytics, amplitude, uservoice
            Crashlytics.start(this);
            Amplitude.initialize(this, "f2e30b127eaac53a789011a96c0e6a54");
            // Set this up once when your application launches
            Config config = new Config("joinwonder.uservoice.com");
            config.setForumId(282749);
            // config.identifyUser("USER_ID", "User Name", "email@example.com");
            UserVoice.init(config, this);

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_splash);
        }
	}
	
	@Override
    public void onResume() {
        super.onResume();
        if (checkPlayServices()){
            timer = new Timer();

            if (!userInfo.getBoolean("isVerified", false)) {
                final ImageButton getStarted = (ImageButton) findViewById(R.id.splash_get_started);
                final Handler mHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        getStarted.setVisibility(View.VISIBLE);
                        getStarted.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(SplashActivity.this, NewVerifyActivity.class));
                                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                            }
                        });
                    }
                };

                timer.schedule(new TimerTask() {
                    public void run() {
                        mHandler.obtainMessage(1).sendToTarget();
                    }
                }, delayTime);
            } else {
                timer.schedule(new TimerTask() {
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }

                }, delayTime);
            }
        }
        Amplitude.startSession();

    }
	
	@Override
    public void onPause() {
		super.onPause();
		Amplitude.endSession();
	}

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null){
            timer.cancel();
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Utilities.goBackHome(this);
    }
}
