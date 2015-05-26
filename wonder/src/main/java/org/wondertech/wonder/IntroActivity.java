package org.wondertech.wonder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amplitude.api.Amplitude;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends Activity {
	private ArrayList<TextView> person;
	private ArrayList<ImageView> message;
	private TextView title;
	private Button btn;
	private ImageView smallPhone;
	private AnimatorSet set;
	private AnimatorSet set1;
	private Handler mHandler;
	private DisplayMetrics dm;
	private boolean animEnd;
	private int btnState;
	private FrameLayout carLayout;
	private TextView smallCar;
	private TextView smallPerson;
	private TextView smallCrash;
	private FrameLayout carAnim;
	private int smallPhoneTop;
	private int smallPhoneLeft;
	private int smallPhoneRight;
	private ImageView stopSign;
	private FrameLayout bigPhoneLayout;
	private ArrayList<TextView> emoji;
	private RelativeLayout.LayoutParams lp;
	private RelativeLayout btnLayout;
	private float dpwidth;
	private ProgressDialog progressDialog;
	
	//public static Map<String, String> names;  //id, name
		public static Map<String, String> phones; //phone,name
		public static Map<String, String> ids;    //phone,id
		public static Map<String, String> contacts;
		private SharedPreferences userInfo;
		private SharedPreferences userContact;
		private SharedPreferences userId;
		TextView tv;
		String buffer;
		private int[] personIds = {R.id.intro_person1, R.id.intro_person2,R.id.intro_person3};
		private int[] messageIds = {R.id.intro_message1, R.id.intro_message2, R.id.intro_message3};
	private static final String[] PROJECTION = new String[] {
		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
		    ContactsContract.Contacts.DISPLAY_NAME,
		    ContactsContract.CommonDataKinds.Phone.DATA
		};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_intro);
		initiateContacts();
		findIds();
		
		animEnd = false;
		btnState = 0;
		
		dm = new DisplayMetrics(); 
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        dpwidth = dm.widthPixels/dm.density;
        findIds();
			
        btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (btnState == 0)
				{
					animEnd = true;
					btn.setBackgroundResource(R.drawable.what_can_i_do_button_depressed);
					btn.setEnabled(false);
				}
				
				else if (btnState == 1)
				{
					btn.setBackgroundResource(R.drawable.what_can_i_do_button_depressed);
					btn.setEnabled(false);
					mHandler.obtainMessage(3).sendToTarget();
				}
				else if (btnState == 2)
				{
					if (progressDialog == null) {
					       progressDialog = Utilities.createProgressDialog(IntroActivity.this);
					       progressDialog.show();
					       } else {
					       progressDialog.show();
					       }
					new UploadContact().execute();	
				}
					
			}

		});
        
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
            	if (msg.what == 1)
            	{	
            		if (!animEnd)
            		set.start();
            		else
            		{
    					Timer timer = new Timer();
    			        timer.schedule(new TimerTask()
    			        {
    			            public void run() 
    			            {
    			            	mHandler.obtainMessage(2).sendToTarget();
    			            }
    			        }, 4000);
    			        btnState = 1;
    			        firstAnimation();
    			        
    			
            		}
            		
            		
            	}
            	
            	if (msg.what == 2)
            	{
					btn.setText("What can I do?");
            		btn.setEnabled(true);
            		btn.setBackgroundResource(R.drawable.what_can_i_do_button_2);
            	}
            	if (msg.what == 3)
            	{
            		secondAnimation();
            	}
            		
            }
        };
        set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
        	 
        	@Override
        	public void onAnimationEnd(Animator animation) {
        	    super.onAnimationEnd(animation);
        	    mHandler.obtainMessage(1).sendToTarget();
        	}
        	 
        	});
        
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        Amplitude.startSession();
        Amplitude.logEvent("Introduction View Opened");
        //mHandler.obtainMessage(1).sendToTarget();
    }
	
	@Override
	public void onWindowFocusChanged (boolean hasFocus){
	    super.onWindowFocusChanged(hasFocus);
	    if(hasFocus){
	    	
	    	if (btnState == 0)
	    	{
	    		ObjectAnimator animator1 = ObjectAnimator.ofFloat(message.get(0), "translationX", smallPhone.getLeft()*0.7f + smallPhone.getRight()*0.3f, person.get(0).getLeft());
		        animator1.setDuration(1200);
		        ObjectAnimator animator2 = ObjectAnimator.ofFloat(message.get(0), "translationY", smallPhone.getTop(), person.get(0).getBottom() - message.get(0).getHeight()/2);
		        animator2.setDuration(1200);
		        ObjectAnimator animator3 = ObjectAnimator.ofFloat(message.get(0), "alpha", 1f, 0);
		        animator3.setDuration(1200);
		        animator3.setInterpolator(new AccelerateInterpolator());
		        ObjectAnimator animator4 = ObjectAnimator.ofFloat(message.get(0), "scaleX", 0.3f, 1.0f);
		        animator4.setDuration(1200);
		        ObjectAnimator animator5 = ObjectAnimator.ofFloat(message.get(0), "scaleY", 0.3f, 1.0f);
		        animator5.setDuration(1200);
		        
		        ObjectAnimator animator6 = ObjectAnimator.ofFloat(message.get(1), "translationX", smallPhone.getLeft()*0.7f + smallPhone.getRight()*0.3f, 
		        		person.get(1).getLeft());
		        animator6.setDuration(1200);
		        ObjectAnimator animator7 = ObjectAnimator.ofFloat(message.get(1), "translationY", smallPhone.getTop(), person.get(1).getBottom() - message.get(0).getHeight()/2);
		        animator7.setDuration(1200);
		        ObjectAnimator animator8 = ObjectAnimator.ofFloat(message.get(1), "alpha", 1f, 0);
		        animator8.setDuration(1200);
		        animator8.setInterpolator(new AccelerateInterpolator());
		        ObjectAnimator animator9 = ObjectAnimator.ofFloat(message.get(1), "scaleX", 0.3f, 1.0f);
		        animator9.setDuration(1200);
		        ObjectAnimator animator10 = ObjectAnimator.ofFloat(message.get(1), "scaleY", 0.3f, 1.0f);
		        animator10.setDuration(1200);
		        
		        ObjectAnimator animator11 = ObjectAnimator.ofFloat(message.get(2), "translationX", smallPhone.getLeft()*0.7f + smallPhone.getRight()*0.3f, person.get(2).getLeft());
		        animator11.setDuration(1200);
		        ObjectAnimator animator12 = ObjectAnimator.ofFloat(message.get(2), "translationY", smallPhone.getTop(), person.get(2).getBottom() - message.get(0).getHeight()/2);
		        animator12.setDuration(1200);
		        ObjectAnimator animator13 = ObjectAnimator.ofFloat(message.get(2), "alpha", 1f, 0);
		        animator13.setDuration(1200);
		        animator13.setInterpolator(new AccelerateInterpolator());
		        ObjectAnimator animator14 = ObjectAnimator.ofFloat(message.get(2), "scaleX", 0.3f, 1.0f);
		        animator14.setDuration(1200);
		        ObjectAnimator animator15 = ObjectAnimator.ofFloat(message.get(2), "scaleY", 0.3f, 1.0f);
		        animator15.setDuration(1200);
		        
		        
		        set.play(animator1).with(animator2);
		        set.play(animator1).with(animator3);
		        set.play(animator1).with(animator4);
		        set.play(animator1).with(animator5);
		        
		        set.play(animator1).before(animator6);
		        set.play(animator6).with(animator7);
		        set.play(animator6).with(animator8);
		        set.play(animator6).with(animator9);
		        set.play(animator6).with(animator10);
		        
		        set.play(animator6).before(animator11);
		        set.play(animator11).with(animator12);
		        set.play(animator11).with(animator13);
		        set.play(animator11).with(animator14);
		        set.play(animator11).with(animator15);
		        
		        mHandler.obtainMessage(1).sendToTarget();
	    	}
	    	smallPhoneTop = smallPhone.getTop();
	    	smallPhoneLeft = smallPhone.getLeft();
	    	smallPhoneRight = smallPhone.getRight();
			
	    }
	    
	    
	}
	
	private class UploadContact extends AsyncTask<Void, Void, Void>{
		protected Void doInBackground(Void... params) {
			String TargetURL = userInfo.getString("URL", "") + "populateContacts";
			HttpClient httpClient = new DefaultHttpClient();
			try
			{
				HttpPost request = new HttpPost(TargetURL);
                JSONObject json = new JSONObject();
                json.put("client_id", userInfo.getString("client_id", ""));
                json.put("uuid", userInfo.getString("uuid", ""));
                json.put("app_version", userInfo.getString("app_version", ""));
                
                JSONArray ja = new JSONArray();
                
                Iterator<Entry<String, String>> iter = phones.entrySet().iterator();
				while (iter.hasNext()) {
				HashMap.Entry<String, String> entry = (HashMap.Entry<String, String>) iter.next();
				String phone = entry.getKey();
				String name = entry.getValue();
				JSONObject js = new JSONObject();
                //js.put("phone", PhoneNumberUtils.stripSeparators(phone));
                js.put("phone", phone);
				js.put("name", name);
				userContact.edit().putString(phone,name).commit();
				userId.edit().putString(phone,ids.get(phone)).commit();
                Log.v(phone, name);
                ja.put(js);
				}
				
                json.put("contacts", ja);
				
                StringEntity se = new StringEntity( json.toString());
                
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
                request.setHeader("Accept", "application/json");
                request.setEntity(se);
				HttpResponse httpResponse = httpClient.execute(request);
				String retSrc = EntityUtils.toString(httpResponse.getEntity());
				JSONObject result = new JSONObject(retSrc);
				ja = result.getJSONArray("contacts");
				Log.v("get response", retSrc);
				for (int i = 0; i < ja.length(); i++) {
					JSONObject jo = (JSONObject) ja.get(i);
					Log.v(jo.getString("phone"),phones.get(jo.getString("phone")));
				}
				startActivity(new Intent(IntroActivity.this, InviteActivity.class));
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}
	
	@Override
	public void onBackPressed() {
		Utilities.goBackHome(this);
	}
	
	private void initiateContacts()
	{
		userInfo = getSharedPreferences("user_info", 0);
		userContact = getSharedPreferences("user_contact", 0);
		userId = getSharedPreferences("user_id", 0);
		
		phones = new HashMap<String, String>();
		contacts = new HashMap<String, String>();
		ids = new HashMap<String, String>();
		
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
		if (cursor != null) {
		    try {
		        final int contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
		        final int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		        final int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
		        String contactId;
		        String displayName, phoneNumber;
		        while (cursor.moveToNext()) {
		        	phoneNumber = cursor.getString(phoneIndex);
		        	if (PhoneNumberUtils.stripSeparators(phoneNumber).length() != 10)
		        		continue;
		            contactId = cursor.getString(contactIdIndex);
		            displayName = cursor.getString(displayNameIndex);
		            String[] tmp = displayName.split(" ", 2);
		            if (tmp[0].equals("\u26A0\uD83D\uDE97") || tmp[0].equals("\u2705"))
		            {
		            	displayName = tmp[1];
		            }        
		            phones.put(PhoneNumberUtils.stripSeparators(phoneNumber), displayName);
		            ids.put(PhoneNumberUtils.stripSeparators(phoneNumber), contactId);
		            Log.i(PhoneNumberUtils.stripSeparators(phoneNumber),displayName);
		        }
		    } finally {
		        cursor.close();
		    }
		}
	}
	
	private void findIds()
	{
		person = new ArrayList<TextView>();
		emoji = new ArrayList<TextView>();
		message = new ArrayList<ImageView>();
		for (int i = 0; i < personIds.length; ++i)
		{
			person.add((TextView)findViewById(personIds[i]));
			message.add((ImageView)findViewById(messageIds[i]));
		}
		
		
		title = (TextView)findViewById(R.id.intro_title);
		btn = (Button)findViewById(R.id.intro_button);
		btnLayout = (RelativeLayout)findViewById(R.id.intro_btnlayout);
		
		smallCar = (TextView)findViewById(R.id.intro_smallcar);
		smallPerson = (TextView)findViewById(R.id.intro_smallperson);
		smallCrash = (TextView)findViewById(R.id.intro_smallcrash);
		smallCar.setText("\uD83D\uDE97");
		smallPerson.setText("\uD83D\uDC66");
		smallCrash.setText("\uD83D\uDCA5");
		carAnim = (FrameLayout)findViewById(R.id.intro_car_anim);
		bigPhoneLayout = (FrameLayout)findViewById(R.id.big_phone_layout);

		person.get(0).setText("\uD83D\uDC67");
		person.get(1).setText("\uD83D\uDC68");
		person.get(2).setText("\uD83D\uDC69");
		
		smallPhone = (ImageView)findViewById(R.id.intro_phone);
		carLayout = (FrameLayout)findViewById(R.id.intro_car);
	}
	
	private void firstAnimation()
	{
		carLayout.setVisibility(View.VISIBLE);
        
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(carAnim, "translationX", dm.widthPixels, dm.widthPixels/2);
        animator1.setDuration(2000);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(carAnim, "translationX",dm.widthPixels/2, 0);
        animator2.setDuration(1500);
        animator2.addListener(new AnimatorListenerAdapter() {
	        	 
        	@Override
        	public void onAnimationEnd(Animator animation) {
        	    super.onAnimationEnd(animation);
        	    Vibrator v = (Vibrator) IntroActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
        	    // Vibrate for 500 milliseconds
        	    v.vibrate(500);
        	}

        	});
        animator2.setInterpolator(new AccelerateInterpolator(0.6f));
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(smallPerson, "rotation", -30f);
        animator3.setDuration(100);
        
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(smallPerson, "translationX", -10f);
        animator4.setDuration(100);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(smallPerson, "translationY", 10f);
        animator5.setDuration(100);
        ObjectAnimator animator6 = ObjectAnimator.ofFloat(smallCrash, "scaleX", 0, 1);
        animator6.setDuration(100);
        ObjectAnimator animator7 = ObjectAnimator.ofFloat(smallCrash, "scaleY", 0, 1);
        animator7.setDuration(100);
            	
       
        /*RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins((int) ((smallPhoneLeft*0.7f + smallPhoneRight*0.3f)/dm.density), (int)(smallPhoneTop/(float)dm.density), 0, 0);
        message.get(0).setLayoutParams(layoutParams);*/
        ObjectAnimator animator8 = ObjectAnimator.ofFloat(message.get(0), "translationX", smallPhoneLeft*0.7f + smallPhoneRight*0.3f, smallPhoneLeft*0.7f + smallPhoneRight*0.3f);
        animator8.setDuration(1800);
        animator8.addListener(new AnimatorListenerAdapter() {
       	 
        	@Override
        	public void onAnimationEnd(Animator animation) {
        	    super.onAnimationEnd(animation);
        	    message.get(0).setAlpha(1f);
		        message.get(0).setScaleX(0.6f);
		        message.get(0).setScaleY(0.6f);
		        message.get(0).bringToFront();
        	}

        	});
        ObjectAnimator animator9 = ObjectAnimator.ofFloat(message.get(0), "translationX", smallPhoneLeft*0.7f + smallPhoneRight*0.3f, dm.widthPixels/2);
        animator9.setDuration(200);
        ObjectAnimator animator11;
        if (dpwidth > 350)
        {
        	animator11 = ObjectAnimator.ofFloat(message.get(0), "translationY", smallPhoneTop, Math.round(240 * dm.density));
            animator11.setDuration(200);
        }
        else
        {
        	animator11 = ObjectAnimator.ofFloat(message.get(0), "translationY", smallPhoneTop, Math.round(190 * dm.density));
            animator11.setDuration(200);
        }
        
        ObjectAnimator animator10 = ObjectAnimator.ofFloat(message.get(0), "translationX", dm.widthPixels/2, 0);
        animator10.setDuration(1500);
        animator10.setInterpolator(new AccelerateInterpolator(0.6f));
        ObjectAnimator animator12 = ObjectAnimator.ofFloat(message.get(0), "alpha", 1f, 0);
        animator12.setDuration(100);
        /*ObjectAnimator animator3 = ObjectAnimator.ofFloat(message.get(0), "alpha", 1f, 0);
        animator3.setDuration(1200);
        animator3.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(message.get(0), "scaleX", 0.3f, 0.8f);
        animator4.setDuration(1200);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(message.get(0), "scaleY", 0.3f, 0.8f);
        animator5.setDuration(1200);*/
        ObjectAnimator animator13 = ObjectAnimator.ofFloat(title, "alpha", 1f, 0);
        animator13.setDuration(400);
        animator13.addListener(new AnimatorListenerAdapter() {
        	 
        	@Override
        	public void onAnimationEnd(Animator animation) {
        	    super.onAnimationEnd(animation);
        	    title.setText("Texting drivers by accident causes accidents.");
        	}

        	});
        ObjectAnimator animator14 = ObjectAnimator.ofFloat(title, "alpha", 0, 1f);
        animator14.setDuration(400);
        
        set1 = new AnimatorSet(); 
        set1.play(animator1).before(animator2);
        set1.play(animator2).before(animator3);
        set1.play(animator3).with(animator4);
        set1.play(animator3).with(animator5);
        set1.play(animator3).with(animator6);
        set1.play(animator3).with(animator7);
        set1.play(animator1).with(animator8);
        set1.play(animator8).before(animator9);
        set1.play(animator9).before(animator10);
        set1.play(animator9).with(animator11);
        set1.play(animator7).with(animator12);
        set1.play(animator1).with(animator13);
        set1.play(animator13).before(animator14);
        set1.start();
	}
	
	private void secondAnimation(){
		//stopSign = (ImageView)findViewById(R.id.intro_stop);
		//stopSign.setVisibility(View.GONE);
		carLayout.setVisibility(View.GONE);
		final TextView person4 = (TextView)findViewById(R.id.intro_person4);
		person4.setVisibility(View.VISIBLE);
		person4.setText("\uD83D\uDC66");
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(title, "alpha", 1f, 0);
	        animator1.setDuration(400);
	        animator1.addListener(new AnimatorListenerAdapter() {
	        	 
	        	@Override
	        	public void onAnimationEnd(Animator animation) {
	        	    super.onAnimationEnd(animation);
	        	    title.setText("Wonder shows you who\n" +
	        	    		"it's safe to text.");
	        	}
	
	        	});
	        ObjectAnimator animator2 = ObjectAnimator.ofFloat(title, "alpha", 0, 1f);
	        animator2.setDuration(400);
	        ObjectAnimator animator3 = ObjectAnimator.ofFloat(smallPhone, "translationY", dm.heightPixels);
        animator3.setDuration(500);
        lp = new RelativeLayout.LayoutParams(
			        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        
        emoji.add((TextView)findViewById(R.id.emoji1));
    	    emoji.add((TextView)findViewById(R.id.emoji2));
    	    emoji.add((TextView)findViewById(R.id.emoji3));
    	    emoji.add((TextView)findViewById(R.id.emoji4));
    	    
        animator3.addListener(new AnimatorListenerAdapter() {
	        	 
	        	@Override
	        	public void onAnimationEnd(Animator animation) {
	        	    super.onAnimationEnd(animation);
	        	    bigPhoneLayout.setVisibility(View.VISIBLE);
	        	    bigPhoneLayout.bringToFront();
	        	    
	        	    emoji.get(0).setText("\u274C\uD83D\uDE97");
	        	    int a = person.get(0).getLeft() + (int)(30 * dm.density);
	        	    int b = person.get(0).getTop();
	        	    int c;
	        	    person.get(0).setLayoutParams(lp);
	        	    TranslateAnimation anim1;
	        	    if (dpwidth > 350)
	        	    {
	        	    	anim1 = new TranslateAnimation(
		        	    		a - (int)(30 * dm.density) , a,
		        	    		b ,btnLayout.getTop() - ((441 - 212 + 12) * dm.density));
	        	    }
	        	    else
	        	    {
	        	    	anim1 = new TranslateAnimation(
		        	    		a - (int)(30 * dm.density), a,
		        	    		b ,btnLayout.getTop() - ((319 - 150 + 8) * dm.density));
	        	    }
  			     	anim1.setDuration(400); 
  			     	anim1.setFillAfter(true);
  			     	person.get(0).setAnimation(anim1);
  			     	anim1.startNow();
  			  
  			     	
  			     	c = person.get(1).getLeft();
        	    b = person.get(1).getTop();
        	    person.get(1).setLayoutParams(lp);
        	    TranslateAnimation anim2;
        	    if (dpwidth > 350)
        	    {
  			     	anim2 = new TranslateAnimation(
        	    		c,a,
        	    		b,btnLayout.getTop() - ((441 - 282 + 12) * dm.density));
        	    }
        	    else
        	    {
        	    	anim2 = new TranslateAnimation(
            	    		c,a,
            	    		b,btnLayout.getTop() - ((319 - 202 + 12) * dm.density));
        	    }
			     	anim2.setDuration(400);      	
			     	anim2.setFillAfter(true);
			     	person.get(1).setAnimation(anim2);
			     	anim2.setStartOffset(200);
			     	anim2.startNow();
			     	
			     	c = person.get(2).getLeft();
        	    b = person.get(2).getTop();
        	    person.get(2).setLayoutParams(lp);
        	    TranslateAnimation anim3;
        	    if (dpwidth > 350)
        	    {
        	    	anim3 = new TranslateAnimation(
            	    		c,a,
            	    		b,btnLayout.getTop() - ((441 - 348 + 10) * dm.density));
        	    }
        	    else{
        	    	anim3 = new TranslateAnimation(
            	    		c,a,
            	    		b,btnLayout.getTop() - ((319 - 248 + 8) * dm.density));
        	    }
			     	
			     	anim3.setDuration(400);      	
			     	anim3.setFillAfter(true);
			     	person.get(2).setAnimation(anim3);
			     	anim3.setStartOffset(400);
			     	anim3.startNow();
			     	
			     			   			    
			     	//c = smallPerson.getLeft();
			     	
			     	c = person4.getLeft();
        	    b = person4.getTop();
			     	FrameLayout.LayoutParams _rootLayoutParams = new FrameLayout.LayoutParams(
        	    		FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        	    //smallPerson.setLayoutParams(_rootLayoutParams);
        	    //_rootLayoutParams.setMargins(0,0,0,0);
        	    person4.setLayoutParams(lp);
        	    TranslateAnimation anim4;
        	    if (dpwidth > 350)
        	    {
        	    	anim4 = new TranslateAnimation(
            	    		c,a,
            	    		b,btnLayout.getTop() - ((441 - 145 + 8) * dm.density));
        	    }
        	    else{
        	    	anim4 = new TranslateAnimation(
            	    		c,a,
            	    		b,btnLayout.getTop() - ((319 - 100 + 8) * dm.density));
        	    }
			     	
			     	anim4.setDuration(400);      	
			     	anim4.setFillAfter(true);
			     	person4.setAnimation(anim4);
			     	anim4.setStartOffset(600);
			     	anim4.startNow();
			     	
			     	//smallPerson.setRotation(0f);
			     	//smallCar.setVisibility(View.INVISIBLE);
			     	//smallCrash.setVisibility(View.INVISIBLE);
	        		}
	        	});
        
        
			        
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(bigPhoneLayout, "translationX", 
        		dm.widthPixels,0);
	        animator4.setDuration(1000);
	        
	        ObjectAnimator animator5 = ObjectAnimator.ofFloat(emoji.get(0), "ScaleX", 
        		0f,1f);
        animator5.setDuration(200);
        ObjectAnimator animator6 = ObjectAnimator.ofFloat(emoji.get(0), "ScaleY", 
        		0f,1f);
        animator6.setDuration(200);

        ObjectAnimator animator7 = ObjectAnimator.ofFloat(emoji.get(1), "ScaleX", 
        		0f,1f);
        animator7.setDuration(200);
        ObjectAnimator animator8 = ObjectAnimator.ofFloat(emoji.get(1), "ScaleY", 
        		0f,1f);
        animator8.setDuration(200);
        
        ObjectAnimator animator9 = ObjectAnimator.ofFloat(emoji.get(2), "ScaleX", 
        		0f,1f);
        animator9.setDuration(200);
        ObjectAnimator animator10 = ObjectAnimator.ofFloat(emoji.get(2), "ScaleY", 
        		0f,1f);
        animator10.setDuration(200);
        
        ObjectAnimator animator11 = ObjectAnimator.ofFloat(emoji.get(3), "ScaleX", 
        		0f,1f);
        animator11.setDuration(200);
        ObjectAnimator animator12 = ObjectAnimator.ofFloat(emoji.get(3), "ScaleY", 
        		0f,1f);
        animator12.setDuration(200);
        
        ObjectAnimator animator13 = ObjectAnimator.ofFloat(person4, "ScaleX", 
        		1f,0.6f);
        animator13.setDuration(400);
        ObjectAnimator animator14 = ObjectAnimator.ofFloat(person4, "ScaleY", 
        		1f,0.6f);
        animator14.setDuration(400);
        ObjectAnimator animator15 = ObjectAnimator.ofFloat(person.get(0), "ScaleX", 
        		1f,0.6f);
        animator15.setDuration(400);
        ObjectAnimator animator16 = ObjectAnimator.ofFloat(person.get(0), "ScaleY", 
        		1f,0.6f);
        animator16.setDuration(400);
        ObjectAnimator animator17 = ObjectAnimator.ofFloat(person.get(1), "ScaleX", 
        		1f,0.6f);
        animator17.setDuration(400);
        ObjectAnimator animator18 = ObjectAnimator.ofFloat(person.get(1), "ScaleY", 
        		1f,0.6f);
        animator18.setDuration(400);
        ObjectAnimator animator19 = ObjectAnimator.ofFloat(person.get(2), "ScaleX", 
        		1f,0.6f);
        animator19.setDuration(200);
        ObjectAnimator animator20 = ObjectAnimator.ofFloat(person.get(2), "ScaleY", 
        		1f,0.6f);
        animator20.setDuration(200);
        
        animator12.addListener(new AnimatorListenerAdapter() {
	        	 
	        	@Override
	        	public void onAnimationEnd(Animator animation) {
	        	    super.onAnimationEnd(animation);
	        	    btn.setEnabled(true);
					btn.setText("Show me who is \uD83D\uDD34 & \u2705");
	        	    btn.setBackgroundResource(R.drawable.protect_my_contacts_input);
	        	    btnState = 2;
	        	}
	
	        	});
        
	             
        AnimatorSet set = new AnimatorSet();
        set.play(animator1).before(animator2);
        set.play(animator2).before(animator3);
        set.play(animator3).before(animator5);
        set.play(animator4).before(animator5);
        set.play(animator5).with(animator6);
        set.play(animator6).before(animator7);
        set.play(animator7).with(animator8);
        set.play(animator8).before(animator9);
        set.play(animator9).with(animator10);
        set.play(animator10).before(animator11);
        set.play(animator11).with(animator12);
        
        set.play(animator5).with(animator13);
        set.play(animator5).with(animator14);
        set.play(animator7).with(animator15);
        set.play(animator7).with(animator16);
        set.play(animator9).with(animator17);
        set.play(animator9).with(animator18);
        set.play(animator11).with(animator19);
        set.play(animator11).with(animator20);
        set.start();
	}
	
	@Override
    public void onPause() {
		super.onPause();
		Amplitude.endSession();
	}
}
