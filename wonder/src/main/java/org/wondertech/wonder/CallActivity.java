package org.wondertech.wonder;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amplitude.api.Amplitude;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.data.WonderContract;

public class CallActivity extends Activity {
	private String phone;
	private TextView name;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		Intent intent = getIntent();
		String ID = intent.getStringExtra("MessageId");
		String mes = intent.getStringExtra("Message");
		name = (TextView)findViewById(R.id.call_name);
		TextView time = (TextView)findViewById(R.id.call_time); 
		TextView message = (TextView)findViewById(R.id.call_message);
        final String[] tmp = ID.split("-", 3);
        phone = tmp[0];
		setName(phone);
        String type = tmp[1];
        time.setText(DateUtils.getRelativeTimeSpanString((long) (Double.parseDouble(tmp[2])),
				System.currentTimeMillis(),
				DateUtils.SECOND_IN_MILLIS));
        Bitmap photo = new QuickContactHelper(this, phone).getThumbnail();
		RoundImageView pic = (RoundImageView)findViewById(R.id.call_pic);
		if (photo != null) {
			pic.setImageBitmap(photo);
		} else {
			pic.setImageResource(R.drawable.profile_circle);
		}

        if (type.equals("0")) {
        	message.setText(mes);
        } else {
        	message.setText("Sent you a call request");
        }
        
        ImageButton call = (ImageButton)findViewById(R.id.call_call);
        call.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        JSONObject eventProperties = new JSONObject();
		        try {
		            eventProperties.put("Type", "Phone");
		        } catch (JSONException exception) {
		        }
		        Amplitude.logEvent("Response Made", eventProperties);
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"  
		                + phone));  
		        startActivity(intent); 
			}
		});
        ImageButton SMS = (ImageButton)findViewById(R.id.call_message_message);
        SMS.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				JSONObject eventProperties = new JSONObject();
		        try {
		            eventProperties.put("Type", "Message");
		        } catch (JSONException exception) {
		        }
		        Amplitude.logEvent("Response Made", eventProperties);
				Uri uri = Uri.parse("smsto:" + phone);
			    Intent sendIntent = new Intent(Intent.ACTION_VIEW, uri);
			    sendIntent.putExtra("exit_on_sent", true);
			    startActivity(sendIntent);
			}
		});
        ImageButton freeCall = (ImageButton)findViewById(R.id.call_free_call);
        freeCall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				JSONObject eventProperties = new JSONObject();
				try {
					eventProperties.put("Type", "VoIP");
				} catch (JSONException exception) {
				}
				Amplitude.logEvent("Response Made", eventProperties);
				Intent callIntent = new Intent(CallActivity.this, VOIPActivity.class);
				callIntent.putExtra("phone", phone);
				startActivity(callIntent);
			}
		});
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.call, menu);
        getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setTitle("Back");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    public void onResume() {
        super.onResume();
        Amplitude.startSession();
        JSONObject eventProperties = new JSONObject();
        try {
            eventProperties.put("Contact", phone);
        } catch (JSONException exception) {
        }
        Amplitude.logEvent("Response View Opened", eventProperties);
	}
	
	@Override
    public void onPause() {
		super.onPause();
		Amplitude.endSession();
	}

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
	}

}
