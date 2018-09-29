package edu.dartmouth.cs.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;
import edu.dartmouth.cs.myapplication.Threads.DeleteEntryTask;
import edu.dartmouth.cs.myapplication.Threads.SaveEntryTask;
import edu.dartmouth.cs.myapplication.fragments.NumDialogFragment;
import edu.dartmouth.cs.myapplication.fragments.StartFragment;

import static edu.dartmouth.cs.myapplication.fragments.StartFragment.KEY_ACTIVITY_NUM;
import static edu.dartmouth.cs.myapplication.fragments.StartFragment.KEY_INPUT_NUM;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MINUTE;

public class ManuelInputActivity extends AppCompatActivity {
    //Keys to sent from recycle view
    private static final String KEY_ENTRY = "entry";
    private static final String KEY_HIS = "his";

    //keys to save data on phone flip
    private final static String UNIT_INDEX = "UNIT_INDEX";
    private final static String KEY_TYPE = "type";
    private final static String KEY_INPUT = "input";
    private static final String KEY_BUTTON = "button";
    private static final String KEY_PRE = "pre";
    private static final String KEY_ACTIVITY = "activity";
    private static final String KEY_CALORIE = "calorie";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_DATE = "date";
    private static final String KEY_DISTANCE = "dis";
    private static final String KEY_TIME = "time";
    private static final String KEY_HEART = "heart";
    private static final String KEY_DUR = "dur";

    //to save email
    private static final String EMAIL_KEY = "email key";

    // can i delete
    private boolean delete =false;

    //type of units
    private boolean disType=false;

    private Calendar cal= Calendar.getInstance();

    //For Conversions
    private static final Double KMStoMILES=0.621371;
    private static final Double MILEStoKMS=1.60934;

    //hisEntry
    NewExerciseEntry entryHis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manuel_input);

        Intent intent = getIntent();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //can i delete
        delete = getIntent().getBooleanExtra(KEY_HIS,false);
        //Type of units
        disType=prefs.getString(UNIT_INDEX," ").equals("Imperial");

        //Set up data from entry in database
        if (delete){

            try {
                entryHis=(NewExerciseEntry)intent.getSerializableExtra(KEY_ENTRY);
                setData(entryHis);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        //else set up staring values
        else {
            //Set Date and Time
            if (disType) {
                final TextView disView = findViewById(R.id.text_distance_input);
                disView.setText(R.string.text_miles);
            }
            final TextView dateView = findViewById(R.id.text_date_input);
            final TextView timeView = findViewById(R.id.text_time_input);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
            dateView.setText(sdf.format(cal.getTime()));
            Date x = cal.getTime();
            cal.setTime(x);
            cal.set(Calendar.HOUR_OF_DAY,cal.get(HOUR));
            cal.set(Calendar.MINUTE, cal.get(MINUTE));
            timeView.setText(getString(R.string.time, cal.get(HOUR), cal.get(MINUTE)));
            final TextView activityView = findViewById(R.id.text_activity_input);
            activityView.setText(getIntent().getStringExtra(StartFragment.KEY_SPINNER));

        }
        //set up scene of rotation
        if (savedInstanceState!=null){
            setStrings(savedInstanceState);
        }
        //Set up toolbar
        Toolbar myToolbar = findViewById(R.id.toolbar_input);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //set Onclick if not from database
        if (!delete) {
            setClicks();
        }
    }

    //Change a String back into a Calender Object
    private Calendar stringToDate(String time) throws ParseException {
        android.icu.text.SimpleDateFormat sdf = new android.icu.text.SimpleDateFormat("MM/dd/yy HH:mm");
        Date date = sdf.parse(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
    //Set of Data form entry
    private void setData(NewExerciseEntry entry) throws ParseException {
        final TextView activityString = findViewById(R.id.text_activity_input);
        final TextView calorieString = findViewById(R.id.text_Calorie_input);
        final TextView commentString = findViewById(R.id.text_comment_input);
        final TextView dateString = findViewById(R.id.text_date_input);
        final TextView distanceString = findViewById(R.id.text_distance_input);
        final TextView durationString = findViewById(R.id.text_duration_input);
        final TextView heartbeatString = findViewById(R.id.text_heartbeat_input);
        final TextView timeString = findViewById(R.id.text_time_input);

        //format time
        Calendar setCal = stringToDate(entry.getmDateTime());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm",Locale.US);
        String time=sdf.format(setCal.getTime());
        SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yy",Locale.US);
        String date =sdfDate.format(setCal.getTime());

        //Set all the texts
        activityString.setText(String.format(Locale.US,"%s",getResources().getStringArray(R.array.activity_array)[entry.getmActivityType()]));
        calorieString.setText(String.format(Locale.US,"%d cal",entry.getmCalorie()));
        commentString.setText(entry.getmComment());
        dateString.setText(date);
        if (!disType) {
            distanceString.setText(String.format(Locale.US, "%1$,.2f kms", entry.getmDistance()*MILEStoKMS));
        }
        else{
            distanceString.setText(String.format(Locale.US, "%1$,.2f miles", entry.getmDistance()));
        }
        durationString.setText(String.format(Locale.US,"%d mins",entry.getmDuration()));
        heartbeatString.setText(String.format(Locale.US,"%d bpm",entry.getmHeartRate()));
        timeString.setText(time);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.save_action):
                if (!delete) {
                    //Save all the data
                    int inputID = getIntent().getIntExtra(KEY_INPUT_NUM, 0);
                    int activityID = getIntent().getIntExtra(KEY_ACTIVITY_NUM, 0);
                    final TextView calorieString = findViewById(R.id.text_Calorie_input);
                    int calories = Integer.parseInt(removeTag(calorieString.getText().toString()));
                    final TextView commentString = findViewById(R.id.text_comment_input);
                    String comment = commentString.getText().toString();
                    final TextView distanceString = findViewById(R.id.text_distance_input);
                    Double dis = Double.parseDouble(removeTag(distanceString.getText().toString()));
                    final TextView durationString = findViewById(R.id.text_duration_input);
                    Integer dur = Integer.parseInt(removeTag(durationString.getText().toString()));
                    final TextView heartbeatString = findViewById(R.id.text_heartbeat_input);
                    int heartbeat = Integer.parseInt(removeTag(heartbeatString.getText().toString()));
                    if (!disType){
                        dis=KMStoMILES*dis;
                    }
                    new SaveEntryTask().execute(inputID, activityID, cal, dur, dis, 0.0, 0.0, calories, 0.0, heartbeat, comment
                            ,this,null,0,0,getSharedPreferences("myAppPackage", 0).getString(EMAIL_KEY,""));
                } else {// Delete Entry
                    if (entryHis.getOnCloud()==0){
                        new DeleteEntryTask().execute(this, entryHis);
                        toMain();
                    }
                    else if (isInternetAvailable()) {
                        deleteEntry();
                    }
                    else{
                        Toast.makeText(this,"No Internet to Delete",Toast.LENGTH_LONG).show();
                    }
                }
        }
    return super.onOptionsItemSelected(item);
    }
    //deletes from firebase then from sql
    private void deleteEntry(){
        final Activity activity=this;
        Log.d("mauel",entryHis.getOnCloud()+": cloud");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/" + mAuth.getCurrentUser().getUid() +
                "/exercise_entries/" + (entryHis.getmDateTime()).replace("/", ""));

        ref.removeValue().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    new DeleteEntryTask().execute(activity, entryHis); }
                    else {
                    Toast.makeText(activity, "Delete Failed", Toast.LENGTH_LONG).show();
                    }
                }
            });
        toMain();
    }
    //Internet aviallable
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    //Intent to go to main
    private void toMain(){
        Intent main = new Intent(ManuelInputActivity.this, MainActivity.class);
        startActivity(main);
        finish();
    }
    //Sets Strings
    private void setStrings(Bundle strings) {
        final TextView activityString = findViewById(R.id.text_activity_input);
        final TextView calorieString = findViewById(R.id.text_Calorie_input);
        final TextView commentString = findViewById(R.id.text_comment_input);
        final TextView dateString = findViewById(R.id.text_date_input);
        final TextView distanceString = findViewById(R.id.text_distance_input);
        final TextView durationString = findViewById(R.id.text_duration_input);
        final TextView heartbeatString = findViewById(R.id.text_heartbeat_input);
        final TextView timeString = findViewById(R.id.text_time_input);

        activityString.setText(strings.getString(KEY_ACTIVITY));
        calorieString.setText(strings.getString(KEY_CALORIE));
        commentString.setText(strings.getString(KEY_COMMENT));
        dateString.setText(strings.getString(KEY_DATE));
        distanceString.setText(strings.getString(KEY_DISTANCE));
        durationString.setText(strings.getString(KEY_DUR));
        heartbeatString.setText(strings.getString(KEY_HEART));
        timeString.setText(strings.getString(KEY_TIME));

    }

    //Open a Dialog to enter data
    private void openDialog(String title,String pre,int ID,boolean keyboard){
        Bundle bundle = new Bundle();
        bundle.putString(KEY_INPUT, title);
        bundle.putString(KEY_PRE,pre);
        bundle.putInt(KEY_BUTTON,ID);
        bundle.putBoolean(KEY_TYPE,keyboard);
        FragmentManager fm = getFragmentManager();
        NumDialogFragment fragment = new NumDialogFragment();
        fragment.setArguments(bundle);
        fragment.show(fm, "dialog");
    }

    //Set on click listenters
    private void setClicks(){
        final LinearLayout calorie = findViewById(R.id.layout_calorie);
        final LinearLayout comment = findViewById(R.id.layout_comment);
        final LinearLayout date = findViewById(R.id.layout_date);
        final LinearLayout distance = findViewById(R.id.layout_distance);
        final LinearLayout duration = findViewById(R.id.layout_duration);
        final LinearLayout heartbeat = findViewById(R.id.layout_heartbeat);
        final LinearLayout time = findViewById(R.id.layout_time);


        //Sets up Dialog and sends revelant Data
        calorie.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                openDialog("Calories","cals",R.id.text_Calorie_input,false);
            }
        });
        comment.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                openDialog("Comment"," ",R.id.text_comment_input,true);
            }
        });

        //Pick Date
        date.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                new DatePickerDialog(ManuelInputActivity.this, datePicker, cal
                        .get(Calendar.YEAR), cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        if (disType) {
            distance.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    openDialog("Distance", "miles", R.id.text_distance_input, false);
                }
            });
        }
        else{
            distance.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    openDialog("Distance", "kms", R.id.text_distance_input, false);
                }
            });
        }
        duration.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                openDialog("Duration","mins",R.id.text_duration_input,false);
                }
        });
        heartbeat.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                openDialog("Heartbeat","bpm",R.id.text_heartbeat_input,false);
                }
        });
        //Set Time Picker
        time.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                TimePickerDialog partyTimePicker = new TimePickerDialog(ManuelInputActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        final TextView timeView=findViewById(R.id.text_time_input);
                        timeView.setText( getString(R.string.time,hour,minute));
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, minute);
                    }
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                partyTimePicker.setTitle("Select Party Time");
                partyTimePicker.show();
            }
        });

    }
    //Inflate Menu
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_bar_input, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu){
        if (delete) {//change button if from history
            if (getSupportActionBar()!=null) {
                getSupportActionBar().setTitle("Entry");
                menu.findItem(R.id.save_action).setTitle("Delete");
            }
        }
        return true;
    }

    //Save all text Views
    protected void onSaveInstanceState(Bundle outState) {
        final TextView activityView = findViewById(R.id.text_activity_input);
        final TextView calorieString = findViewById(R.id.text_Calorie_input);
        final TextView commentString = findViewById(R.id.text_comment_input);
        final TextView dateString = findViewById(R.id.text_date_input);
        final TextView distanceString = findViewById(R.id.text_distance_input);
        final TextView durationString = findViewById(R.id.text_duration_input);
        final TextView heartbeatString = findViewById(R.id.text_heartbeat_input);
        final TextView timeString = findViewById(R.id.text_time_input);
        outState.putString(KEY_ACTIVITY, activityView.getText().toString());
        outState.putString(KEY_CALORIE, calorieString.getText().toString());
        outState.putString(KEY_COMMENT, commentString.getText().toString());
        outState.putString(KEY_DATE, dateString.getText().toString());
        outState.putString(KEY_DISTANCE, distanceString.getText().toString());
        outState.putString(KEY_DUR, durationString.getText().toString());
        outState.putString(KEY_HEART, heartbeatString.getText().toString());
        outState.putString(KEY_TIME, timeString.getText().toString());
        super.onSaveInstanceState(outState);
    }

    //Gets Date from class
    final DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateView();

        }

    };
    private void updateDateView(){
        TextView dateView = findViewById(R.id.text_date_input);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
        Date date=cal.getTime();
        cal.setTime(date);
        dateView.setText(sdf.format(date));
    }
    //Remove tags from the text like "cals" or "Miles"
    private String removeTag(String string){
        return string.substring(0, string.indexOf(' '));
    }


}

