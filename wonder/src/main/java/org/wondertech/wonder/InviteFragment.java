package org.wondertech.wonder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InviteFragment extends Fragment{
	public static InviteFragment newInstance() {
		InviteFragment fragment = new InviteFragment();
        return fragment;
    }
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_invite, container, false);
        TextView tv = (TextView)rootView.findViewById(R.id.invite_content);
        tv.setText("\u2705up on loved\nones who \uD83D\uDE97!");
        return rootView;
    }
}
