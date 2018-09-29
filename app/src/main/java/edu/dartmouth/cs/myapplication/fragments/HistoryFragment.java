package edu.dartmouth.cs.myapplication.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.dartmouth.cs.myapplication.Adapters.MyRecyclerAdapter;
import edu.dartmouth.cs.myapplication.Database.EntryDataSource;
import edu.dartmouth.cs.myapplication.Database.MyRunsSqlHelper;
import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;
import edu.dartmouth.cs.myapplication.R;
import edu.dartmouth.cs.myapplication.Threads.LoadEntriesTask;
import edu.dartmouth.cs.myapplication.Threads.UpdateEntryTask;

//Aaron Svendsen

public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<NewExerciseEntry>> {

    private static final String TAG = "history";//Debug Tag

    private List<NewExerciseEntry> entries;//entries shown
    private MyRecyclerAdapter mAdapter;

    private static final int ALL_ENTRIES_LOADER_ID = 1;//loader id

    //databases
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    Context activity;

    LoaderManager mLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);

    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        activity=getActivity();
        mLoader = getLoaderManager();
        setHasOptionsMenu(true);
        entries=new ArrayList<>();
        mLoader.initLoader(ALL_ENTRIES_LOADER_ID, null, this).forceLoad();
        setRecycler();
        if (isInternetAvailable()) {
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            setFireBaseListener();
        }

    }
    // sets n listenrs
    private void setFireBaseListener() {

        //reference of the users entries
        DatabaseReference ref = database.getReference("users/" + mAuth.getCurrentUser().getUid() +
                "/exercise_entries/");

        final Fragment frag= this;
        // Attach a listener to read the data at our posts reference
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        EntryDataSource dataSource =new EntryDataSource(activity);
                        if(dataSnapshot!=null) {
                            dataSource.open();
                            for (DataSnapshot snap : dataSnapshot.getChildren()) {
                                NewExerciseEntry entry = snap.getValue(NewExerciseEntry.class);//get entires form firebase
                                if (entry != null) {
                                    if (entry.getEmail().equals(mAuth.getCurrentUser().getEmail())&&!dataSource.doesEntryExist(entry.getmDateTime())) {//if there not in sql
                                        createEntry(entry);//add to sql
                                    }
                                }
                            }
                            dataSource.close();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
        // CHild event listener to change data when database changes
        ref.addChildEventListener(new ChildEventListener() {
            //adds new child to the database
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                NewExerciseEntry entry = dataSnapshot.getValue(NewExerciseEntry.class);
                if (entry != null) {
                    if (entry.getEmail().equals(mAuth.getCurrentUser().getEmail()))
                    createEntry(entry);
                }
                //NewExerciseEntry entry=(NewExerciseEntry)dataSnapshot.getValue();
                //mAdapter.notifyDataSetChanged()
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    NewExerciseEntry entry=dataSnapshot.getValue(NewExerciseEntry.class);
                    Log.d(TAG,"changed");
                    if (entry != null) {
                        EntryDataSource dataSource =new EntryDataSource(activity);
                        dataSource.open();
                        dataSource.deleteEntry(entry);
                        createEntry(entry);
                        dataSource.close();
                    }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                NewExerciseEntry entry=dataSnapshot.getValue(NewExerciseEntry.class);
                if (entry != null) {
                    EntryDataSource dataSource =new EntryDataSource(activity);
                    dataSource.open();
                    dataSource.deleteEntry(entry);
                    dataSource.close();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    //make new entry
    private void createEntry(NewExerciseEntry entry){
        EntryDataSource dataSource =new EntryDataSource(activity);
        dataSource.open();
        if (!dataSource.doesEntryExist(entry.getmDateTime())) {//checks if entry exists
            try {
                dataSource.createExerciseEntry(entry.getmInputType(), entry.getmActivityType(), stringToDate(entry.getmDateTime()), entry.getmDuration(), entry.getmDistance(), entry.getmAvgPace()
                        , entry.getmAvgSpeed(), entry.getmCalorie(), entry.getmClimb(), entry.getmHeartRate(), entry.getmComment(), entry.getmLocationList(), entry.getOnCloud(), entry.getOnBoard(), entry.getEmail());
            } catch (ParseException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        dataSource.close();

    }
    //checks if we have internet
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    //given by prof
    private void insertFireBase(final NewExerciseEntry entry) {
        Log.d(TAG,"inserting in firebase");
        DatabaseReference ref = database.getReference("users/" + mAuth.getCurrentUser().getUid() +
                "/exercise_entries/" + (entry.getmDateTime()).replace("/",""));
        ref.setValue(entry).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ContentValues cv =new ContentValues();
                    cv.put(MyRunsSqlHelper.ON_CLOUD,1);
                    new UpdateEntryTask().execute(entry,cv,getActivity());

                } else {
                    // Failed
                    if (task.getException() != null)
                        Toast.makeText(getActivity(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        Log.w(TAG, task.getException().getMessage());
                }
            }
        });
    }

    //Sets up Recycle View
    private void setRecycler(){
        RecyclerView mRecyclerView = getActivity().findViewById(R.id.recycler_entry);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerAdapter(entries);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }
    //string to date
    private Calendar stringToDate(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm");
        Date date = sdf.parse(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
    //adds sync button
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync:
                if (isInternetAvailable()&&entries!=null) {
                    Log.d(TAG,"Internet working");
                    for (NewExerciseEntry entry: entries){
                        if(entry.getOnCloud()==0){
                            entry.setOnCloud(1);//set on cloud
                            Log.d(TAG,"enter");
                            insertFireBase(entry);//insert into firebase
                        }
                    }
                }
           Log.d(TAG,"got here");
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public android.content.Loader<List<NewExerciseEntry>> onCreateLoader(int id, Bundle args) {
        if(id == ALL_ENTRIES_LOADER_ID){
            // create an instance of the loader in our case AsyncTaskLoader
            // which loads a List of Comments List<Comment>
    return new LoadEntriesTask(this.getActivity());
        }
        return null;
    }

    @Override
    public void onLoadFinished(android.content.Loader<List<NewExerciseEntry>> loader, List<NewExerciseEntry> data) {
        Log.d(TAG,"in");
        if (data!=null) {
            Log.d(TAG,"non null");
            if (data.size() > 0) {
                Log.d(TAG,"set");
                entries = new ArrayList<>();
                for (NewExerciseEntry entry:data){//loads the entries from sql
                    if (entry.getEmail().equals(getActivity().getSharedPreferences("myAppPackage", 0).getString("email key","")))
                    entries.add(entry);
                }
                setRecycler();
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<List<NewExerciseEntry>> loader) {
        if(loader.getId() == ALL_ENTRIES_LOADER_ID&entries!=null){
            entries.clear();
            mAdapter.notifyDataSetChanged();
        }

    }
}
