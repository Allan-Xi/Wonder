package org.wondertech.wonder;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.amplitude.api.Amplitude;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RegisterActivity extends Activity {
	
	private SharedPreferences userInfo;
    private GoogleCloudMessaging gcm;          
    private String token_id;
    private String uuid;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private DisplayMetrics dm;
    private EditText phone;
    private FrameLayout layout;
    private ProgressDialog progressDialog;
    private ImageView bg;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	userInfo = getSharedPreferences("user_info", 0);
    	
        final TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        uuid = tManager.getDeviceId();
        userInfo.edit().putString("uuid", uuid).apply();
        userInfo.edit().putInt("messageNum", 0).apply();
        
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        
        dm = new DisplayMetrics(); 
        getWindowManager().getDefaultDisplay().getMetrics(dm);  
        //float width = dm.widthPixels/dm.density;
        //Log.v("width", Float.toString(width));
        
        layout = (FrameLayout)findViewById(R.id.register_layout);
        bg = (ImageView)findViewById(R.id.register_bg);
        
        final Button getCode = (Button)findViewById(R.id.verify);
        getCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (phone.getText().toString().length() == 10)
				{
					Amplitude.setUserId(phone.getText().toString());
					JSONObject eventProperties = new JSONObject();
					try {
					    eventProperties.put("Phone", phone.getText().toString());
					} catch (JSONException exception) {
					}
					Amplitude.logEvent("Verification Code Requested", eventProperties);
					if (progressDialog == null) {
					       progressDialog = Utilities.createProgressDialog(RegisterActivity.this);
					       progressDialog.show();
					       } else {
					       progressDialog.show();
					       }
					new GetVerificationCode().execute();
				}
			}
		});
        
        phone = (EditText)findViewById(R.id.phone);
        phone.setHintTextColor(Color.parseColor("#cacaca"));
        
        layout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = layout.getRootView().getHeight() - layout.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                	getCode.setVisibility(View.VISIBLE);
        	    	//phone.setText(tManager.getLine1Number());
        	    	bg.setImageResource(R.drawable.splash_page);
                }
                else
                {
                	getCode.setVisibility(View.GONE);
        	    	bg.setImageResource(R.drawable.firstpage);
                }
             }
        });

        
        if (checkPlayServices()) {             
			gcm = GoogleCloudMessaging.getInstance(this);
			new Register().execute(); 
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
				finish();         
			}       
			return false;     
			}     
		return true; 
	}
    
    private class GetVerificationCode extends AsyncTask<Void, Void, Void>{
    	protected Void doInBackground(Void... params) {
            userInfo.edit().putString("os_type", "Android API " + Integer.
            		toString(android.os.Build.VERSION.SDK_INT)).apply();
            userInfo.edit().putString("country_code", "+1").apply();
            
            String TargetURL = userInfo.getString("URL", "") + "instantiateUser";
			HttpClient httpClient = new DefaultHttpClient();
			try
			{
				HttpPost request = new HttpPost(TargetURL);
				uuid = userInfo.getString("uuid", "");
                JSONObject json = new JSONObject();
                json.put("country_code", userInfo.getString("country_code", "+1"));
                EditText et = (EditText)findViewById(R.id.phone);
                userInfo.edit().putString("phone", PhoneNumberUtils.stripSeparators(et.getText().toString())).commit();
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
					progressDialog.dismiss();
					startActivity(new Intent(RegisterActivity.this, VerifyActivity.class));
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}
    
    private class Register extends AsyncTask<Void, Void, Void>{
    	protected Void doInBackground(Void... params) {
		try {
				token_id = gcm.register(Utilities.SENDER_ID);
				userInfo.edit().putString("token_id", token_id).commit();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return null; 	
		}
	}
    
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        overridePendingTransition(0,0);   
        Amplitude.startSession();
        Amplitude.logEvent("Landing View Opened");
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
