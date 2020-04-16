package com.ag.test.simpleblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ag.test.simpleblog.models.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private FirebaseRecyclerAdapter<Post, BlogViewHolder> firebaseRecyclerAdapter;
    private RecyclerView postRecycler;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mlikedatabase;
    private boolean mProcessLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        postRecycler = findViewById(R.id.post_recycler);
        postRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mAuth = FirebaseAuth.getInstance();
        mlikedatabase = FirebaseDatabase.getInstance().getReference().child("like");
        DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("user");
        mDatabaseUser.keepSynced(true);
        mlikedatabase.keepSynced(true);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        Query dataQuery = FirebaseDatabase.getInstance().getReference().child("blog");
        dataQuery.keepSynced(true);
        FirebaseRecyclerOptions<Post> fireOptions = new FirebaseRecyclerOptions.Builder<Post>().setQuery(dataQuery, Post.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, BlogViewHolder>(fireOptions) {
            @Override
            protected void onBindViewHolder(@NonNull BlogViewHolder holder, final int position, @NonNull Post model) {
                final String post_key = getRef(position).getKey();

                holder.setTitle(model.getTitle());
                holder.setDesc(model.getDescription());
                holder.setImage(getApplicationContext(), model.getImage());
                holder.setUserName(model.getName());
                holder.setLikebtn(post_key);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singleIntent = new Intent(MainActivity.this, PostSingleActivity.class);
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

            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.post_recy_row, parent, false);
                return new BlogViewHolder(view);
            }
        };
        if (mAuth.getCurrentUser() != null) {
            postRecycler.setAdapter(firebaseRecyclerAdapter);
        }
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(MainActivity.this, CurrentUserPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    void logout() {
        mAuth.signOut();
    }

    static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView title;
        ImageButton mlikebtn;
        DatabaseReference mlikedatabase;
        FirebaseAuth mAuth;

        BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            title = mView.findViewById(R.id.post_title);
            mlikebtn = mView.findViewById(R.id.post_like);
            mlikedatabase = FirebaseDatabase.getInstance().getReference().child("like");
            mAuth = FirebaseAuth.getInstance();
            mlikedatabase.keepSynced(true);
        }

        void setLikebtn(final String post_key) {
            mlikedatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        mlikebtn.setImageResource(R.drawable.ic_liked);
                    } else {
                        mlikebtn.setImageResource(R.drawable.ic_unliked);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        void setTitle(String titleText) {
            title.setText(titleText);
        }

        void setDesc(String descText) {
            TextView desc = mView.findViewById(R.id.post_desc);
            desc.setText(descText);
        }

        void setImage(Context context, String imageText) {
            ImageView imageView = mView.findViewById(R.id.post_image);
            Picasso.get().load(imageText).into(imageView);
        }

        void setUserName(String userName) {
            TextView post_username = mView.findViewById(R.id.post_username);
            post_username.setText(userName);
        }

    }
}
