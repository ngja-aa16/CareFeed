package com.carefeed.android.carefeed;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView mChangePassword, mLogout;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);

        mToolbar = (Toolbar) findViewById(R.id.setting_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mChangePassword = (TextView) findViewById(R.id.txt_change_password);
        mLogout = (TextView) findViewById(R.id.txt_logout);

        mAuth = FirebaseAuth.getInstance();

        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder confirmationDialogBuilder = new AlertDialog.Builder(SettingActivity.this, R.style.AlertDialog);

                confirmationDialogBuilder.setTitle("Logout");
                confirmationDialogBuilder.setMessage("Press logout to lougout");
                confirmationDialogBuilder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        finish();
                        Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
                confirmationDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                confirmationDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);

                int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);

                Dialog confirmationDialog = confirmationDialogBuilder.create();
                confirmationDialog.show();
                confirmationDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });
    }
}
