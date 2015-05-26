package org.wondertech.wonder.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.MainActivity;
import org.wondertech.wonder.SetDrivingActivity;
import org.wondertech.wonder.Utils.Utilities;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class ActivityRecognitionIntentService extends IntentService {
    /**
     * Sets an identifier for the service
     */
	NotificationCompat.Builder mBuilder;
	
	static public int count = 0; 
	static public int driving = 0;
	static public boolean isDrive = false;
	static String filename = "Activityfile";
	static FileOutputStream outputStream;
	private String res;
	static private LinkedList<DrivingStatus> queue = new LinkedList<DrivingStatus>();
	static private long startTime;
	
    public ActivityRecognitionIntentService() {
        super("ActivityRecognitionIntentService");
    }
    
    private SharedPreferences userInfo;
    int mNotificationId;
    NotificationManager mNotifyMgr;

    @Override
    protected void onHandleIntent(Intent intent) {
    	 if (PreferenceManager.getDefaultSharedPreferences(ActivityRecognitionIntentService.this).
          		getBoolean("detectDriving",true))
    	 {
    	userInfo = getSharedPreferences("user_info", 0);
    	// If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity
            DetectedActivity mostProbableActivity =
                    result.getMostProbableActivity();
            /*
             * Get the probability that this activity is the
             * the user's actual activity
             */
            int confidence = mostProbableActivity.getConfidence();
            /*
             * Get an integer describing the type of activity
             */
            int activityType = mostProbableActivity.getType();
            String activityName = getNameFromType(activityType);
            /*
             * At this point, you have retrieved all the information
             * for the current update. You can display this
             * information to the user in a notification, or
             * send it to an Activity or Service in a broadcast
             * Intent.
             */
            long currentTime = System.currentTimeMillis()/1000;
            /*for (int i = 0; i < count; ++i)
            {
            	if (currentTime - queue.peek().time > 600)
            	{
            		if (queue.peek().driving)
                		driving--;
            		queue.remove();
            		count--;
            	}
            	else
            		break;
            }*/
            
            if (count == 30)
            {
            	if (queue.peek().driving)
            		driving--;
            	queue.remove();
            }
            else
            	count++;
            
            if (activityName == "in_vehicle")
            {
            	driving++;
            	queue.add(new DrivingStatus(true, currentTime));
            }
            else
            	queue.add(new DrivingStatus(false, currentTime));
            
            //Log.v("queue", Integer.toString(queue.size()));
            Log.v(activityName, Integer.toString(confidence)+ "%");
			Log.v("count", Integer.toString(count));

            /*mBuilder =
     			    new NotificationCompat.Builder(this)
     			    .setSmallIcon(R.drawable.ic_launcher)
     			    .setContentTitle("My notification");
         	
    		mBuilder.setContentText(activityName + " confidence:" + Integer.toString(confidence)+ "%");
    		
         	mNotificationId = (int)System.currentTimeMillis()/1000;;
         	// Gets an instance of the NotificationManager service
         	mNotifyMgr = 
         	        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         	// Builds the notification and issues it.
         	mNotifyMgr.notify(mNotificationId, mBuilder.build()); 
            //Log.v("Date", DateFormat.getDateTimeInstance().format(new Date()));*/
            //Log.v(activityName, Integer.toString(confidence)+ "%");
            /*res = DateFormat.getDateTimeInstance().format(new Date()) + 
            		"  " + activityName + "  " + Integer.toString(confidence)+ "%\n";
            //Log.v("info", res);
            try {
            	  outputStream = openFileOutput(filename, ActivityRecognitionIntentService.MODE_APPEND);
            	  outputStream.write(res.getBytes());
            	  outputStream.close();
            	} catch (Exception e) {
            	  e.printStackTrace();
            	}*/
            //if (driving > 4 && LocationService.res >= 5)
			if (false)
			//if (driving > 4)
            {
            	if (!isDrive || currentTime - startTime > 240)
            	{
            		if (!userInfo.getBoolean("manualStop", false))
            			new SetDrivngTask().execute(true);
            	}
            		
            	if (!isDrive)
            	{
            		userInfo.edit().putBoolean("autoDriving", true).apply();
            		Intent intent1 = new Intent(ActivityRecognitionIntentService.this, SetDrivingActivity.class);
            		intent1.putExtra("auto", true);
            		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		startActivity(intent1);
            	}
            	isDrive = true;
            	startTime = currentTime;
            	userInfo.edit().putBoolean("isDriving", true).apply();
            	/*mBuilder =
         			    new NotificationCompat.Builder(this)
         			    .setSmallIcon(R.drawable.ic_launcher)
         			    .setContentTitle("My notification");
             	
         		mBuilder.setContentText("driving detected by activityRecognition");
             	
         		 mNotificationId = (int)System.currentTimeMillis()/1000;;
             	// Gets an instance of the NotificationManager service
         		mNotifyMgr = 
             	        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
             	// Builds the notification and issues it.
             	mNotifyMgr.notify(mNotificationId, mBuilder.build());
             	res = DateFormat.getDateTimeInstance().format(new Date()) + 
                		"driving detected by activityRecognition" + "\n"; 
             	try {
              	  outputStream = openFileOutput(filename, ActivityRecognitionIntentService.MODE_APPEND);
              	  outputStream.write(res.getBytes());
              	  outputStream.close();
              	} catch (Exception e) {
              	  e.printStackTrace();
              	}*/
            }
            else
            {
            	//if (count >= 18 && isDrive && LocationService.meanSpeed < 5)
				if (count >= 30 && userInfo.getBoolean("isDriving", false) && driving < 5)
            	{
            		//if (currentTime - startTime > 480)
            		{
						DetectionDrivingService.ttask.cancel();
						userInfo.edit().putBoolean("autoDriving", false).apply();
						userInfo.edit().putBoolean("isDriving", false).apply();
						new SetDrivngTask().execute(false);
						Intent intent1 = new Intent(ActivityRecognitionIntentService.this, MainActivity.class);
						intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent1);
                    	/*if (!userInfo.getBoolean("manualStop", false))
                    	{
                    		Intent intent1 = new Intent(ActivityRecognitionIntentService.this, MainActivity.class);
                    		intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    		startActivity(intent1);
                    	}
                    	userInfo.edit().putBoolean("manualStop", false).apply();*/
                    	
                    	/*mBuilder =
                 			    new NotificationCompat.Builder(this)
                 			    .setSmallIcon(R.drawable.ic_launcher)
                 			    .setContentTitle("My notification");
                     	
                 		mBuilder.setContentText("stop driving by activity recognition");
                     	
                 		mNotificationId = (int)System.currentTimeMillis()/1000;;
                     	// Gets an instance of the NotificationManager service
                 		mNotifyMgr = 
                     	        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                     	// Builds the notification and issues it.
                     	mNotifyMgr.notify(mNotificationId, mBuilder.build());
                     	res = DateFormat.getDateTimeInstance().format(new Date()) + 
                        		"driving detected by activityRecognition" + "\n"; 
                     	try {
                      	  outputStream = openFileOutput(filename, ActivityRecognitionIntentService.MODE_APPEND);
                      	  outputStream.write(res.getBytes());
                      	  outputStream.close();
                      	} catch (Exception e) {
                      	  e.printStackTrace();
                      	}
                     	
                     	res = DateFormat.getDateTimeInstance().format(new Date()) + 
                        		"  " + "stop driving"+ "\n"; 
                		//Log.v("info", res);
                     	try {
                      	  outputStream = openFileOutput(filename, ActivityRecognitionIntentService.MODE_APPEND);
                      	  outputStream.write(res.getBytes());
                      	  outputStream.close();
                      	} catch (Exception e) {
                      	  e.printStackTrace();
                      	}*/
            		}
            	}
            }
            
            
            /*if (count >= 18)
            {
            	long lastTime = MainActivity.currentTime;
            	MainActivity.currentTime = System.currentTimeMillis()/1000;
            	long elapsedTime = MainActivity.currentTime - lastTime;
            	Location lastLocation = MainActivity.mCurrentLocation;
            	MainActivity.mCurrentLocation = MainActivity.mLocationClient.getLastLocation();
            	//float[] distance = new float[5];
            	MainActivity.mCurrentLocation.getSpeed();
            	double distance = distFrom(lastLocation.getLatitude(), lastLocation.getLongitude(),
            			MainActivity.mCurrentLocation.getLatitude(), MainActivity.mCurrentLocation.getLongitude());
            	//Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), 
                //			MainActivity.mCurrentLocation.getLatitude(), MainActivity.mCurrentLocation.getLongitude(),
            	//		distance);
            	double speed = distance/elapsedTime * 3.6;
            	if (driving > 4)
            	{
            		new SetDrivngTask().execute(true);
            		mBuilder =
             			    new NotificationCompat.Builder(this)
             			    .setSmallIcon(R.drawable.ic_launcher)
             			    .setContentTitle("My notification");
                 	
             		mBuilder.setContentText("driving detected by activityRecognition");
                 	
             		 mNotificationId = (int)System.currentTimeMillis()/1000;;
                 	// Gets an instance of the NotificationManager service
             		mNotifyMgr = 
                 	        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                 	// Builds the notification and issues it.
                 	mNotifyMgr.notify(mNotificationId, mBuilder.build());
                 	isDrive = true;
                 	res = DateFormat.getDateTimeInstance().format(new Date()) + 
                    		"driving detected by activityRecognition" + "\n"; 
                 	//Log.v("info", res);
                 	try {
                  	  outputStream = openFileOutput(filename, ActivityRecognitionIntentService.MODE_APPEND);
                  	  outputStream.write(res.getBytes());
                  	  outputStream.close();
                  	} catch (Exception e) {
                  	  e.printStackTrace();
                  	}
            	}
            	else
            	{
            		if (isDrive)
            		{
            			mBuilder =
                 			    new NotificationCompat.Builder(this)
                 			    .setSmallIcon(R.drawable.ic_launcher)
                 			    .setContentTitle("My notification");
                     	
                 		mBuilder.setContentText("stop driving by activity recognition");
                     	
                 		mNotificationId = (int)System.currentTimeMillis()/1000;;
                     	// Gets an instance of the NotificationManager service
                 		mNotifyMgr = 
                     	        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                     	// Builds the notification and issues it.
                     	mNotifyMgr.notify(mNotificationId, mBuilder.build());
                     	isDrive = false;
            		}
            		
            		res = DateFormat.getDateTimeInstance().format(new Date()) + 
                    		"  " + "stop driving"+ "  " + Double.toString(speed) + "\n"; 
            		//Log.v("info", res);
                 	try {
                  	  outputStream = openFileOutput(filename, ActivityRecognitionIntentService.MODE_APPEND);
                  	  outputStream.write(res.getBytes());
                  	  outputStream.close();
                  	} catch (Exception e) {
                  	  e.printStackTrace();
                  	}
	
            	}
            	count = 0;
            	driving = 0;
            }  */
        	
        	//Intent it = new Intent("android.intent.action.MY_ACTIVITY");  
            //sendBroadcast(it); 
        	
        } else {
            /*
             * This implementation ignores intents that don't contain
             * an activity update. If you wish, you can report them as
             * errors.
             */
        }
    	 }
        
    }
    
    
    
    private String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
    
    static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371; //kilometers
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c * 1000;

        return dist;
        }

	class SetDrivngTask extends AsyncTask<Boolean, Void, Void> {

		@Override
		protected Void doInBackground(Boolean... params) {
			/*if (params[0])
			{
				Log.v("driving", "server");
			}
			else
				Log.v("stop driving", "server");*/
			String TargetURL = userInfo.getString("URL", "") + "isDriving";
			HttpURLConnection urlConnection = null;
			try {
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
				json.put("auto_mode", true);
				json.put("is_driving", params[0]);
				json.put("secs_remaining", 240);

				OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
				try {
					out.write(json.toString());
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					out.close();
				}

				int HttpResult = urlConnection.getResponseCode();
				if (HttpResult == HttpURLConnection.HTTP_OK) {
				} else {
					System.out.println(urlConnection.getResponseMessage());
				}
			} catch (MalformedURLException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();

			} catch (JSONException e) {

				e.printStackTrace();

			} finally {
				if (urlConnection != null)
					urlConnection.disconnect();
			}
			return null;
		}
	}

    class DrivingStatus
    {
    	public boolean driving;
    	public long time;
    	public DrivingStatus(boolean status, long time)
    	{
    		this.driving = status;
    		this.time = time;
    	}
    }
}