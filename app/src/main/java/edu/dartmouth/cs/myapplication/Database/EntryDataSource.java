package edu.dartmouth.cs.myapplication.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.dartmouth.cs.myapplication.Adapters.MyRecyclerAdapter;
import edu.dartmouth.cs.myapplication.Models.MyLatLng;
import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;

public class EntryDataSource {
    // Database fields
    private SQLiteDatabase database;
    private MyRunsSqlHelper dbHelper;
    private String[] allColumns = { MyRunsSqlHelper.COLUMN_ID,MyRunsSqlHelper.INPUT_TYPE,MyRunsSqlHelper.ACTIVITY_TYPE,
            MyRunsSqlHelper.DATE_TIME,MyRunsSqlHelper.DURATION_TIME
    ,MyRunsSqlHelper.DISTANCE_TIME,MyRunsSqlHelper.AVG_PACE,MyRunsSqlHelper.AVG_SPEED,
            MyRunsSqlHelper.CALROIES,MyRunsSqlHelper.CLIMB,MyRunsSqlHelper.HEART_RATE
    ,MyRunsSqlHelper.COMMENT_TEXT,MyRunsSqlHelper.GPS_DATA, MyRunsSqlHelper.ON_CLOUD,MyRunsSqlHelper.ON_BOARD,MyRunsSqlHelper.EMAIL};

    //Log tag
    private static final String TAG = "Database";

    //Set up
    public EntryDataSource(Context context) {
        dbHelper = new MyRunsSqlHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createExerciseEntry(int mInputType, int mActivityType, Calendar mDateTime, int mDuration, double mDistance, double mAvgPace
    , double mAvgSpeed, int mCalorie, double mClimb, int mHeartRate, String mComment, ArrayList<MyLatLng> mLocationList, int cloud, int board
    ,String email) throws ParseException, IOException, ClassNotFoundException {

        //Put all values in content values
        ContentValues values = new ContentValues();
        values.put(MyRunsSqlHelper.ACTIVITY_TYPE, mActivityType);
        values.put(MyRunsSqlHelper.INPUT_TYPE, mInputType);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yy HH:mm", Locale.US); //chnage from cal to Sting
        values.put(MyRunsSqlHelper.DATE_TIME,  (sdf.format(mDateTime.getTime()))); //datetime
        values.put(MyRunsSqlHelper.DURATION_TIME, mDuration);
        values.put(MyRunsSqlHelper.DISTANCE_TIME, mDistance);
        values.put(MyRunsSqlHelper.AVG_PACE, mAvgPace);
        values.put(MyRunsSqlHelper.AVG_SPEED, mAvgSpeed);
        values.put(MyRunsSqlHelper.CALROIES, mCalorie);
        values.put(MyRunsSqlHelper.CLIMB, mClimb);
        values.put(MyRunsSqlHelper.HEART_RATE, mHeartRate);
        values.put(MyRunsSqlHelper.COMMENT_TEXT, mComment);
        byte[] array = arrayToByte(mLocationList);
        values.put(MyRunsSqlHelper.GPS_DATA,array);
        values.put(MyRunsSqlHelper.ON_CLOUD,cloud);
        values.put(MyRunsSqlHelper.ON_BOARD,board);
        values.put(MyRunsSqlHelper.EMAIL,email);

        long insertId = database.insert(MyRunsSqlHelper.TABLE_HISTORY, null, values);

        //Check database for testing purposes
        Cursor cursor = database.query(MyRunsSqlHelper.TABLE_HISTORY,
                allColumns, MyRunsSqlHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        //NewExerciseEntry newEntry = cursorToEntry(cursor);

        // Log the comment stored
        Log.d(TAG, "comment = " + cursorToEntry(cursor).toString()
                + " insert ID = " + insertId);

        cursor.close();
        //return newEntry; //no need to return
    }
    public boolean doesEntryExist(String date){
        Cursor cursor = database.query(MyRunsSqlHelper.TABLE_HISTORY,
                allColumns, MyRunsSqlHelper.DATE_TIME + " = " + "'"+date+"'", null,
                null, null, null);
        Log.d(TAG,cursor.getCount()+"");
        return cursor.getCount()>0;
    }
    //deletes an entry from the database
    public void deleteEntry(NewExerciseEntry entry) {
        String id = entry.getmDateTime();
        Log.d(TAG, "delete comment = " + id);
        System.out.println("Comment deleted with id: " + id);
        database.delete(MyRunsSqlHelper.TABLE_HISTORY, MyRunsSqlHelper.DATE_TIME
                 +" = " + "'"+entry.getmDateTime()+"'", null);
    }
    public void updateEntry(String date, ContentValues cv){
        database.update(MyRunsSqlHelper.TABLE_HISTORY, cv, MyRunsSqlHelper.DATE_TIME+ " = " +"'"+date+"'", null);
    }

//      No need to delete all for now
//    public void deleteAllEntries() {
//        System.out.println("Comment deleted all");
//        Log.d(TAG, "delete all = ");
//        database.delete(MyRunsSqlHelper.TABLE_HISTORY, null, null);
//    }

    //Get all the entries form the database
    public List<NewExerciseEntry> getAllEntries() throws ParseException, IOException, ClassNotFoundException {
        List<NewExerciseEntry> entries = new ArrayList<>(); //return object

        Cursor cursor = database.query(MyRunsSqlHelper.TABLE_HISTORY,
                allColumns, null, null, null, null, null);

        //Search through database
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            NewExerciseEntry entry= cursorToEntry(cursor);
            Log.d(TAG, "get comment = " + cursorToEntry(cursor).toString());
            entries.add(entry);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return entries;
    }
    //Gets entry form Cursor Object
    private NewExerciseEntry cursorToEntry(Cursor cursor) throws ParseException, IOException, ClassNotFoundException {
        NewExerciseEntry entry= new NewExerciseEntry();
        entry.setId(cursor.getLong(0));
        entry.setmInputType(cursor.getInt(1));
        entry.setmActivityType(cursor.getInt(2));
        entry.setmDateTime(cursor.getString(3));
        entry.setmDuration(cursor.getInt(4));
        entry.setmDistance(cursor.getDouble(5));
        entry.setmAvgPace(cursor.getDouble(6));
        entry.setmAvgSpeed(cursor.getDouble(7));
        entry.setmCalorie(cursor.getInt(8));
        entry.setmClimb(cursor.getDouble(9));
        entry.setmHeartRate(cursor.getInt(10));
        entry.setmComment(cursor.getString(11));
        entry.setmLocationList(byteToArray(cursor.getBlob(12)));
        entry.setOnCloud(cursor.getInt(13));
        entry.setOnBoard(cursor.getInt(14));
        entry.setEmail(cursor.getString(15));
        return entry;
    }
    private byte[] arrayToByte(ArrayList<MyLatLng> locData) throws IOException {
        if(locData == null)return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        for (MyLatLng element : locData) {
            out.writeUTF(element.latitude+","+element.longitude);

        }
        return baos.toByteArray();
    }

    private ArrayList<MyLatLng> byteToArray(byte[] data) throws IOException {
        if(data == null)return null;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(bais);
        ArrayList<MyLatLng> array = new ArrayList<>();
        while (in.available() > 0) {
            String element = in.readUTF();
            String s = element;
            s = s.substring(0, s.indexOf(","));
            double lat = Double.parseDouble(s);
            //Log.d(TAG,"lat:"+lat);
            s = element;
            s = s.substring(s.indexOf(",")+1,s.length()-1);
            double lng = Double.parseDouble(s);
            //Log.d(TAG,"lon:"+lng);
            MyLatLng toAdd = new MyLatLng(lat,lng);
            array.add(toAdd);
        }
        return array;

    }
}
