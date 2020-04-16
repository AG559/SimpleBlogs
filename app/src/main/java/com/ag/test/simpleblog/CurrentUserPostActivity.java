package com.ag.test.simpleblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ag.test.simpleblog.models.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CurrentUserPostActivity extends AppCompatActivity {
    private FirebaseRecyclerAdapter<Post, MainActivity.BlogViewHolder> adapter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mlikedatabase;
    private Query mDatabase;
    private boolean mProcessLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user_post);
        RecyclerView recyclerView = findViewById(R.id.current_user_post_recycler);
        mAuth = FirebaseAuth.getInstance();
        mlikedatabase = FirebaseDatabase.getInstance().getReference().child("like");
        DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("user");
        mDatabaseUser.keepSynced(true);
        mlikedatabase.keepSynced(true);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("blog").orderByChild("uid").equalTo(mAuth.getUid());
        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("blog");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(CurrentUserPostActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>().setQuery(mDatabase, Post.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(CurrentUserPostActivity.this));
        adapter = new FirebaseRecyclerAdapter<Post, MainActivity.BlogViewHolder>(options) {
            @NonNull
            @Override
            public MainActivity.BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(CurrentUserPostActivity.this).inflate(R.layout.post_recy_row, parent, false);
                return new MainActivity.BlogViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MainActivity.BlogViewHolder holder, final int position, @NonNull Post model) {
                final String post_key = getRef(position).getKey();

                holder.setTitle(model.getTitle());
                holder.setDesc(model.getDescription());
                holder.setImage(getApplicationContext(), model.getImage());
                holder.setUserName(model.getName());
                holder.setLikebtn(post_key);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleIntent = new Intent(CurrentUserPostActivity.this, PostSingleActivity.class);
                        singleIntent.putExtra("blog_id", post_key);
                        startActivity(singleIntent);
                    }
                });
                holder.mlikebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        mlikedatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                        mlikedatabase.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike = false;
                                    } else {
                                        mlikedatabase.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("default");
                                        mProcessLike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }

        };
        if (mAuth.getCurrentUser() != null) {
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        adapter.startListening();
    }
}
