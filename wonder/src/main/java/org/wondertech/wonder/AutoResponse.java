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
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.ArcTranslateAnimation;
import org.wondertech.wonder.Utils.Utilities;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class AutoResponse extends Activity {
    private final static float INVISIBLE = 0;
    private final static float VISIBLE = 1;
    private final static long DURATION = 2000;
    private static float TRANSDEVIATE = 200;
    private static int BUBBLEMOVEDURATION = 1100;
    private ImageView bubble;
    private ImageView notification;
    private SharedPreferences userInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_auto_response);
        userInfo = getSharedPreferences("user_info", 0);
        bubble = new ImageView(this);
        bubble.setImageResource(R.drawable.message_bubble);
        notification = new ImageView(this);
        notification.setImageResource(R.drawable.response_notification);
        final RelativeLayout layout = (RelativeLayout)findViewById(R.id.response_layout);
        layout.addView(bubble);
        bubble.setVisibility(View.INVISIBLE);
        layout.addView(notification);
        notification.setVisibility(View.INVISIBLE);
        bubble.setVisibility(View.VISIBLE);
        notification.setVisibility(View.VISIBLE);
        notification.setAlpha(INVISIBLE);
        final ImageView leftView = (ImageView)findViewById(R.id.response_left);
        final ImageView rightView = (ImageView)findViewById(R.id.response_right);
        ObjectAnimator anim = ObjectAnimator.ofFloat(bubble, "alpha", INVISIBLE, INVISIBLE);
        anim.setDuration(DURATION);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                final ArcTranslateAnimation anim1 = new ArcTranslateAnimation(
                        (leftView.getLeft() + leftView.getRight()) / 2 - bubble.getWidth() / 2,
                        (rightView.getLeft() + rightView.getRight()) / 2 - bubble.getWidth() / 2,
                        (leftView.getTop() + leftView.getBottom()) / 2 - bubble.getHeight() / 2,
                        (leftView.getTop() + leftView.getBottom()) / 2 - bubble.getHeight() / 2,
                        -TRANSDEVIATE);
                anim1.setDuration(BUBBLEMOVEDURATION);
                anim1.setFillAfter(true);

                anim1.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        ObjectAnimator anim0 = ObjectAnimator.ofFloat(bubble, "alpha", INVISIBLE, VISIBLE);
                        anim0.setDuration(BUBBLEMOVEDURATION/2 );
                        ObjectAnimator anim1 = ObjectAnimator.ofFloat(bubble, "alpha", VISIBLE, INVISIBLE);
                        anim1.setDuration(BUBBLEMOVEDURATION/2 );
                        AnimatorSet set = new AnimatorSet();
                        set.play(anim0).before(anim1);
                        set.start();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ArcTranslateAnimation anim2 = new ArcTranslateAnimation(
                                (rightView.getLeft() + rightView.getRight()) / 2 - notification.getWidth() / 2,
                                (leftView.getLeft() + leftView.getRight()) / 2 - notification.getWidth() / 2,
                                (leftView.getTop() + leftView.getBottom()) / 2 - notification.getHeight() / 2,
                                (leftView.getTop() + leftView.getBottom()) / 2 - notification.getHeight() / 2,
                                TRANSDEVIATE);
                        anim2.setDuration(BUBBLEMOVEDURATION);
                        anim2.setFillAfter(true);
                        anim2.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                ObjectAnimator anim0 = ObjectAnimator.ofFloat(notification, "alpha", INVISIBLE, VISIBLE);
                                anim0.setDuration(BUBBLEMOVEDURATION/2 );
                                ObjectAnimator anim1 = ObjectAnimator.ofFloat(notification, "alpha", VISIBLE, INVISIBLE);
                                anim1.setDuration(BUBBLEMOVEDURATION/2 );
                                AnimatorSet set = new AnimatorSet();
                                set.play(anim0).before(anim1);
                                set.start();
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                bubble.startAnimation(anim1);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        notification.startAnimation(anim2);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                bubble.startAnimation(anim1);
            }
        });
        anim.start();

        ImageButton enable = (ImageButton)findViewById(R.id.response_enable);
        ImageButton disable = (ImageButton)findViewById(R.id.response_disable);
        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.getDefaultSharedPreferences(AutoResponse.this).
                        edit().putBoolean("autoReply",true).apply();
                new FinalizeRegistration().execute();
            }
        });
        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.getDefaultSharedPreferences(AutoResponse.this).
                        edit().putBoolean("autoReply",false).apply();
                new FinalizeRegistration().execute();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    class FinalizeRegistration extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "finalizeRegistration";
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
                json.put("first_name", "");
                json.put("last_name", "");

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
                if(HttpResult ==HttpURLConnection.HTTP_OK){
                    startActivity(new Intent(AutoResponse.this, MainActivity.class));
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
