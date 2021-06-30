package com.example.ichat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;

public class OtpSignUp extends AppCompatActivity {
    TextView mChangeNumber;
    EditText mGetOtp;
    android.widget.Button mVerifyOtp;
    String enterOtp;
    FirebaseAuth firebaseAuth;
    ProgressBar mProgressBarOtpAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_sign_up);
        mChangeNumber=findViewById(R.id.changenumber);
        mVerifyOtp=findViewById(R.id.verifyotp);
        mGetOtp=findViewById(R.id.entercode);
        mProgressBarOtpAuth=findViewById(R.id.otpsinguppb);
        firebaseAuth=FirebaseAuth.getInstance();


        mChangeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(OtpSignUp.this, MainActivity.class);
                startActivity(intent);
            }
        });
        mVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterOtp=mGetOtp.getText().toString();
                //check
                if (enterOtp.isEmpty()){
                    Toast.makeText(OtpSignUp.this, "Enter Code", Toast.LENGTH_SHORT).show();
                }else{
                    mProgressBarOtpAuth.setVisibility(View.VISIBLE);
                    String coderecived = getIntent().getStringExtra("otp");// fetchiing key from main activity
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(coderecived,enterOtp);
                    signInWithPhoneAuthCredential(credential);
            }}
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    mProgressBarOtpAuth.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Login Sucessfull", Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(OtpSignUp.this,SetProfile.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        mProgressBarOtpAuth.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

    }
}