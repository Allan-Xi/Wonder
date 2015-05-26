package org.wondertech.wonder;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.amplitude.api.Amplitude;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;
import org.wondertech.wonder.adapter.MessageAdapter;
import org.wondertech.wonder.data.WonderContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GetMessageActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{
	private static SharedPreferences userInfo;
	private ListView messageList;
	private MessageAdapter adapter;
	private Cursor mCursor;
	private Set<String> deleteList;
	private ProgressDialog progressDialog;
	private static final int MESSAGE_LOADER = 0;

	public static int NOTIFICATION_COL_PHONE;
	public static int NOTIFICATION_COL_ISREAD;
	public static int NOTIFICATION_COL_TIME;
	public static int NOTIFICATION_COL_CONTENT;
	public static int NOTIFICATION_COL_TYPE;
	public static int NOTIFICATION_COL_MESSAGE_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_message);
		userInfo = getSharedPreferences("user_info", 0);
		messageList = (ListView)findViewById(R.id.message_list);
		mCursor = getContentResolver().query(
				WonderContract.NotificationEntry.CONTENT_URI,
				null,
				null,
				null,
				WonderContract.NotificationEntry.COLUMN_TIME + " DESC"
		);
		NOTIFICATION_COL_CONTENT = mCursor.getColumnIndex(WonderContract.NotificationEntry.COLUMN_CONTENT);
		NOTIFICATION_COL_ISREAD = mCursor.getColumnIndex(WonderContract.NotificationEntry.COLUMN_IS_READ);
		NOTIFICATION_COL_MESSAGE_ID = mCursor.getColumnIndex(WonderContract.NotificationEntry.COLUMN_MESSAGE_ID);
		NOTIFICATION_COL_PHONE = mCursor.getColumnIndex(WonderContract.NotificationEntry.COLUMN_PHONE);
		NOTIFICATION_COL_TIME = mCursor.getColumnIndex(WonderContract.NotificationEntry.COLUMN_TIME);
		NOTIFICATION_COL_TYPE = mCursor.getColumnIndex(WonderContract.NotificationEntry.COLUMN_TYPE);

		adapter = new MessageAdapter(GetMessageActivity.this, null, 0);
		messageList.setAdapter(adapter);
		getLoaderManager().initLoader(MESSAGE_LOADER, null, GetMessageActivity.this);
		messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cur = (Cursor) parent.getItemAtPosition(position);
				String messageId = cur.getString(cur.getColumnIndex(WonderContract.NotificationEntry.COLUMN_MESSAGE_ID));
				if (!cur.getString(cur.getColumnIndex(WonderContract.NotificationEntry.COLUMN_PHONE))
						.equals("Wonder Team")) {
					Intent intent = new Intent(GetMessageActivity.this, CallActivity.class);
					intent.putExtra("MessageId", messageId);
					intent.putExtra("Message", cur.getString(cur.getColumnIndex(WonderContract.NotificationEntry.COLUMN_CONTENT)));
					startActivity(intent);
				}
				ContentValues values = new ContentValues();
				values.put(WonderContract.NotificationEntry.COLUMN_IS_READ, 1);
				getContentResolver().update(WonderContract.NotificationEntry.CONTENT_URI,
						values,
						WonderContract.NotificationEntry.COLUMN_MESSAGE_ID + " =?",
						new String[]{messageId}
				);
				cur.close();
			}
		});

		messageList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		messageList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
												  long id, boolean checked) {
				// Here you can do something when items are selected/de-selected,
				// such as update the title in the CAB
				adapter.getCursor().moveToPosition(position);
				int idIndex = adapter.getCursor().getColumnIndex(WonderContract.NotificationEntry.COLUMN_MESSAGE_ID);
				if (!checked && deleteList.contains(adapter.getCursor().getString(idIndex))) {
					deleteList.remove(adapter.getCursor().getString(idIndex));
				}
				if (checked && !deleteList.contains(adapter.getCursor().getString(idIndex))) {
					deleteList.add(adapter.getCursor().getString(idIndex));
				}
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				// Respond to clicks on the actions in the CAB
				switch (item.getItemId()) {
					case R.id.action_discard:
						if (progressDialog == null) {
							progressDialog = Utilities.createProgressDialog(GetMessageActivity.this);
						}
						progressDialog.show();
						new DeleteMessage().execute();
						mode.finish(); // Action picked, so close the CAB
						return true;
					default:
						return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// Inflate the menu for the CAB
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.message, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// Here you can make any necessary updates to the activity when
				// the CAB is removed. By default, selected items are deselected/unchecked.
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// Here you can perform updates to the CAB due to
				// an invalidate() request
				deleteList = new HashSet<>();
				return false;
			}
		});
		new GetMessage().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.get_message, menu);
		getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setTitle("Notifications");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this,
				WonderContract.NotificationEntry.CONTENT_URI,
				null,
				null,
				null,
				WonderContract.NotificationEntry.COLUMN_TIME + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	private class GetMessage extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			String TargetURL = userInfo.getString("URL", "") + "getNotifications";
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
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line + "\n");
					}
					br.close();
					JSONObject result = new JSONObject(sb.toString());
					JSONObject ja = result.getJSONObject("notifications");
					Iterator<?> keys = ja.keys();
					List<ContentValues> bulkInsertContentValues = new ArrayList<>();
					while( keys.hasNext() ) {
						String key = (String) keys.next();
						final String[] tmp = key.split("-", 3);

						Cursor cur = getContentResolver().query(
								WonderContract.NotificationEntry.CONTENT_URI,
								null,
								WonderContract.NotificationEntry.COLUMN_MESSAGE_ID + " =?",
								new String[]{key},
								null
						);
						if (cur.getCount() != 0) {
							cur.close();
							continue;
						}else {
							cur.close();
						}

						ContentValues values = new ContentValues();
						values.put(WonderContract.NotificationEntry.COLUMN_CONTENT, ja.getString(key));
						values.put(WonderContract.NotificationEntry.COLUMN_TYPE, Integer.parseInt(tmp[1]));
						values.put(WonderContract.NotificationEntry.COLUMN_TIME, (long)Double.parseDouble(tmp[2]));
						values.put(WonderContract.NotificationEntry.COLUMN_PHONE, tmp[0]);
						values.put(WonderContract.NotificationEntry.COLUMN_IS_READ, 0);
						values.put(WonderContract.NotificationEntry.COLUMN_MESSAGE_ID, key);
						bulkInsertContentValues.add(values);
					}
					ContentValues[] bulkInsertContentValuesArr = new ContentValues[bulkInsertContentValues.size()];
					bulkInsertContentValuesArr = bulkInsertContentValues.toArray(bulkInsertContentValuesArr);
					getContentResolver().bulkInsert(WonderContract.NotificationEntry.CONTENT_URI,
							bulkInsertContentValuesArr);
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
	
	private class DeleteMessage extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			String TargetURL = userInfo.getString("URL", "") + "clearNotifications";
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
				JSONArray ja = new JSONArray();
				Iterator<String> keys = deleteList.iterator();
				while( keys.hasNext() ){
					String key = keys.next();
					getContentResolver().delete(WonderContract.NotificationEntry.CONTENT_URI,
							WonderContract.NotificationEntry.COLUMN_MESSAGE_ID + " =?",
							new String[]{key});
					if (!key.equals("Wonder Team"))
						ja.put(key);
				}

				json.put("ids", ja);

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
					progressDialog.dismiss();
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
	@Override
    public void onResume() {
        super.onResume();
        Amplitude.startSession();
        Amplitude.logEvent("Messages View Opened");
    }
	
	@Override
    public void onPause() {
		super.onPause();
		Amplitude.endSession();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCursor.close();
	}
}
