package edu.dartmouth.cs.myapplication.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.dartmouth.cs.myapplication.ManuelInputActivity;
import edu.dartmouth.cs.myapplication.MapsActivity;
import edu.dartmouth.cs.myapplication.Models.MyLatLng;
import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;
import edu.dartmouth.cs.myapplication.R;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    //Conversion factor
    private static final Double MILEStoKMS=1.60934;

    //keys to send data for onclick listener
    public static final String KEY_HIS = "his";
    public static final String KEY_ENTRY = "entry";

    //key to get unit preference
    private static final String UNIT_INDEX = "UNIT_INDEX";

    //DataSet
    private List<NewExerciseEntry> mDataset;

    //View to format each entry
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout mLayout;
        ViewHolder(LinearLayout v) {
            super(v);
            mLayout = v;
        }
    }

    //Sets DataSet
    public MyRecyclerAdapter(List<NewExerciseEntry> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MyRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                           int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_entry, parent, false);
        return new ViewHolder(v);
    }

    //Set up each item in Recycle View
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        //make position final
        final int pos=position;

        //get entry
        NewExerciseEntry entry =mDataset.get(pos);
        //get context
        final Context context =holder.mLayout.getContext();

        //get prefs for Unit type
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean disType=prefs.getString(UNIT_INDEX," ").equals("Imperial");

        //Get all the Views that I want to set
        LinearLayout horiztonalLayout = (LinearLayout) (holder.mLayout).getChildAt(0);
        LinearLayout horiztonalLayout2 = (LinearLayout) (holder.mLayout).getChildAt(1);
        TextView type = (TextView)(horiztonalLayout).getChildAt(0);
        TextView stats = (TextView)(horiztonalLayout).getChildAt(1);
        TextView time = (TextView)(horiztonalLayout2).getChildAt(0);
        String typeText=context.getResources().getStringArray(R.array.activity_array)[mDataset.get(position).getmActivityType()];
        String inputText=context.getResources().getStringArray(R.array.type_array)[mDataset.get(position).getmInputType()];

        //Set All the text
        type.setText(String.format("%s:%s", typeText, inputText));
        if (disType) {//check for unit type
            stats.setText(String.format(Locale.US, "%.2f miles, %d mins", entry.getmDistance(), entry.getmDuration()));
        }
        else{
            stats.setText(String.format(Locale.US, "%.2f kms, %d mins", entry.getmDistance()*MILEStoKMS, entry.getmDuration()));
        }
        //format Date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yy HH:mm", Locale.US);
        try {
            time.setText(sdf.format(stringToDate(entry.getmDateTime()).getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Set click listener
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent main;
                NewExerciseEntry entry=mDataset.get(pos);
                ArrayList<MyLatLng> array =entry.getmLocationList();
                entry.setmLocationList(null);
                if (array!=null){
                    main =new Intent(context, MapsActivity.class);
                    main.putExtra(KEY_ENTRY, entry);//what entry
                    Parcelable[] arrayP=new Parcelable[array.size()];
                    for (int i=0;i<array.size();i++){
                        MyLatLng loc =array.get(i);
                        arrayP[i]=new LatLng(loc.latitude,loc.longitude);
                    }
                    main.putExtra("hi", arrayP);
                    main.putExtra(KEY_HIS, true);//tells manuel input that we are coming from history
                }
                else {
                    main = new Intent(context, ManuelInputActivity.class);
                    main.putExtra(KEY_ENTRY, entry);//what entry
                    main.putExtra(KEY_HIS, true);//tells manuel input that we are coming from history
                }
                holder.mLayout.getContext().startActivity(main);
            }
        });
    }

    // Return the size of your dataset
    @Override
    public int getItemCount() {
        if (mDataset==null) {
            return 0;
        }
        return mDataset.size();
    }

    //Change a String back into a Calender Object
    private Calendar stringToDate(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm");
        Date date = sdf.parse(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
