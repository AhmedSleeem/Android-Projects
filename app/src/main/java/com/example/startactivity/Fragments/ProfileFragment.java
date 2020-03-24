package com.example.startactivity.Fragments;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.startactivity.Model.User;
import com.example.startactivity.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {



    CircleImageView priofie_image;
    TextView username;


    DatabaseReference reference;
    FirebaseUser firebaseUser;


    StorageReference storageReference;
    private static final int IMAGE_REQUEST=1;
    private Uri imageurl;
    private StorageTask uploadTask;



    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        priofie_image=view.findViewById(R.id.profile_image);
        username=view.findViewById(R.id.username);

        storageReference= FirebaseStorage.getInstance().getReference("uploads");


        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        reference= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    priofie_image.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Glide.with(getContext()).load(user.getImageURL()).into(priofie_image);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        priofie_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });


        return view;
    }

    private void openImage() {

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }


    private String getFileExtention(Uri uri)
    {
        ContentResolver contentResolver=getContext().getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }



    private void uploadImage(){
        final ProgressDialog pd=new ProgressDialog(getContext());
        pd.setMessage("uploading");
        pd.show();

        if (imageurl!=null){

            final StorageReference fileReference=storageReference.child(System.currentTimeMillis()+"."+getFileExtention(imageurl));


            uploadTask=fileReference.putFile(imageurl);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUri=task.getResult();
                            String mUri=downloadUri.toString();

                            reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                            HashMap<String,Object>hashMap= new HashMap<>();
                            hashMap.put("imageURL",mUri);
                            reference.updateChildren(hashMap);
                            pd.dismiss();

                        }else{
                            Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });

        }
        else{
            Toast.makeText(getContext(), "No Image selected", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==IMAGE_REQUEST&&resultCode== RESULT_OK&&data!=null&&data.getData()!=null){
            imageurl=data.getData();
        }

        if (uploadTask!=null&&uploadTask.isInProgress()){
            Toast.makeText(getContext(), "upload in progress", Toast.LENGTH_SHORT).show();
        }else{
            uploadImage();
        }


    }
}
