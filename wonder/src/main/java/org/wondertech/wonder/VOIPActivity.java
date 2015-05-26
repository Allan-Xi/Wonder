package org.wondertech.wonder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;
import org.wondertech.wonder.data.WonderContract;
import org.wondertech.wonder.services.GCMNotificationIntentService;
import org.wondertech.wonder.services.VOIPService;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class VOIPActivity extends Activity {
	private Call call;
	private int PAGE;
	private SharedPreferences userInfo;
	private String phone;
	private LinearLayout buttons;
	private ImageButton endCall;
	private MediaPlayer mediaPlayer;
	private TextView name;
	private TextView ring;
	private int recLen = 0;
	private final static int INTERVAL = 1000;
	private static RoundImageView pic;
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_voip);
    	userInfo = getSharedPreferences("user_info", 0);
		phone = getIntent().getStringExtra("phone");
		ImageButton hangup = (ImageButton)findViewById(R.id.voip_hang_up);
		ImageButton accept = (ImageButton)findViewById(R.id.voip_accept);
		name = (TextView)findViewById(R.id.voip_name);
		pic = (RoundImageView)findViewById(R.id.voip_pic);
		
		ring = (TextView)findViewById(R.id.voip_ring);
		endCall = (ImageButton)findViewById(R.id.voip_end_call);
		buttons = (LinearLayout)findViewById(R.id.voip_buttons);

		if (phone == null)
		{
			call = VOIPService.call;
			PAGE = 0;
			buttons.setVisibility(View.VISIBLE);
    		endCall.setVisibility(View.GONE);
    		if (call.getCallId() != null){
				setName(call.getRemoteUserId());
			}
    		else{
				setName(GCMNotificationIntentService.callFromPhone);
			}
		}
		else
		{
			setName(phone);
			CallClient callClient = VOIPService.sinchClient.getCallClient();
			call = callClient.callUser(phone);
			PAGE = 1;
			endCall.setVisibility(View.VISIBLE);
    		buttons.setVisibility(View.GONE);
    		ring.setText("ringing...");
		}
		call.addCallListener(new SinchCallListener());
		hangup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				call.hangup();
			}
		});
		
		accept.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				call.answer();
			}
		});
		
		endCall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				call.hangup();
			}
		});
		
		
	}
	
	private class SinchCallListener implements CallListener {
	    @Override
	    public void onCallEnded(Call endedCall) {
	    	setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
	    	if (PAGE == 1)
	    	{
	    		if (mediaPlayer != null)
					mediaPlayer.stop();
				finish();
	    	}
	    	else
	    	{
	    		if (VOIPService.mediaPlayer != null)
					VOIPService.mediaPlayer.stop();
				startActivity(new Intent(VOIPActivity.this, MainActivity.class));
	    	}
	    }

	    @Override
	    public void onCallEstablished(Call establishedCall) {
	        //incoming call was picked up
	    	setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
	    	handler.postDelayed(runnable, INTERVAL);
	    	if (PAGE == 0)
	    	{
	    		endCall.setVisibility(View.VISIBLE);
	    		buttons.setVisibility(View.GONE);
				if (call.getCallId() != null){
					setName(call.getRemoteUserId());
				}
				else{
					setName(GCMNotificationIntentService.callFromPhone);
				}
	    	}
	    	if (PAGE == 1)
	    	{
	    		mediaPlayer.stop();
	    	}
	    	else
				VOIPService.mediaPlayer.stop();
	    }

	    @Override
	    public void onCallProgressing(Call progressingCall) {
	        //call is ringing
	    	if (PAGE == 1)
	    	{
	    		mediaPlayer = MediaPlayer.create(VOIPActivity.this, R.raw.ringback);
		    	mediaPlayer.setLooping(true);
		    	mediaPlayer.start();
	    	}	 
	    	else
	    	{
				if (call.getCallId() != null){
					setName(call.getRemoteUserId());
				}
				else{
					setName(GCMNotificationIntentService.callFromPhone);
				}
	    	}
	    	
	    	
	    }

	    @Override
	    public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
	        //don't worry about this right now
	    	new startRTCCall().execute(pushPairs.get(0).getPushPayload());
	    }
	    
	}
	
    private class startRTCCall extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String TargetURL = userInfo.getString("URL", "") + "startRTCCall";
			HttpURLConnection urlConnection = null;
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
				json.put("phone", phone);
				json.put("voip_info", params[0]);

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
					Log.v("caller", "ok");
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
    
    Handler handler = new Handler();  
    Runnable runnable = new Runnable() {  
        @Override  
        public void run() {  
            recLen++;  
            ring.setText(DateUtils.formatElapsedTime(recLen));  
            handler.postDelayed(this, INTERVAL);
        }  
    };

	private void setName(String phone){
		Cursor cur = getContentResolver().query(
				WonderContract.ContactEntry.CONTENT_URI,
				new String[]{WonderContract.ContactEntry.COLUMN_NAME,
						WonderContract.ContactEntry.COLUMN_PHONE},
				WonderContract.ContactEntry.COLUMN_PHONE + " =?",
				new String[]{phone},
				null
		);
		try {
			if (cur.getCount() != 0) {
				cur.moveToFirst();
				name.setText(cur.getString(cur.getColumnIndex(WonderContract.ContactEntry.COLUMN_NAME)));
			} else
				name.setText("Unknown");
		} finally {
			cur.close();
		}
		Bitmap photo = new QuickContactHelper(this, phone).getThumbnail();
		if (photo != null) {
			pic.setImageBitmap(photo);
		}
		else {
			pic.setImageResource(R.drawable.profile_circle);
		}
	}
}
