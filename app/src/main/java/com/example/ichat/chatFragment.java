package com.example.ichat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.nio.file.FileStore;

public class chatFragment extends Fragment {
    private FirebaseFirestore firebaseFirestore;
    LinearLayoutManager linearLayoutManager;
    private FirebaseAuth firebaseAuth;
    ImageView mImageViewofUser;
    FirestoreRecyclerAdapter<firebasemodel,NoteViewHolder>chatAdapter;
    RecyclerView mREcyclerView;


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
         View view=inflater.inflate(R.layout.chatfragment,container,false);
         firebaseAuth=FirebaseAuth.getInstance();
         firebaseFirestore=FirebaseFirestore.getInstance();
         mREcyclerView=view.findViewById(R.id.recyclerview);
         //Query query= firebaseFirestore.collection("Users");
        Query query=firebaseFirestore.collection("Users").whereNotEqualTo("uid",firebaseAuth.getUid());
        FirestoreRecyclerOptions<firebasemodel>allusername=new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query,firebasemodel.class).build();
        chatAdapter=new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusername) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull NoteViewHolder holder, int position, @NonNull @NotNull firebasemodel model) {
                holder.particularusername.setText(model.getName());
                String uri=model.getImage();
                Picasso.get().load(uri).into(mImageViewofUser);
                if (model.getStatus().equals("Online")){
                    holder.statusofuser.setText(model.getStatus());
                    holder.statusofuser.setTextColor(Color.GREEN);
                }
                else{
                    holder.statusofuser.setText(model.getStatus());
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent(getActivity(), SpecificChat.class);
                        intent.putExtra("name",model.getName());
                        intent.putExtra("receiveruid",model.getUid());
                        intent.putExtra("imageuri",model.getImage());
                        startActivity(intent);

                    }
                });




            }

            @NonNull
            @NotNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.chatviewlayout,parent,false);
                return  new NoteViewHolder(view);
            }
        };
        mREcyclerView.setHasFixedSize(true);
        linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mREcyclerView.setLayoutManager(linearLayoutManager);
        mREcyclerView.setAdapter(chatAdapter);
        return view;


    }
    public  class NoteViewHolder  extends  RecyclerView.ViewHolder{
        private TextView particularusername;
        private  TextView statusofuser;


        public NoteViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            particularusername=itemView.findViewById(R.id.nameofuser);
            statusofuser=itemView.findViewById(R.id.statusofuser);
            mImageViewofUser=itemView.findViewById(R.id.imageviewofuser);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(chatAdapter!= null);{
            chatAdapter.stopListening();
        }
    }
}
