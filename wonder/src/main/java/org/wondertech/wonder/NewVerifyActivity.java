package org.wondertech.wonder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.util.Linkify;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amplitude.api.Amplitude;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NewVerifyActivity extends Activity {
    private static SharedPreferences userInfo;
    private static String uuid;
    private AnimatorSet set;
    private TextView status;
    private Handler handler;
    private int checkAcessCount = 0;
    private int dotCount = 0;
    private TextView dots;
    private boolean dotDir = true;
    private ProgressDialog progressDialog = null;
    private static final int REQUESTCODE = 100;
    private static final int REPEATREQUESTMESSAGE = 200;
    private static final int VERIFIEDMESSAGE = 201;
    private static final int DOTANIMMESSAGE = 202;
    private static final int DOTPERIOD = 200;
    private static final int CHECKACCESSTIMES = 3;
    private static final int AFTERVERIFYINTERVAL = 2000;
    private static final int DOTNUMBER = 3;
    private static final float SMALLSIZE = 0.8f;
    private static final float BIGSIZE = 1.2f;
    private static final int ANIMDURATION = 1000;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_new_verify);

        userInfo = getSharedPreferences("user_info", 0);

        final TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        uuid = tManager.getDeviceId();
        userInfo.edit().putString("uuid", uuid).apply();

        //to be updated
        userInfo.edit().putInt("messageNum", 0).apply();

        set = new AnimatorSet();

        Linkify.TransformFilter transformFilter = new Linkify.TransformFilter() { public final String transformUrl(final Matcher match, String url) { return ""; } };
        final TextView privacy = (TextView)findViewById(R.id.new_verify_privacy);
        Pattern pattern = Pattern.compile("Terms of Service");
        String scheme = "https://www.joinwonder.com/tos";
        Linkify.addLinks(privacy, pattern, scheme, null,
                transformFilter);
        pattern = Pattern.compile("Privacy Policy");
        scheme = "https://www.joinwonder.com/privacy";
        Linkify.addLinks(privacy, pattern, scheme, null,
                transformFilter);

        final ImageButton sendSMS = (ImageButton)findViewById(R.id.new_verify_send);
        sendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressDialog == null) {
                    progressDialog = Utilities.createProgressDialog(NewVerifyActivity.this);
                    progressDialog.show();
                } else {
                    progressDialog.show();
                }
                new CreateUser().execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUESTCODE && resultCode == RESULT_OK) {
            setContentView(R.layout.activity_verifying);
            status = (TextView)findViewById(R.id.verifying_status);
            dots = (TextView)findViewById(R.id.verifying_dot);
            dots.setVisibility(View.VISIBLE);
            handler = new Handler()
            {
              @Override
              public void handleMessage(Message msg)
              {
                  if (msg.what == REPEATREQUESTMESSAGE)
                  {
                      if (checkAcessCount < CHECKACCESSTIMES)
                      {
                          new Timer().schedule(new TimerTask() {
                              public void run() {
                                  new CheckAccess().execute();
                              }
                          },  3 * (checkAcessCount + 1) * 1000);
                      }
                      else
                      {
                          dots.setVisibility(View.INVISIBLE);
                          status.setText(getString(R.string.verify_fail));
                          set.cancel();
                          new Timer().schedule(new TimerTask() {
                              public void run() {
                                  startActivity(new Intent(NewVerifyActivity.this, RegisterActivity.class));
                              }
                          }, AFTERVERIFYINTERVAL);
                      }

                  }
                  else if (msg.what == VERIFIEDMESSAGE)
                  {
                      dots.setVisibility(View.INVISIBLE);
                      status.setText(getString(R.string.verify_success));
                      set.cancel();
                      new Timer().schedule(new TimerTask()
                      {
                          public void run()
                          {
                              startActivity(new Intent(NewVerifyActivity.this, SellingAnimation.class));
                              overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                          }
                      }, AFTERVERIFYINTERVAL);
                  }
                  else if (msg.what == DOTANIMMESSAGE)
                  {
                      String t = "";
                      for (int i = 0; i <= dotCount; ++i) {
                          t += '.';
                      }
                      if (dotDir)
                        dotCount++;
                      else
                        dotCount--;
                      if (dotCount == DOTNUMBER)
                      {
                          dotCount = 1;
                          dotDir = false;
                      }
                      if (dotCount == -1)
                      {
                          dotCount = 1;
                          dotDir = true;
                      }
                      dots.setText(t);
                  }
                  super.handleMessage(msg);
              }
            };
            new CheckAccess().execute();
            new Timer().schedule(new TimerTask() {
                public void run() {
                    handler.sendEmptyMessage(DOTANIMMESSAGE);
                }
            }, 0, DOTPERIOD);

            ImageView circles = (ImageView)findViewById(R.id.verifying_circles);

            ObjectAnimator anim1 = ObjectAnimator.ofFloat(circles, "scaleX", SMALLSIZE, BIGSIZE);
            anim1.setDuration(ANIMDURATION);
            ObjectAnimator anim2 = ObjectAnimator.ofFloat(circles, "scaleY", SMALLSIZE, BIGSIZE);
            anim2.setDuration(ANIMDURATION);
            ObjectAnimator anim3 = ObjectAnimator.ofFloat(circles, "scaleX", BIGSIZE, SMALLSIZE);
            anim3.setDuration(ANIMDURATION);
            ObjectAnimator anim4 = ObjectAnimator.ofFloat(circles, "scaleY", BIGSIZE, SMALLSIZE);
            anim4.setDuration(ANIMDURATION);

            set.play(anim1).with(anim2);
            set.play(anim1).before(anim3);
            set.play(anim3).with(anim4);

            set.addListener(new AnimatorListenerAdapter() {

                private boolean mCanceled;

                @Override
                public void onAnimationStart(Animator animation) {
                    mCanceled = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mCanceled = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!mCanceled) {
                        animation.start();
                    }
                }
            });
            set.start();
        }
    }

    private class CheckAccess extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "checkAccess";
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
                json.put("client_id", userInfo.getString("client_id", ""));
                json.put("uuid", userInfo.getString("uuid", ""));

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
                if(HttpResult == HttpURLConnection.HTTP_UNAUTHORIZED){
                    handler.sendEmptyMessage(REPEATREQUESTMESSAGE);
                    checkAcessCount++;
                }
                else if(HttpResult == HttpURLConnection.HTTP_OK){
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream(),"utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    JSONObject result = new JSONObject(sb.toString());
                    userInfo.edit().putString("phone",result.getString("phone")).apply();
                    handler.sendEmptyMessage(VERIFIEDMESSAGE);
                }
                else{
                    startActivity(new Intent(NewVerifyActivity.this, RegisterActivity.class));
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

    private class CreateUser extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            userInfo.edit().putString("os_type", "Android API " + Integer.
                    toString(android.os.Build.VERSION.SDK_INT)).apply();

            String TargetURL = userInfo.getString("URL", "") + "createUser";
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
                json.put("os_type", userInfo.getString("os_type", ""));
                json.put("uuid", userInfo.getString("uuid", ""));

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
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream(),"utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    JSONObject result = new JSONObject(sb.toString());
                    userInfo.edit().putString("client_id",result.getString("client_id")).apply();
                    Intent sendIntent = new Intent(NewVerifyActivity.this, VerifyMessage.class);
                    sendIntent.putExtra("twilio_phone",
                            result.getString("twilio_phone"));
                    sendIntent.putExtra("sms_body",
                            result.getString("sms_hash"));
                    startActivityForResult(sendIntent, REQUESTCODE);
                }
                else{
                    startActivity(new Intent(NewVerifyActivity.this, RegisterActivity.class));
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Amplitude.startSession();
        Amplitude.logEvent("Verify View Opened");
    }

    @Override
    public void onPause() {
        super.onPause();
        Amplitude.endSession();
    }

    @Override
    public void onBackPressed() {
        Utilities.goBackHome(this);
    }
}
