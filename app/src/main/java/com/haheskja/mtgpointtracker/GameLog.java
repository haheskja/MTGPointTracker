package com.haheskja.mtgpointtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Person;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLog extends AppCompatActivity {
    private static final String TAG = "GameLog";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Game> gameArrayList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private League league;
    private TextView usr_1, usr_2, usr_3, usr_4, score_1, score_2, score_3, score_4, toolbarTitle, numGames, status;
    private List<String> parList;
    private List<Integer> totalScore;
    private String ongoing;
    private boolean isLoaded = false;
    private boolean isOngoing = false;
    private int gameNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamelog);
        mRecyclerView = findViewById(R.id.recycleView);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //Set the top toolbar as the actionbar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Home button
        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.baseline_arrow_back_white_36dp);

        //Define xml fields
        usr_1 = findViewById(R.id.leaderboard_name_1);
        usr_2 = findViewById(R.id.leaderboard_name_2);
        usr_3 = findViewById(R.id.leaderboard_name_3);
        usr_4 = findViewById(R.id.leaderboard_name_4);
        score_1 = findViewById(R.id.leaderboard_score_1);
        score_2 = findViewById(R.id.leaderboard_score_2);
        score_3 = findViewById(R.id.leaderboard_score_3);
        score_4 = findViewById(R.id.leaderboard_score_4);
        numGames = findViewById(R.id.num_games);
        status = findViewById(R.id.status);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        toolbarTitle = findViewById(R.id.toolbartitle);
        toolbarTitle.setText(getIntent().getStringExtra("LeagueName"));

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        gameArrayList = new ArrayList<>();
        mAdapter = new GameLogAdapter(this, gameArrayList);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        getLeague();
    }

    private void getLeague(){
        DocumentReference docRef = db.collection("leagues").document(getIntent().getStringExtra("LeagueId"));
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                League newLeague = documentSnapshot.toObject(League.class);
                league = newLeague;
                league.setId(documentSnapshot.getId());
                isOngoing = league.isOngoing();

                parList = league.getParticipants();
                totalScore = league.getTotalscore();
                getGames();
            }
        });
    }

    private void getGames(){
        db.collection("leagues").document(getIntent().getStringExtra("LeagueId"))
                .collection("games")
                .orderBy("gamename", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Game newGame = document.toObject(Game.class);
                                newGame.setId(document.getId());
                                gameArrayList.add(newGame);
                            }
                            mAdapter.notifyDataSetChanged();
                            gameNum = gameArrayList.size();
                            updateUI(sortLeaderboard());
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void updateUI(List<User> user){
        if(league.isOngoing()){
            ongoing = "Ongoing";
        }
        else{
            ongoing = "Completed";
        }
        status.setText(getString(R.string.gamelog_status, ongoing));
        numGames.setText(getString(R.string.gamelog_num_games, gameNum, league.getNumgames()));

        usr_1.setText(user.get(0).getUsername());
        usr_2.setText(user.get(1).getUsername());
        usr_3.setText(user.get(2).getUsername());
        usr_4.setText(user.get(3).getUsername());

        score_1.setText(String.valueOf(user.get(0).getScore()));
        score_2.setText(String.valueOf(user.get(1).getScore()));
        score_3.setText(String.valueOf(user.get(2).getScore()));
        score_4.setText(String.valueOf(user.get(3).getScore()));
        isLoaded = true;

    }

    private List<User> sortLeaderboard(){
        List<User> user = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            User newUser = new User(totalScore.get(i), parList.get(i));
            user.add(newUser);
        }
        Collections.sort(user);
        return user;
    }

    private void overwriteGameDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Overwrite game in progress?");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startGameFragment();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void startGameFragment(){
        Intent i = new Intent();

        //Send league name, id, and next gamenumber
        i.putExtra("LeagueName", getIntent().getStringExtra("LeagueName"));
        i.putExtra("LeagueId", league.getId());
        i.putExtra("GameNum", ++gameNum);
        i.putExtra("NumGames", league.getNumgames());

        //Send players username
        i.putExtra("Par1", parList.get(0));
        i.putExtra("Par2", parList.get(1));
        i.putExtra("Par3", parList.get(2));
        i.putExtra("Par4", parList.get(3));

        //Send players totalscore
        i.putExtra("Totalscore1", totalScore.get(0));
        i.putExtra("Totalscore2", totalScore.get(1));
        i.putExtra("Totalscore3", totalScore.get(2));
        i.putExtra("Totalscore4", totalScore.get(3));

        setResult(Activity.RESULT_OK, i);
        finish();
    }

    private void startGame(){
        if(league.isOngoing()){
            if(getSharedPreferences(mAuth.getCurrentUser().getUid(), MODE_PRIVATE).getBoolean("IsOngoing", false)){
                overwriteGameDialog();
            }
            else{
                startGameFragment();
            }
        }
    }

    @Override
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
                if(isOngoing){
                    startGame();
                }
                else if(!isLoaded){
                    Toast.makeText(GameLog.this, "League is not loaded.",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(GameLog.this, "League is completed.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

