package com.haheskja.mtgpointtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class GameLog extends AppCompatActivity {
    private static final String TAG = "GameLog";
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Game> gameArrayList;
    FirebaseFirestore db;
    League league;
    TextView toolbarTitle;
    List<String> parList;
    List<Integer> totalScore;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamelog);
        mRecyclerView = findViewById(R.id.recycleView);
        db = FirebaseFirestore.getInstance();

        //Set the top toolbar as the actionbar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Home button
        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.baseline_arrow_back_white_36dp);

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

    public void getLeague(){
        DocumentReference docRef = db.collection("leagues").document(getIntent().getStringExtra("LeagueId"));
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                League newLeague = documentSnapshot.toObject(League.class);
                league = newLeague;
                league.setId(documentSnapshot.getId());
                parList = league.getParticipants();
                totalScore = league.getTotalscore();
                getGames();
            }
        });
    }

    public void getGames(){
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

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void startGame(){
        int gameNum = gameArrayList.size();

        Intent i = new Intent();

        //Send league name, id, and next gamenumber
        i.putExtra("LeagueName", getIntent().getStringExtra("LeagueName"));
        i.putExtra("LeagueId", league.getId());
        i.putExtra("GameNum", ++gameNum);

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

