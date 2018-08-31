package edu.uncc.chatroom;

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
import com.google.firebase.database.FirebaseDatabase;
import com.nj.imagepicker.ImagePicker;
import com.nj.imagepicker.listener.ImageResultListener;
import com.nj.imagepicker.result.ImageResult;
import com.nj.imagepicker.utils.DialogConfiguration;

import org.w3c.dom.Text;

public class SignupActivity extends AppCompatActivity {
    private EditText fnameInput, lnameInput, emailInput,
    passwordInput, passwordInput2;
    private Button signupButton, cancelButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fnameInput = findViewById(R.id.fnameInput);
        lnameInput = findViewById(R.id.lnameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        passwordInput2 = findViewById(R.id.passwordInput2);
        signupButton = findViewById(R.id.signupButton);
        cancelButton = findViewById(R.id.cancelButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fname = fnameInput.getText().toString();
                final String lname = lnameInput.getText().toString();
                final String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                String password2 = passwordInput2.getText().toString();

                // Validation
                if(TextUtils.isEmpty(fname))
                    fnameInput.setError("Please enter your first name!");
                else if(TextUtils.isEmpty(lname))
                    lnameInput.setError("Please enter your last name!");
                else if(TextUtils.isEmpty(email))
                    emailInput.setError("Please enter your email!");
                else if(TextUtils.isEmpty(password))
                    passwordInput.setError("Please enter your password!");
                else if(TextUtils.isEmpty(password2))
                    passwordInput2.setError("Please repeat your password!");
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailInput.setError("Please enter a valid email address!");
                }
                else {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Your account" +
                                                    "has been created!",
                                            Toast.LENGTH_SHORT).show();
                                    // Save user data to database
                                    String uid = firebaseAuth.getCurrentUser().getUid();
                                    firebaseDatabase.getReference().child("users").child(uid)
                                            .setValue(new User(fname, lname, email));
                                    // Start chat activity
                                    Intent i = new Intent(SignupActivity.this, ChatActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                                else {
                                    Toast.makeText(SignupActivity.this, "Something went wrong",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                }

            }
        });
    }
}
