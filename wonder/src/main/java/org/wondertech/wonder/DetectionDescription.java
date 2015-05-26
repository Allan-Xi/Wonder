package org.wondertech.wonder;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;


public class DetectionDescription extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_detection_description);
        TextView text = (TextView)findViewById(R.id.detection_text);
        text.setText("Wonder automatically senses\nwhen you drive and changes\nyou from" +
                " ✅ to 🔴🚗");
        ImageButton back = (ImageButton)findViewById(R.id.detect_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetectionDescription.this.finish();
            }
        });
    }
}
