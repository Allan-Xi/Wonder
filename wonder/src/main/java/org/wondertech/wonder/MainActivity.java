/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wondertech.wonder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.amplitude.api.Amplitude;
import com.readystatesoftware.viewbadger.BadgeView;
import com.uservoice.uservoicesdk.UserVoice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;
import org.wondertech.wonder.adapter.DriverAdapter;
import org.wondertech.wonder.adapter.drawerAdapter;
import org.wondertech.wonder.data.WonderContract;
import org.wondertech.wonder.services.ContactService;
import org.wondertech.wonder.services.DetectionDrivingService;
import org.wondertech.wonder.services.VOIPService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    static public Map<String, JSONObject> Orbit;
	private static ListView driver_list;
    public static DriverAdapter adapter;
    public static Cursor mCursor;
	private static SharedPreferences userInfo;

	
	static public ArrayList<String> Driving;
	
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerItems;
    private static FrameLayout drive_clue;
    private static FrameLayout questionLayout;
    private static ImageButton mainInvite;
    
    private Fragment mainFragment;

    private BadgeView mBadgeView;

    public static final String[] PROJECTION = new String[] {
            WonderContract.ContactEntry._ID,
            WonderContract.ContactEntry.COLUMN_NAME,
            WonderContract.ContactEntry.COLUMN_PHONE,
            WonderContract.ContactEntry.COLUMN_PHONE_TYPE,
            WonderContract.ContactEntry.COLUMN_INVITED,
            WonderContract.ContactEntry.COLUMN_ON_WONDER,
            WonderContract.ContactEntry.COLUMN_LEAVE_MESSAGE,
            WonderContract.ContactEntry.COLUMN_REQUEST_CALL,
            WonderContract.ContactEntry.COLUMN_NOTIFY,
            WonderContract.ContactEntry.COLUMN_RAW_ID
    };

    public static int CONTACT_COL_PHONE_INDEX;
    public static int CONTACT_COL_NAME_INDEX;
    public static int CONTACT_COL_LEAVE_MESSAGE_INDEX;
    public static int CONTACT_COL_REQUEST_CALL_INDEX;
    public static int CONTACT_COL_NOTIFY_INDEX;

    private ShareActionProvider mShareActionProvider;

    private static final int DRIVER_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
              
        userInfo = getSharedPreferences("user_info", 0);

        startService(new Intent(this, ContactService.class));
        startService(new Intent(this, VOIPService.class));
        startService(new Intent(this, DetectionDrivingService.class));

        if (userInfo.getBoolean("mainFirst",true))
        {
            ContentValues values = new ContentValues();
            values.put(WonderContract.NotificationEntry.COLUMN_IS_READ, 0);
            values.put(WonderContract.NotificationEntry.COLUMN_PHONE,"Wonder Team");
            values.put(WonderContract.NotificationEntry.COLUMN_TIME, System.currentTimeMillis());
            values.put(WonderContract.NotificationEntry.COLUMN_TYPE, 0);
            values.put(WonderContract.NotificationEntry.COLUMN_CONTENT,
                    "Welcome to Wonder!\n\n" +
                            "If a contact sees you are \uD83D\uDD34\uD83D\uDE97 and" +
                            " leaves a message or sends a call request, you'll " +
                            "be notified. Find them here!");
            values.put(WonderContract.NotificationEntry.COLUMN_MESSAGE_ID, "Wonder Team");
            getContentResolver().insert(WonderContract.NotificationEntry.CONTENT_URI,
                    values);
        }

        mCursor = getContentResolver().query(
                WonderContract.ContactEntry.CONTENT_URI,
                PROJECTION,
                WonderContract.ContactEntry.COLUMN_DRIVING + " =?",
                new String[]{"1"},
                WonderContract.ContactEntry.COLUMN_NAME + " ASC"
        );

        CONTACT_COL_PHONE_INDEX = mCursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_PHONE);
        CONTACT_COL_NAME_INDEX = mCursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_NAME);
        CONTACT_COL_LEAVE_MESSAGE_INDEX = mCursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_LEAVE_MESSAGE);
        CONTACT_COL_REQUEST_CALL_INDEX = mCursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_REQUEST_CALL);
        CONTACT_COL_NOTIFY_INDEX = mCursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_NOTIFY);

        adapter = new DriverAdapter(MainActivity.this, null, 0);
        getLoaderManager().initLoader(DRIVER_LOADER, null, MainActivity.this);

        Orbit = new HashMap<>();
        Driving = new ArrayList<>();

		
		mainFragment = new MainFragment();
        mTitle = mDrawerTitle = getTitle();
        mDrawerTitle = "Wonder";
        mDrawerItems = getResources().getStringArray(R.array.drawers_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new drawerAdapter(this));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.navigation,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
            	getActionBar().setTitle(mDrawerTitle);
            	
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
            	getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
        
        if (getIntent().hasExtra("background"))
        {
        	moveTaskToBack(true);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        new GetDrivers().execute();
        new GetOrbit().execute();
        if (userInfo.getBoolean("isDriving", false)) {
            Intent intent = new Intent(MainActivity.this, SetDrivingActivity.class);
            intent.putExtra("auto", true);
            startActivity(intent);
        }
        new GetMessage().execute();
    }
    
    @Override
    public void onStop() {
        super.onStop();  
    }

    @Override
    public void onPause() {
        super.onPause();  
        Amplitude.endSession();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setTitle("Wonder");

        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I promise not to text you when you drive. Promise me the same? See when I drive on www.joinwonder.com");
        sendIntent.setType("text/plain");
        setShareIntent(sendIntent);

        
        return super.onCreateOptionsMenu(menu);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.unread_messages).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_item_share).setVisible(!drawerOpen);
        MenuItem menuItem = menu.findItem(R.id.unread_messages);
        LinearLayout container = new LinearLayout(this);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ImageButton iconView = new ImageButton(this);
        iconView.setImageDrawable(menuItem.getIcon());
        iconView.setBackgroundResource(android.R.color.transparent);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GetMessageActivity.class));
                //userInfo.edit().putInt("messageNum", 0).apply();
                //long currentTime = System.currentTimeMillis() / 1000;
                //Zendrive.startDrive(Long.toString(currentTime));
            }
        });
        container.addView(iconView);
        int unreadNum = 0;
        Cursor cur = getContentResolver().query(
                WonderContract.NotificationEntry.CONTENT_URI,
                new String[]{WonderContract.NotificationEntry.COLUMN_IS_READ},
                WonderContract.NotificationEntry.COLUMN_IS_READ + " =?",
                new String[]{"0"},
                null
        );
        try {
            if (cur != null){
                unreadNum = cur.getCount();
            }
        }finally {
            cur.close();
        }

        if (unreadNum > 0)
        {
            mBadgeView = new BadgeView(this, iconView);
            mBadgeView.setText(Integer.toString(unreadNum));
            mBadgeView.setTextSize(10f);
            mBadgeView.show();
        }

        menuItem.setActionView(container);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.unread_messages:
            startActivity(new Intent(MainActivity.this, GetMessageActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                WonderContract.ContactEntry.CONTENT_URI,
                PROJECTION,
                WonderContract.ContactEntry.COLUMN_DRIVING + " =?",
                new String[]{"1"},
                WonderContract.ContactEntry.COLUMN_NAME + " ASC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        if (data.getCount() == 0){
            setDrivingLayout(false);
        }else{
            setDrivingLayout(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        /*Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);*/
    	switch(position)
    	{
    	case 0:
    	{
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, mainFragment).commit();
            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            setTitle(mDrawerItems[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
            break;
    	}
    	case 1:
    	{
    		startActivity(new Intent(MainActivity.this, GetMessageActivity.class));
    		break;
    	}
    	/*case 2:
    	{
    		startActivity(new Intent(MainActivity.this, SetDrivingActivity.class));
    		break;
    	}*/
    	case 2:
    	{
    		startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    		break;
    	}
        case 3:
        {
            UserVoice.launchUserVoice(this);
            break;
        }
    	case 4:
    	{
    		JSONObject eventProperties = new JSONObject();
			try {
			    eventProperties.put("Location", "Side Menu");
			} catch (JSONException exception) {
			}
			Amplitude.logEvent("Invite Button Pressed", eventProperties);
    		startActivity(new Intent(MainActivity.this, InviteActivity.class));
    		break;	
    	}
    	}	
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    
    public static class MainFragment extends Fragment {
    	private FragmentActivity myContext;
    	private InteractionFragmentAdapter mAdapter;
    	private ViewPager mPager;
        public MainFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView text = (TextView)rootView.findViewById(R.id.main_text);
            TextView main = (TextView)rootView.findViewById(R.id.main_title);
            if (userInfo.getBoolean("mainFirst",true)) {
                main.setText("You're all set!");
                userInfo.edit().putBoolean("mainFirst", false).apply();
            } else {
                main.setText("None of your contacts\non Wonder are driving.");
                main.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
            }
            text.setText("Come back\nto Wonder\nwhen you\nsee \uD83D\uDD34\uD83D\uDE97");
    		questionLayout = (FrameLayout)rootView.findViewById(R.id.question_layout);
            driver_list = (ListView)rootView.findViewById(R.id.driving_list);
            View footerView = ((LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_layout, null, false);
            driver_list.addFooterView(footerView);
            drive_clue = (FrameLayout)rootView.findViewById(R.id.drive_clue);
            mainInvite = (ImageButton)footerView.findViewById(R.id.main_invite);
            mAdapter = new InteractionFragmentAdapter(
            		myContext.getSupportFragmentManager());

            mPager = (ViewPager)rootView.findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);
            mainInvite.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    JSONObject eventProperties = new JSONObject();
                    try {
                        eventProperties.put("Location", "Main View Drivers");
                    } catch (JSONException exception) {
                    }
                    Amplitude.logEvent("Invite Button Pressed", eventProperties);
                    myContext.startActivity(new Intent(myContext, InviteActivity.class));
                }
            });

            driver_list.setAdapter(adapter);
            return rootView;
        }
        
        @Override
        public void onAttach(Activity activity) {
            myContext=(FragmentActivity) activity;
            super.onAttach(activity);
        }
        
        @Override
        public void onResume() {
            super.onResume();
            Amplitude.startSession();
            mAdapter.notifyDataSetChanged();
        }
    }
    
    private class GetOrbit extends AsyncTask<Void, Void, Void> {
    	Boolean update = false;
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

                    JSONObject options = result.getJSONObject("options");
                    if (options.has("android_update_required"))
                        update =options.getBoolean("android_update_required");
                    else
                        update = false;

                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = (JSONObject) ja.get(i);
                        String id;
                        String phone = jo.getString("phone");
                        Cursor cur = getContentResolver().query(
                                WonderContract.ContactEntry.CONTENT_URI,
                                new String[]{WonderContract.ContactEntry.COLUMN_RAW_ID,
                                        WonderContract.ContactEntry.COLUMN_PHONE},
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
                                    Utilities.updateToAndroid(MainActivity.this, id, Utilities.DRIVING);
                                } else {
                                    Utilities.updateToAndroid(MainActivity.this, id, Utilities.ONORBIT);
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
		
		@Override
		protected void onPostExecute(Void result) {
			if (update)
			{
				new AlertDialog.Builder(MainActivity.this)
				.setMessage("A new update for Wonder is available!").setTitle("Wonder")
				.setPositiveButton("Update", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			        	Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
	                	Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
	                	try {
	                	  startActivity(goToMarket);
	                	} catch (ActivityNotFoundException e) {
	                	  startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
	                	}
			        }
			     })
			    .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			        }
			     })
			     .setIcon(R.drawable.biglogo)
			     .show();
			}
		  }
		}
    

	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
        if (mCursor != null)
            mCursor.close();
	}
	
	@Override
	public void onBackPressed() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}
	
	private void setDrivingLayout(Boolean flag)
	{
		if (flag) {
    		drive_clue.setVisibility(View.INVISIBLE);
    		mainInvite.setVisibility(View.VISIBLE);
    		questionLayout.setVisibility(View.GONE);
    	} else {
			drive_clue.setVisibility(View.VISIBLE);
    		mainInvite.setVisibility(View.GONE);
			questionLayout.setVisibility(View.VISIBLE);
		}
	}

    private class GetDrivers extends AsyncTask<Void, Void, Void> {
        Boolean hasDriving = false;
        @Override
        protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "getDrivers";
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
                    Log.v("Get Drivers", sb.toString());
                    JSONArray drivers = new JSONArray(sb.toString());
                    Cursor cur = getContentResolver().query(WonderContract.ContactEntry.CONTENT_URI,
                            null,
                            WonderContract.ContactEntry.COLUMN_ON_WONDER + " =? AND "
                            + WonderContract.ContactEntry.COLUMN_DRIVING + " =?",
                            new String[]{"1", "0"},
                            null
                    );
                    try{
                        final int idIndex = cur.getColumnIndex(WonderContract.ContactEntry.COLUMN_RAW_ID);
                        while (cur.moveToNext()){
                            String id = cur.getString(idIndex);
                            Utilities.updateToAndroid(MainActivity.this, id, Utilities.ONORBIT);
                            ContentValues values = new ContentValues();
                            values.put(WonderContract.ContactEntry.COLUMN_DRIVING, 0);
                            values.put(WonderContract.ContactEntry.COLUMN_NOTIFY, 0);
                            values.put(WonderContract.ContactEntry.COLUMN_LEAVE_MESSAGE, 0);
                            values.put(WonderContract.ContactEntry.COLUMN_REQUEST_CALL, 0);
                            getContentResolver().update(WonderContract.ContactEntry.CONTENT_URI,
                                    values,
                                    WonderContract.ContactEntry.COLUMN_RAW_ID + " =?",
                                    new String[]{id}
                            );
                        }
                    }finally {
                        cur.close();
                    }

                    cur = getContentResolver().query(WonderContract.ContactEntry.CONTENT_URI,
                            null,
                            WonderContract.ContactEntry.COLUMN_ON_WONDER + " =? AND "
                                    + WonderContract.ContactEntry.COLUMN_DRIVING + " =?",
                            new String[]{"1", "1"},
                            null
                    );
                    try{
                        final int idIndex = cur.getColumnIndex(WonderContract.ContactEntry.COLUMN_RAW_ID);
                        while (cur.moveToNext()){
                            String id = cur.getString(idIndex);
                            Utilities.updateToAndroid(MainActivity.this, id, Utilities.ONORBIT);
                            ContentValues values = new ContentValues();
                            values.put(WonderContract.ContactEntry.COLUMN_DRIVING, 0);
                            getContentResolver().update(WonderContract.ContactEntry.CONTENT_URI,
                                    values,
                                    WonderContract.ContactEntry.COLUMN_RAW_ID + " =?",
                                    new String[]{id}
                            );
                        }
                    }finally {
                        cur.close();
                    }
                    if (drivers.length() == 0){
                        hasDriving = false;
                    }else {
                        hasDriving = true;
                    }
                    for (int i = 0; i < drivers.length(); i++){
                        String phone = drivers.getString(i);
                        cur = getContentResolver().query(WonderContract.ContactEntry.CONTENT_URI,
                                null,
                                WonderContract.ContactEntry.COLUMN_PHONE + " =?",
                                new String[]{phone},
                                null
                        );
                        try{
                            cur.moveToFirst();
                            final int idIndex = cur.getColumnIndex(WonderContract.ContactEntry.COLUMN_RAW_ID);
                            String id = cur.getString(idIndex);
                            Utilities.updateToAndroid(MainActivity.this, id, Utilities.DRIVING);
                            ContentValues values = new ContentValues();
                            values.put(WonderContract.ContactEntry.COLUMN_DRIVING, 1);
                            getContentResolver().update(WonderContract.ContactEntry.CONTENT_URI,
                                    values,
                                    WonderContract.ContactEntry.COLUMN_RAW_ID + " =?",
                                    new String[]{id}
                            );
                        }finally {
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setDrivingLayout(hasDriving);
        }
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            invalidateOptionsMenu();
        }
    }

}