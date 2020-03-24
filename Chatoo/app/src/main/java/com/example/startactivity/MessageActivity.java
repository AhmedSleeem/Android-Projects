package com.example.startactivity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.startactivity.Notification.ApiService;
import com.example.startactivity.Notification.Client;
import com.example.startactivity.Notification.Data;
import com.example.startactivity.Notification.MyResponse;
import com.example.startactivity.Notification.Sender;
import com.example.startactivity.Notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Adapters.MessageAdapter;
import com.example.startactivity.Model.Chat;
import com.example.startactivity.Model.User;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser firebaseUser;

    DatabaseReference reference;

    MessageAdapter messageAdapter;

    String userid;

    List<Chat>mchat;
    RecyclerView recyclerView;

    ImageButton btn_send;
    EditText txt_send;
    Intent intent;

    ValueEventListener seenListener;


    ApiService apiService;

    boolean notify=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this,Main22Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });


        apiService= Client.getInstance("https://fcm.googleapis.com/").create(ApiService.class);
        mchat=new ArrayList<>();


        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);




        profile_image=findViewById(R.id.profile_image);
        username=findViewById(R.id.username);

        btn_send=findViewById(R.id.btn_send);
        txt_send=findViewById(R.id.txt_send);

        intent=getIntent();

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
         userid=intent.getStringExtra("userid");



        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;
                String msg=txt_send.getText().toString();
                if (!msg.equals("")){
                    if (notify) {
                        sendMessage(firebaseUser.getUid(), userid, msg);
                    }
                    notify=false;

                }
                txt_send.setText("");
            }
        });






        reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }
                else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
                readMessage(firebaseUser.getUid(),userid,user.getImageURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenMessage(userid);
    }

    private void seenMessage(final String userid)
    {
        reference=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid())&&chat.getSender().equals(userid)){
                        HashMap<String,Object>map = new HashMap<>();
                        map.put("isseen","true");
                        snapshot.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private  void sendMessage(String sender, final String receiver, String message){

     DatabaseReference reference =FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> map = new HashMap<>();
        map.put("sender",sender);
        map.put("receiver",receiver);
        map.put("message",message);
        map.put("isseen","false");

        reference.child("Chats").push().setValue(map);

        final DatabaseReference childref= FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(userid);

        childref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    childref.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final  String msg=message;
        reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                sendNotifications(receiver,user.getUsername(),msg);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    private void sendNotifications(String receiver, final String username, final String msg) {

        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");


        Query query= tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data= new Data(firebaseUser.getUid(),R.mipmap.ic_launcher,username+": "+msg,"New Message"
                    ,userid);
                    Sender sender=new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                    .enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code()==200){

                                if (response.body().success==1){
                                    Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }


                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });











    }


    private void readMessage(final String myid, final String userid, final String imageURL){

        reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Chat value = snapshot.getValue(Chat.class);
                    assert value != null;
                   if ((value.getSender().equals(myid)&&value.getReceiver().equals(userid))||
                           (value.getSender().equals(userid)&&value.getReceiver().equals(myid))){
                       mchat.add(value);
                   }
                    messageAdapter=new MessageAdapter(MessageActivity.this,mchat,imageURL);
                    recyclerView.setAdapter(messageAdapter);
                }


            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }





    private void status(String status){
        reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String,Object>map= new HashMap<>();
        map.put("status",status);
        reference.updateChildren(map);
    }

    @Override
    protected void onStart() {
        super.onStart();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }

















}
