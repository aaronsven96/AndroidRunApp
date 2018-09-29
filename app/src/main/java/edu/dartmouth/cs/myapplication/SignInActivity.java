package edu.dartmouth.cs.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.signin.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// Aaron Svendsen

public class SignInActivity extends AppCompatActivity {

    //prefs indecises
    private static final String SIGNED_IN ="SIGN_IN";
    private static final String EMAIL_KEY = "email key";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences prefs = getSharedPreferences("myAppPackage", 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //see if logged out
        if (getIntent().getBooleanExtra("key_sign_in", false)) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(SIGNED_IN, false);
                editor.apply();
                mAuth.signOut();//if not signed in then sign out
        }
        //log in if logged in
        if (prefs.getBoolean(SIGNED_IN,false)){
            logIn();
            finish();
        }

        //set up toolbar
        Toolbar myToolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(myToolbar);

        //set up button handlers
        onClick(prefs);

    }
    public void onClick(final SharedPreferences prefs){
        final Button mRegister = findViewById(R.id.register_button);
        final Button mSignIn = findViewById(R.id.sign_in_button);

        mRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent profile = new Intent(SignInActivity.this, ProfileActivity.class);
                startActivity(profile);
            }
        });
        mSignIn.setOnClickListener(new View.OnClickListener(){
            //check password
            public void onClick(View view) {
                EditText passwordView = findViewById(R.id.text_sign_in_password);
                EditText emailView = findViewById(R.id.text_sign_in_name);
                String password = passwordView.getText().toString();
                String email = emailView.getText().toString();

                if (checkpassword(email,password)){
                    checkPassFireBase(email,password);
                }
            }

        });
    }

    //Intent to log in
    private void logIn() {
        Intent main = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(main);
    }

    //checks if info is correct
    private boolean  checkpassword(String email,String password) {

        EditText passwordView = findViewById(R.id.text_sign_in_password);
        EditText emailView = findViewById(R.id.text_sign_in_name);

        View focusView = null;

        boolean infoCorrect=true;

        passwordView.setError(null);
        emailView.setError(null);
        // Is there a password and does it meet the policy
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            infoCorrect=false;
        }
      if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            infoCorrect=false;
        }
        if (focusView!=null) {
            focusView.requestFocus();
        }
        return infoCorrect;
    }
    //checks password in firebase
    public void checkPassFireBase(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            final SharedPreferences prefs = getSharedPreferences("myAppPackage", 0);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(EMAIL_KEY,user.getEmail());
                            editor.putBoolean(SIGNED_IN,true);
                            editor.apply();
                            logIn();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
