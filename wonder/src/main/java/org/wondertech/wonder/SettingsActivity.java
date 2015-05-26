package org.wondertech.wonder;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;
import org.wondertech.wonder.data.WonderContract;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SettingsActivity extends PreferenceActivity{
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private SharedPreferences userInfo;
	private static final boolean ALWAYS_SIMPLE_PREFS = false;
	private static final String DEFAULT_SMS = "I’m driving right now. I’ll get back to you soon. See on joinwonder.com/drive";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		userInfo = getSharedPreferences("user_info", 0);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowTitleEnabled(true);
			getActionBar().setTitle("Back");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);
		final EditTextPreference pref = (EditTextPreference)findPreference(
				"replyContent");
		pref.setSummary(userInfo.getString("replySMS", DEFAULT_SMS));
		pref.setText(userInfo.getString("replySMS", DEFAULT_SMS));
		pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				pref.setSummary(newValue.toString());
				userInfo.edit().putString("replySMS", newValue.toString()).apply();
				return true;
			}
		});
		// Add 'notifications' preferences, and a corresponding header.
		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_notification);
		Preference button = (Preference)findPreference("Report a Bug");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		                @Override
		                public boolean onPreferenceClick(Preference arg0) { 
		                    Intent intent = new Intent(SettingsActivity.this, WebActivity.class);
		                    intent.putExtra("URL", "https://joinwonder.com/bug/");
		                    startActivity(intent);
		                    return true;
		                }
		            });
		button = (Preference)findPreference("Rate and Comment");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		                @Override
		                public boolean onPreferenceClick(Preference arg0) { 
		                	Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
		                	Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		                	try {
		                	  startActivity(goToMarket);
		                	} catch (ActivityNotFoundException e) {
		                	  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
		                	}
		                    return true;
		                }
		            });

		// Add 'data and sync' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_data_sync);
		button = findPreference("Terms of Service");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		                @Override
		                public boolean onPreferenceClick(Preference arg0) { 
		                    Intent intent = new Intent(SettingsActivity.this, WebActivity.class);
		                    intent.putExtra("URL", "https://joinwonder.com/tos/");
		                    startActivity(intent);
		                    return true;
		                }
		            });
		button = findPreference("Privacy Policy");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		                @Override
		                public boolean onPreferenceClick(Preference arg0) { 
		                    Intent intent = new Intent(SettingsActivity.this, WebActivity.class);
		                    intent.putExtra("URL", "https://joinwonder.com/privacy/");
		                    startActivity(intent);
		                    return true;
		                }
		            });
		
		fakeHeader = new PreferenceCategory(this);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_signout);
		button = findPreference("Sign out");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		                @Override
		                public boolean onPreferenceClick(Preference arg0) { 
		                	new AlertDialog.Builder(SettingsActivity.this)
							.setMessage("Driving statuses will be removed from" +
									" your contact book.").setTitle("Are you sure?")
							.setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
						        public void onClick(DialogInterface dialog, int which) { 
						        	new Signout().execute();
						        }
						     })
						    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						        public void onClick(DialogInterface dialog, int which) { 
						        }
						     })
						     .setIcon(R.drawable.biglogo)
						     .show();
		                    return true;
		                }
		            });

	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	
	private class Signout extends AsyncTask<Void, Void, Void>{
    	protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "logout";
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
				json.put("uuid", userInfo.getString("uuid", ""));
				json.put("app_version", userInfo.getString("app_version", ""));
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
					userInfo.edit().clear().apply();
					ContentResolver cr = getContentResolver();
					Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
					if (cursor != null) {
						try {
							final int contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
							String contactId;
							while (cursor.moveToNext()) {
								contactId = cursor.getString(contactIdIndex);
								Utilities.updateToAndroid(SettingsActivity.this, contactId, Utilities.RESTORE);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							cursor.close();
						}
					}
					cr.delete(WonderContract.ContactEntry.CONTENT_URI,
							null,
							null);
					cr.delete(WonderContract.NotificationEntry.CONTENT_URI,
							null,
							null);
					startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
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

	private static final String[] PROJECTION = new String[] {
	    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
	    ContactsContract.Contacts.DISPLAY_NAME,
	    ContactsContract.CommonDataKinds.Phone.DATA
	};
}
