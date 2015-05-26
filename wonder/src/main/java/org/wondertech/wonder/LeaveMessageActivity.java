package org.wondertech.wonder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amplitude.api.Amplitude;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;
import org.wondertech.wonder.data.WonderContract;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LeaveMessageActivity extends Activity {
	private SharedPreferences userInfo;
	private String phone;
	private EditText et;
	private ProgressDialog progressDialog;
	private String name;
	private SharedPreferences leaveMessage;
	private Cursor cursor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		leaveMessage = getSharedPreferences("leave_message", 0);
		setContentView(R.layout.activity_leave_message);
		final Intent intent = getIntent();
		phone = intent.getStringExtra("phone");
		userInfo = getSharedPreferences("user_info", 0);
		TextView tv = (TextView)findViewById(R.id.receiver);
		et = (EditText)findViewById(R.id.message_content);
		et.setText(leaveMessage.getString(phone, ""));
		Button btn = (Button)findViewById(R.id.leave_message);
		cursor = getContentResolver().query(
				WonderContract.ContactEntry.CONTENT_URI,
				null,
				WonderContract.ContactEntry.COLUMN_PHONE + " =?",
				new String[]{phone},
				null
		);
		try{
			cursor.moveToFirst();
			name = cursor.getString(cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_NAME));
		}finally {
			cursor.close();
		}

		tv.setText("For: \uD83D\uDD34\uD83D\uDE97 " + name);

		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = et.getEditableText().toString();
				if (content.isEmpty())
				{
					Toast.makeText(LeaveMessageActivity.this,
							"Your message has been cancelled!",
							Toast.LENGTH_SHORT).show();
					leaveMessage.edit().putString(phone, "").apply();
					JSONObject eventProperties = new JSONObject();
					try {
					    eventProperties.put("Empty String", true);
					} catch (JSONException exception) {
					}
					Amplitude.logEvent("Message Left", eventProperties);
					if (progressDialog == null) {
					       progressDialog = Utilities.createProgressDialog(LeaveMessageActivity.this);
					       progressDialog.show();
					       } else {
					       progressDialog.show();
					       }
				}
				else
				{
					Toast.makeText(LeaveMessageActivity.this, 
							"You have left a message!", 
							Toast.LENGTH_SHORT).show();
					JSONObject eventProperties = new JSONObject();
					try {
					    eventProperties.put("Empty String", false);
					} catch (JSONException exception) {
					}
					Amplitude.logEvent("Message Left", eventProperties);
					if (progressDialog == null) {
					       progressDialog = Utilities.createProgressDialog(LeaveMessageActivity.this);
					       progressDialog.show();
					       } else {
					       progressDialog.show();
					       }
					leaveMessage.edit().putString(phone, et.getText().toString()).apply();
					
				}
				new LeaveMessage().execute(content);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.leave_message, menu);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setTitle("Leave a Message");
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
	
	private class LeaveMessage extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String TargetURL = userInfo.getString("URL", "") + "sendNotifications";
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
				json.put("type", 0);
				json.put("phone", phone);
				json.put("message", params[0]);

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

				}
				else if (HttpResult ==HttpURLConnection.HTTP_UNAUTHORIZED){
					Toast.makeText(LeaveMessageActivity.this,
							name + "  isn't driving now.",
							Toast.LENGTH_SHORT).show();
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

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			ContentValues values = new ContentValues();
			values.put(WonderContract.ContactEntry.COLUMN_LEAVE_MESSAGE, 1);
			getContentResolver().update(
					WonderContract.ContactEntry.CONTENT_URI,
					values,
					WonderContract.ContactEntry.COLUMN_PHONE + " =?",
					new String[]{phone}
			);
			progressDialog.dismiss();
			startActivity(new Intent(LeaveMessageActivity.this, MainActivity.class));
		}
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
        Amplitude.logEvent("Leave Message View Opened");
	}
}
