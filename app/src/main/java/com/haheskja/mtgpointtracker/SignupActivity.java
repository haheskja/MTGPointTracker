package com.haheskja.mtgpointtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    private TextView etUsername, etEmail, etPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.username);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();
    }

    private void createAccount(final String username, final String email, final String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("createAccount", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Map<String, Object> dbUser = new HashMap<>();
                            dbUser.put("username", username);
                            dbUser.put("usernamenocaps", username.toLowerCase());
                            dbUser.put("email", email.toLowerCase());
                            db.collection("users").document(user.getUid()).set(dbUser);
                            getSharedPreferences(user.getUid(), MODE_PRIVATE).edit().putString("Username", username).apply();

                            startApp(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("createAccount", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            startApp(null);
                        }
                    }
                });
    }

    private void startApp(FirebaseUser currentUser){
        if(currentUser != null){
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    public void signUp(View v){
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(validate(username, email, password) && !isLoading){
            isLoading = true;
            checkIfUsernameExist(username, email, password);
        }

    }

    private void checkIfUsernameExist(final String username, final String email, final String password){
        db.collection("users")
                .whereEqualTo("usernamenocaps", username.toLowerCase())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean exist = false;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                exist = true;
                            }
                            if(!exist){
                                createAccount(username, email, password);
                            }
                            else{
                                Toast.makeText(SignupActivity.this, "Username already taken.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private boolean validEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private boolean validate(String username, String email, String password){
        if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            if(validEmail(email)){
                if(password.length() > 5){
                    return true;
                }
                else{
                    Toast.makeText(SignupActivity.this, "Password needs at least 6 characters",
                            Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(SignupActivity.this, "Please enter a valid email.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(SignupActivity.this, "No fields can be blank.",
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }



}

