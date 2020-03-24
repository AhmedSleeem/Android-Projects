package com.example.startactivity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText username,email,password;
    Button btn_register;
    FirebaseAuth auth;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username= findViewById(R.id.username);
        email= findViewById(R.id.email);
        password= findViewById(R.id.password);
        btn_register= findViewById(R.id.btn_register);
        auth=FirebaseAuth.getInstance();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth= FirebaseAuth.getInstance();
                String txt_username=username.getText().toString();
                String txt_email=email.getText().toString();
                String txt_password=password.getText().toString();
                if (TextUtils.isEmpty(txt_username)||TextUtils.isEmpty(txt_email)||TextUtils.isEmpty(txt_password)){
                    Toast.makeText(RegisterActivity.this,"fill the empty fields",Toast.LENGTH_LONG>>1).show();
                }
                else if (txt_password.length()<6){
                    Toast.makeText(RegisterActivity.this,"weak password it must be at least 6 char",Toast.LENGTH_LONG>>1).show();
                }
                else {
                    register(txt_username,txt_email,txt_password);
                }

            }
        });

    }
    private  void register(final String username, String email, String password){
        auth=FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email,password)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userUid = firebaseUser.getUid();
                    reference= FirebaseDatabase.getInstance().getReference("Users").child(userUid);
                    HashMap<String,String>hashMap= new HashMap<>();
                    hashMap.put("id",userUid);
                    hashMap.put("username",username);
                    hashMap.put("imageURL","default");
                    hashMap.put("status","offline");
                    hashMap.put("search",username.toLowerCase());
                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                                if (task2.isSuccessful()){
                                    Intent intent= new Intent(RegisterActivity.this,Main22Activity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                        }
                    });
                }
                else{
                    Toast.makeText(RegisterActivity.this,"you cant register with this email and password",Toast.LENGTH_LONG>>1).show();
                }
            }
        });
    }
}
