package edu.uncc.chatroom;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nj.imagepicker.ImagePicker;
import com.nj.imagepicker.listener.ImageResultListener;
import com.nj.imagepicker.result.ImageResult;
import com.nj.imagepicker.utils.DialogConfiguration;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ChatActivity extends AppCompatActivity {
    EditText messageInput;
    TextView nameText;
    ImageButton imageButton, sendButton, logoutButton;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    DatabaseReference firebaseDatabase;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageInput = findViewById(R.id.messageInput);
        imageButton = findViewById(R.id.imageButton);
        sendButton = findViewById(R.id.sendButton);
        logoutButton = findViewById(R.id.logoutButton);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        nameText = findViewById(R.id.nameText);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        firebaseDatabase.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameText.setText(dataSnapshot.child("firstName").getValue(String.class)
                        + " " + dataSnapshot.child("lastName").getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        // Recycler view
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("messages");
        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .build();


        final FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_message, parent, false);

                return new MessageViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder holder, final int position, final Message model) {
                if(model.getUid().compareTo(uid) == 0) {
                    holder.deleteButton.setVisibility(View.VISIBLE);
                }
                firebaseDatabase.child("users").child(model.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        String fname = dataSnapshot.child("firstName").getValue(String.class);
                        String lname = dataSnapshot.child("lastName").getValue(String.class);
                        String message = model.getMessage();

                        if(isURL(message)) {
                            holder.messageText.setVisibility(View.GONE);
                            Picasso.get().load(model.getMessage())
                            .into(holder.imageView);
                            holder.imageView.setVisibility(View.VISIBLE);
                        }
                        else holder.messageText.setText(message);
                        holder.authorText.setText(fname + " " + lname);
                        holder.timeText.setText(model.getTime());

                        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getRef(position).removeValue();
                            }
                        });

                        holder.commentButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String m = messageInput.getText().toString();
                                if(TextUtils.isEmpty(m))
                                    messageInput.setError("Please enter your message");
                                else {
                                    String time = new SimpleDateFormat("HH:mm MM-dd-YYYY").format(Calendar.getInstance().getTime());
                                    getRef(position).child("child_messages").push().setValue(new Message(
                                            m,
                                            uid,
                                            time,
                                            null
                                    )).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            messageInput.setText("");
                                        }
                                    });
                                }
                            }
                        });

                        if(getRef(position).child("child_messages") != null) {
                            Query q = getRef(position).child("child_messages");
                            FirebaseRecyclerOptions<Message> opt =
                                    new FirebaseRecyclerOptions.Builder<Message>()
                                            .setQuery(q, Message.class)
                                            .build();
                            final FirebaseRecyclerAdapter ad = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(opt) {
                                @NonNull
                                @Override
                                public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view = LayoutInflater.from(parent.getContext())
                                            .inflate(R.layout.child_chat_message, parent, false);
                                    return new MessageViewHolder(view);
                                }
                                @Override
                                protected void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position, @NonNull final Message model) {
                                    if(model.getUid().compareTo(uid) == 0) {
                                        holder.deleteButton.setVisibility(View.VISIBLE);
                                    }
                                    holder.messageText.setText(model.getMessage());
                                    firebaseDatabase.child("users").child(model.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            String fname = dataSnapshot.child("firstName").getValue(String.class);
                                            String lname = dataSnapshot.child("lastName").getValue(String.class);
                                            String message = model.getMessage();

                                            holder.messageText.setText(message);
                                            holder.authorText.setText(fname + " " + lname);
                                            holder.timeText.setText(model.getTime());

                                            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    getRef(position).removeValue();
                                                }
                                            });
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    });
                                };
                            };
                            holder.childChatView.setLayoutManager(new LinearLayoutManager(ChatActivity.this,
                                    LinearLayoutManager.VERTICAL, false));
                            ad.startListening();
                            holder.childChatView.setAdapter(ad);
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString();
                if(TextUtils.isEmpty(message))
                    messageInput.setError("Please enter your message");
                else {
                    String time = new SimpleDateFormat("HH:mm MM-dd-YYYY").format(Calendar.getInstance().getTime());
                    firebaseDatabase.child("messages").push().setValue(new Message(
                            message,
                            uid,
                            time,
                            null
                    )).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            messageInput.setText("");
                        }
                    });
                }
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.build(new DialogConfiguration().setTitle("Choose")
                    .setOptionOrientation(LinearLayoutCompat.HORIZONTAL),
                    new ImageResultListener() {
                    @Override
                    public void onImageResult(ImageResult imageResult) {
                        final DatabaseReference db = firebaseDatabase.child("messages").push();
                        final StorageReference storage = FirebaseStorage.getInstance().getReference()
                                .child("images").child(uid + "/" + db.getKey());

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        imageResult.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        final byte[] data = baos.toByteArray();

                        final UploadTask uploadTask = storage.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) { }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String time = new SimpleDateFormat("HH:mm MM-dd-YYYY")
                                                .format(Calendar.getInstance().getTime());
                                        db.setValue(new Message(
                                            uri.toString(),
                                            uid,
                                            time,
                                            null
                                        ));
                                    }
                                });
                            };
                        });
                    }
                }).show(getSupportFragmentManager());
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChatActivity.this, MainActivity.class));
                finish();
            }
        });

    }
    public boolean isURL(String url) {
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }
        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }
}
