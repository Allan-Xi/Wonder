package org.wondertech.wonder.data;

/**
 * Created by xiyu on 5/11/15.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the wonder database.
 */
public class WonderContract {

    public static final String CONTENT_AUTHORITY = "org.wondertech.wonder";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CONTACT = "contact";
    public static final String PATH_NOTIFICATION = "notification";


    /* Inner class that defines the table contents of the contact table */
    public static final class ContactEntry implements BaseColumns {

        public static final String TABLE_NAME = "contact";
        public static final String COLUMN_RAW_ID = "rawId"; //String
        public static final String COLUMN_PHONE = "phone";  //String
        public static final String COLUMN_NAME = "name";    //String
        public static final String COLUMN_DRIVING = "driving";  //int
        public static final String COLUMN_ON_WONDER = "onWonder"; //int
        public static final String COLUMN_INVITED = "invited";  //int
        public static final String COLUMN_IMAGE_URL = "imageURL"; //String
        public static final String COLUMN_LAST_STATUS_SYNC = "lastStatusSync"; //int
        public static final String COLUMN_PHONE_TYPE = "phoneType"; //String
        public static final String COLUMN_NOTIFY = "notify"; //int
        public static final String COLUMN_LEAVE_MESSAGE = "message"; //int
        public static final String COLUMN_REQUEST_CALL = "call"; //int

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTACT).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACT;

        public static Uri buildContactUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    /* Inner class that defines the table contents of the notification table */
    public static final class NotificationEntry implements BaseColumns{
        public static final String TABLE_NAME = "notification";
        public static final String COLUMN_PHONE = "phone";   //String
        public static final String COLUMN_TYPE = "type";   //int
        public static final String COLUMN_TIME = "time";   //int
        public static final String COLUMN_IS_READ = "isRead";  //int
        public static final String COLUMN_CONTENT = "content"; //String
        public static final String COLUMN_MESSAGE_ID = "messageId";//String

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTIFICATION).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTIFICATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTIFICATION;

        public static Uri buildNotificationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
