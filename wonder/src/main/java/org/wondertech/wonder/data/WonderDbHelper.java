package org.wondertech.wonder.data;

/**
 * Created by xiyu on 5/11/15.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.wondertech.wonder.data.WonderContract.ContactEntry;
import org.wondertech.wonder.data.WonderContract.NotificationEntry;

/**
 * Manages a local database for weather data.
 */
public class WonderDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "wonder.db";

    public WonderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_CONTACT_TABLE = "CREATE TABLE " + ContactEntry.TABLE_NAME + " (" +
                ContactEntry._ID + " INTEGER PRIMARY KEY," +
                ContactEntry.COLUMN_RAW_ID + " TEXT NOT NULL, " +
                ContactEntry.COLUMN_PHONE + " TEXT NOT NULL, " +
                ContactEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                ContactEntry.COLUMN_DRIVING + " INTEGER NOT NULL, " +
                ContactEntry.COLUMN_ON_WONDER + " INTEGER NOT NULL, " +
                ContactEntry.COLUMN_INVITED + " INTEGER NOT NULL, " +
                ContactEntry.COLUMN_IMAGE_URL + " NULL, " +
                ContactEntry.COLUMN_LAST_STATUS_SYNC + " NULL, " +
                ContactEntry.COLUMN_PHONE_TYPE + " TEXT NOT NULL, " +
                ContactEntry.COLUMN_NOTIFY + " INTEGER NOT NULL, " +
                ContactEntry.COLUMN_LEAVE_MESSAGE + " INTEGER NOT NULL, " +
                ContactEntry.COLUMN_REQUEST_CALL + " INTEGER NOT NULL " +
                ");";

        final String SQL_CREATE_NOTIFICATION_TABLE = "CREATE TABLE " + NotificationEntry.TABLE_NAME + " (" +
                NotificationEntry._ID + " INTEGER PRIMARY KEY," +
                NotificationEntry.COLUMN_PHONE + " TEXT NOT NULL, " +
                NotificationEntry.COLUMN_TYPE + " INTEGER NOT NULL, " +
                NotificationEntry.COLUMN_TIME + " INTEGER NOT NULL, " +
                NotificationEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                NotificationEntry.COLUMN_MESSAGE_ID + " TEXT NOT NULL, " +
                NotificationEntry.COLUMN_IS_READ + " INTEGER NOT NULL " +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_CONTACT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_NOTIFICATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ContactEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
