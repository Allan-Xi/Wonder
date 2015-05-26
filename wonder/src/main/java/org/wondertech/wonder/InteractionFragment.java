package org.wondertech.wonder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;
import org.wondertech.wonder.Utils.Utilities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.amplitude.api.Amplitude;


public final class InteractionFragment extends Fragment{
	private FragmentActivity myContext;
	private SharedPreferences userInfo;
	private int n;
	public static InteractionFragment newInstance() {
		InteractionFragment fragment = new InteractionFragment();
        return fragment;
    }
	
	@Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		userInfo = myContext.getSharedPreferences("user_info", 0);
		View rootView = inflater.inflate(R.layout.fragment_initial, container, false);
        final ImageButton invite = (ImageButton)rootView.findViewById(R.id.question_invite);
		userInfo.edit().putInt("inviteNum", Utilities.questionNum);
        invite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				JSONObject eventProperties = new JSONObject();
				try {
				    eventProperties.put("Location", "Main View No Drivers");
				} catch (JSONException exception) {
				}
				Amplitude.logEvent("Invite Button Pressed", eventProperties);
				myContext.startActivity(new Intent(myContext, InviteActivity.class));
			}
		});


        return rootView;
    }
	@Override
    public void onPause() {
		super.onPause();
		Amplitude.endSession();
	}
	@Override
    public void onResume() {
        super.onResume();
		Amplitude.startSession();
	}

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoder.encode() failed for " + s);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
