package edu.dartmouth.cs.myapplication.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Arrays;


public class MyRunsSqlHelper extends SQLiteOpenHelper {

    //Database
    private static final String DATABASE_NAME = "history11.db";
    private static final int DATABASE_VERSION = 1;

    //Fields
    public static final String COLUMN_ID = "_id";
    public static final String TABLE_HISTORY = "history11";
    public static final String ACTIVITY_TYPE = "activity";
    public static final String INPUT_TYPE = "input";
    public static final String DATE_TIME = "date";
    public static final String DURATION_TIME = "duration";
    public static final String DISTANCE_TIME = "distance";
    public static final String AVG_PACE = "pace";
    public static final String AVG_SPEED = "speed";
    public static final String CALROIES = "calories";
    public static final String CLIMB = "climb";
    public static final String HEART_RATE = "heart";
    public static final String COMMENT_TEXT = "comment";
    public static final String GPS_DATA = "gps";
    public static final String ON_CLOUD ="cloud";
    public static final String ON_BOARD="board";
    public static final String EMAIL = "email";

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_HISTORY +
            " (" + COLUMN_ID + " INTEGER primary key autoincrement," +
            INPUT_TYPE + " INTEGER not null," +
            ACTIVITY_TYPE + " INTEGER not null," +
            DATE_TIME+ " CALENDER not null," +
            DURATION_TIME +" INTEGER not null," +
            DISTANCE_TIME + " FLOAT," +
            AVG_PACE+ " FLOAT," +
            AVG_SPEED+ " FLOAT," +
            CALROIES  + " INTEGER," +
            CLIMB  + " FLOAT," +
            HEART_RATE + " INTEGER," +
            COMMENT_TEXT + " TEXT," +
            GPS_DATA + " BLOB," +
            ON_CLOUD + " INTEGER," +
            ON_BOARD + " INTEGER," +
            EMAIL+" STRING);";

    //Create hleper
    MyRunsSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase mDataBase;
        mDataBase = getReadableDatabase();
        Cursor dbCursor = mDataBase.query(TABLE_HISTORY, null, null, null, null, null, null);
        String[] columnNames = dbCursor.getColumnNames();
        dbCursor.close();
        Log.d("Tag", Arrays.toString(columnNames));
    }

    //Make Database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    //if app is upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MyRunsSqlHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(db);
    }
}