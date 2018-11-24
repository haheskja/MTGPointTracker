package com.haheskja.mtgpointtracker;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
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
    TextView usr_1, usr_2, usr_3, usr_4, score_1, score_2, score_3, score_4, toolbartitle, game1;
    List<String> list;
    League league;
    Game game;
    ConstraintLayout cl;

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
        cl = findViewById(R.id.cl);
        getLeague();
    }

    public void getLeague(){
        DocumentReference docRef = db.collection("leagues").document(getIntent().getStringExtra("LeagueId"));
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                League newLeague = documentSnapshot.toObject(League.class);
                league = newLeague;
                getGames();
            }
        });
    }

    public void getGames(){
        DocumentReference docRef = db.collection("leagues").document(getIntent().getStringExtra("LeagueId")).
                collection("games").document("game1");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Game newGame = documentSnapshot.toObject(Game.class);
                newGame.setId(documentSnapshot.getId());
                game = newGame;
                updateUI();
            }
        });
    }

    public void updateUI(){
        list = league.getParticipants();
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

        /*public void createGameList(){
        TextView game2 = new TextView(this);
        game2.setId(View.generateViewId());
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(100, 20, 0, 0);
        game2.setLayoutParams(params);
        game2.setText(game.getId());
        game2.setBackgroundColor(ContextCompat.getColor(this, R.color.darkGray)); // hex color 0xAARRGGBB
        game2.setPadding(80, 20, 20, 20);// in pixels (left, top, right, bottom)

        cl.addView(game2);


        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(cl);
        constraintSet.connect(game2.getId(),ConstraintSet.TOP,R.id.leaderboard_name_4,ConstraintSet.BOTTOM,0);
        constraintSet.connect(game2.getId(),ConstraintSet.START,cl.getId(),ConstraintSet.START,0);
        constraintSet.connect(game2.getId(),ConstraintSet.END,cl.getId(),ConstraintSet.END,0);
        constraintSet.applyTo(cl);

    }*/


    public void startGame(){
        Intent i = new Intent();
        i.putExtra("LeagueName", getIntent().getStringExtra("LeagueName"));
        i.putExtra("Par1", list.get(0));
        i.putExtra("Par2", list.get(1));
        i.putExtra("Par3", list.get(2));
        i.putExtra("Par4", list.get(3));
        setResult(Activity.RESULT_OK, i);
        finish();
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
                startGame();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

