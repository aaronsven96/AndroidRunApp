package edu.dartmouth.cs.myapplication.Threads;

import android.app.Activity;
import android.content.ContentValues;
import android.os.AsyncTask;

import java.util.Objects;

import edu.dartmouth.cs.myapplication.Database.EntryDataSource;
import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;

public class UpdateEntryTask extends AsyncTask<Object,Integer,Long> {
    @Override
    protected Long doInBackground(Object... objects) {
        NewExerciseEntry entry = (NewExerciseEntry) objects[0];
        ContentValues cv = (ContentValues) objects[1];
        EntryDataSource datasource = new EntryDataSource((Activity) objects[2]);
        datasource.open();
        datasource.updateEntry(entry.getmDateTime(),cv);
        datasource.close();
        return null;
    }
}
