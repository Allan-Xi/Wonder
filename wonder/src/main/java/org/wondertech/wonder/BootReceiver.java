package org.wondertech.wonder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;
	
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
        	alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        	Intent intent1 = new Intent(context, AlarmReceiver.class);
        	alarmIntent = PendingIntent.getBroadcast(context, 0, intent1, 0);
        	alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
        	        AlarmManager.INTERVAL_FIFTEEN_MINUTES,
        	        AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
        }
    }
}
