package org.wondertech.wonder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class WhoisDriving extends Activity {
    private static SharedPreferences userInfo;
    private static SharedPreferences userContact;
    private static SharedPreferences userId;
    private static SharedPreferences phType;
    public static Map<String, String> phones; //phone,name
    public static Map<String, String> ids;    //phone,id
    public static Map<String, String> contacts;
    public static Map<String, String> types; //phone, type
    private static long ANIMPERIOD = 3000;
    private ProgressDialog progressDialog = null;

    private static final String[] PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.DATA,
            ContactsContract.CommonDataKinds.Phone.TYPE
    };
    private static final String[] nonDrivinglist = {
            "          ✅ Amelia",
            "          ✅ Kevin",
            "          ✅ Pops"
    };

    private static final String[] Drivinglist = {
            "          \uD83D\uDD34\uD83D\uDE97 Amelia",
            "          \uD83D\uDD34\uD83D\uDE97 Kevin",
            "          \uD83D\uDD34\uD83D\uDE97 Pops"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_whois_driving);
        initiateContacts();
        Spanned text = Html.fromHtml("Wonder shows you<br></br>who is driving<br></br>in <b>real time.</b>");
        TextView title = (TextView)findViewById(R.id.whodriving_title);
        title.setText(text);
        final TextView[] phone = new TextView[3];
        final boolean[] textflag = {false, false, false};

        phone[2] = (TextView)findViewById(R.id.whodriving_phone4);
        phone[1] = (TextView)findViewById(R.id.whodriving_phone3);
        phone[0] = (TextView)findViewById(R.id.whodriving_phone2);
        final ImageView tag = (ImageView)findViewById(R.id.whodriving_tag);
        for (int i = 0; i < phone.length; ++i){
            phone[i].setText(nonDrivinglist[i]);
        }


        final ImageButton next = (ImageButton)findViewById(R.id.whodriving_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressDialog == null) {
                    progressDialog = Utilities.createProgressDialog(WhoisDriving.this);
                    progressDialog.show();
                } else {
                    progressDialog.show();
                }
                new UploadContact().execute();
            }
        });

        final Handler mHandler = new Handler() {
            int count = 0;
            public void handleMessage(Message msg) {
                if (count == 0)
                {
                    phone[0].setText(Drivinglist[0]);
                    tag.setVisibility(View.VISIBLE);
                    tag.setImageResource(R.drawable.driving_tag);
                    count++;
                }
                else if (count == 1)
                {
                    phone[0].setText(nonDrivinglist[0]);
                    tag.setVisibility(View.VISIBLE);
                    tag.setImageResource(R.drawable.safe_to_text_tag);
                    count++;
                }
                else
                {
                    tag.setVisibility(View.INVISIBLE);
                    Random rand = new Random();
                    int randomNum = rand.nextInt(3);
                    if (textflag[randomNum])
                    {
                        phone[randomNum].setText(nonDrivinglist[randomNum]);
                        textflag[randomNum] = false;
                    }
                    else
                    {
                        phone[randomNum].setText(Drivinglist[randomNum]);
                        textflag[randomNum] = true;
                    }
                }
            }
        };

        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            public void run()
            {
                mHandler.obtainMessage(1).sendToTarget();
            }
        },  ANIMPERIOD, ANIMPERIOD);
    }

    private void initiateContacts()
    {
        userInfo = getSharedPreferences("user_info", 0);
        userContact = getSharedPreferences("user_contact", 0);
        userId = getSharedPreferences("user_id", 0);
        phType = getSharedPreferences("phone_type", 0);

        phones = new HashMap<>();
        contacts = new HashMap<>();
        ids = new HashMap<>();
        types = new HashMap<>();

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
        List<ContentValues> bulkInsertContentValues = new ArrayList<>();
        if (cursor != null) {
            try {
                final int contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                final int displayNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                final int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
                final int phoneType = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

                String contactId;
                String displayName, phoneNumber;

                while (cursor.moveToNext()) {
                    phoneNumber = PhoneNumberUtils.stripSeparators(cursor.getString(phoneIndex));
                    if (PhoneNumberUtils.stripSeparators(phoneNumber).length() != 10)
                        continue;
                    contactId = cursor.getString(contactIdIndex);
                    displayName = cursor.getString(displayNameIndex);
                    String[] tmp = displayName.split(" ", 2);
                    if (tmp[0].equals("\u26A0\uD83D\uDE97") || tmp[0].equals("\u2705"))
                    {
                        displayName = tmp[1];
                    }
                    phones.put(phoneNumber, displayName);
                    ids.put(phoneNumber, contactId);
                    String phoneTypeLabel = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), cursor.getInt(phoneType), "");
                    types.put(phoneNumber, phoneTypeLabel);
                    phType.edit().putString(PhoneNumberUtils.stripSeparators(phoneNumber), phoneTypeLabel).apply();

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
                    bulkInsertContentValues.add(contactValues);
                }
            } finally {
                cursor.close();
                ContentValues[] bulkInsertContentValuesArr = new ContentValues[bulkInsertContentValues.size()];
                bulkInsertContentValuesArr = bulkInsertContentValues.toArray(bulkInsertContentValuesArr);
                cr.delete(WonderContract.ContactEntry.CONTENT_URI, null, null);
                cr.bulkInsert(WonderContract.ContactEntry.CONTENT_URI, bulkInsertContentValuesArr);
            }
        }
    }

    private class UploadContact extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            String TargetURL = userInfo.getString("URL", "") + "populateContacts";
            HttpURLConnection urlConnection = null;
            StringBuilder sb = new StringBuilder();
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
                json.put("client_id", userInfo.getString("client_id", ""));
                json.put("uuid", userInfo.getString("uuid", ""));
                json.put("app_version", userInfo.getString("app_version", ""));

                JSONArray ja = new JSONArray();

                String[] Projection = new String[]{WonderContract.ContactEntry.COLUMN_PHONE,
                        WonderContract.ContactEntry.COLUMN_NAME};
                Cursor cursor = getContentResolver().query(WonderContract.ContactEntry.CONTENT_URI,
                        Projection,
                        null,
                        null,
                        null
                );

                final int phoneIndex = cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_PHONE);
                final int nameIndex = cursor.getColumnIndex(WonderContract.ContactEntry.COLUMN_NAME);
                while (cursor.moveToNext())
                {
                    String phone = cursor.getString(phoneIndex);
                    String name = cursor.getString(nameIndex);
                    JSONObject js = new JSONObject();
                    js.put("phone", phone);
                    js.put("name", name);
                    userContact.edit().putString(phone,name).apply();
                    userId.edit().putString(phone,ids.get(phone)).apply();
                    Log.v("contact", "cursor");
                    Log.v(phone, name);
                    ja.put(js);
                }

                json.put("contacts", ja);

                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                try{
                    out.write(json.toString());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    out.close();
                    cursor.close();
                }

                int HttpResult =urlConnection.getResponseCode();
                if(HttpResult ==HttpURLConnection.HTTP_OK){
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream(),"utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    JSONObject result = new JSONObject(sb.toString());
                    ja = result.getJSONArray("contacts");
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = (JSONObject) ja.get(i);
                        ContentValues cv = new ContentValues();
                        cv.put(WonderContract.ContactEntry.COLUMN_ON_WONDER, 1);
                        getContentResolver().update(WonderContract.ContactEntry.CONTENT_URI,
                                cv,
                                WonderContract.ContactEntry.COLUMN_PHONE + "= ?",
                                new String[]{jo.getString("phone")}
                        );
                    }
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            startActivity(new Intent(WhoisDriving.this, InviteActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }
}
