package edu.dartmouth.cs.myapplication.Threads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;

import edu.dartmouth.cs.myapplication.Database.EntryDataSource;
import edu.dartmouth.cs.myapplication.MainActivity;
import edu.dartmouth.cs.myapplication.Models.MyLatLng;

//Aaron Svendsen
//Task to Save Data

public class SaveEntryTask extends AsyncTask<Object, Integer, Long> {

    //Reference to Context to get out of manuel inout activity
    private WeakReference<Context> weakContext;

    @Override
    protected Long doInBackground(Object... objects) {
        weakContext=new WeakReference<Context>((Activity)objects[11]);
        EntryDataSource datasource = new EntryDataSource((Activity) objects[11]);
        datasource.open();
        try {//insert all data
            datasource.createExerciseEntry((int)objects[0],(int)objects[1],(java.util.Calendar) objects[2],
                    (int)objects[3],(double)objects[4], (double)objects[5],(double)objects[6],
                    (int)objects[7],(double) objects[8],(int) objects[9],(String)objects[10],(ArrayList<MyLatLng>) objects[12],
                    (int)objects[13],(int)objects[14],(String)objects[15]);
            datasource.close();
        } catch (ParseException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
    @Override
    protected void onProgressUpdate(Integer... progress) {
    }
    @Override
    protected void onPostExecute(Long result) {
        Context context;
        //if still in manuel input leave
        if (weakContext!=null){
            context=weakContext.get();
            Intent main = new Intent(context, MainActivity.class);
            context.startActivity(main);
            ((Activity)context).finish();
        }
    }
}
