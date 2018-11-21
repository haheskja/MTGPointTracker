package com.haheskja.mtgpointtracker;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeagueInfoActivity extends AppCompatActivity {
    private static final String TAG = "LeagueInfoActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView usr_1, usr_2, usr_3, usr_4, score_1, score_2, score_3, score_4, toolbartitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leagueinfo);

        //Set the top toolbar as the actionbar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Home button
        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.baseline_arrow_back_white_36dp);

        usr_1 = findViewById(R.id.leaderboard_name_1);
        usr_2 = findViewById(R.id.leaderboard_name_2);
        usr_3 = findViewById(R.id.leaderboard_name_3);
        usr_4 = findViewById(R.id.leaderboard_name_4);
        score_1 = findViewById(R.id.leaderboard_score_1);
        score_2 = findViewById(R.id.leaderboard_score_2);
        score_3 = findViewById(R.id.leaderboard_score_3);
        score_4 = findViewById(R.id.leaderboard_score_4);
        toolbartitle = findViewById(R.id.toolbartitle);
        toolbartitle.setText(getIntent().getStringExtra("LeagueName"));

        getLeague();
    }

    public void getLeague(){
        DocumentReference docRef = db.collection("leagues").document(getIntent().getStringExtra("LeagueId"));
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                League league = documentSnapshot.toObject(League.class);
                updateUI(league);
            }
        });
    }

    public void updateUI(League league){
        List<String> list = league.getParticipants();
        List<Integer> scorelist = league.getTotalscore();

        usr_1.setText(list.get(0));
        usr_2.setText(list.get(1));
        usr_3.setText(list.get(2));
        usr_4.setText(list.get(3));

        score_1.setText(scorelist.get(0).toString());
        score_2.setText(scorelist.get(1).toString());
        score_3.setText(scorelist.get(2).toString());
        score_4.setText(scorelist.get(3).toString());
    }

    public void addGame(){

    }


    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_add:
                addGame();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

