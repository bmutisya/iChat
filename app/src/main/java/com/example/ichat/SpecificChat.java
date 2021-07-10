package com.example.ichat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SpecificChat extends AppCompatActivity {
    EditText mGetMessage;
    ImageButton mSendMessageButton;
    CardView mSendMessageCardView;

    androidx.appcompat.widget.Toolbar mToolBarOfSpecificChat;
    ImageView mImageViwOfSpecificUser;
    TextView mNameOFSpecificUser;
    private String enteredmessage;
    Intent intent;
    String mReceiverName,mSenderName, mReceiveruid,mSenderuid;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    String senderroom, receiverroom;
    ImageButton mBackBUttonOfSpecificChat;
    RecyclerView mMessageRecyclerView;
    String currenttime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_chat);

        mGetMessage=findViewById(R.id.getmessage);
        mSendMessageCardView=findViewById(R.id.cardviewofsendmessage);
        mSendMessageButton=findViewById(R.id.imageviewofsendmessage);
        mToolBarOfSpecificChat=findViewById(R.id.toolbarofSpecificChat);
        mNameOFSpecificUser=findViewById(R.id.nameofspecificuser);
        mImageViwOfSpecificUser=findViewById(R.id.specificuserimageinimageview);
        mBackBUttonOfSpecificChat=findViewById(R.id.backbuttonofspecificchat);
        messagesArrayList=new ArrayList<>();
        mMessageRecyclerView=findViewById(R.id.recyclerviewofspecificchat);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(linearLayoutManager);
        messagesAdapter =new MessagesAdapter(SpecificChat.this,messagesArrayList);
        mMessageRecyclerView.setAdapter(messagesAdapter);

        intent=getIntent();
        setSupportActionBar(mToolBarOfSpecificChat);
        mToolBarOfSpecificChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "toolbar clicked", Toast.LENGTH_SHORT).show();
            }
        });
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        calendar=Calendar.getInstance();
        simpleDateFormat= new SimpleDateFormat("hh:mm a");

        mSenderuid=firebaseAuth.getUid();
        mReceiveruid=getIntent().getStringExtra("receiveruid");
        mReceiverName=getIntent().getStringExtra("name");
        senderroom=mSenderuid+mReceiveruid;
        receiverroom=mReceiveruid+mSenderuid;

        DatabaseReference databaseReference=firebaseDatabase.getReference().child("chats").child(senderroom).child("messages");
        messagesAdapter= new MessagesAdapter(SpecificChat.this, messagesArrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot snapshot1 :snapshot.getChildren()){
                    Messages messages=snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });





        mBackBUttonOfSpecificChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mNameOFSpecificUser.setText(mReceiverName);
        String uri= intent.getStringExtra("imageuri");
        if (uri.isEmpty()){
            Toast.makeText(getApplicationContext(), "Null is Receive", Toast.LENGTH_SHORT).show();
        }else
        {
            Picasso.get().load(uri).into(mImageViwOfSpecificUser);
        }
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enteredmessage=mGetMessage.getText().toString();
                if (enteredmessage.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter Text", Toast.LENGTH_SHORT).show();
                }else {
                    Date date= new Date();
                    currenttime=simpleDateFormat.format(calendar.getTime());
                    Messages messages=new Messages( enteredmessage,firebaseAuth.getUid(),date.getTime(),currenttime);
                    firebaseDatabase=FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("chats")
                            .child(senderroom)
                            .child("messages")
                            .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            firebaseDatabase.getReference()
                                    .child("chats")
                                    .child(receiverroom)
                                    .child("messages")
                                    .push()
                                    .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                }
                            });

                        }
                    });
                    mGetMessage.setText(null);

                }

            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(messagesAdapter!= null);{
            messagesAdapter.notifyDataSetChanged();
        }
    }
}