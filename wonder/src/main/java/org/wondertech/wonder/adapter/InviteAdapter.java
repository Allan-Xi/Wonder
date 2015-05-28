package org.wondertech.wonder.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.wondertech.wonder.InviteActivity;
import org.wondertech.wonder.QuickContactHelper;
import org.wondertech.wonder.R;
import org.wondertech.wonder.RoundImageView;
import org.wondertech.wonder.Utils.Utilities;

/**
 * Created by xiyu on 5/13/15.
 */
public class InviteAdapter extends CursorAdapter {

    public static class ViewHolder
    {
        public final TextView name;
        public final TextView initials;
        public final ImageView addPlus;
        public final RoundImageView photo;
        public final TextView type;
        public final TextView phone;
        public final TextView firstName;

        public ViewHolder(View view) {
            name = (TextView)view.findViewById(R.id.invite_lastname);
            phone = (TextView)view.findViewById(R.id.invite_phone);
            initials = (TextView)view.findViewById(R.id.invite_initials);
            addPlus = (ImageView)view.findViewById(R.id.invite_add);
            photo = (RoundImageView)view.findViewById(R.id.invite_photo);
            firstName = (TextView)view.findViewById(R.id.invite_firstname);
            type = (TextView)view.findViewById(R.id.invite_phone_type);
        }
    }

    public InviteAdapter(Context context, Cursor c, int flags){
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.invite_item, null);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String phone = cursor.getString(InviteActivity.CONTACT_COL_PHONE_INDEX);
        String name = cursor.getString(InviteActivity.CONTACT_COL_NAME_INDEX);
        String type = cursor.getString(InviteActivity.CONTACT_COL_PHONE_TYPE_INDEX);
        int onWonder = cursor.getInt(InviteActivity.CONTACT_COL_ON_WONDER_INDEX);
        int inVited = cursor.getInt(InviteActivity.CONTACT_COL_INVITED_INDEX);
        String abbre = "";
        String[] t = name.split(" ", 2);
        for (int i = 0; i < t.length; ++i) {
            abbre += String.valueOf(t[i].charAt(0)).toUpperCase();
        }
        holder.initials.setText(abbre);
        if (t.length > 1 && !t[1].isEmpty())
            holder.name.setText(t[1]);
        holder.phone.setText(Utilities.phoneFormat(phone));
        if (inVited == 0 && onWonder == 0){
            holder.addPlus.setVisibility(View.VISIBLE);
        } else {
            holder.addPlus.setVisibility(View.GONE);
        }
        Bitmap photo = new QuickContactHelper(context, phone).getThumbnail();
        holder.photo.setAlpha(0f);
        if (photo != null) {
            holder.photo.setAlpha(1f);
            holder.photo.setImageBitmap(photo);
        }
        if (inVited == 1 && onWonder == 0) {
            if (!t[0].isEmpty())
                holder.firstName.setText("\uD83D\uDD58" + t[0]);
        } else if (onWonder == 1) {
            if (!t[0].isEmpty())
                holder.firstName.setText("\u2705" + t[0]);
        } else {
            if (!t[0].isEmpty())
                holder.firstName.setText("" + t[0]);
        }
        holder.type.setText(type);
    }
}
