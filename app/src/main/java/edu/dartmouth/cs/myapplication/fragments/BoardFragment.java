package edu.dartmouth.cs.myapplication.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.dartmouth.cs.myapplication.Adapters.BoardRecylerAdapter;
import edu.dartmouth.cs.myapplication.Database.EntryDataSource;
import edu.dartmouth.cs.myapplication.Database.MyRunsSqlHelper;
import edu.dartmouth.cs.myapplication.Models.BoardEntry;
import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;
import edu.dartmouth.cs.myapplication.R;
import edu.dartmouth.cs.myapplication.Threads.LoadEntriesTask;
import edu.dartmouth.cs.myapplication.Threads.UpdateEntryTask;

public class BoardFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<NewExerciseEntry>> {

    private final static String TAG ="board_frag"; //Tag for debugging

    private static final int ALL_ENTRIES_LOADER_ID = 1;//to Load entries

    //Firebase
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private BoardRecylerAdapter mAdapter;

    //arrays for exercise entries
    private List<NewExerciseEntry> entries;
    private ArrayList<BoardEntry> boardEntries;

    private static final Double MILEStoKMS=1.60934;//conversion
    private boolean bSync=true;//only sync once

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_board, container, false);

    }
    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState){
        setHasOptionsMenu(true);//change options menu
        boardEntries=new ArrayList<>();
        getJsonItems();//get items
        setRecycler();//set reclycle view
    }

    //set sync button
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sync:
                if (bSync) {
                    LoaderManager mLoader = getLoaderManager();
                    mLoader.initLoader(ALL_ENTRIES_LOADER_ID, null, this).forceLoad();
                    bSync=false;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Sets up recycler
    private void setRecycler(){
        RecyclerView mRecyclerView = getActivity().findViewById(R.id.recycler_board);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new BoardRecylerAdapter(boardEntries);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    //hash fuction
    public String makeSHA1Hash(String input)
            throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        byte[] buffer = input.getBytes("UTF-8");
        md.update(buffer);
        byte[] digest = md.digest();

        StringBuilder hexStr = new StringBuilder();
        for (byte aDigest : digest) {
            hexStr.append(Integer.toString((aDigest & 0xff) + 0x100, 16).substring(1));
        }
        return hexStr.toString();
    }

    //Given by prof
    public void postJsonItems(final NewExerciseEntry entry) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        JSONObject jsonObject = new JSONObject();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        try {
            if(prefs.getBoolean("PRIVACY_INDEX",true)) {
                jsonObject.put("email", makeSHA1Hash(mAuth.getCurrentUser().getEmail()));
            }
            else {
                jsonObject.put("email", mAuth.getCurrentUser().getEmail());
            }

            java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String date =sdfDate.format(stringToDate(entry.getmDateTime()).getTime());
            jsonObject.put("activity_type", this.getResources().getStringArray(R.array.activity_array)[entry.getmActivityType()]);
            jsonObject.put("activity_date", entry.getmDateTime());
            jsonObject.put("input_type", this.getResources().getStringArray(R.array.type_array)[entry.getmInputType()]);
            jsonObject.put("duration", entry.getmDuration());
            jsonObject.put("distance", String.format(Locale.US,"%1$,.2f",entry.getmDistance()*MILEStoKMS));

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, "http://129.170.212.93:5000/upload_exercise", jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG,"response");
                        try {
                            if(!response.has("result") || !response.getString("result").equalsIgnoreCase("SUCCESS")){
                              Log.d(TAG,"not sucessful");
                            }
                            else{
                                Log.d(TAG,"sync was successful");
                                if (entry!=null) {
                                    getJsonItems();
                                    setRecycler();
                                    //if oncloud then insert in firebase first
                                    if (entry.getOnCloud()==1) {
                                        insertFireBase(entry);//if sucessfull put new entry in firbase
                                    }
                                    else{//just insert in sql
                                        updateEntrySql(entry);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        if(error.getMessage() != null)
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }){
        };
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(jsonObjectRequest);
    }

    //insert in firebase
    private void insertFireBase(final NewExerciseEntry entry) {
        Log.d(TAG,"inserting in firebase");

        //set new enrty with onBoard equal to 1
        DatabaseReference ref = database.getReference("users/" + mAuth.getCurrentUser().getUid() +
                "/exercise_entries/" + (entry.getmDateTime()).replace("/",""));
        ref.setValue(entry).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    //if inserted in firebase, update sql
                    updateEntrySql(entry);
                } else {
                    // Failed
                    if (task.getException() != null)
                        Toast.makeText(getActivity(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    Log.w(TAG, task.getException().getMessage());
                }
            }
        });
    }

    //updates an entry in sql
    public void updateEntrySql(NewExerciseEntry entry){
        ContentValues cv =new ContentValues();
        cv.put(MyRunsSqlHelper.ON_BOARD,1);
        new UpdateEntryTask().execute(entry,cv,getActivity());//change on board in entry

    }
    //Given by prof
    public void getJsonItems(){
        boardEntries =new ArrayList<>();
        Log.d(TAG,"request data");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, "http://129.170.212.93:5000/get_exercises", null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        // Parse the JSON array and each JSON objects inside it
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonobject = response.getJSONObject(i);
                                String duration = jsonobject.getString("duration");
                                String email = jsonobject.getString("email");
                                String activityDate = jsonobject.getString("activity_date");
                                String activityType = jsonobject.getString("activity_type");
                                String inputType = jsonobject.getString("input_type");
                                String distance = jsonobject.getString("distance");

                                boardEntries.add(new BoardEntry(email,activityDate,activityType,inputType,duration,distance));
                                mAdapter.notifyDataSetChanged();
                                Log.d(TAG,"new entry");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        if(error.getMessage() != null)
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(jsonArrayRequest);
    }

    //Functions for the loader that get and checks our items
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
        if (data!=null) {//makes sure data isn;t null
            if (data.size() > 0) {//has entries
                entries = data;
                for (NewExerciseEntry entry:entries){
                    Log.d(TAG,"got entry:"+entry.getOnBoard());
                    if (entry.getOnBoard()==0){//if not baorded then add to board
                        try {
                            Log.d(TAG,"posting");
                            postJsonItems(entry);//post on board
                        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                }
                boardEntries=new ArrayList<>();
                getJsonItems();//reset the board
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

    //String to Date
    private Calendar stringToDate(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm");
        Date date = sdf.parse(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
