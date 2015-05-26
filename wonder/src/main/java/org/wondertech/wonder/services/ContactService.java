package org.wondertech.wonder.services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;
import org.wondertech.wonder.data.WonderContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by xiyu on 5/14/15.
 */
public class ContactService extends Service {

    private int mContactCount;
    private static Boolean add = false;
    private static SharedPreferences userInfo;
    private static final String[] PROJECTION = new String[] {
            WonderContract.ContactEntry.COLUMN_NAME,
            WonderContract.ContactEntry.COLUMN_PHONE,
            WonderContract.ContactEntry.COLUMN_PHONE_TYPE,
            WonderContract.ContactEntry.COLUMN_INVITED,
            WonderContract.ContactEntry.COLUMN_ON_WONDER,
            WonderContract.ContactEntry.COLUMN_RAW_ID
    };

    private static final String[] CONTACTPROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.DATA,
            ContactsContract.CommonDataKinds.Phone.TYPE
    };


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        userInfo = getSharedPreferences("user_info", 0);
        mContactCount = getContactCount();
        this.getContentResolver().registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, true, mObserver);
    }

    private int getContactCount() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI, null, null, null,
                    null);
            if (cursor != null) {
                return cursor.getCount();
            } else {
                return 0;
            }
        } catch (Exception ignore) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    private ContentObserver mObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            final int currentCount = getContactCount();
            if (currentCount < mContactCount) {
                // DELETE HAPPEN.
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(WonderContract.ContactEntry.CONTENT_URI,
                            PROJECTION,
                            null,
                            null,
                            null
                    );
                    int phoneIndex = cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_PHONE);
                    while (cursor.moveToNext()) {
                        String phone = cursor.getString(phoneIndex);
                        Cursor cur = null;
                        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                        Phonenumber.PhoneNumber numberProto = null;
                        try {
                            numberProto = phoneUtil.parse(phone, "US");
                        } catch (NumberParseException e) {
                            e.printStackTrace();
                        }
                        String phoneNumber = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                        try {
                            cur = getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    new String[]{ContactsContract.CommonDataKinds.Phone.DATA},
                                    ContactsContract.CommonDataKinds.Phone.DATA + " =?",
                                    new String[]{phoneNumber},
                                    null);
                            if (cur.getCount() == 0) {
                                add = false;
                                new setContactDeltas().execute(phone);
                                new DeleteMessage().execute(phone);
                                initiateContacts();
                               Log.v("delete", phone);
                            }
                        }finally {
                            if (cur != null)
                                cur.close();
                        }
                    }
                }finally {
                    cursor.close();
                }
            } else if (currentCount == mContactCount) {
                // UPDATE HAPPEN.
            } else {
                // INSERT HAPPEN.

                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            CONTACTPROJECTION,
                            null,
                            null,
                            null
                    );
                    final int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
                    final int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    while (cursor.moveToNext()) {
                        String phone = PhoneNumberUtils.stripSeparators(cursor.getString(phoneIndex));
                        if (phone.length() != 10)
                            continue;
                        Cursor cur = null;
                        try{
                            cur = getContentResolver().query(
                                    WonderContract.ContactEntry.CONTENT_URI,
                                    new String[]{WonderContract.ContactEntry.COLUMN_PHONE},
                                    WonderContract.ContactEntry.COLUMN_PHONE + " =?",
                                    new String[]{phone},
                                    null);
                            if ( cur.getCount() == 0){
                                add = true;
                                String name = cursor.getString(nameIndex);
                                new setContactDeltas().execute(phone, name);
                                initiateContacts();
                                Log.v("insert", phone);
                            }
                        }finally {
                            if (cur != null)
                                cur.close();
                        }
                    }
                }finally {
                    cursor.close();
                }

            }
            mContactCount = currentCount;
        }

    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    private class setContactDeltas extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... params) {
            String TargetURL = userInfo.getString("URL", "") + "setContactDeltas";
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
                json.put("client_id", userInfo.getString("client_id", ""));
                json.put("uuid", userInfo.getString("uuid", ""));
                json.put("app_version", userInfo.getString("app_version", ""));

                JSONArray ja = new JSONArray();
                JSONObject js = new JSONObject();
                JSONArray emptyJa = new JSONArray();
                if (add) {
                    js.put("phone", params[0]);
                    js.put("name", params[1]);
                } else {
                    js.put("phone", params[0]);
                }
                ja.put(js);
                if (add) {
                    json.put("add", ja);
                    json.put("remove", emptyJa);
                } else {
                    json.put("remove", ja);
                    json.put("add", emptyJa );
                }

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

                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                Phonenumber.PhoneNumber numberProto = null;
                try {
                    numberProto = phoneUtil.parse(params[0], "US");
                } catch (NumberParseException e) {
                    e.printStackTrace();
                }
                String phone = phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
                if (add){
                    Cursor cursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            CONTACTPROJECTION,
                            ContactsContract.CommonDataKinds.Phone.DATA + " =?",
                            new String[]{phone},
                            null);
                    try{
                        if (cursor != null){
                            final int contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                            final int displayNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                            final int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
                            final int phoneType = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

                            String contactId;
                            String displayName, phoneNumber;

                            while (cursor.moveToNext()) {
                                phoneNumber = PhoneNumberUtils.stripSeparators(cursor.getString(phoneIndex));
                                contactId = cursor.getString(contactIdIndex);
                                displayName = cursor.getString(displayNameIndex);
                                String[] tmp = displayName.split(" ", 2);
                                if (tmp[0].equals("\u26A0\uD83D\uDE97") || tmp[0].equals("\u2705"))
                                {
                                    displayName = tmp[1];
                                }
                                String phoneTypeLabel = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), cursor.getInt(phoneType), "");

                                ContentValues contactValues = new ContentValues();
                                contactValues.put(WonderContract.ContactEntry.COLUMN_RAW_ID, contactId);
                                contactValues.put(WonderContract.ContactEntry.COLUMN_PHONE, phoneNumber);
                                contactValues.put(WonderContract.ContactEntry.COLUMN_NAME, displayName);
                                contactValues.put(WonderContract.ContactEntry.COLUMN_DRIVING, 0);
                                contactValues.put(WonderContract.ContactEntry.COLUMN_ON_WONDER, 0);
                                contactValues.put(WonderContract.ContactEntry.COLUMN_INVITED, 0);
                                contactValues.put(WonderContract.ContactEntry.COLUMN_PHONE_TYPE, phoneTypeLabel);
                                contactValues.put(WonderContract.ContactEntry.COLUMN_NOTIFY, 0);
                                contactValues.put(WonderContract.ContactEntry.COLUMN_LEAVE_MESSAGE, 0);
                                contactValues.put(WonderContract.ContactEntry.COLUMN_REQUEST_CALL, 0);
                                getContentResolver().insert(
                                        WonderContract.ContactEntry.CONTENT_URI,
                                        contactValues
                                );
                            }
                        }
                    }finally {
                            cursor.close();
                    }
                }else{
                    getContentResolver().delete(
                            WonderContract.ContactEntry.CONTENT_URI,
                            WonderContract.ContactEntry.COLUMN_PHONE + " =?",
                            new String[]{params[0]}
                    );
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

    private void initiateContacts()
    {
        userInfo = getSharedPreferences("user_info", 0);
        ContentResolver cr = getContentResolver();

        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DATA},
                null, null, null);

        if (cursor != null) {
            try {
                final int contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                final int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
                String contactId, phoneNumber;

                while (cursor.moveToNext()) {
                    phoneNumber = PhoneNumberUtils.stripSeparators(cursor.getString(phoneIndex));
                    if (PhoneNumberUtils.stripSeparators(phoneNumber).length() != 10)
                        continue;
                    contactId = cursor.getString(contactIdIndex);
                    ContentValues contactValues = new ContentValues();
                    contactValues.put(WonderContract.ContactEntry.COLUMN_RAW_ID, contactId);
                    contactValues.put(WonderContract.ContactEntry.COLUMN_ON_WONDER, 0);
                    cr.update(
                            WonderContract.ContactEntry.CONTENT_URI,
                            contactValues,
                            WonderContract.ContactEntry.COLUMN_PHONE + " =?",
                            new String[]{phoneNumber}
                    );
                }
            } finally {
                cursor.close();
            }
        }
        new GetOrbit().execute();
    }

    private class GetOrbit extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "getOrbit";
            HttpURLConnection urlConnection = null;
            StringBuilder sb = new StringBuilder();
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
                json.put("client_id", userInfo.getString("client_id", ""));
                json.put("uuid", userInfo.getString("uuid", ""));
                json.put("app_version", userInfo.getString("app_version", ""));

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
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream(),"utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    JSONObject result = new JSONObject(sb.toString());
                    JSONArray ja = result.getJSONArray("contacts");
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = (JSONObject) ja.get(i);
                        ContentValues cv = new ContentValues();
                        cv.put(WonderContract.ContactEntry.COLUMN_ON_WONDER, 1);
                        getContentResolver().update(WonderContract.ContactEntry.CONTENT_URI,
                                cv,
                                WonderContract.ContactEntry.COLUMN_PHONE + "= ?",
                                new String[]{jo.getString("phone")}
                        );
                        String id = null;
                        Cursor cur = getContentResolver().query(
                                WonderContract.ContactEntry.CONTENT_URI,
                                new String[]{WonderContract.ContactEntry.COLUMN_RAW_ID,
                                        WonderContract.ContactEntry.COLUMN_PHONE},
                                WonderContract.ContactEntry.COLUMN_PHONE + " =?",
                                new String[]{jo.getString("phone")},
                                null
                        );
                        cur.moveToFirst();
                        if (cur.getCount() != 0){
                            try{
                                int idIndex = cur.getColumnIndex(WonderContract.ContactEntry.COLUMN_RAW_ID);
                                id = cur.getString(idIndex);
                                if (jo.getBoolean("is_driving"))
                                {
                                    try {
                                        Utilities.updateToAndroid(ContactService.this, id, Utilities.DRIVING);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                else
                                {
                                    try {
                                        Utilities.updateToAndroid(ContactService.this, id, Utilities.ONORBIT);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }finally {
                                cur.close();
                            }
                        }else{
                            cur.close();
                        }
                    }
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

    private class DeleteMessage extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            getContentResolver().delete(WonderContract.NotificationEntry.CONTENT_URI,
                    WonderContract.NotificationEntry.COLUMN_PHONE + " =?",
                    new String[]{params[0]});
            String TargetURL = userInfo.getString("URL", "") + "clearNotifications";
            HttpURLConnection urlConnection = null;
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
                JSONArray ja = new JSONArray();
                Cursor cursor = getContentResolver().query(WonderContract.NotificationEntry.CONTENT_URI,
                        null,
                        WonderContract.NotificationEntry.COLUMN_PHONE + " =?",
                        new String[]{params[0]},
                        null);
                try{
                    if (cursor != null){
                        int idIndex = cursor.getColumnIndex(WonderContract.NotificationEntry.COLUMN_MESSAGE_ID);
                        while (cursor.moveToNext()){
                            String id = cursor.getString(idIndex);
                            ja.put(id);
                        }
                    }
                }finally {
                    cursor.close();
                }
                json.put("ids", ja);

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
    }
}
