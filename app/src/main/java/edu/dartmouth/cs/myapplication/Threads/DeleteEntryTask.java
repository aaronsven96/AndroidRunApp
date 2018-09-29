package edu.dartmouth.cs.myapplication.Threads;

import android.app.Activity;
import android.os.AsyncTask;
import edu.dartmouth.cs.myapplication.Database.EntryDataSource;
import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;

//Aaron Svendsen
//Task to Delete Data

public class DeleteEntryTask extends AsyncTask<Object, Integer, Long> {

    @Override
    protected Long doInBackground(Object[] objects) {

        EntryDataSource datasource = new EntryDataSource((Activity) objects[0]);
        datasource.open();
        datasource.deleteEntry((NewExerciseEntry) objects[1]);
        datasource.close();
        return null;
    }
    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(Long result) {

    }
}
