package org.wondertech.wonder.AsyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by xiyu on 5/26/15.
 */
public class SetDrivingTask extends AsyncTask<Boolean, Void, Void> {
    private Context mContext;
    private static final int EXPIRETIME = 240;

    public SetDrivingTask (Context context){
        mContext = context;
    }

    @Override
    protected Void doInBackground(Boolean... params) {
        if (params[0]) {
            Log.v("driving", "server");
        }
        else
            Log.v("stop driving", "server");

        SharedPreferences userInfo = mContext.getSharedPreferences("user_info", 0);
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
            json.put("secs_remaining", EXPIRETIME);

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
