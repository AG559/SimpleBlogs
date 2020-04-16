package com.ag.test.simpleblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {
    private EditText mtitle, mpostDec;
    private ImageButton selectImage;
    private static final int GALLERY_INTENT_CODE = 101;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference imageRef;
    private Uri filePath;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mtitle = findViewById(R.id.mtitle);
        mpostDec = findViewById(R.id.mpostdec);
        selectImage = findViewById(R.id.selectImage);
        Button submitPost = findViewById(R.id.submitPost);
        mAuth = FirebaseAuth.getInstance();
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_INTENT_CODE);
            }
        });
        submitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                filePath = data.getData();
                selectImage.setImageURI(filePath);

            }
        }
    }

    private void submitPost() {
        final String title = mtitle.getText().toString();
        final String dec = mpostDec.getText().toString();
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(dec)) {
            imageRef = firebaseStorage.getReference().child("blog/" + filePath.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(filePath);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();
                        final DatabaseReference newPost = mDatabase.child("blog").push();
                        String currentUId = mAuth.getCurrentUser().getUid();
                        final DatabaseReference currentUser = mDatabase.child("user").child(currentUId);

                        currentUser.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                newPost.child("title").setValue(title);
                                newPost.child("description").setValue(dec);
                                newPost.child("image").setValue(downloadUri.toString());
                                newPost.child("uid").setValue(mAuth.getCurrentUser().getUid());
                                newPost.child("name").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            startActivity(new Intent(PostActivity.this, MainActivity.class));
                                            Toast.makeText(PostActivity.this, "Success Upload Image", Toast.LENGTH_LONG).show();
                                            Log.d("Heyy", downloadUri.toString());
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            });
        }
    }
}
