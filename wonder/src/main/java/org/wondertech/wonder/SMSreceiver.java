package org.wondertech.wonder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SMSreceiver extends BroadcastReceiver{
	private SharedPreferences userInfo;
	private SharedPreferences userOrbit;

	@Override
	public void onReceive(Context context, Intent intent) {
		userInfo = context.getSharedPreferences("user_info", 0);
		userOrbit = context.getSharedPreferences("user_orbit", 0);
		if (intent.getAction().equals(
				"android.provider.Telephony.SMS_RECEIVED"
				)
				&&userInfo.getBoolean("isDriving", false)
				&&PreferenceManager.getDefaultSharedPreferences(context).
        		getBoolean("autoReply",false)
				)
		{
			String sb = "";
			Bundle bundle = intent.getExtras();
			
			if (!bundle.isEmpty())
			{
				Object[] pdus = (Object[])bundle.get("pdus");
				SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; ++i)
				{
					messages[i] = SmsMessage.
							createFromPdu((byte[])pdus[i]);
				}
				for (SmsMessage message : messages)
				{
					sb = message.getDisplayOriginatingAddress();

					if (!userOrbit.contains(sb))
					{
						SmsManager sm = SmsManager.getDefault();
						String text = userInfo.getString("replySMS", "I’m driving right now. I’ll get back to you soon. See on joinwonder.com/drive");
						List<String> texts = splitEqually(text, 50);
						for (int i = 0; i < texts.size(); ++i)
						{
							sm.sendTextMessage(sb,null,texts.get(i),null,null);
						}

					}
					
					
				}					
			}

		}				
	}

	public static List<String> splitEqually(String text, int size) {
		// Give the list the right capacity to start with. You could use an array
		// instead if you wanted.
		List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

		for (int start = 0; start < text.length(); start += size) {
			ret.add(text.substring(start, Math.min(text.length(), start + size)));
		}
		return ret;
	}
}
