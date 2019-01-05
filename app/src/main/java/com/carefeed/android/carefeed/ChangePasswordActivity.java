package com.carefeed.android.carefeed;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mNewPassword, mConfirmPassword;
    private TextView mValid, mMatch;
    private ProgressBar mProgressBar;

    // --> Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_change_password);

        mToolbar = (Toolbar) findViewById(R.id.main_app_bar);
        mNewPassword = (EditText) findViewById(R.id.change_password_new);
        mConfirmPassword = (EditText) findViewById(R.id.change_password_confirm);
        mValid = (TextView) findViewById(R.id.txt_password_format);
        mMatch = (TextView) findViewById(R.id.txt_password_match);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        // setting toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // --> get Firebase
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        validateInputField();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater infalter = getMenuInflater();
        infalter.inflate(R.menu.done_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.done:
                mProgressBar.setVisibility(View.VISIBLE);
                doneOnClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // check password match
    private boolean checkPasswordMatch(String password, String confirmPassword){
        if(!password.equals(confirmPassword))
            return false;
        else
            return true;
    }

    // check password match with the pattern set
    private boolean passwordPattern(String password){
        Pattern pattern;
        Matcher matcher;
        final String pw_pattern = getString(R.string.password_pattern);
        pattern = Pattern.compile(pw_pattern);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    private boolean checkValid(String newPassword, String confirmPassword){
        if(passwordPattern(newPassword) && checkPasswordMatch(newPassword, confirmPassword))
            return true;
        else
            return false;
    }

    private void doneOnClick(){
        if(mUser != null) {
            if (checkValid(mNewPassword.getText().toString(), mConfirmPassword.getText().toString())) {
                mUser.updatePassword(mNewPassword.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()) {
                               Toast.makeText(ChangePasswordActivity.this, "Change Password Successful", Toast.LENGTH_SHORT).show();
                               mProgressBar.setVisibility(View.GONE);
                               finish();
                           } else {
                               Toast.makeText(ChangePasswordActivity.this, "Change Password Failed", Toast.LENGTH_SHORT).show();
                               mProgressBar.setVisibility(View.GONE);
                           }
                       }
                   });
            } else{
                Toast.makeText(ChangePasswordActivity.this, "Incorrect information provided.", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    private void validateInputField(){
        mNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!passwordPattern(mNewPassword.getText().toString())){

                    mValid.setVisibility(View.VISIBLE);
                    mValid.setText("Invalid password.");
                    mValid.setTextColor(Color.RED);
                } else{
                    mValid.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(checkPasswordMatch(mNewPassword.getText().toString(), mConfirmPassword.getText().toString())){
                    mMatch.setVisibility(View.GONE);
                } else{
                    mMatch.setVisibility(View.VISIBLE);
                }
            }
        });

        mConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(checkPasswordMatch(mNewPassword.getText().toString(), mConfirmPassword.getText().toString())){
                    mMatch.setVisibility(View.GONE);
                } else{
                    mMatch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}
