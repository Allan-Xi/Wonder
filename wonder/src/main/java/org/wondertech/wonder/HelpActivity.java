package org.wondertech.wonder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_help);		
		Button btn = (Button)findViewById(R.id.help_got);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HelpActivity.this, MainActivity.class));
			}
		});
		TextView tv1 = (TextView)findViewById(R.id.help_emoji1);
		tv1.setText("\uD83D\uDD34 \uD83D\uDE97");
		TextView tv2 = (TextView)findViewById(R.id.help_emoji2);
		tv2.setText("\uD83D\uDD34 \uD83D\uDE97");
	}
}
