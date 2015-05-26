package org.wondertech.wonder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class ProtectDriver extends Activity {
    private SharedPreferences userInfo;
    private GoogleCloudMessaging gcm;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static float INVISIBLE = 0;
    private final static float VISIBLE = 1;
    private final static long DURATION = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_protect_driver);
        userInfo = getSharedPreferences("user_info", 0);
        ImageView phone = (ImageView)findViewById(R.id.protect_phone);
        ImageView logo = (ImageView)findViewById(R.id.protect_logo);
        ImageView alert = (ImageView)findViewById(R.id.protect_notification);
        ImageView arrow = (ImageView)findViewById(R.id.protect_arrow);
        final ImageButton ok = (ImageButton)findViewById(R.id.protect_ok);

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            new Register().execute();
        }

        ObjectAnimator anim1 = ObjectAnimator.ofFloat(phone, "alpha",
                INVISIBLE,VISIBLE);
        anim1.setDuration(DURATION);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(logo, "alpha",
                INVISIBLE,VISIBLE);
        anim2.setDuration(DURATION);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(alert, "alpha",
                INVISIBLE,VISIBLE);
        anim3.setDuration(DURATION);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(arrow, "alpha",
                INVISIBLE,VISIBLE);
        anim4.setDuration(DURATION);
        anim3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ok.setAlpha(VISIBLE);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new setTokenID().execute();
                    }
                });
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(anim1).before(anim2);
        set.play(anim2).before(anim3);
        set.play(anim3).with(anim4);
        set.start();
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
                finish();
            }
            return false;
        }
        return true;
    }
    private class Register extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            try {
                String token_id = gcm.register(Utilities.SENDER_ID);
                userInfo.edit().putString("token_id", token_id).commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class setTokenID extends AsyncTask<Void, Void, Void>{
        protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "setTokenID";
            HttpURLConnection urlConnection = null;
            StringBuilder sb = new StringBuilder();
            try
            {
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
                json.put("token_id", userInfo.getString("token_id", ""));

                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                try{
                    out.write(json.toString());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    out.close();
                }

                int HttpResult =urlConnection.getResponseCode();
                if(HttpResult == HttpURLConnection.HTTP_OK){
                    startActivity(new Intent(ProtectDriver.this, EnableDriving.class));
                }
                else{
                    System.out.println(urlConnection.getResponseMessage());
                }
            } catch (MalformedURLException e) {

                e.printStackTrace();
            }
            catch (IOException e) {

                e.printStackTrace();

            } catch (JSONException e) {

                e.printStackTrace();

            }finally{
                if(urlConnection!=null)
                    urlConnection.disconnect();
            }
            return null;
        }
    }
}
