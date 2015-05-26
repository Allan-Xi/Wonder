package org.wondertech.wonder.Utils;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.WindowManager.BadTokenException;

import org.wondertech.wonder.R;

import java.util.ArrayList;

public class Utilities {
	static public String SENDER_ID = "612618664136";
	public static int questionNum = 2;
	public static int CONNECTTIMEOUT = 10000;
	public static int READTIMEOUT = 10000;
	public static final int DRIVING = 0;
	public static final int ONORBIT = 1;
	public static final int RESTORE = 2;
	
	public static ProgressDialog createProgressDialog(Context mContext) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        try {
                dialog.show();
        } catch (BadTokenException e) {

        }
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progressdialog);
        // dialog.setMessage(Message);
        return dialog;
    	}
	
	public static void goBackHome(Context mContext)
	{
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(startMain);
	}
	public static String phoneFormat(String s)
	{
		if (s.length() < 10)
			return s;
		StringBuilder res = new StringBuilder();
		res.append("(");
		for (int i = 0; i < s.length(); ++i)
		{
			res.append(s.charAt(i));
			switch(i)
			{
			case 2:
				res.append(')');
				res.append(' ');break;
			case 5:
				res.append('-');break;
			}
		}
		return res.toString();
	}

	public static  void updateToAndroid(Context context, String rawContactId, int flag) throws Exception{

		String prefix = "";
		switch (flag){
			case DRIVING:
				prefix =  "\u26A0\uD83D\uDE97";
				break;
			case ONORBIT:
				prefix =  "\u2705";
				break;
			case RESTORE:
				prefix = "";
				break;
			default:
		}

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		// Name
		ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
		builder.withSelection(ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?", new String[]{rawContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE});
		builder.withValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX, prefix);
		ops.add(builder.build());

		// Update
		try
		{
			context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
