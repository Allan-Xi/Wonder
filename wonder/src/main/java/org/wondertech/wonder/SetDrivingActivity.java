package org.wondertech.wonder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.wondertech.wonder.AsyncTasks.SetDrivingTask;

public class SetDrivingActivity extends Activity {
	private SharedPreferences userInfo;
	int remain_time;
	CountDownTimer cdt;
	private String s;
	SpannableString ss1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set_driving);
		remain_time = 31;
		userInfo = getSharedPreferences("user_info", 0);
		TextView tv = (TextView)findViewById(R.id.not_disturb_2);
		tv.setText("\uD83D\uDD34 \uD83D\uDE97");
		Button endtrip = (Button)findViewById(R.id.end_trip_2);
		endtrip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SetDrivingTask(SetDrivingActivity.this).execute(false);
				userInfo.edit().putBoolean("autoDriving", false).apply();
				Intent intent1 = new Intent(SetDrivingActivity.this, MainActivity.class);
				intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent1);
			}
		});

		
		final TextView countdown = (TextView)findViewById(R.id.countdown_2);
		ImageButton minus_time = (ImageButton)findViewById(R.id.minus_time_2);
		ImageButton plus_time = (ImageButton)findViewById(R.id.plus_time_2);
		TextView autoDetection = (TextView)findViewById(R.id.set_auto);
		
		if (getIntent().hasExtra("auto")) {
			countdown.setVisibility(View.GONE);
			minus_time.setVisibility(View.GONE);
			plus_time.setVisibility(View.GONE);
			autoDetection.setVisibility(View.VISIBLE);
			
		}
		else {
			countdown.setVisibility(View.VISIBLE);
			minus_time.setVisibility(View.VISIBLE);
			plus_time.setVisibility(View.VISIBLE);
			autoDetection.setVisibility(View.GONE);
			userInfo.edit().putBoolean("isDriving", true).apply();
			new SetDrivngTask().execute(remain_time * 60);
			cdt = new CountDownTimer(remain_time * 60000, 60000) {

		     public void onTick(long millisUntilFinished) {
		    	s = Long.toString(millisUntilFinished/60000) + "\nmins";
		 		ss1=  new SpannableString(s);
		 		ss1.setSpan(new RelativeSizeSpan(2.5f), 0, s.indexOf("\n"), 0);
		 		countdown.setText(ss1);
		     }

		     public void onFinish() {
		    	 new SetDrivingTask(SetDrivingActivity.this).execute(false);
		     }
		  };
		  cdt.start();
		minus_time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (remain_time >= 5)
					remain_time -= 5;
				else
					remain_time = 0;
			    s = Integer.toString(remain_time) + "\nmins";
		 		ss1=  new SpannableString(s);
		 		ss1.setSpan(new RelativeSizeSpan(2.5f), 0, s.indexOf("\n"), 0);
		 		countdown.setText(ss1);
					cdt.cancel();
					new SetDrivngTask().execute(remain_time * 60);
				cdt = new CountDownTimer(remain_time * 60000, 60000) {

				     public void onTick(long millisUntilFinished) {
				    	 s = Long.toString(millisUntilFinished/60000) + "\nmins";
					 		ss1=  new SpannableString(s);
					 		ss1.setSpan(new RelativeSizeSpan(2.5f), 0, s.indexOf("\n"), 0);
					 		countdown.setText(ss1);
				     }

				     public void onFinish() {
				    	 new SetDrivingTask(SetDrivingActivity.this).execute(false);
				     }
				  };
				  cdt.start();

			}
		});
		
		plus_time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    remain_time += 5;
			    s = Integer.toString(remain_time) + "\nmins";
		 		ss1=  new SpannableString(s);
		 		ss1.setSpan(new RelativeSizeSpan(2.5f), 0, s.indexOf("\n"), 0);
		 		countdown.setText(ss1);
					cdt.cancel();
				new SetDrivngTask().execute(remain_time * 60);
			    cdt = new CountDownTimer(remain_time * 60000, 60000) {

				     public void onTick(long millisUntilFinished) {
				    	 s = Long.toString(millisUntilFinished/60000) + "\nmins";
					 		ss1=  new SpannableString(s);
					 		ss1.setSpan(new RelativeSizeSpan(2.5f), 0, s.indexOf("\n"), 0);
					 		countdown.setText(ss1);
					 }

				     public void onFinish() {
				    	 new SetDrivingTask(SetDrivingActivity.this).execute(false);
				     }
				  };
				  cdt.start();
			}
		});
		}
	}

	class SetDrivngTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
        	userInfo.edit().putBoolean("isDriving", true).apply();
        	String TargetURL = userInfo.getString("URL", "") + "isDriving";
			HttpClient httpClient = new DefaultHttpClient();
			try
			{
				HttpPost request = new HttpPost(TargetURL);
	            JSONObject json = new JSONObject();
				json.put("app_version", userInfo.getString("app_version", ""));
	            json.put("uuid", userInfo.getString("uuid", ""));
	            json.put("client_id", userInfo.getString("client_id", ""));
	            json.put("auto_mode", false);
	            json.put("is_driving", true);
	            json.put("secs_remaining", params[0]);

	            StringEntity se = new StringEntity( json.toString());
	            Log.v("request", json.toString());
	            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	            request.setEntity(se);
				HttpResponse httpResponse = httpClient.execute(request);
				if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
				{
					Log.v("isDriving", Integer.toString(params[0]));
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
        }
    }
	
	@Override
	public void onBackPressed() {
	}
}
