package org.wondertech.wonder;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amplitude.api.Amplitude;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class VerifyActivity extends Activity{

	private SharedPreferences userInfo;
	private TextView disPhone;
	private ImageButton up;
	private ArrayList<TextView> code;
	private EditText et;
	private Handler mHandler;
	private ImageButton resend;
	private SMSreceiver rec;
	private TextView privacy;
	private CountDownTimer cdt;
	private int[] ids = {R.id.code1, R.id.code2, R.id.code3, R.id.code4};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_verify);
		rec = new SMSreceiver();
		
		TransformFilter transformFilter = new TransformFilter() { public final String transformUrl(final Matcher match, String url) { return ""; } }; 
		privacy = (TextView)findViewById(R.id.verify_privacy);
		Pattern pattern = Pattern.compile("TOS");
		String scheme = "https://www.joinwonder.com/tos";
		Linkify.addLinks(privacy, pattern, scheme, null,
				transformFilter);
		pattern = Pattern.compile("Privacy Policy");
		scheme = "https://www.joinwonder.com/privacy";
		Linkify.addLinks(privacy, pattern, scheme, null,
				transformFilter);
		userInfo = getSharedPreferences("user_info", 0);
    	disPhone = (TextView)findViewById(R.id.dis_phone);
    	disPhone.setText(Utilities.phoneFormat(userInfo.getString("phone", "")));
    	code = new ArrayList<TextView>();
    	for (int i = 0; i < ids.length; ++i)
    		code.add((TextView)findViewById(ids[i]));
    	 
    	 mHandler = new Handler() {
             public void handleMessage(Message msg) {
            	 for (int i = 0; i < ids.length; ++i)
            	 {
            		 YoYo.with(Techniques.Shake)
              	    .duration(800)
              	    .playOn(findViewById(ids[i]));
            	 }
             }
         };
         
         et = (EditText)findViewById(R.id.help_edit);
         et.requestFocus();
         et.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String t = et.getText().toString();
				
				for (int i = 0; i < t.length(); ++i)
				{
					code.get(i).setText("" + t.charAt(i));
				}
				for (int i = t.length(); i < 4; ++i)
				{
					code.get(i).setText("");
				}
				if (t.length() == 4)
				{
					new Verification().execute();
				}
			}
        	 
         });
    	 
    	up = (ImageButton)findViewById(R.id.up);
    	up.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				VerifyActivity.this.finish();
			}
		});
    	
    	resend = (ImageButton)findViewById(R.id.resend_code);
    	resend.setEnabled(false);
   	 	resend.setImageResource(R.drawable.resend_verification_button);
    	cdt = new CountDownTimer(10000, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 
		     }

		     public void onFinish() {
		    	 resend.setEnabled(true);
		    	 resend.setImageResource(R.drawable.resend_verification_button_able);
		     }
		  };
		  cdt.start();
		  
    	resend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Amplitude.logEvent("Verification Code Resend Requested");
				cdt.start();
				resend.setEnabled(false);
		   	 	resend.setImageResource(R.drawable.resend_verification_button);
				new GetVerificationCode().execute();
			}
		});
    	
    	
	}
	
	class Verification extends AsyncTask<Void, Void, Void>{
		protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "checkVerificationCode";
			HttpClient httpClient = new DefaultHttpClient();
			try
			{
				HttpPost request = new HttpPost(TargetURL);
                JSONObject json = new JSONObject();
                json.put("country_code", userInfo.getString("country_code", ""));
                json.put("phone", userInfo.getString("phone", ""));
                json.put("app_version", userInfo.getString("app_version", ""));
                json.put("uuid", userInfo.getString("uuid", ""));
                json.put("verification_code", et.getText().toString());
                
                     
                StringEntity se = new StringEntity( json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                request.setEntity(se);
				HttpResponse httpResponse = httpClient.execute(request);
				if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
				{
					String retSrc = EntityUtils.toString(httpResponse.getEntity());
					JSONObject result = new JSONObject(retSrc);
					userInfo.edit().putString("client_id", result.getString("client_id")).apply();
					startActivity(new Intent(VerifyActivity.this, SellingAnimation.class));
				}
				else
				{
					mHandler.obtainMessage(1).sendToTarget();
				}
				
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	private class GetVerificationCode extends AsyncTask<Void, Void, Void>{
    	protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "instantiateUser";
			HttpClient httpClient = new DefaultHttpClient();
			try
			{
				HttpPost request = new HttpPost(TargetURL);
				String uuid = userInfo.getString("uuid", "");
                JSONObject json = new JSONObject();
                json.put("country_code", userInfo.getString("country_code", ""));
                json.put("phone", userInfo.getString("phone", ""));
                json.put("app_version", userInfo.getString("app_version", ""));
                json.put("os_type", userInfo.getString("os_type", ""));
                json.put("uuid", uuid);
                                     
                StringEntity se = new StringEntity( json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                request.setEntity(se);
				HttpResponse httpResponse = httpClient.execute(request);
				if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
				{
					
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        Amplitude.startSession();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(rec, filter);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        Amplitude.logEvent("Verify View Opened");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(rec);
	}
	
	private class SMSreceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					"android.provider.Telephony.SMS_RECEIVED"))
			{
				String sb;
				Bundle bundle = intent.getExtras();
				
				if (!bundle.isEmpty())
				{
					Object[] pdus = (Object[])bundle.get("pdus");
					SmsMessage[] messages = new SmsMessage[pdus.length];
					for (int i = 0; i < pdus.length; ++i)
					{
						messages[i] = SmsMessage.
								createFromPdu((byte[])pdus[i]);
					}
					for (SmsMessage message : messages)
					{
						sb = message.getDisplayMessageBody();
						sb = sb.substring(0, 4);
						et.setText(sb);
					}					
				}
			}				
		}	
	}
	
	@Override
    public void onPause() {
		super.onPause();
		Amplitude.endSession();
	}
	
}