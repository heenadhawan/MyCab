package com.android.mycab;

import android.app.ProgressDialog;
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

public class CustomerLoginRegister_activity extends AppCompatActivity {
    private TextView CreateCustomerAccount;
    private TextView TitleCustomer;
    private Button LoginCustomerButton;
    private Button RegisterCustomerButton;
    private EditText CustomerEmail;
    private EditText CustomerPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_customer_login_register_activity);
        CreateCustomerAccount = (TextView) findViewById(R.id.customer_register_link);
        TitleCustomer = (TextView) findViewById(R.id.customer_status);
        LoginCustomerButton = (Button) findViewById(R.id.customer_login_btn);
        RegisterCustomerButton = (Button) findViewById(R.id.customer_register_btn);
        CustomerEmail = (EditText) findViewById(R.id.customer_email);
        CustomerPassword = (EditText) findViewById(R.id.customer_password);
        loadingBar = new ProgressDialog(this);

        RegisterCustomerButton.setVisibility(View.INVISIBLE);
        RegisterCustomerButton.setEnabled(false);

        CreateCustomerAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CreateCustomerAccount.setVisibility(View.INVISIBLE);
                LoginCustomerButton.setVisibility(View.INVISIBLE);
                TitleCustomer.setText("Driver Registration");

                RegisterCustomerButton.setVisibility(View.VISIBLE);
                RegisterCustomerButton.setEnabled(true);
            }
        });
        RegisterCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = CustomerEmail.getText().toString();
                String password = CustomerPassword.getText().toString();
                RegisterCustomer(email, password);
            }
        });
        LoginCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = CustomerEmail.getText().toString();
                String password = CustomerPassword.getText().toString();
                LoginCustomer(email, password);
            }
        });
    }

    private void LoginCustomer(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(CustomerLoginRegister_activity.this, "Please write your Email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(CustomerLoginRegister_activity.this, "Please write your Password...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Customer Login :");
            loadingBar.setMessage("Please checking all the credentials...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CustomerLoginRegister_activity.this, "Driver Login Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(CustomerLoginRegister_activity.this, "Driver Login UnSuccessfull....", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }
    }
    private void RegisterCustomer(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(CustomerLoginRegister_activity.this, "Please write your Email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(CustomerLoginRegister_activity.this, "Please write your Password...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Please wait :");
            loadingBar.setMessage("While system is performing processing on your data...");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(CustomerLoginRegister_activity.this, "Driver Register Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                            else{
                                Toast.makeText(CustomerLoginRegister_activity.this, "Driver Register UnSuccessfull....", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });
        }
    }

    }

