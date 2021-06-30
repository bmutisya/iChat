package com.example.ichat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SetProfile extends AppCompatActivity {
    private CardView mGetUserImage;
    private ImageView mGetUserImageView;
    private  static  int PICK_iMAGE =123;
    private Uri imagePath;
    private EditText mGetUsername;
    private  android.widget.Button mSaveProfile;
    private FirebaseAuth firebaseAuth;
    private  String name;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String imageUriAcessToken;
    private FirebaseFirestore firebaseFirestore;

    ProgressBar mProgressBarSetProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mGetUsername=findViewById(R.id.getusername);
        mGetUserImage=findViewById(R.id.getuserImage);
        mGetUserImageView=findViewById(R.id.getuserimageinimageview);
        mProgressBarSetProfile=findViewById(R.id.saveProfilepb);
        mSaveProfile=findViewById(R.id.saveProfile);

        mGetUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
               // startActivityForResult(intent,PICK_iMAGE);
               startActivityForResult(intent,PICK_iMAGE);


            }
        });
        mSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name=mGetUsername.getText().toString();
                if(name.isEmpty()){
                    Toast.makeText(SetProfile.this, "Username Empty", Toast.LENGTH_SHORT).show();
                } else  if (imagePath==null){
                    Toast.makeText(SetProfile.this, "Update your Image", Toast.LENGTH_SHORT).show();
                }else {
                    mProgressBarSetProfile.setVisibility(View.VISIBLE);
                    sendDataForNewUser();
                    mProgressBarSetProfile.setVisibility(View.INVISIBLE);
                    Intent intent=new Intent(SetProfile.this,ChatActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private  void sendDataForNewUser(){
        sendDataToRealTimeDatabase();
    }
    private void sendDataToRealTimeDatabase(){
        name=mGetUsername.getText().toString().trim();
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.getUid());
        userprofile mUserProfile= new userprofile(name,firebaseAuth.getUid());
        databaseReference.setValue(mUserProfile);
        Toast.makeText(this, "User Profile Added", Toast.LENGTH_SHORT).show();
        sendImageToStorage();

    }
    private void sendImageToStorage(){
        StorageReference imageRef=storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic");
        //image compression
        Bitmap bitmap=null;
        try {
            bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),imagePath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25,byteArrayOutputStream);
        byte[] data=byteArrayOutputStream.toByteArray();

        ///image on db
        UploadTask uploadTask=imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUriAcessToken=uri.toString();
                        Toast.makeText(SetProfile.this, "URI Get Sucess", Toast.LENGTH_SHORT).show();
                        sendDataToCloudFirestore();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(SetProfile.this, "URI get Failed", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(SetProfile.this, "Imaage is Uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(SetProfile.this, "Image not Uploaded", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void sendDataToCloudFirestore() {
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String,Object> userdata=new HashMap<>();
        userdata.put("name",name);
        userdata.put("image",imageUriAcessToken);
        userdata.put("uid",firebaseAuth.getUid());
        userdata.put("status","Online");
        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(SetProfile.this, "Data on cloud Firestore sucess", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode==PICK_iMAGE && resultCode==RESULT_OK)
        {
            imagePath=data.getData();
            mGetUserImageView.setImageURI(imagePath);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
}