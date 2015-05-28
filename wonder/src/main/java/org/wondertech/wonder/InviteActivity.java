package org.wondertech.wonder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FilterQueryProvider;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.amplitude.api.Amplitude;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;
import org.wondertech.wonder.adapter.InviteAdapter;
import org.wondertech.wonder.data.WonderContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class InviteActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{
    static final int ONBOARD_REQUEST = 1;
	private GridView gv;
	private SearchView sv;
	public Handler mHandler;
	private ImageView icon;
	public static Map<String, String> phones;
	public static List<String> phone;
	private SharedPreferences userInfo;
	static public Map<String, JSONObject> Orbit;
	private InviteAdapter adapter;
	private Intent sendIntent;
	static private Boolean invited = false;
	private ProgressDialog progressDialog = null;
	private ImageButton next;
    private int contactNum = 0;
    private static ArrayList<String> invites;
    private static ArrayList<String> cancels;
	private static long LIGHTANIMDURATION = 2000;
	private Cursor cursor;
	private static final int INVITE_LOADER = 0;

	private static final String[] PROJECTION = new String[] {
			WonderContract.ContactEntry._ID,
			WonderContract.ContactEntry.COLUMN_NAME,
			WonderContract.ContactEntry.COLUMN_PHONE,
			WonderContract.ContactEntry.COLUMN_PHONE_TYPE,
			WonderContract.ContactEntry.COLUMN_INVITED,
			WonderContract.ContactEntry.COLUMN_ON_WONDER
	};

	public static int CONTACT_COL_NAME_INDEX;
	public static int CONTACT_COL_PHONE_INDEX;
	public static int CONTACT_COL_PHONE_TYPE_INDEX;
	public static int CONTACT_COL_INVITED_INDEX;
	public static int CONTACT_COL_ON_WONDER_INDEX;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        invites = new ArrayList<>();
        cancels = new ArrayList<>();

		userInfo = getSharedPreferences("user_info", 0);
		super.onCreate(savedInstanceState);
		if (!userInfo.getBoolean("isVerified", false))
		{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		setContentView(R.layout.activity_invite);
		TextView tv = (TextView)findViewById(R.id.invite_title_text);
		tv.setText(getString(R.string.invite_title));

		phones = new HashMap<>();
		Orbit = new HashMap<>();
		gv = (GridView)findViewById(R.id.invite_gridview);
		sv = (SearchView)findViewById(R.id.invite_searchview);

		cursor = getContentResolver().query(WonderContract.ContactEntry.CONTENT_URI,
				PROJECTION,
				null,
				null,
				WonderContract.ContactEntry.COLUMN_NAME + " ASC"
		);

		CONTACT_COL_INVITED_INDEX = cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_INVITED);
		CONTACT_COL_NAME_INDEX = cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_NAME);
		CONTACT_COL_ON_WONDER_INDEX = cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_ON_WONDER);
		CONTACT_COL_PHONE_INDEX = cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_PHONE);
		CONTACT_COL_PHONE_TYPE_INDEX = cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_PHONE_TYPE);

		adapter = new InviteAdapter(this, cursor, 0);
		gv.setAdapter(adapter);
		getLoaderManager().initLoader(INVITE_LOADER, null, InviteActivity.this);
		gv.setTextFilterEnabled(true);
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cur = (Cursor) parent.getItemAtPosition(position);
				String name = cur.getString(CONTACT_COL_NAME_INDEX);
				String text = name + " is already on Wonder!\n" +
						"You'll see when " + name + " is driving.";
				String phone = cur.getString(CONTACT_COL_PHONE_INDEX);
				if (cur.getInt(CONTACT_COL_ON_WONDER_INDEX) == 1) {
					Toast.makeText(InviteActivity.this, text, Toast.LENGTH_SHORT).show();
				} else {
					invited = true;
					next.setImageResource(R.drawable.next_button);
					new GetInvites().execute(phone);
				}
			}
		});
		adapter.setFilterQueryProvider(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence constraint) {
				String name = constraint.toString();
				return getContentResolver().query(WonderContract.ContactEntry.CONTENT_URI,
						PROJECTION,
						WonderContract.ContactEntry.COLUMN_NAME + " LIKE '%" + name + "%'",
						null,
						WonderContract.ContactEntry.COLUMN_NAME + " ASC");
			}
		});
		sv.setIconifiedByDefault(false);
		sv.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				adapter.getFilter().filter(newText);
				return true;
			}
		});

		//light animation
		icon = (ImageView)findViewById(R.id.invite_icon);
		mHandler = new Handler() {
			boolean flag = false;
		    public void handleMessage(Message msg) {
		        if (flag)
		        {
			    	icon.setImageResource(R.drawable.green_light_save); 
			    	flag = false;
		        }
		        else
		        {
		        	icon.setImageResource(R.drawable.red_light_stop); 
			    	flag = true;
		        }
		    }
		};
		Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            public void run() 
            {
            	mHandler.obtainMessage(1).sendToTarget();
            }
        }, 0, LIGHTANIMDURATION);


        next = (ImageButton)findViewById(R.id.invite_next);
        if (userInfo.getBoolean("isVerified", false))
        	next.setVisibility(View.GONE);
        else
	        next.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					userInfo.edit().putBoolean("isVerified", true).apply();
		        	if (invited) {
						startNewActivity();
		        	} else {
		        		new AlertDialog.Builder(InviteActivity.this)
						.setMessage(getString(R.string.invite_not_invite_text)).setTitle(R.string.invite_not_invite_title)
								.setPositiveButton(R.string.invite_not_invite_ok, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {

									}
								})
								.setNegativeButton(R.string.invite_not_invite_cancel, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										startNewActivity();
									}
								})
								.setIcon(R.drawable.biglogo)
								.show();
		        	}
				}
			});
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.invite, menu);
		getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setTitle("Back");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this,
				WonderContract.ContactEntry.CONTENT_URI,
				PROJECTION,
				null,
				null,
				WonderContract.ContactEntry.COLUMN_NAME + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	private class GetInvites extends AsyncTask<String, Void, Void> {
		private String text;
		private String phone;
		@Override
		protected Void doInBackground(String... params) {
			phone = params[0];
			String TargetURL = userInfo.getString("URL", "") + "getInvites";
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
				json.put("phone", params[0]);

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
					text = result.getString("text");
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
		protected void onPostExecute(Void result) {
			sendIntent = new Intent(InviteActivity.this, InviteMessageActivity.class);
			sendIntent.putExtra("phone", phone);
			sendIntent.putExtra("text", text);
			sendIntent.putExtra("onboard", !userInfo.getBoolean("isVerified", false));
			startActivityForResult(sendIntent, ONBOARD_REQUEST);
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		Amplitude.startSession();
		if (!userInfo.getBoolean("isVerified", false)) {
			Amplitude.logEvent("Onboard Invite View Opened");
		} else {
			Amplitude.logEvent("Invite View Opened");
		}
		new GetOrbit().execute();

	}
	@Override
    public void onPause() {
		super.onPause();
		Amplitude.endSession();
		if (progressDialog != null)
			progressDialog.dismiss();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ONBOARD_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data.getBooleanExtra("type", false))
                {
                    invites.add(data.getStringExtra("phone"));
                }
                else
                {
                    cancels.add(data.getStringExtra("phone"));
                }
            }
        }
    }

    private void startNewActivity()
    {
		if (progressDialog == null) {
			progressDialog = Utilities.createProgressDialog(InviteActivity.this);
		}
		progressDialog.show();
		if (!userInfo.getBoolean("isVerified", false))
        {
            JSONObject eventProperties = new JSONObject();
            try {
                JSONArray ja = new JSONArray();
                for (String phone : invites)
                {
                    ja.put(phone);
                }
                eventProperties.put("Invites", ja);
                eventProperties.put("Invite Count", invites.size());
                ja = new JSONArray();
                for (String phone : cancels)
                {
                    if (!invites.contains(phone))
                        ja.put(phone);
                }
                eventProperties.put("Cancels", ja);
                eventProperties.put("Cancel Count", ja.length());
                eventProperties.put("Contact Count", contactNum);
            } catch (JSONException exception) {
            }
            Amplitude.logEvent("Onboard Invite Complete", eventProperties);
        }
		progressDialog.dismiss();
        startActivity(new Intent(InviteActivity.this, ProtectDriver.class));
    }

	@Override
	public void onBackPressed() {
		Utilities.goBackHome(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cursor != null){
			cursor.close();
		}
	}

	private class GetOrbit extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			String TargetURL = userInfo.getString("URL", "") + "getOrbit";
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
				json.put("client_id", userInfo.getString("client_id", ""));
				json.put("uuid", userInfo.getString("uuid", ""));
				json.put("app_version", userInfo.getString("app_version", ""));

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
					JSONArray ja = result.getJSONArray("contacts");

					for (int i = 0; i < ja.length(); i++) {
						JSONObject jo = (JSONObject) ja.get(i);
						String id;
						String phone = jo.getString("phone");
						Cursor cur = getContentResolver().query(
								WonderContract.ContactEntry.CONTENT_URI,
								new String[]{WonderContract.ContactEntry.COLUMN_RAW_ID,
										WonderContract.ContactEntry.COLUMN_PHONE,
										WonderContract.ContactEntry.COLUMN_ON_WONDER},
								WonderContract.ContactEntry.COLUMN_PHONE + " =?",
								new String[]{phone},
								null
						);
						cur.moveToFirst();
						if (cur.getCount() != 0){
							try{
								int idIndex = cur.getColumnIndex(WonderContract.ContactEntry.COLUMN_RAW_ID);
								id = cur.getString(idIndex);
								if (jo.getBoolean("is_driving")) {
									Utilities.updateToAndroid(InviteActivity.this, id, Utilities.DRIVING);
								} else {
									Utilities.updateToAndroid(InviteActivity.this, id, Utilities.ONORBIT);
								}
								int onWonderIndex = cur.getColumnIndex(WonderContract.ContactEntry.COLUMN_ON_WONDER);
								int onWonder = cur.getInt(onWonderIndex);
								if (onWonder == 0){
									ContentValues values = new ContentValues();
									values.put(WonderContract.ContactEntry.COLUMN_ON_WONDER, 1);
									getContentResolver().update(WonderContract.ContactEntry.CONTENT_URI,
											values,
											WonderContract.ContactEntry.COLUMN_PHONE + " =?",
											new String[]{phone}
									);
								}
							}finally {
								cur.close();
							}
						}else{
							cur.close();
						}
					}
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

			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				if(urlConnection!=null)
					urlConnection.disconnect();
			}
			return null;
		}
	}
}
