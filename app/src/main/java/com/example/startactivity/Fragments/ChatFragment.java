package com.example.startactivity.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.startactivity.Model.Chat;
import com.example.startactivity.Model.Chatlist;
import com.example.startactivity.Notification.Token;
import com.example.startactivity.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import Adapters.UserAdapter;
import com.example.startactivity.Model.User;
import com.google.firebase.iid.FirebaseInstanceId;


public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;


    private UserAdapter userAdapter;
    private List<User> musers;


    FirebaseUser firebaseUser;

    DatabaseReference reference;

    private List<Chatlist> userslist;




    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView=view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        userslist=new ArrayList<>();
        musers=new ArrayList<>();

        reference=FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userslist.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    userslist.add(chatlist);

                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        ubdateToken(FirebaseInstanceId.getInstance().getToken());


        return view;
    }

    private void ubdateToken(String refreshToken) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token= new Token(refreshToken);
        reference.child(firebaseUser.getUid()).setValue(token);

    }


    private void chatList() {

        musers=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                musers.clear();
                if (dataSnapshot.exists())
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (int i=0;i<userslist.size();++i){
                        Chatlist chatlist = userslist.get(i);
                        if (user.getId().equals(chatlist.getId())){
                            musers.add(user);
                        }
                    }
                }
                userAdapter=new UserAdapter(getContext(),musers,true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
