package edu.dartmouth.cs.myapplication.Threads;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import edu.dartmouth.cs.myapplication.Database.EntryDataSource;
import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;

//Aaron Svendsen
//Task to Load Data

public class LoadEntriesTask extends AsyncTaskLoader<List<NewExerciseEntry>> {
    private EntryDataSource dataSource;
    public LoadEntriesTask(Context context) {
        super(context);
        dataSource=new EntryDataSource(context);
    }

    @Override
    public List<NewExerciseEntry> loadInBackground() {
        try {
            dataSource.open();
            List<NewExerciseEntry> list= dataSource.getAllEntries();
            dataSource.close();
            return list;
        } catch (ParseException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
