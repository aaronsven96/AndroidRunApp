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
import edu.dartmouth.cs.myapplication.Models.BoardEntry;
import edu.dartmouth.cs.myapplication.Models.NewExerciseEntry;
import edu.dartmouth.cs.myapplication.R;

public class BoardRecylerAdapter extends RecyclerView.Adapter<BoardRecylerAdapter.ViewHolder> {

    private List<BoardEntry> mDataset;

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
    public BoardRecylerAdapter(List<BoardEntry> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public BoardRecylerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                           int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.board_entry, parent, false);
        return new ViewHolder(v);
    }

    //Set up each item in Recycle View
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        //make position final
        final int pos=position;

        //get entry
        BoardEntry entry =mDataset.get(pos);
        //get context
        final Context context =holder.mLayout.getContext();

        //Get all the Views that I want to set
        LinearLayout horiztonalLayout = (LinearLayout) (holder.mLayout).getChildAt(0);
        LinearLayout verticalLayout = (LinearLayout) (horiztonalLayout).getChildAt(0);
        LinearLayout horiztonalLayout2 = (LinearLayout) (horiztonalLayout).getChildAt(1);
        TextView type = (TextView)(verticalLayout).getChildAt(0);
        TextView stats = (TextView)(verticalLayout).getChildAt(1);
        TextView email = (TextView) holder.mLayout.getChildAt(1);
        TextView time = (TextView)(horiztonalLayout2).getChildAt(0);
        type.setText(String.format(Locale.US,"%s: %s",entry.getInput_type(),entry.getActivity_type()));
        stats.setText(String.format(Locale.US,"%s kms, %s mins",entry.getDistance(),entry.getDuration()));
        email.setText(String.format(Locale.US,"%s",entry.getEmail()));
        time.setText(String.format(Locale.US,"%s",entry.getActivity_date()));
    }
    // Return the size of your dataset
    @Override
    public int getItemCount() {
        if (mDataset==null) {
            return 0;
        }
        return mDataset.size();
    }
}
