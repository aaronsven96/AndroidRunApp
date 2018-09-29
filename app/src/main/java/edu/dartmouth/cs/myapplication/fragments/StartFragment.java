package edu.dartmouth.cs.myapplication.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.Objects;
import edu.dartmouth.cs.myapplication.ManuelInputActivity;
import edu.dartmouth.cs.myapplication.MapsActivity;
import edu.dartmouth.cs.myapplication.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class StartFragment extends Fragment {

    public static final String KEY_IS_GPS ="gps";
    public static final String KEY_SPINNER = "spinner";
    public static final String KEY_ACTIVITY_NUM = "spinner num";
    public static final String KEY_INPUT_NUM = "spinner input";

    public StartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false);

    }
    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        final Spinner spinner = Objects.requireNonNull(getView()).findViewById(R.id.spinner_activity);
        final Spinner spinner_type = getView().findViewById(R.id.spinner_input);

        setHasOptionsMenu(true);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.activity_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(),
                R.array.type_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_type.setAdapter(adapter2);
        final FloatingActionButton mStartButton=getView().findViewById(R.id.button_input);
        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner_type.getSelectedItem().toString().equals("Automatic")){
                    spinner.setEnabled(false);
                }
                else {
                    spinner.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mStartButton.setOnClickListener(new View.OnClickListener(){
            //check password
            public void onClick(View view) {
                switch (spinner_type.getSelectedItem().toString()) {
                    case "Automatic": {
                        if (checkPermissions()) {
                            Intent profile = new Intent(getView().getContext(), MapsActivity.class);
                            profile.putExtra(KEY_IS_GPS,false);
                            profile.putExtra(KEY_SPINNER, spinner.getSelectedItem().toString());
                            profile.putExtra(KEY_ACTIVITY_NUM,spinner.getSelectedItemPosition());
                            profile.putExtra(KEY_INPUT_NUM,spinner_type.getSelectedItemPosition());
                            startActivity(profile);
                        }
                        break;
                    }
                    case "GPS": {
                        if (checkPermissions()) {
                            Intent profile = new Intent(getView().getContext(), MapsActivity.class);
                            profile.putExtra(KEY_IS_GPS,true);
                            profile.putExtra(KEY_SPINNER, spinner.getSelectedItem().toString());
                            profile.putExtra(KEY_ACTIVITY_NUM,spinner.getSelectedItemPosition());
                            profile.putExtra(KEY_INPUT_NUM,spinner_type.getSelectedItemPosition());
                            startActivity(profile);
                        }
                        break;
                    }
                    case "Manuel": {
                        Intent profile = new Intent(getView().getContext(), ManuelInputActivity.class);
                        profile.putExtra(KEY_SPINNER, spinner.getSelectedItem().toString());
                        profile.putExtra(KEY_ACTIVITY_NUM,spinner.getSelectedItemPosition());
                        profile.putExtra(KEY_INPUT_NUM,spinner_type.getSelectedItemPosition());
                        startActivity(profile);
                        break;
                    }
                }
            }
        });
    }
    public void onPrepareOptionsMenu(Menu menu){
            menu.findItem(R.id.sync).setVisible(false);
    }

    private boolean checkPermissions(){
        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)  {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return false;
        }
        else {
            return true;
        }
    }
}
