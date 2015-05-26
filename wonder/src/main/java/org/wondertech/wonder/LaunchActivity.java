package org.wondertech.wonder;

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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

import com.amplitude.api.Amplitude;
import com.viewpagerindicator.CirclePageIndicator;

public class LaunchActivity extends FragmentActivity {
	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private SharedPreferences userInfo;
	private ImageButton done;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_launch);
		userInfo = getSharedPreferences("user_info", 0);
		
		mPager = (ViewPager)findViewById(R.id.launch_pager);
		//mPager.setCurrentItem(0);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        final CirclePageIndicator mIndicator = (CirclePageIndicator)findViewById(R.id.launch_indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setSnap(true);
        done = (ImageButton)findViewById(R.id.slide_done);
        
  	    done.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (PreferenceManager.getDefaultSharedPreferences(LaunchActivity.this).getBoolean("detectDriving",false))
				{
					new AlertDialog.Builder(LaunchActivity.this)
					.setMessage("When a contact does not have Wonder and sends you " +
							"a text when you're driving, would you like to auto " +
							"respond with \"I'm driving right now. I'll get back to you soon.\"?").setTitle("Enable Auto Reply?")
					.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        	PreferenceManager.getDefaultSharedPreferences(LaunchActivity.this).
		            		edit().putBoolean("autoReply",true).apply();
				        	JSONObject eventProperties = new JSONObject();
				        	try {
				        	    eventProperties.put("Answer", "Yes");
				        	} catch (JSONException exception) {
				        	}
				        	Amplitude.logEvent("Onboard Auto Reply Enabled", eventProperties);
				        	if (progressDialog == null) {
							       progressDialog = Utilities.createProgressDialog(LaunchActivity.this);
							       progressDialog.show();
							       } else {
							       progressDialog.show();
							       }
							new setTokenID().execute();	
				        }
				     })
				    .setNegativeButton("No", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        	PreferenceManager.getDefaultSharedPreferences(LaunchActivity.this).
		            		edit().putBoolean("autoReply",false).apply();
				        	JSONObject eventProperties = new JSONObject();
				        	try {
				        	    eventProperties.put("Answer", "No");
				        	} catch (JSONException exception) {
				        	}
				        	Amplitude.logEvent("Onboard Auto Reply Enabled", eventProperties);
				        	if (progressDialog == null) {
							       progressDialog = Utilities.createProgressDialog(LaunchActivity.this);
							       progressDialog.show();
							       } else {
							       progressDialog.show();
							       }
							new setTokenID().execute();	
				        }				        	
				     })
				     .setIcon(R.drawable.biglogo)
				     .show();
				}
				else
				{
					if (progressDialog == null) {
					       progressDialog = Utilities.createProgressDialog(LaunchActivity.this);
					       progressDialog.show();
					       } else {
					       progressDialog.show();
					       }
					new setTokenID().execute();	
				}
				
				
				
									}
		});
  	    mIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == 3)
					done.setVisibility(View.VISIBLE);
				else
					done.setVisibility(View.GONE);
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        
	}
	
	/**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return slideFragment.create(position);
        }

        @Override
        public int getCount() {
            return 4;
        }
    }
    
    class setTokenID extends AsyncTask<Void, Void, Void>{
		protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "setTokenID";
			HttpClient httpClient = new DefaultHttpClient();
			try
			{
				HttpPost request = new HttpPost(TargetURL);
                JSONObject json = new JSONObject();
                json.put("app_version", userInfo.getString("app_version", ""));
                json.put("uuid", userInfo.getString("uuid", ""));
                json.put("client_id", userInfo.getString("client_id", ""));
                json.put("token_id", userInfo.getString("token_id", ""));
                     
                StringEntity se = new StringEntity( json.toString());
                Log.v("request", json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                request.setEntity(se);
				HttpResponse httpResponse = httpClient.execute(request);
				if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
				{
					Amplitude.logEvent("Signup Complete");
					new FinalizeRegistration().execute();
									}
				
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}
	
	class FinalizeRegistration extends AsyncTask<Void, Void, Void>{
		protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "finalizeRegistration";
			HttpClient httpClient = new DefaultHttpClient();
			try
			{
				HttpPost request = new HttpPost(TargetURL);
                JSONObject json = new JSONObject();
                json.put("app_version", userInfo.getString("app_version", ""));
                json.put("uuid", userInfo.getString("uuid", ""));
                json.put("client_id", userInfo.getString("client_id", ""));
                json.put("first_name", "");
                json.put("last_name", "");
                     
               StringEntity se = new StringEntity( json.toString());
                Log.v("request", json.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                request.setEntity(se);
				HttpResponse httpResponse = httpClient.execute(request);
				if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
				{
					startActivity(new Intent(LaunchActivity.this, MainActivity.class));	
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
        Amplitude.logEvent("Explanation View Opened");
        if (progressDialog != null)
        	progressDialog.dismiss();   
    }
	@Override
	public void onBackPressed() {
    	Utilities.goBackHome(this);
	}
	
	@Override
    public void onPause() {
		super.onPause();
		Amplitude.endSession();
	}
	
}
