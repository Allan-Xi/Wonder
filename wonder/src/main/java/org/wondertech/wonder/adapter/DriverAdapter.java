package org.wondertech.wonder.adapter;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amplitude.api.Amplitude;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.LeaveMessageActivity;
import org.wondertech.wonder.MainActivity;
import org.wondertech.wonder.QuickContactHelper;
import org.wondertech.wonder.R;
import org.wondertech.wonder.RoundImageView;
import org.wondertech.wonder.Utils.Utilities;
import org.wondertech.wonder.data.WonderContract;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by xiyu on 5/14/15.
 */
public class DriverAdapter extends CursorAdapter {

    private static SharedPreferences userInfo;
    public static class ViewHolder
    {
        public TextView name;
        public TextView status;
        public ImageView greendot;
        public ImageView phoneIcon;
        public ImageView messageIcon;
        public TextView car;
        public TextView initial;
        public ImageButton notify;
        public RoundImageView photo;

        public ViewHolder(View view) {
            name = (TextView)view.findViewById(R.id.driver_name);
            status = (TextView)view.findViewById(R.id.driver_status);
            greendot = (ImageView)view.findViewById(R.id.driver_dot);
            car = (TextView)view.findViewById(R.id.driver_car);
            initial = (TextView)view.findViewById(R.id.driver_initials);
            photo = (RoundImageView)view.findViewById(R.id.driver_photo);
            messageIcon = (ImageView)view.findViewById(R.id.driver_sms_icon);
            phoneIcon = (ImageView)view.findViewById(R.id.driver_phone_icon);
            notify = (ImageButton)view.findViewById(R.id.driver_notify);
        }
    }

    public DriverAdapter(Context context, Cursor c, int flags){
        super(context, c, flags);
        userInfo = context.getSharedPreferences("user_info", 0);
    }
    
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.driver_item, null);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.car.setText("\uD83D\uDE97");
        final String phone = cursor.getString(MainActivity.CONTACT_COL_PHONE_INDEX);
        final String name = cursor.getString(MainActivity.CONTACT_COL_NAME_INDEX);
        final int leaveMessage = cursor.getInt(MainActivity.CONTACT_COL_LEAVE_MESSAGE_INDEX);
        final int requestCall = cursor.getInt(MainActivity.CONTACT_COL_REQUEST_CALL_INDEX);
        final int notify = cursor.getInt(MainActivity.CONTACT_COL_NOTIFY_INDEX);
        String abbre = "";
        String[] t = name.split(" ", 2);
        for (int i = 0; i < t.length; ++i)
        {
            abbre += String.valueOf(t[i].charAt(0)).toUpperCase();
        }
        holder.name.setText(name);
        holder.initial.setText(abbre);
        Bitmap photo = new QuickContactHelper(context, phone).getThumbnail();
        holder.photo.setAlpha(0f);
        if (photo != null) {
            holder.photo.setAlpha(1f);
            holder.photo.setImageBitmap(photo);
        }
        if (leaveMessage == 0){
            holder.messageIcon.setVisibility(View.GONE);
        }else {
            holder.messageIcon.setVisibility(View.VISIBLE);
        }
        if (requestCall == 1){
            holder.phoneIcon.setVisibility(View.VISIBLE);
        } else{
            holder.phoneIcon.setVisibility(View.GONE);
        }
        if (notify == 0) {
            holder.notify.setImageResource(R.drawable.notify_me_button);
            holder.greendot.setVisibility(View.INVISIBLE);
            holder.status.setText("When finished driving:");
        } else {
            holder.notify.setImageResource(R.drawable.more_button);
            holder.greendot.setVisibility(View.VISIBLE);
            holder.status.setText("You'll be notified!");
        }



        final Context mContext = context;

        holder.notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notify == 1){
                    final Dialog dialog = new Dialog(mContext, R.style.CustomDialogTheme);
                    dialog.setContentView(R.layout.show_more_dialog);
                    Window window = dialog.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();
                    wlp.gravity = Gravity.BOTTOM;
                    window.setAttributes(wlp);
                    dialog.show();
                    ImageButton notNotify = (ImageButton)dialog.findViewById(R.id.more_not_notify);
                    notNotify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ContentValues values = new ContentValues();
                            values.put(WonderContract.ContactEntry.COLUMN_NOTIFY, 0);
                            mContext.getContentResolver().update(
                                    WonderContract.ContactEntry.CONTENT_URI,
                                    values,
                                    WonderContract.ContactEntry.COLUMN_PHONE + " =?",
                                    new String[]{phone}
                            );
                            new SubscribeDriving().execute(phone, "false");
                            dialog.dismiss();
                        }
                    });

                    final ImageButton requestcall = (ImageButton)dialog.findViewById(R.id.more_request_call);
                    requestcall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Dialog dialog = new Dialog(mContext, R.style.CustomDialogTheme);
                            dialog.setContentView(R.layout.request_call_dialog);
                            String abbre = "";
                            String[] t = name.split(" ", 2);
                            for (int i = 0; i < t.length; ++i) {
                                abbre += String.valueOf(t[i].charAt(0)).toUpperCase();
                            }
                            TextView initials = (TextView) dialog.findViewById(R.id.request_call_initials);
                            initials.setText(abbre);
                            TextView text = (TextView) dialog.findViewById(R.id.request_call_text);
                            text.setText("Ask " + name + " to call you\n" +
                                    "when finished driving.");
                            ImageButton confirm = (ImageButton) dialog.findViewById(R.id.request_call_confirm);
                            if (requestCall == 0)
                                confirm.setImageResource(R.drawable.call_me_button);
                            else
                                confirm.setImageResource(R.drawable.cancel_button);

                            confirm.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Amplitude.startSession();
                                    Amplitude.logEvent("Request Call View Opened");

                                    if (requestCall == 0) {
                                        JSONObject eventProperties = new JSONObject();
                                        try {
                                            eventProperties.put("Requested", true);
                                        } catch (JSONException exception) {
                                        }
                                        Amplitude.logEvent("Call Request", eventProperties);
                                        ContentValues values = new ContentValues();
                                        values.put(WonderContract.ContactEntry.COLUMN_REQUEST_CALL, 1);
                                        mContext.getContentResolver().update(
                                                WonderContract.ContactEntry.CONTENT_URI,
                                                values,
                                                WonderContract.ContactEntry.COLUMN_PHONE + " =?",
                                                new String[]{phone}
                                        );
                                        Toast.makeText(mContext,
                                                "You've asked for a call!",
                                                Toast.LENGTH_SHORT).show();
                                        new requestCall().execute(phone, "true");
                                    } else {
                                        JSONObject eventProperties = new JSONObject();
                                        try {
                                            eventProperties.put("Requested", false);
                                        } catch (JSONException exception) {
                                        }
                                        Amplitude.logEvent("Call Request", eventProperties);
                                        ContentValues values = new ContentValues();
                                        values.put(WonderContract.ContactEntry.COLUMN_REQUEST_CALL, 0);
                                        mContext.getContentResolver().update(
                                                WonderContract.ContactEntry.CONTENT_URI,
                                                values,
                                                WonderContract.ContactEntry.COLUMN_PHONE + " =?",
                                                new String[]{phone}
                                        );
                                        new requestCall().execute(phone, "false");
                                        Toast.makeText(mContext,
                                                "Your call request has been cancelled!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    dialog.dismiss();
                                    Amplitude.endSession();
                                }
                            });
                            dialog.show();
                        }
                    });

                    ImageButton leavemessage = (ImageButton)dialog.findViewById(R.id.more_leave_message);
                    leavemessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, LeaveMessageActivity.class);
                            intent.putExtra("phone", phone);
                            mContext.startActivity(intent);
                        }
                    });

                    ImageButton cancel = (ImageButton)dialog.findViewById(R.id.more_cancel);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                } else {
                    ContentValues values = new ContentValues();
                    values.put(WonderContract.ContactEntry.COLUMN_NOTIFY, 1);
                    mContext.getContentResolver().update(
                            WonderContract.ContactEntry.CONTENT_URI,
                            values,
                            WonderContract.ContactEntry.COLUMN_PHONE + " =?",
                            new String[]{phone}
                    );
                    new SubscribeDriving().execute(phone, "true");
                }
            }
        });

    }

    private class SubscribeDriving extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String TargetURL = userInfo.getString("URL", "") + "sendNotifications";
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
                json.put("type", 2);
                json.put("phone", params[0]);
                if (params[1] == "true")
                    json.put("disabled", false);
                else
                    json.put("disabled", true);

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
                    Log.v("notify", "success");
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

    private class requestCall extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String TargetURL = userInfo.getString("URL", "") + "sendNotifications";
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
                json.put("type", 1);
                json.put("phone", params[0]);
                if (params[1] == "true")
                    json.put("callback_requested", true);
                else
                    json.put("callback_requested", false);

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
                    Log.v("requestCall", "success");
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
}
