package com.example.ichat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    EditText mGetPhoneNumber;
    android.widget.Button mSendOtp;
    CountryCodePicker mCountryCodePicker;
    String countyCode;
    String phoneNumber;
    FirebaseAuth firebaseAuth;
    ProgressBar mProgressBarOfMain;

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String codeSent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCountryCodePicker=findViewById(R.id.countrycode);
        mSendOtp=findViewById(R.id.sendotp);
        mGetPhoneNumber=findViewById(R.id.getphonenumber);
        mProgressBarOfMain=findViewById(R.id.mainactivitypb);
        firebaseAuth=FirebaseAuth.getInstance();

        countyCode=mCountryCodePicker.getSelectedCountryCodeWithPlus();//required by firebase to send code
        mCountryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countyCode=mCountryCodePicker.getSelectedCountryCodeWithPlus();
            }
        });

        mSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number;
                number=mGetPhoneNumber.getText().toString();

                if(number.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Enter you Number", Toast.LENGTH_SHORT).show();
                }
                else if(number.length()<10)
                {
                    Toast.makeText(getApplicationContext(), "Please Enter Correct Number", Toast.LENGTH_SHORT).show();
                }else
                {
                    mProgressBarOfMain.setVisibility(View.VISIBLE);
                    phoneNumber=countyCode+number;
                    PhoneAuthOptions options=PhoneAuthOptions.newBuilder(firebaseAuth).setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(MainActivity.this)
                            .setCallbacks(mCallbacks)
                            .build();

                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });
        mCallbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull @org.jetbrains.annotations.NotNull PhoneAuthCredential phoneAuthCredential) {
                //how to automatically fetch otp

            }

            @Override
            public void onVerificationFailed(@NonNull @org.jetbrains.annotations.NotNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull @org.jetbrains.annotations.NotNull String s, @NonNull @org.jetbrains.annotations.NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(MainActivity.this, "Code is sent", Toast.LENGTH_SHORT).show();
                mProgressBarOfMain.setVisibility(View.INVISIBLE);
                codeSent= s;// store the sent code in the s variable so that we can verify it in the next activity.
                Intent intent=new Intent(MainActivity.this,OtpSignUp.class);
                intent.putExtra("otp",codeSent);
                startActivity(intent);
            }
        };

    }
//an ovverride method on start, if user is already logged in
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            Intent intent=new Intent(MainActivity.this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}