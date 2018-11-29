package com.haheskja.mtgpointtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeagueNewActivity extends AppCompatActivity {
    private static final String TAG = "LeagueNewActivity";
    EditText etLeagueName, par_2, par_3, par_4, num_games;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaguenew);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        //Set the top toolbar as the actionbar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Home button
        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.baseline_arrow_back_white_36dp);

        etLeagueName = findViewById(R.id.input_name);
        par_2 = findViewById(R.id.input_par_2);
        par_3 = findViewById(R.id.input_par_3);
        par_4 = findViewById(R.id.input_par_4);
        num_games = findViewById(R.id.input_games);
    }

    public void createLeagueButton(View view){
        String par2 = par_2.getText().toString().trim();
        String par3 = par_3.getText().toString().trim();
        String par4 = par_4.getText().toString().trim();
        String leagueName = etLeagueName.getText().toString().trim();
        String numGames = num_games.getText().toString().trim();
        String username = getSharedPreferences(user.getUid(), MODE_PRIVATE).getString("Username", "");

        if(validate(leagueName, numGames, username, par2, par3, par4)){
            createLeague(leagueName, numGames, username, par2, par3, par4);

        }
    }

    public boolean validate(String leagueName, String numGames, String par1, String par2, String par3, String par4){
        if(!TextUtils.isEmpty(par1) &&
                !TextUtils.isEmpty(par2) &&
                !TextUtils.isEmpty(par3) &&
                !TextUtils.isEmpty(par4) &&
                !TextUtils.isEmpty(leagueName) &&
                !TextUtils.isEmpty(numGames)){
            return true;
        }
        else{
            Toast.makeText(this, "No fields can be blank.",
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void goBack(){
        MainActivity.dataChanged = true;
        finish();
    }

    public void createLeague(final String leagueName, final String numGames, String par1, String par2, String par3, String par4){
        final List<String> participantList = new ArrayList<>();
        final List<String> participantIDList = new ArrayList<>();
        final List<String> finalPartList = new ArrayList<>();
        final List<Integer> totalscore = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            totalscore.add(0);
        }
        participantList.add(par1);
        participantList.add(par2);
        participantList.add(par3);
        participantList.add(par4);

        Query firstQuery = db.collection("users").whereEqualTo("usernamenocaps", participantList.get(0).toLowerCase());
        Query secondQuery = db.collection("users").whereEqualTo("usernamenocaps", participantList.get(1).toLowerCase());
        Query thirdQuery = db.collection("users").whereEqualTo("usernamenocaps", participantList.get(2).toLowerCase());
        Query fourthQuery = db.collection("users").whereEqualTo("usernamenocaps", participantList.get(3).toLowerCase());

        Task firstTask = firstQuery.get();
        Task secondTask = secondQuery.get();
        Task thirdTask = thirdQuery.get();
        Task fourthTask = fourthQuery.get();

        Task combinedTask = Tasks.whenAllSuccess(firstTask, secondTask, thirdTask, fourthTask).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> list) {
                for(Object query : list){
                    QuerySnapshot qs = (QuerySnapshot) query;
                    for(QueryDocumentSnapshot document : qs){
                        participantIDList.add(document.getId());
                        finalPartList.add(document.getString("username"));
                    }
                }

                if(finalPartList.size() == participantList.size()){
                    Map<String, Object> newLeague = new HashMap<>();
                    newLeague.put("name", leagueName);
                    newLeague.put("ongoing", true);
                    newLeague.put("numgames", Integer.valueOf(numGames));
                    newLeague.put("participants", finalPartList);
                    newLeague.put("participantsid", participantIDList);
                    newLeague.put("totalscore", totalscore);


                    db.collection("leagues")
                            .add(newLeague)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                    Toast.makeText(LeagueNewActivity.this, leagueName + " was added successfully.",
                                            Toast.LENGTH_SHORT).show();
                                    goBack();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                    Toast.makeText(LeagueNewActivity.this, "Couldn't add new league.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                }
                else{
                    Toast.makeText(LeagueNewActivity.this, "Could not find one or more usernames.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    public boolean onCreateOptionsMenu(Menu menu){
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

