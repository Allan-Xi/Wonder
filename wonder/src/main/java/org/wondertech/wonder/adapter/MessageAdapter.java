package org.wondertech.wonder.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.wondertech.wonder.GetMessageActivity;
import org.wondertech.wonder.QuickContactHelper;
import org.wondertech.wonder.R;
import org.wondertech.wonder.RoundImageView;
import org.wondertech.wonder.data.WonderContract;

/**
 * Created by xiyu on 5/18/15.
 */
public class MessageAdapter extends CursorAdapter {

    public static class ViewHolder
    {
        public final TextView name;
        public final TextView time;
        public final ImageView requestCall;
        public final RoundImageView photo;
        public final TextView message;
        public final TextView icon;
        public final ImageView redDot;

        public ViewHolder(View view) {
            name = (TextView)view.findViewById(R.id.unread_name);
            time = (TextView)view.findViewById(R.id.unread_time);
            message = (TextView)view.findViewById(R.id.unread_message);
            icon = (TextView)view.findViewById(R.id.unread_icon);
            requestCall = (ImageView)view.findViewById(R.id.unread_request_call);
            redDot = (ImageView)view.findViewById(R.id.unread_red_dot);
            photo = (RoundImageView)view.findViewById(R.id.unread_icon_bg);
        }
    }

    public MessageAdapter(Context context, Cursor c, int flags){
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.unread_item, null);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String phone = cursor.getString(GetMessageActivity.NOTIFICATION_COL_PHONE);
        int type = cursor.getInt(GetMessageActivity.NOTIFICATION_COL_TYPE);
        int isRead = cursor.getInt(GetMessageActivity.NOTIFICATION_COL_ISREAD);
        String content = cursor.getString(GetMessageActivity.NOTIFICATION_COL_CONTENT);
        long time = cursor.getLong(GetMessageActivity.NOTIFICATION_COL_TIME);
        String name;

        if (phone.equals("Wonder Team")){
            name = phone;
        }else {
            Cursor cur = context.getContentResolver().query(
                    WonderContract.ContactEntry.CONTENT_URI,
                    new String[]{WonderContract.ContactEntry.COLUMN_NAME,
                            WonderContract.ContactEntry.COLUMN_PHONE},
                    WonderContract.ContactEntry.COLUMN_PHONE + " =?",
                    new String[]{phone},
                    null
            );
            try {
                if (cur.getCount() != 0) {
                    cur.moveToFirst();
                    name = cur.getString(cur.getColumnIndex(WonderContract.ContactEntry.COLUMN_NAME));
                } else
                    name = "Unknown";
            } finally {
                cur.close();
            }
        }
            holder.name.setText(name);
            if (type == 0) {
                holder.message.setText(content);
                holder.requestCall.setVisibility(View.GONE);
            } else {
                holder.message.setText("Sent you a call request");
                holder.requestCall.setVisibility(View.VISIBLE);
            }
            if (isRead == 0){
                holder.redDot.setVisibility(View.VISIBLE);
            }else{
                holder.redDot.setVisibility(View.GONE);
            }

            if (name.equals("Wonder Team")) {
                holder.icon.setBackgroundResource(R.drawable.biglogo);
                holder.icon.setText("");
            } else {
                holder.icon.setBackgroundResource(android.R.color.transparent);
                Bitmap photo = new QuickContactHelper(context, phone).getThumbnail();
                holder.photo.setAlpha(0f);
                if (photo != null) {
                    holder.photo.setAlpha(1f);
                    holder.photo.setImageBitmap(photo);
                }else{
                    if (!name.equals("Unknown")){
                        String abbre = "";
                        String[] t = name.split(" ", 2);
                        for (int i = 0; i < t.length; ++i)
                        {
                            abbre += String.valueOf(t[i].charAt(0)).toUpperCase();
                            holder.icon.setText(abbre);
                            holder.icon.setBackgroundResource(R.drawable.notification_bg);
                        }
                    }else{
                        holder.icon.setText("");
                    }
                }
            }
            if (!name.equals("Wonder Team")) {
                holder.time.setText( DateUtils.getRelativeTimeSpanString((time),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS));
            } else {
                holder.time.setText("");
            }
    }
}
