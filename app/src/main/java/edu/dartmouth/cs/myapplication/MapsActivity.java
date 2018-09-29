package edu.dartmouth.cs.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import edu.dartmouth.cs.myapplication.Adapters.MyRecyclerAdapter;
import edu.dartmouth.cs.myapplication.Models.MyLatLng;
import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;
import edu.dartmouth.cs.myapplication.Services.ActivityService;
import edu.dartmouth.cs.myapplication.Services.DetectedActivityIntentService;
import edu.dartmouth.cs.myapplication.Services.LocationService;
import edu.dartmouth.cs.myapplication.Threads.DeleteEntryTask;
import edu.dartmouth.cs.myapplication.Threads.SaveEntryTask;
import edu.dartmouth.cs.myapplication.fragments.StartFragment;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Debug Tag
    private static final String TAG="maps";

    //Saved Instance Keys
    private static final String FIRST_KEY = "first";
    private static final String START_ALT_KEY = "start alt";
    private static final String END_ALT_KEY = "end alt";
    private static final String DIS_KEY = "dis sve";
    private static final String CAL_START_KEY = "cal start";
    private static final String CAL_PREV_KEY = "cal prev";
    private static final String AVG_SPEED_KEY = "avg speed";
    private static final String CALORIES_KEY = "cal key";
    private static final String LOCATIONS_KEY = "loc key";
    private static final String EMAIL_KEY = "email key";

    private GoogleMap mMap;

    //Units
    private static final double KMStoMILES=0.621371;
    private final static String UNIT_INDEX = "UNIT_INDEX";
    private double METERStoFEET=3.28084;

    //line to draw
    Polyline line;

    //main intent
    private Intent intent;

    //What are we using this for
    private boolean isMetric;
    private boolean isGPS;
    private boolean his;

    private LocalBroadcastManager manager;

    //thins to keep track of as map updates
    private boolean skipFirst=true;
    private boolean first=true;
    private double startAlt=0;
    private double endAlt=0;
    private double dis=0;
    private Calendar calStart;
    private Calendar calPrev;
    private double avgSpeed=0;
    private int calories=0;
    private Marker end;
    private ArrayList<LatLng> locations;
    private int[] activityLevel=new int[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Setup Toolbar
        Toolbar myToolbar =  findViewById(R.id.toolbar_map);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //get intent
        intent = getIntent();

        //is this history
        his=intent.getBooleanExtra(MyRecyclerAdapter.KEY_HIS,false);

        //if we are getting map stuff setup map variables
        if (!his) {
            TextView activityView =findViewById(R.id.display_activity);
            activityView.setText(String.format(Locale.US, "Activity: %s", intent.getStringExtra(StartFragment.KEY_SPINNER)));
            locations = new ArrayList<>();
            manager= LocalBroadcastManager.getInstance(this);
        }

        //on phone flip
        if (savedInstanceState!=null&&!his){
            first = savedInstanceState.getBoolean(FIRST_KEY,false);
            startAlt=savedInstanceState.getDouble(START_ALT_KEY,0);
            endAlt =savedInstanceState.getDouble(END_ALT_KEY,0);
            dis=savedInstanceState.getDouble(DIS_KEY,0);
            calStart=(Calendar) savedInstanceState.getSerializable(CAL_START_KEY);
            calPrev=(Calendar) savedInstanceState.getSerializable(CAL_PREV_KEY);
            avgSpeed=savedInstanceState.getDouble(AVG_SPEED_KEY,0);
            savedInstanceState.getInt(CALORIES_KEY,0);
            Parcelable[] arrayP=savedInstanceState.getParcelableArray(LOCATIONS_KEY);
            if (arrayP!=null) {
                for (Parcelable p : arrayP) {
                    locations.add((LatLng) p);
                }
            }
        }

        //get if we are using gps only
        isGPS=intent.getBooleanExtra(StartFragment.KEY_IS_GPS,false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Type of units
        isMetric=prefs.getString(UNIT_INDEX," ").equals("Metric");
    }
    @Override
    protected void onDestroy() {
        Log.d(TAG,"Destroyed");//stop services
        stopService(new Intent(this, ActivityService.class));
        stopService(new Intent(this, LocationService.class));
        super.onDestroy();
    }

    //save or delete the activity with the button
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_map_action:
                if (!his&calStart!=null) {
                    int max=0;
                    int best=activityLevel[1];

                    //get most used activity
                    for(int i=1;i<activityLevel.length;i++){
                        if (best<activityLevel[i]){
                            max=i;
                            best=activityLevel[i];
                        }
                    }
                    int activity;
                    if (isGPS){
                        activity=intent.getIntExtra(StartFragment.KEY_ACTIVITY_NUM, 0);
                    }
                    else{
                        activity=getActivityNum(max);
                    }
                    double dAlt=(endAlt-startAlt);
                    //save the data
                    ArrayList<MyLatLng> locs = new ArrayList<>();
                    for (LatLng loc:locations){
                        locs.add(new MyLatLng(loc.latitude,loc.longitude));
                    }
                    new SaveEntryTask().execute(intent.getIntExtra(StartFragment.KEY_INPUT_NUM, 0),activity,
                            calPrev,(int)(getTimeBetween(calStart, calPrev)*60), (dis/1000)*KMStoMILES, getPace(avgSpeed), avgSpeed, calories,
                            dAlt, 0, " ",this,locs,0,0,getSharedPreferences("myAppPackage", 0).getString(EMAIL_KEY,""));
                    }
                    //if in history delete
                else if (his){
                    NewExerciseEntry entryHis=(NewExerciseEntry) getIntent().getSerializableExtra(MyRecyclerAdapter.KEY_ENTRY);
                    if (entryHis.getOnCloud()==0){
                        new DeleteEntryTask().execute(this, entryHis);
                        toMain();
                    }
                    else if (isInternetAvailable()) {
                        deleteEntry(entryHis);
                    }
                    else{
                        Toast.makeText(this,"No Internet to Delete",Toast.LENGTH_LONG).show();
                    }

                    deleteEntry(entryHis);

                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //Internet aviallable
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    //deletes entry from firebase
    private void deleteEntry(final NewExerciseEntry entryHis){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/" + mAuth.getCurrentUser().getUid() +
                "/exercise_entries/" + (entryHis.getmDateTime()).replace("/", ""));
        final Activity activity=this;
        ref.removeValue().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){//if sucess then delete from sql
                    new DeleteEntryTask().execute(activity, entryHis);
                }
                else{
                    Toast.makeText(activity,"Delete Failed",Toast.LENGTH_LONG).show();
                }
            }
        });
        toMain();
    }
    //goes to main
    private void toMain() {
        Intent main = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(main);
        finish();
    }

    //change name of button if coming from history
    public boolean onPrepareOptionsMenu(Menu menu){
        if (his) {//change button if from history
            if (getSupportActionBar()!=null) {
                getSupportActionBar().setTitle("Run");
                menu.findItem(R.id.save_map_action).setTitle("Delete");
            }
        }
        return true;
    }
    //save all the variables
    protected void onSaveInstanceState(Bundle outState) {
        if (!his) {
            outState.putBoolean(FIRST_KEY, first);
            outState.putDouble(START_ALT_KEY, startAlt);
            outState.putDouble(END_ALT_KEY, endAlt);
            outState.putDouble(DIS_KEY, dis);
            outState.putSerializable(CAL_START_KEY, calStart);
            outState.putSerializable(CAL_PREV_KEY, calPrev);
            outState.putDouble(AVG_SPEED_KEY, avgSpeed);
            outState.putInt(CALORIES_KEY, calories);
            Parcelable[] arrayP = new Parcelable[locations.size()];
            for (int i = 0; i < locations.size(); i++) {
                arrayP[i] = locations.get(i);
            }
            outState.putParcelableArray(LOCATIONS_KEY, arrayP);
        }
        super.onSaveInstanceState(outState);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_bar_map, menu);
        return true;
    }

    //update all the texts with a new location
    private void updateWithNewLocation(Location location) {

        //Make Latlng
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng loc = new LatLng(latitude,longitude);

        //add
        locations.add(loc);

        TextView climbView = findViewById(R.id.display_climb);
        TextView calorieView = findViewById(R.id.display_calorie);
        TextView disView =findViewById(R.id.display_distance);
        TextView AvgView = findViewById(R.id.display_avg_speed);
        TextView speedView =findViewById(R.id.display_speed);

        //fisrt location set up map
        if (first) {
             first = false;
             startAlt = location.getAltitude();
             endAlt = startAlt;
             calStart = Calendar.getInstance();
             calStart.setTime(calStart.getTime());
             calPrev=calStart;
             mMap.addMarker(new MarkerOptions().position(loc).title("Start"));
             end = mMap.addMarker(new MarkerOptions().position(loc).title("end"));
        }
        //on all other locations update
        else{
            Calendar currentTime= Calendar.getInstance();
            currentTime.setTime(currentTime.getTime());
            Location prev = new Location("");

            double timeFromStart=getTimeBetween(calStart,currentTime);
            double timeFromLast=getTimeBetween(calPrev,currentTime);

            prev.setLatitude(locations.get(locations.size()-2).latitude);
            prev.setLongitude(locations.get(locations.size()-2).longitude);

            endAlt =location.getAltitude();

            double travel =prev.distanceTo(location);

            dis=dis+travel;

            double curSpeed;
            if(isMetric) {
                curSpeed = (travel) /timeFromLast;
                avgSpeed = (dis) /timeFromStart;
                AvgView.setText(String.format(Locale.US,"Avg Speed: %1$,.2f m/s",avgSpeed/(60*60)));
                speedView.setText(String.format(Locale.US,"Speed: %1$,.2f m/s", curSpeed /(60*60)));
                disView.setText(String.format(Locale.US, "Distance %1$,.2f m ",dis));
                climbView.setText(String.format(Locale.US,"Climb:%1$,.2f meters",endAlt-startAlt));
            }
            else{
                curSpeed =((travel/1000) /timeFromLast)*KMStoMILES;
                avgSpeed = ((dis/1000) /timeFromStart)*KMStoMILES;
                AvgView.setText(String.format(Locale.US,"Avg Speed: %1$,.2f mph",avgSpeed));
                speedView.setText(String.format(Locale.US,"Speed: %1$,.2f mph", curSpeed));
                disView.setText(String.format(Locale.US, "Distance: %1$,.2f miles ",(dis/1000)*KMStoMILES));
                climbView.setText(String.format(Locale.US,"Climb:%1$,.2f feet",(endAlt-startAlt)*METERStoFEET));

            }
            //basic Calorie fomrula that assumes 100 cal per mile
            calories=(int)((dis/10)*KMStoMILES);
            calorieView.setText(String.format(Locale.US,"Calories:%s",calories));
            calPrev=currentTime;
            if (end!=null) {
                end.remove();
            }
            end = mMap.addMarker(new MarkerOptions().position(loc).title("End"));
        }
        //if phone was flipped then make new line
        if (line==null) {
            PolylineOptions lineOpt= new
                    PolylineOptions()
                    .width(5)
                    .startCap(new RoundCap())
                    .endCap(new RoundCap())
                    .color(Color.RED);
            line = mMap.addPolyline(lineOpt);
            mMap.addMarker(new MarkerOptions().position(locations.get(0)).title("Start"));
        }
        //set line locations
        if (locations != null) {
            line.setPoints(locations);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        //if looking at history
        if (his){
            ArrayList<LatLng> array= new ArrayList<>();
            for (Parcelable p : intent.getParcelableArrayExtra("hi")){
                array.add((LatLng)p);
            }
            setData(array,intent.getSerializableExtra(MyRecyclerAdapter.KEY_ENTRY));
        }
        //else set up services and broadcasters
        else {
            if (!isGPS) {
                Intent intent = new Intent(this, ActivityService.class);
                Log.d(TAG, "start activity service");
                startService(intent);
            }

            IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
            locBroadcastReceiver br = new locBroadcastReceiver();
            manager.registerReceiver(br, filter);
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
        }
        // Add a marker in Sydney and move the camera
    }

    //sets all the data on history
    private void setData(ArrayList<LatLng> array, Serializable serializableExtra) {
        NewExerciseEntry entry = (NewExerciseEntry) serializableExtra;
        double sAvgSpeed=entry.getmAvgSpeed();
        double sAlt=entry.getmClimb();
        double sDis =entry.getmDistance();

        TextView climbView = findViewById(R.id.display_climb);
        TextView calorieView = findViewById(R.id.display_calorie);
        TextView disView =findViewById(R.id.display_distance);
        TextView AvgView = findViewById(R.id.display_avg_speed);

        TextView activityView =findViewById(R.id.display_activity);
        activityView.setText(String.format(Locale.US, "Activity: %s", this.getResources()
                .getStringArray(R.array.activity_array)[entry.getmActivityType()]));

        if (!isMetric){
            sAvgSpeed=sAvgSpeed*KMStoMILES;
            sAlt=sAlt*METERStoFEET;
            sDis=(sDis/1000)*KMStoMILES;
            climbView.setText(String.format(Locale.US,"Climb:%1$,.2f feet",sAlt));
            disView.setText(String.format(Locale.US, "Distance %1$,.2f miles ",sDis));
            AvgView.setText(String.format(Locale.US,"Avg Speed: %1$,.2f mph",sAvgSpeed));
        }
        else {
            disView.setText(String.format(Locale.US, "Distance %1$,.2f ms ",sDis));
            AvgView.setText(String.format(Locale.US, "Avg Speed: %1$,.2f m/s", sAvgSpeed/(60*60)));
            climbView.setText(String.format(Locale.US, "Climb:%1$,.2f meters", sAlt));
        }
        calorieView.setText(String.format(Locale.US, "%d Calories", entry.getmCalorie()));

        PolylineOptions lineOpt= new
                PolylineOptions()
                .width(5)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
                .color(Color.RED);
        line = mMap.addPolyline(lineOpt);
        line.setPoints(array);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(array.get(array.size()-1)));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18));
        mMap.addMarker(new MarkerOptions().position(array.get(0)).title("Start"));
        mMap.addMarker(new MarkerOptions().position(array.get(array.size()-1)).title("End"));
    }

    //Time betweem two calenders in hours
    private double getTimeBetween(Calendar prev,Calendar cur){
        double prevHour = (double) prev.get(Calendar.HOUR)+(prev.get(Calendar.MINUTE)/60.0)+
                prev.get(Calendar.SECOND)/(60*60.0)+prev.get(Calendar.MILLISECOND)/(60*60.0*1000);
        double curHour = (double) cur.get(Calendar.HOUR)+(cur.get(Calendar.MINUTE)/60.0)+
                cur.get(Calendar.SECOND)/(60*60.0)+cur.get(Calendar.MILLISECOND)/(60*60.0*1000);
        return curHour-prevHour;
    }
    //get the pace
    private double getPace(double pace){
        return (1/(pace*KMStoMILES))*60;
    }
    //recieves location and activity updates
    public class locBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int broadcastNUm=intent.getIntExtra(LocationService.ACTION_KEY,0);
            if (broadcastNUm==LocationService.NEW_LOCATION) {
                Location loc = Objects.requireNonNull(intent.getExtras()).getParcelable(LocationService.LOC_KEY);
                if (loc!=null&&!skipFirst) {
                    updateWithNewLocation(loc);
                }
                skipFirst=false;
            }
            if (broadcastNUm== DetectedActivityIntentService.NEW_ACTIVITY){
                int type =intent.getIntExtra("type",0);
                TextView activity = findViewById(R.id.display_activity);
                activity.setText(handleUserActivity(type));
            }
        }
    }

    //adapted from code given by prof returns a label
    private String handleUserActivity(int type) {
        String label = "Unknown";
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = "In_Vehicle";
                activityLevel[DetectedActivity.IN_VEHICLE]=activityLevel[DetectedActivity.IN_VEHICLE]+1;
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = "On_Bicycle";
                activityLevel[DetectedActivity.ON_BICYCLE]=activityLevel[DetectedActivity.ON_BICYCLE]+1;
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = "On_Foot";
                activityLevel[DetectedActivity.ON_FOOT]=activityLevel[DetectedActivity.ON_FOOT]+1;
                break;
            }
            case DetectedActivity.RUNNING: {
                label = "Running";
                activityLevel[DetectedActivity.RUNNING]=activityLevel[DetectedActivity.RUNNING]+1;
                break;
            }
            case DetectedActivity.STILL: {
                label = "Still";
                activityLevel[DetectedActivity.STILL]=activityLevel[DetectedActivity.STILL]+1;
                break;
            }
            case DetectedActivity.TILTING: {
                label = "Tilting";
                activityLevel[DetectedActivity.TILTING]=activityLevel[DetectedActivity.TILTING]+1;
                break;
            }
            case DetectedActivity.WALKING: {
                label = "Walking";
                activityLevel[DetectedActivity.WALKING]=activityLevel[DetectedActivity.WALKING]+1;
                break;
            }
            case DetectedActivity.UNKNOWN: {
                activityLevel[DetectedActivity.UNKNOWN]=activityLevel[DetectedActivity.UNKNOWN]+1;
                break;
            }
        }

        Log.d(TAG, "broadcast:onReceive(): Activity is " + label);

        return label;
    }

    //gets activity num from detected activity num
    private int getActivityNum(int i){
        int activity=0;
        if (i==0){
            activity=1;
        }
        if (i==1){
            activity=3;
        }
        if (i==2){
            activity=1;
        }
        if (i==3){
            activity=2;
        }
        if (i==4){
            activity=12;
        }
        if (i==5){
            activity=1;
        }
        if (i==6){
            activity=1;
        }
        if (i==7){
            activity=1;
        }
        if (i==8){
            activity=0;
        }
        Log.d(TAG,"save activity:"+activity);
        return activity;
    }


}