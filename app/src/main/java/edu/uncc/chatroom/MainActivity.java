package edu.uncc.chatroom;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton, signupButton;
    private FirebaseAuth firebaseAuth;

    // Check if user is already logged in
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            Toast.makeText(MainActivity.this, "Logged in successfully!",
                    Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, ChatActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);

        firebaseAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                // Validation
                if(TextUtils.isEmpty(email))
                    emailInput.setError("Please enter your email!");
                else if(TextUtils.isEmpty(password))
                    passwordInput.setError("Please enter your password!");
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInput.setError("Please enter a valid email address!");
                }
                // Everything is correct
                else {
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener((Activity)v.getContext(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Logged in successfully!",
                                            Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(MainActivity.this, ChatActivity.class);
                                    startActivity(i);
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "Authentication failed!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                    });
                }
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(i);
            }
        });
    }
}
