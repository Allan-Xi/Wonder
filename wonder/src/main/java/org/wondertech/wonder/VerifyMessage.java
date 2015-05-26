package org.wondertech.wonder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;


public class VerifyMessage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        SmsManager sm = SmsManager.getDefault();
        sm.sendTextMessage(intent.getStringExtra("twilio_phone"),null,
                intent.getStringExtra("sms_body"),null,null);
        Intent returnIntent = new Intent();
        setResult(RESULT_OK,returnIntent);
        finish();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_verify_message);
        EditText content = (EditText)findViewById(R.id.verify_content);
        content.setText(intent.getStringExtra("sms_body"));
        Button verify = (Button)findViewById(R.id.verify_message);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
