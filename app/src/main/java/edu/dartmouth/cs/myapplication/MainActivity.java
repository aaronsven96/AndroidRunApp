package edu.dartmouth.cs.myapplication;

// Aaron Svendsen

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.InetAddress;

import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;
import edu.dartmouth.cs.myapplication.Threads.LoadEntriesTask;
import edu.dartmouth.cs.myapplication.fragments.BoardFragment;
import edu.dartmouth.cs.myapplication.fragments.HistoryFragment;
import edu.dartmouth.cs.myapplication.fragments.StartFragment;

public class MainActivity extends AppCompatActivity {
    private static final String START_TAB_KEY = "start_tab";
    private static final String TAG = "main";
    private Fragment current;
    int startFrag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_view);
        //get start tab
        SharedPreferences prefs = getSharedPreferences("myAppPackage", 0);
        startFrag = prefs.getInt(START_TAB_KEY,0);
        //setup toolar
        Toolbar myToolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);
        BottomNavigationView navigation = findViewById(R.id.navigation_bar);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //start current tab
        if (savedInstanceState != null) {
            //Restore the fragment's instance
            current = getFragmentManager().getFragment(savedInstanceState, "myFragmentName");
        }
        else if (startFrag==0){
            current =new StartFragment();
        }
        else if (startFrag==1){
            current=new HistoryFragment();
            navigation.setSelectedItemId(R.id.history_button);
        }
        else{
            current=new BoardFragment();
            navigation.setSelectedItemId(R.id.board_button);
            }
        changeFragment(current);

    }
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getFragmentManager().putFragment(outState, "myFragmentName", current);
    }
    public void changeFragment(Fragment fragment){
        getFragmentManager().beginTransaction().replace(R.id.content_main, fragment).commit();
        }
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_bar_profile, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent leave;
        switch (item.getItemId()) {
            case R.id.edit:
                leave = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(leave);
                break;

            case R.id.setting:
                leave = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(leave);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            final SharedPreferences prefs = getSharedPreferences("myAppPackage", 0);
            SharedPreferences.Editor editor = prefs.edit();
            switch (item.getItemId()) {
                case(R.id.history_button):
                    current=new HistoryFragment();
                    changeFragment(current);
                    editor.putInt(START_TAB_KEY,1);
                    break;
                case (R.id.start_button):
                    current=new StartFragment();
                    changeFragment(current);
                    editor.putInt(START_TAB_KEY,0);
                    break;
                case (R.id.board_button):
                    current=new BoardFragment();
                    changeFragment(current);
                    editor.putInt(START_TAB_KEY,2);
                    break;

            }
            editor.apply();
            return true;
        }
    };
}
