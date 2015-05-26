package org.wondertech.wonder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amplitude.api.Amplitude;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;
import org.wondertech.wonder.data.WonderContract;

import java.util.ArrayList;

public class InviteMessageActivity extends Activity {
	private SharedPreferences userInfo;
	private String phone;
	private EditText et;
	private ProgressDialog progressDialog;
	private String name;

	private Cursor cursor;

	private static final String[] PROJECTION = new String[] {
			WonderContract.ContactEntry.COLUMN_NAME,
			WonderContract.ContactEntry.COLUMN_PHONE,
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_message);
		final Intent intent = getIntent();
		phone = intent.getStringExtra("phone");
		userInfo = getSharedPreferences("user_info", 0);
		try {
			cursor = getContentResolver().query(
					WonderContract.ContactEntry.CONTENT_URI,
					PROJECTION,
					WonderContract.ContactEntry.COLUMN_PHONE + "= ?",
					new String[]{phone},
					null
			);
			cursor.moveToFirst();
			int nameIndex = cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_NAME);
			name = cursor.getString(nameIndex);
		}finally {
			cursor.close();
		}
		TextView tv = (TextView)findViewById(R.id.invite_receiver);
		et = (EditText)findViewById(R.id.invite_content);
		Button btn = (Button)findViewById(R.id.invite_message);
		if (intent.hasExtra("text"))
		{
			tv.setText("For: " + name);
			et.setText(intent.getStringExtra("text"));
		}
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (progressDialog == null) {
					progressDialog = Utilities.createProgressDialog(InviteMessageActivity.this);
				}
				progressDialog.show();
				String content = et.getEditableText().toString();
					
					if (intent.hasExtra("text"))
					{
						JSONObject eventProperties = new JSONObject();
						try {
						    eventProperties.put("Recipient", phone);
						    eventProperties.put("Sender", userInfo.getString("phone", ""));
						    if (getIntent().getBooleanExtra("onboard", true))
						    	eventProperties.put("View", "Onboard Invite");
						    else
						    	eventProperties.put("View", "Invite");
						} catch (JSONException exception) {
						}
						Amplitude.logEvent("Invite", eventProperties);
						SmsManager sm = SmsManager.getDefault();

						ArrayList<String> texts = sm.divideMessage(content);
						for (int i = 0; i < texts.size(); ++i)
						{
							sm.sendTextMessage(phone,null,texts.get(i),null,null);
						}

						ContentValues cv = new ContentValues();
						cv.put(WonderContract.ContactEntry.COLUMN_INVITED, 1);
						getContentResolver().update(
								WonderContract.ContactEntry.CONTENT_URI,
								cv,
								WonderContract.ContactEntry.COLUMN_PHONE + " =?",
								new String[]{phone}
						);

                        Intent returnIntent = new Intent();
                        if (getIntent().getBooleanExtra("onboard", true))
                        {
                            returnIntent.putExtra("phone",phone);
                            returnIntent.putExtra("type", true);
                            setResult(RESULT_OK,returnIntent);
                        }
                        else
                        {
                            setResult(RESULT_CANCELED, returnIntent);
                        }
                        finish();
                    }
				}	
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.invite_message, menu);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setTitle("Invite");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra("onboard", true))
                {
                    returnIntent.putExtra("phone",phone);
                    returnIntent.putExtra("type", false);
                    setResult(RESULT_OK,returnIntent);
                }
                else
                {
                    setResult(RESULT_CANCELED, returnIntent);
                }
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
	}
	
	
	@Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null)
        {
        	progressDialog.dismiss();
        }
    }
	
	@Override
    public void onPause() {
		super.onPause();
		Amplitude.endSession();
	}
	
	@Override
    public void onResume() {
        super.onResume();
        Amplitude.startSession();
	}
}
