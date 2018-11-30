package com.haheskja.mtgpointtracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private TextView username, email, password;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        startApp(currentUser);
    }

    private void startApp(FirebaseUser currentUser){
        if(currentUser != null){
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void findUsername(final FirebaseUser user){
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    getSharedPreferences(user.getUid(), MODE_PRIVATE).edit().putString("Username", task.getResult().getString("username")).apply();
                    Log.d(TAG, "Username: " + task.getResult().getString("username"));
                    startApp(user);
                }
            }
        });
    }

    private void checkCredentials(String email, String password){
        mAuth.signInWithEmailAndPassword(email.toLowerCase(), password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LogIn", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            findUsername(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LogIn", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            startApp(null);
                            isLoading = false;
                        }

                        // ...
                    }
                });

    }

    public void logIn(View v){
        if(validate() && !isLoading){
            isLoading = true;
            checkCredentials(email.getText().toString().trim(), password.getText().toString().trim());
        }

    }

    private boolean validate(){
        if(!TextUtils.isEmpty(email.getText().toString().trim()) &&
                !TextUtils.isEmpty(password.getText().toString().trim())){
            return true;
        }
        else{
            Toast.makeText(LoginActivity.this, "No fields can be blank.",
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    public void goToRegister(View view){
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
    }


}

