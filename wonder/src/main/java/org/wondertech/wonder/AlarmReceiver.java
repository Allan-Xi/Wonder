package org.wondertech.wonder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AlarmReceiver extends BroadcastReceiver {
	private SharedPreferences userInfo;
    @Override
    public void onReceive(Context context, Intent intent) {
    	userInfo = context.getSharedPreferences("user_info", 0);
    	userInfo.edit().putInt("inviteNum", 0).apply();
    }
}
