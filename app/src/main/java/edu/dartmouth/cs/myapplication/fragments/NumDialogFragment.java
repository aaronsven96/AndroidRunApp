package edu.dartmouth.cs.myapplication.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import edu.dartmouth.cs.myapplication.R;

//Aaron Svendsen

public class NumDialogFragment extends DialogFragment {

    private final static String KEY_INPUT = "input";
    private static final String KEY_BUTTON = "button";
    private static final String KEY_PRE = "pre";
    private final static String KEY_TYPE = "type";

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        super.onCreate(savedInstanceState);

        //Builds Alert Dialaog Box
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //takes root because it is a fragment so supressed
        @SuppressLint("InflateParams") final View content =  inflater.inflate(R.layout.dialog_num, null);
        builder.setView(content);
        final Bundle bundle = this.getArguments();
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final TextView inputView = getActivity().findViewById(bundle.getInt(KEY_BUTTON));
                        final EditText editView = content.findViewById(R.id.dialog_edit);
                        String num = editView.getText().toString();
                        if (num.equals("")){
                            num="0";
                        }
                        inputView.setText(String.format("%s %s",num , bundle.getString(KEY_PRE)));
                    }
                });

        //changes texts from info from bundle
        if (bundle != null) {
            boolean input_type=bundle.getBoolean(KEY_TYPE,false);
            final EditText editView = content.findViewById(R.id.dialog_edit);
            //changes keyboard type
            if (input_type){
                editView.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            String text = bundle.getString(KEY_INPUT,"");
            final TextView name =content.findViewById(R.id.dialog_name);
            name.setText(text);
        }
        // Create the AlertDialog object and return it
        return builder.create();
    }
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {

    }
}
