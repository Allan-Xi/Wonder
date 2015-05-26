package org.wondertech.wonder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

import org.wondertech.wonder.Utils.Utilities;

public class FirstDriverActivity extends Activity {
	private SharedPreferences userInfo;
	private ImageButton yes;
	private ImageButton no;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_first_driver);
		userInfo = getSharedPreferences("user_info", 0);
		yes = (ImageButton)findViewById(R.id.driver_yes);
		no = (ImageButton)findViewById(R.id.driver_no);
		yes.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				userInfo.edit().putBoolean("auto_driving", true).commit();
				startActivity(new Intent(FirstDriverActivity.this, LaunchActivity.class));
			}
		});
		no.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				userInfo.edit().putBoolean("auto_driving", false).commit();
				startActivity(new Intent(FirstDriverActivity.this, LaunchActivity.class));
			}
		});
		
		
	}
	
	 @Override
		public void onBackPressed() {
	    	Utilities.goBackHome(this);
		}
}
