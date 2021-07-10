package com.example.ichat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity {
    private EditText mNewUserName;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore firebaseFirestore;
    private ImageView mGetNewUserImageinImageView;
    private StorageReference storageReference;
    private  String ImageURIAccessToken;
    private androidx.appcompat.widget.Toolbar mToolbarofUpdateProfile;
    private ImageButton mBackButtonofUpdateProfile;
    private FirebaseStorage firebaseStorage;
    android.widget.Button mUpdateProfileButton;

    ProgressBar mProgressBarOfUpdateProfile;
    private Uri imagePath;
    Intent intent ;
    private static  int PICK_iMAGE=123;
    String newname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mToolbarofUpdateProfile=findViewById(R.id.toolbarofupdateprofile);
        mBackButtonofUpdateProfile=findViewById(R.id.backbuttonofupdateprofile);
        mGetNewUserImageinImageView=findViewById(R.id.getnewuserimageinimageview);
        mProgressBarOfUpdateProfile=findViewById(R.id.progressbarofupdateprofile);
        mNewUserName=findViewById(R.id.getnewusername);
        mUpdateProfileButton=findViewById(R.id.updateprofilebutton);
        firebaseAuth =FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        intent=getIntent();
        setSupportActionBar(mToolbarofUpdateProfile);

        mBackButtonofUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mNewUserName.setText(intent.getStringExtra("nameofuser"));
        DatabaseReference databaseReference=firebaseDatabase.getReference(firebaseAuth.getUid());
        mUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newname=mNewUserName.getText().toString();
                if (newname.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Name is Empty",Toast.LENGTH_SHORT).show();
                }
                else if (imagePath!=null)
                {
                    mProgressBarOfUpdateProfile.setVisibility(View.VISIBLE);
                    userprofile muserprofile= new userprofile(newname,firebaseAuth.getUid());
                    databaseReference.setValue(muserprofile);

                    updateImageToStorage();
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                    mProgressBarOfUpdateProfile.setVisibility(View.INVISIBLE);
                    Intent intent=new Intent(UpdateProfile.this,ChatActivity.class);
                    startActivity(intent);
                    finish();

                }
                else{
                    mProgressBarOfUpdateProfile.setVisibility(View.VISIBLE);
                    userprofile muserprofile= new userprofile(newname,firebaseAuth.getUid());
                    databaseReference.setValue(muserprofile);
                    UpdateNameonCloudFirestrore();
                    Toast.makeText(getApplicationContext(), "Updated!", Toast.LENGTH_SHORT).show();
                    mProgressBarOfUpdateProfile.setVisibility(View.INVISIBLE);
                    Intent intent=new Intent(UpdateProfile.this,ChatActivity.class);
                    startActivity(intent);
                    finish();

                }
            }
        });
        mGetNewUserImageinImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent,PICK_iMAGE);
            }
        });
        storageReference=FirebaseStorage.getInstance().getReference();
        storageReference.child("Images").child(firebaseAuth.getUid()).child("Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageURIAccessToken=uri.toString();
                Picasso.get().load(uri).into(mGetNewUserImageinImageView);
            }
        });


    }

    private void UpdateNameonCloudFirestrore() {
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        Map<String,Object> userdata=new HashMap<>();
        userdata.put("name",newname);
        userdata.put("image",ImageURIAccessToken);
        userdata.put("uid",firebaseAuth.getUid());
        userdata.put("status","Online");

        documentReference.set(userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Profile Updated sucessfully", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateImageToStorage() {

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
                            ImageURIAccessToken=uri.toString();
                            Toast.makeText(getApplicationContext(), "URI Get Sucess", Toast.LENGTH_SHORT).show();
                            UpdateNameonCloudFirestrore();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Toast.makeText(getApplicationContext(), "URI get Failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                    Toast.makeText(getApplicationContext(), "Image is Updated", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Image not Updated", Toast.LENGTH_SHORT).show();
                }
            });





    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode==PICK_iMAGE && resultCode==RESULT_OK)
        {
            imagePath=data.getData();
            mGetNewUserImageinImageView.setImageURI(imagePath);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    @Override
    protected void onStop() {
        super.onStop();
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status","Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "now user is offline", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        DocumentReference documentReference=firebaseFirestore.collection("Users").document(firebaseAuth.getUid());
        documentReference.update("status","Offline").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "now user is Online", Toast.LENGTH_SHORT).show();

            }
        });
    }
}