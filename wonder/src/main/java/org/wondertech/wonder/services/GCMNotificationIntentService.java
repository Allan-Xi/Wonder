	package org.wondertech.wonder.services;

	import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.GcmBroadcastReceiver;
import org.wondertech.wonder.GetMessageActivity;
import org.wondertech.wonder.MainActivity;
import org.wondertech.wonder.R;
import org.wondertech.wonder.Utils.Utilities;
import org.wondertech.wonder.data.WonderContract;

	public class GCMNotificationIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private SharedPreferences userInfo;
	private String phone;
	public static String callFromPhone;


	public GCMNotificationIntentService() {
		super("GcmIntentService");

	}

	public static final String TAG = "GCMNotificationIntentService";

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v("gcm","received");
		userInfo = getSharedPreferences("user_info", 0);
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				//sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				//sendNotification("Deleted messages on server: "
				//		+ extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				//sendNotification("Message Received from Google GCM Server: ");
				try {
					if (extras.getString("data") != null){
						JSONObject result = new JSONObject(extras.getString("data"));
						Log.v("gcmContent", result.toString());
						if (result.has("voip_info")) {
							String sinchPayload = result.getString("voip_info");
							callFromPhone = result.getString("call_from_phone");
							if (VOIPService.sinchClient != null) {
								VOIPService.sinchClient.relayRemotePushNotificationPayload(sinchPayload);
							}
						}
						else if (result.has("request_type")) {
							phone =  result.getString("phone");
							switch(result.getInt("request_type")) {
								case 1:
								{
									try {
										String id = getId(phone);
										Utilities.updateToAndroid(GCMNotificationIntentService.this, id, Utilities.RESTORE);
										ContentValues values = new ContentValues();
										values.put(WonderContract.ContactEntry.COLUMN_ON_WONDER, 0);
										values.put(WonderContract.ContactEntry.COLUMN_DRIVING, 0);
										values.put(WonderContract.ContactEntry.COLUMN_LEAVE_MESSAGE, 0);
										values.put(WonderContract.ContactEntry.COLUMN_NOTIFY, 0);
										values.put(WonderContract.ContactEntry.COLUMN_REQUEST_CALL, 0);
										values.put(WonderContract.ContactEntry.COLUMN_INVITED, 0);
										getContentResolver().update(WonderContract.ContactEntry.CONTENT_URI,
												values,
												WonderContract.ContactEntry.COLUMN_PHONE + " =?",
												new String[]{phone});
									} catch (Exception e) {
										e.printStackTrace();
									}
									break;
								}
								case 20:
								{
									if (result.getString("phone") == userInfo.getString("phone", "")) {
									/*if (!result.getBoolean("is_driving") && userInfo.getBoolean("isDriving", false)) {
										if (DetectionDrivingService.ttask != null) {
											DetectionDrivingService.ttask.cancel();
											Log.v("cancel", "ok");
										}

										userInfo.edit().putBoolean("autoDriving", false).apply();
										userInfo.edit().putBoolean("isDriving", false).apply();
										new SetDrivingTask(GCMNotificationIntentService.this).execute(false);
										Intent intent1 = new Intent(GCMNotificationIntentService.this, MainActivity.class);
										intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										startActivity(intent1);
									}*/
									}else{
										if (result.getBoolean("is_driving")) {
											ContentValues values = new ContentValues();
											values.put(WonderContract.ContactEntry.COLUMN_DRIVING, 1);
											getContentResolver().update(WonderContract.ContactEntry.CONTENT_URI,
													values,
													WonderContract.ContactEntry.COLUMN_PHONE + " =?",
													new String[]{result.getString("phone")});
											try {
												Utilities.updateToAndroid(GCMNotificationIntentService.this,
														getId(result.getString("phone")), Utilities.DRIVING);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}else{
											ContentValues values = new ContentValues();
											values.put(WonderContract.ContactEntry.COLUMN_DRIVING, 0);
											values.put(WonderContract.ContactEntry.COLUMN_NOTIFY, 0);
											values.put(WonderContract.ContactEntry.COLUMN_LEAVE_MESSAGE, 0);
											values.put(WonderContract.ContactEntry.COLUMN_REQUEST_CALL, 0);
											getContentResolver().update(WonderContract.ContactEntry.CONTENT_URI,
													values,
													WonderContract.ContactEntry.COLUMN_PHONE + " =?",
													new String[]{result.getString("phone")});
											try {
												Utilities.updateToAndroid(GCMNotificationIntentService.this,
														getId(result.getString("phone")), Utilities.ONORBIT);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
									break;
								}
							}
						}
						else if (result.has("alert_type")) {
							switch(result.getInt("alert_type")) {
								case 71:

									NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
											this).setSmallIcon(R.drawable.biglogo)
											.setContentTitle("wonder")
											.setStyle(new NotificationCompat.BigTextStyle().bigText(result.getString("alert_text")))
											.setContentText(result.getString("alert_text"));

									// Creates an explicit intent for an Activity in your app
									Intent resultIntent = new Intent(this, GetMessageActivity.class);

									// The stack builder object will contain an artificial back stack for the
									// started Activity.
									// This ensures that navigating backward from the Activity leads out of
									// your application to the Home screen.
									TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
									// Adds the back stack for the Intent (but not the Intent itself)
									stackBuilder.addParentStack(GetMessageActivity.class);
									// Adds the Intent that starts the Activity to the top of the stack
									stackBuilder.addNextIntent(resultIntent);
									PendingIntent resultPendingIntent =
											stackBuilder.getPendingIntent(
													0,
													PendingIntent.FLAG_UPDATE_CURRENT
											);
									mBuilder.setContentIntent(resultPendingIntent);
									NotificationManager mNotificationManager =
											(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
									// mId allows you to update the notification later on.
									mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
									break;
								case 72:
								case 73:
									sendNotification(result.getString("alert_text"));

							}

						}
					}

						
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.biglogo)
				.setContentTitle("Wonder")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	private String getId(String phone){
		String id = "";
		Cursor cursor = getContentResolver().query(WonderContract.ContactEntry.CONTENT_URI,
				null,
				WonderContract.ContactEntry.COLUMN_PHONE + " =?",
				new String[]{phone},
				null);
		try {
			if (cursor != null && cursor.getCount() > 0){
				cursor.moveToFirst();
				int idIndex = cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_RAW_ID);
				id = cursor.getString(idIndex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			cursor.close();
		}
		return id;
	}
}
