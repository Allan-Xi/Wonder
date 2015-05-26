package org.wondertech.wonder;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;


public class EnableDriving extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_enable_driving);
        TextView title = (TextView)findViewById(R.id.enable_text);
        String text = "So you can focus on<br></br>" +
                "driving, Wonder<br></br><font color=#05c1ff>automatically</font> detects<br></br>" +
                "when you drive.";
        title.setText(Html.fromHtml(text));
        ImageButton enable = (ImageButton)findViewById(R.id.enable_enable);
        ImageButton disable = (ImageButton)findViewById(R.id.enable_disable);
        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.getDefaultSharedPreferences(EnableDriving.this).
                        edit().putBoolean("detectDriving",true).apply();
                startActivity(new Intent(EnableDriving.this, AutoResponse.class));
                overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );

            }
        });
        disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceManager.getDefaultSharedPreferences(EnableDriving.this).
                        edit().putBoolean("detectDriving",false).apply();
                startActivity(new Intent(EnableDriving.this, AutoResponse.class));
                overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );

            }
        });
        ImageButton how = (ImageButton)findViewById(R.id.enable_description);
        how.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EnableDriving.this, DetectionDescription.class));
            }
        });
    }
}
