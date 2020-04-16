package com.ag.test.simpleblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostSingleActivity extends AppCompatActivity {
    private TextView title, description;
    private ImageButton image;
    private Button remove_post;
    private FirebaseUser mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_single);
        title = findViewById(R.id.single_post_title);
        description = findViewById(R.id.single_post_desc);
        image = findViewById(R.id.single_post_image);
        remove_post = findViewById(R.id.single_remove_post);
        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        final String post_key = getIntent().getExtras().getString("blog_id");
        final DatabaseReference mdatabase = FirebaseDatabase.getInstance().getReference().child("blog");
        mdatabase.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_desc = (String) dataSnapshot.child("description").getValue();
                String image_uri = (String) dataSnapshot.child("image").getValue();
                String postuid = (String) dataSnapshot.child("uid").getValue();
                title.setText(post_title);
                description.setText(post_desc);
                Picasso.get().load(image_uri).into(image);
                if (mAuth.getUid().equals(postuid)) {
                    remove_post.setVisibility(View.VISIBLE);
                } else {
                    remove_post.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        remove_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdatabase.child(post_key).removeValue();
                Intent intent = new Intent(PostSingleActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
