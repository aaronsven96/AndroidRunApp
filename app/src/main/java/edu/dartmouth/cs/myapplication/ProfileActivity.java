package edu.dartmouth.cs.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

// Aaron Svendsen

public class ProfileActivity extends AppCompatActivity {

    //Indecices for Prefs
    private static final String NAME_INDEX = "index_name";
    private static final String EMAIL_INDEX = "index_email";
    private static final String PASSWORD_INDEX = "index_password";
    private static final String PHONE_INDEX = "index_phone";
    private static final String MAJOR_INDEX = "index_major";
    private static final String CLASS_INDEX = "index_class";
    private static final String GENDER_INDEX = "index_gender";

    //am i singed in
    private static final String SIGNED_IN ="SIGN_IN";

    //On Result
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int IMAGE_SELECT = 1;
    private static final int IMAGE_CAPTURE = 2;
    private static final int IMAGE_CROP = 3;

    private static final int MY_PERMISSIONS_REQUEST_WRITE = 5;

    //Photo keys
    private static final String KEY_INDEX_PHOTO = "photo";
    private static final String PHOTO_INDEX = "index_photo";

    //options in dialog
    private String[] Picture_Option = {"Camera", "Gallery"};

    //image variables
    private String photoUri=null;
    private Uri imageUri;
    boolean singedIn=false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //firebase
        mAuth = FirebaseAuth.getInstance();

        //set up toolbar
        Toolbar myToolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = this.getSharedPreferences("myAppPackage", 0);
        singedIn=prefs.getBoolean(SIGNED_IN,false);
        final EditText email =findViewById(R.id.text_email);
        if (singedIn){
            loadProfile(this);
            email.setEnabled(false);
        }
        //gets image
        if (savedInstanceState != null) {
            final ImageView mProfile = this.findViewById(R.id.image_profile);
            photoUri= savedInstanceState.getString(KEY_INDEX_PHOTO, "");
            mProfile.setImageURI(Uri.parse(photoUri));
        }
        onClickSetup();
    }
    //sets up click listener
    private void onClickSetup(){
        Button mChange = findViewById(R.id.button_picture);
        mChange.setOnClickListener(new View.OnClickListener() {
            //change photo
            @Override
            public void onClick(View view) {
                //see if permissions given
                if (checkpermissions() & checkStorageWrite()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setTitle("Picture Options");
                    builder.setItems(Picture_Option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int option) {
                            if (option == 0) {
                                sendCameraIntent();
                            } else if (option == 1) {
                                sendGalleryIntent();
                            }
                        }
                    });
                    builder.show();
                }
                else{
                    Toast.makeText(ProfileActivity.this,
                            R.string.no_permisssions,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences prefs = this.getSharedPreferences("myAppPackage", 0);
        boolean singedIn=prefs.getBoolean(SIGNED_IN,false);
        //sends you to next activity
        switch (item.getItemId()) {
            case R.id.register_action:
                if (checkRegistration()){
                    //skip login screen
                    if (singedIn){
                        Intent login;
                        saveProfile(this);
                        login =new Intent(ProfileActivity.this, MainActivity.class);
                        startActivity(login);
                        finish();
                    }
                    else {
                        final EditText mEmail = findViewById(R.id.text_email);
                        final EditText mPassword = findViewById(R.id.text_password);
                        makeAccount(mPassword.getText().toString(),mEmail.getText().toString());
                    }
                }

        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_bar_register, menu);
        return true;
    }
    public boolean onPrepareOptionsMenu(Menu menu){
        // change variables if signed in
        if (singedIn) {
            if (getSupportActionBar()!=null) {
                getSupportActionBar().setTitle("Profile");
                menu.findItem(R.id.register_action).setTitle("Save");
            }
        }
        return true;
    }
    //make sure registration is correct
    private boolean  checkRegistration() {

        View focusView = null;

        boolean infoCorrect=true;
        RadioGroup groupView = findViewById(R.id.button_gender);
        EditText passwordView = findViewById(R.id.text_password);
        EditText emailView = findViewById(R.id.text_email);
        EditText nameView = findViewById(R.id.text_name);

        String password = passwordView.getText().toString();
        String email = emailView.getText().toString();
        String name = nameView.getText().toString();

        passwordView.setError(null);
        emailView.setError(null);
        // Is there a password and does it meet the policy
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            infoCorrect=false;
        }
        else if (password.length()<6){
            passwordView.setError(getString(R.string.error_password_length));
            focusView = passwordView;
            infoCorrect=false;
        }
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            infoCorrect=false;
        }
        else if (!isEmailValid(email)){
            emailView.setError(getString(R.string.error_wrong_email));
            focusView = emailView;
            infoCorrect=false;
        }
        if (TextUtils.isEmpty(name)) {
            nameView.setError(getString(R.string.error_field_required));
            focusView = nameView;
            infoCorrect=false;
        }
        if (focusView!=null) {
            focusView.requestFocus();
        }
        if (groupView.getCheckedRadioButtonId()==-1){
            Toast.makeText(this,"Must select Gender",Toast.LENGTH_LONG).show();
        }
        return infoCorrect;
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //sends intent to crops image
    private void sendCropIntent(Uri imageUri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(imageUri, "image/*");
        cropIntent.putExtra("crop", true);
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 512);
        cropIntent.putExtra("outputY", 512);
        // retrieve data on return
        //cropIntent.putExtra("return-data", true);
        //cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
        // start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, IMAGE_CROP);
    }

    //Asks camera to take a pictur
    private  void sendCameraIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE);
        }
    }

    //Asks Gallery for an image
    private void sendGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_SELECT);
    }

    //loads data into fields
    private static void loadProfile(ProfileActivity context) {
        final RadioButton mMale = context.findViewById(R.id.male_button);
        final RadioButton mFemale = context.findViewById(R.id.female_button);
        final EditText mEmail = context.findViewById(R.id.text_email);
        final EditText mName = context.findViewById(R.id.text_name);
        final EditText mPassword = context.findViewById(R.id.text_password);
        final EditText mPhone = context.findViewById(R.id.text_phone);
        final EditText mMajor = context.findViewById(R.id.text_major);
        final EditText mClass = context.findViewById(R.id.text_class);
        final ImageView mImage =context.findViewById(R.id.image_profile);

        SharedPreferences prefs = context.getSharedPreferences("myAppPackage", 0);
        mEmail.setText(prefs.getString(EMAIL_INDEX, ""));
        mName.setText(prefs.getString(NAME_INDEX, ""));
        mPassword.setText(prefs.getString(PASSWORD_INDEX, ""));
        mPhone.setText(prefs.getString(PHONE_INDEX, ""));
        mMajor.setText(prefs.getString(MAJOR_INDEX, ""));
        if (prefs.contains(PHOTO_INDEX)){
            File imgFile = new  File(prefs.getString(PHOTO_INDEX,""));
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                mImage.setImageBitmap(myBitmap);
            }
        }

        if (prefs.contains(CLASS_INDEX)) {
            mClass.setText(String.valueOf(prefs.getInt(CLASS_INDEX, 0)));
        }
        if (prefs.contains(GENDER_INDEX)) {
            int gender = prefs.getInt(GENDER_INDEX, 0);
            if (gender == 1) {
                mMale.toggle();
            } else {
                mFemale.toggle();
            }
        }

    }
    //saves datat in shared preferences
    private void saveProfile(ProfileActivity context) {
        //get fiels and save
        final EditText mEmail = context.findViewById(R.id.text_email);
        final EditText mName = context.findViewById(R.id.text_name);
        final EditText mPassword = context.findViewById(R.id.text_password);
        final EditText mPhone = context.findViewById(R.id.text_phone);
        final EditText mMajor = context.findViewById(R.id.text_major);
        final EditText mClass = context.findViewById(R.id.text_class);
        final RadioButton mMale = context.findViewById(R.id.male_button);
        final RadioButton mFemale = context.findViewById(R.id.female_button);

        String email=mEmail.getText().toString();
        String pass=mPassword.getText().toString();
        SharedPreferences prefs = context.getSharedPreferences("myAppPackage", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(EMAIL_INDEX, email);
        editor.putString(NAME_INDEX, mName.getText().toString());
        editor.putString(PASSWORD_INDEX, pass);
        editor.putString(PHONE_INDEX, mPhone.getText().toString());
        editor.putString(MAJOR_INDEX, mMajor.getText().toString());
        if(photoUri!=null) {
            if(!photoUri.equals("")) {
                String path = getRealPathFromURI(Uri.parse(photoUri));
                editor.putString(PHOTO_INDEX, path);
            }
        }
        if (!mClass.getText().toString().equals("")) {
            editor.putInt(CLASS_INDEX, Integer.parseInt(mClass.getText().toString()));
        }
        if (mMale.isChecked()) {
            editor.putInt(GENDER_INDEX, 1);
        }
        if (mFemale.isChecked()) {
            editor.putInt(GENDER_INDEX, 0);
        }
        editor.apply();
        // Sign up
    }

    private void makeAccount(String pass,String email){
        final ProfileActivity activity =this;
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    saveProfile(activity);
                    Intent signIn= new Intent(ProfileActivity.this, SignInActivity.class);
                    activity.startActivity(signIn);
                    activity.finish();
                } else {
                    if (task.getException() != null) {
                        Toast.makeText(activity,"Email Failed:"+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    //saves image and gets uri
    public Uri getImageUri(ProfileActivity context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==IMAGE_SELECT && RESULT_OK==resultCode){
            try {
                final Uri imageUr = data.getData();

                final InputStream imageStream;
                if (imageUr != null) {
                    imageStream = getContentResolver().openInputStream(imageUr);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imageUri = getImageUri(this, selectedImage);
                    sendCropIntent(imageUri);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == IMAGE_CAPTURE & RESULT_OK == resultCode){
            //final ImageView mProfile = this.findViewById(R.id.image_profile);
            Bundle extras = data.getExtras();
            if (extras!=null & isExternalStorageWritable()) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageUri = getImageUri(this, imageBitmap);
                //UCrop.of(imageUri, outImage).withAspectRatio(1, 1).start(ProfileActivity.this);; //send to crop

                sendCropIntent(imageUri); //send crop
            }
            else{
                Toast.makeText(ProfileActivity.this,"Can't get Image",Toast.LENGTH_LONG).show();

            }
        }
        if (requestCode ==  IMAGE_CROP& resultCode == RESULT_OK) {
            ImageView mProfile = this.findViewById(R.id.image_profile);
            photoUri=imageUri.toString(); //save finished image
            mProfile.setImageURI(imageUri); //set image
        }
    }

    //Android Devs
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    //Saves Image
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_INDEX_PHOTO, photoUri);
        super.onSaveInstanceState(outState);
    }

    //Gets permission to write
    private boolean checkStorageWrite()
    {
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(ProfileActivity.this,
                        R.string.no_permisssion_write,
                        Toast.LENGTH_LONG).show();
                //Ask Permission agina
                ActivityCompat.requestPermissions(ProfileActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE);
                        return false;
            }
            else{
                //asks permission
                ActivityCompat.requestPermissions(ProfileActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE);
                return false;
            }
        }
        return true;
    }
    //gets permission for photos
    private boolean checkpermissions() {
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation to users the reason that we need the permission?
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this,
                    Manifest.permission.CAMERA)) {
                Toast.makeText(ProfileActivity.this,
                        R.string.need_camera,
                        Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(ProfileActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
                        return false;
            }
            else{
                ActivityCompat.requestPermissions(ProfileActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
                return false;
            }
        }

        return true;
    }
    //provided by prof
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = new String[] { android.provider.MediaStore.Images.ImageColumns.DATA };

        Cursor cursor = getContentResolver().query(contentUri, proj, null,
                null, null);
        int column_index;
        String filename=null;
        if (cursor != null) {
            column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            filename = cursor.getString(column_index);
            cursor.close();
        }
        return filename;
    }
}