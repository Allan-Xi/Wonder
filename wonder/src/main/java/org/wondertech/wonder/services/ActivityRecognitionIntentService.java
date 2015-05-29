package org.wondertech.wonder.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.wondertech.wonder.AsyncTasks.SetDrivingTask;
import org.wondertech.wonder.MainActivity;
import org.wondertech.wonder.SetDrivingActivity;

import java.util.LinkedList;

public class ActivityRecognitionIntentService extends IntentService {

	static public int count = 0; 
	static public int driving = 0;
	static private LinkedList<DrivingStatus> queue = new LinkedList<>();
	static private long startTime = 0;
	private static final int EXPIRETIME = 150000;
	
    public ActivityRecognitionIntentService() {
        super("ActivityRecognitionIntentService");
    }
    private SharedPreferences userInfo;

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
				long currentTime = System.currentTimeMillis();

				if (count == 30) {
					if (queue.peek().driving)
						driving--;
					queue.remove();
				} else
					count++;

				if (activityName == "in_vehicle") {
					driving++;
					queue.add(new DrivingStatus(true, currentTime));
				} else {
					queue.add(new DrivingStatus(false, currentTime));
				}

				Log.v(activityName, Integer.toString(confidence)+ "%");
				Log.v("count", Integer.toString(count));
				Log.v("driving", Integer.toString(driving));
				if (driving > 2){
					if (!userInfo.getBoolean("isDriving", false)) {
						Log.v("start driving", "123");
						userInfo.edit().putBoolean("isDriving", true).apply();
						userInfo.edit().putBoolean("autoDriving", true).apply();
						new SetDrivingTask(ActivityRecognitionIntentService.this).execute(true);
						Intent intent1 = new Intent(ActivityRecognitionIntentService.this, SetDrivingActivity.class);
						intent1.putExtra("auto", true);
						intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent1);
						startTime = currentTime;
					}else {
						if (currentTime - startTime > EXPIRETIME){
							new SetDrivingTask(ActivityRecognitionIntentService.this).execute(true);
							startTime = currentTime;
						}
					}
				}
            else
            {
				if (count >= 30 && userInfo.getBoolean("isDriving", false) && driving < 3)
            	{
					userInfo.edit().putBoolean("autoDriving", false).apply();
					userInfo.edit().putBoolean("isDriving", false).apply();
					new SetDrivingTask(ActivityRecognitionIntentService.this).execute(false);
					Intent intent1 = new Intent(ActivityRecognitionIntentService.this, MainActivity.class);
					intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent1);
            	}
            }
        	
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

    class DrivingStatus {
    	public boolean driving;
    	public long time;
    	public DrivingStatus(boolean status, long time) {
    		this.driving = status;
    		this.time = time;
    	}
    }
}