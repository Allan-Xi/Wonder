package org.wondertech.wonder.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.motion.Smotion;
import com.samsung.android.sdk.motion.SmotionActivityNotification;

import org.wondertech.wonder.AsyncTasks.SetDrivingTask;
import org.wondertech.wonder.MainActivity;
import org.wondertech.wonder.SetDrivingActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xiyu on 5/26/15.
 */
public class CheckMotionDetectionService extends Service {
    private static final long TIMEDELAY = 500000;
    private Smotion mMotion;
    private SmotionActivityNotification mActivityNotification;
    private SmotionActivityNotification.InfoFilter mFilter;
    private static final String TAG = "CheckMotionDetection";
    private static final long SUBSCRIBEDRIVINGINTERVAL = 150000;
    private static long lastDriveTime = 0;
    private static SharedPreferences userInfo;
    private CountDownTimer cdt;
    private static final int EXPIREMINUTES = 4;


    @Override
    public void onCreate() {
        super.onCreate();
        userInfo = getSharedPreferences("user_info", 0);
        TimerTask ttask = new TimerTask() {
            public void run() {
                if (ActivityRecognitionIntentService.count == 0){
                    mMotion = new Smotion();
                    try {
                        mMotion.initialize(CheckMotionDetectionService.this);
                        mActivityNotification =
                                new SmotionActivityNotification(Looper.getMainLooper(), mMotion);
                        mFilter = new SmotionActivityNotification.InfoFilter();
                        mFilter.addActivity(SmotionActivityNotification.Info.STATUS_VEHICLE);
                        mActivityNotification.start(mFilter, changeListener);
                        Log.v(TAG, "start Samsung motion SDK");
                    } catch (IllegalArgumentException e) { // Error handling
                        CheckMotionDetectionService.this.stopSelf();
                    } catch (SsdkUnsupportedException e) { // Error handling
                        if (e.getType() == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED ||
                                e.getType() == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED){
                            Log.v(TAG, "don't support samsung motion sdk.");
                            CheckMotionDetectionService.this.stopSelf();
                        }
                    }

                }else {
                    CheckMotionDetectionService.this.stopSelf();
                }
            }
        };
        new Timer().schedule(ttask, TIMEDELAY);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    // Update the activity data by listener callback.
    final SmotionActivityNotification.ChangeListener changeListener =
            new SmotionActivityNotification.ChangeListener() {
                @Override
                public void onChanged(SmotionActivityNotification.Info info) {
                    // TODO Auto-generated method stub
                    int status = info.getStatus();
                    int accuracy = info.getAccuracy();
                    long timestamp = info.getTimeStamp();
                    if (timestamp - lastDriveTime > SUBSCRIBEDRIVINGINTERVAL
                            && userInfo.getBoolean("autoDriving", true)){
                        if (accuracy == SmotionActivityNotification.Info.ACCURACY_HIGH){
                            Log.v("start driving", "samsung");
                            userInfo.edit().putBoolean("isDriving", true).apply();
                            new SetDrivingTask(CheckMotionDetectionService.this).execute(true);
                            Intent intent1 = new Intent(CheckMotionDetectionService.this, SetDrivingActivity.class);
                            intent1.putExtra("auto", true);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent1);
                            lastDriveTime = timestamp;
                            
                            if (cdt != null)
                                cdt.cancel();
                            cdt = new CountDownTimer(EXPIREMINUTES * 60000, 60000) {

                                public void onTick(long millisUntilFinished) {

                                }

                                public void onFinish() {
                                    userInfo.edit().putBoolean("isDriving", false).apply();
                                    if (userInfo.getBoolean("autoDriving", true)){
                                        new SetDrivingTask(CheckMotionDetectionService.this).execute(false);
                                        Intent intent1 = new Intent(CheckMotionDetectionService.this, MainActivity.class);
                                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent1);
                                    }else {
                                        userInfo.edit().putBoolean("autoDriving", true).apply();
                                    }
                                }
                            };
                            cdt.start();
                        }
                    }
                }
            };


}
