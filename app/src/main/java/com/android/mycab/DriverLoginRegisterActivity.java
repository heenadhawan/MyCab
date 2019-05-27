package com.android.mycab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginRegisterActivity extends AppCompatActivity {
    private TextView CreateDriverAccount;
    private TextView TitleDriver;
    private Button LoginDriverButton;
    private Button RegisterDriverButton;
    private EditText DriverEmail;
    private EditText DriverPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference DriverDatabaseRef;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;
    private FirebaseUser currentUser;
    private String onlineDriverid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_register);
        mAuth = FirebaseAuth.getInstance();
        CreateDriverAccount = (TextView) findViewById(R.id.create_driver_account);
        TitleDriver = (TextView) findViewById(R.id.titlr_driver);
        LoginDriverButton = (Button) findViewById(R.id.login_driver_btn);
        RegisterDriverButton = (Button) findViewById(R.id.register_driver_btn);
        DriverEmail = (EditText) findViewById(R.id.driver_email);
        DriverPassword = (EditText) findViewById(R.id.driver_password);
        loadingBar = new ProgressDialog(this);
        RegisterDriverButton.setVisibility(View.INVISIBLE);
        RegisterDriverButton.setEnabled(false);

        CreateDriverAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateDriverAccount.setVisibility(View.INVISIBLE);
                LoginDriverButton.setVisibility(View.INVISIBLE);
                TitleDriver.setText("Driver Registration");

                RegisterDriverButton.setVisibility(View.VISIBLE);
                RegisterDriverButton.setEnabled(true);
            }
        });
        RegisterDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = DriverEmail.getText().toString();
                String password = DriverPassword.getText().toString();
                RegisterDriver(email, password);
            }
        });
        LoginDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = DriverEmail.getText().toString();
                String password = DriverPassword.getText().toString();
                LoginDriver(email, password);
            }
        });
    }

    private void LoginDriver(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please write your Email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please write your Password...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Driver Login :");
            loadingBar.setMessage("Please checking all the credentials...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                Intent driverintent = new Intent(DriverLoginRegisterActivity.this, DriverMapActivity.class);
                                startActivity(driverintent);
                                Toast.makeText(DriverLoginRegisterActivity.this, "Driver Login Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            } else {
                                Toast.makeText(DriverLoginRegisterActivity.this, "Driver Login UnSuccessfull....", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }
    }
    private void RegisterDriver(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please write your Email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(DriverLoginRegisterActivity.this, "Please write your Password...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Please wait :");
            loadingBar.setMessage("Please wait we are registering  your data...");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                onlineDriverid = mAuth.getCurrentUser().getUid();
                                DriverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(onlineDriverid);
                                DriverDatabaseRef.setValue(true);

                                Intent intent = new Intent(DriverLoginRegisterActivity.this, DriverMapActivity.class);
                                startActivity(intent);

                                loadingBar.dismiss();

                            } else {
                                Toast.makeText(DriverLoginRegisterActivity.this, "Driver Register UnSuccessfull....", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss(); }
                        }
                    });
        }

    }
}
