package com.haheskja.mtgpointtracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GameFragment extends Fragment {
    private static final String TAG = "GameFragment";

    //Objects
    FirebaseFirestore db;
    SharedPreferences prefs;
    SharedPreferences.Editor prefEditor;

    //XML fields
    Button button1, button2, button3, button4;
    TextView par_1_view, par_2_view, par_3_view, par_4_view, noCurrentGame;

    //Game fields
    List<Integer> totalScore;
    List<Integer> currentScore;
    List<String> playersUsername;
    String leagueName, leagueId;
    int gameNum;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void initializeFromShared(){
        Log.d(TAG, "In initializeFromShared");

        //Set league name, id and gamenumber
        leagueName = prefs.getString("LeagueName", "");
        leagueId = prefs.getString("LeagueId", "");
        gameNum = prefs.getInt("GameNum", 0);

        //Set players username
        playersUsername = new ArrayList<>();
        playersUsername.add(prefs.getString("Par1", ""));
        playersUsername.add(prefs.getString("Par2", ""));
        playersUsername.add(prefs.getString("Par3", ""));
        playersUsername.add(prefs.getString("Par4", ""));

        //Set players totalscore
        totalScore = new ArrayList<>();
        totalScore.add(prefs.getInt("Totalscore1", 0));
        totalScore.add(prefs.getInt("Totalscore2", 0));
        totalScore.add(prefs.getInt("Totalscore3", 0));
        totalScore.add(prefs.getInt("Totalscore4", 0));

        //Set players current score
        currentScore = new ArrayList<>();
        currentScore.add(prefs.getInt("Currentscore1", 0));
        currentScore.add(prefs.getInt("Currentscore2", 0));
        currentScore.add(prefs.getInt("Currentscore3", 0));
        currentScore.add(prefs.getInt("Currentscore4", 0));

        //Put the game to Ongoing
        prefEditor.putBoolean("IsOngoing", true).apply();
    }

    public void initializeFromArgs(){
        Log.d(TAG, "In initializeFromArgs");

        //Store league name, id and gamenumber
        leagueName = getArguments().getString("LeagueName", "");
        leagueId = getArguments().getString("LeagueId", "");
        gameNum = getArguments().getInt("GameNum", 0);

        //Store players username
        playersUsername = new ArrayList<>();
        playersUsername.add(getArguments().getString("Par1", ""));
        playersUsername.add(getArguments().getString("Par2", ""));
        playersUsername.add(getArguments().getString("Par3", ""));
        playersUsername.add(getArguments().getString("Par4", ""));

        //Store players totalscore
        totalScore = new ArrayList<>();
        totalScore.add(getArguments().getInt("Totalscore1", 0));
        totalScore.add(getArguments().getInt("Totalscore2", 0));
        totalScore.add(getArguments().getInt("Totalscore3", 0));
        totalScore.add(getArguments().getInt("Totalscore4", 0));

        //Give players a current score
        currentScore = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            currentScore.add(0);
        }

        //Put the game to Ongoing
        prefEditor.putBoolean("IsOngoing", true).apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "In onPause");

        if(prefs.getBoolean("IsOngoing", false)){
            //Store information in sharedpreferences
            //League name, id, gamenumber
            prefEditor.putString("LeagueName", leagueName).apply();
            prefEditor.putString("LeagueId", leagueId).apply();
            prefEditor.putInt("GameNum", gameNum).apply();

            //Players username
            prefEditor.putString("Par1", playersUsername.get(0)).apply();
            prefEditor.putString("Par2", playersUsername.get(1)).apply();
            prefEditor.putString("Par3", playersUsername.get(2)).apply();
            prefEditor.putString("Par4", playersUsername.get(3)).apply();

            //Players totalscore
            prefEditor.putInt("Totalscore1", totalScore.get(0)).apply();
            prefEditor.putInt("Totalscore2", totalScore.get(1)).apply();
            prefEditor.putInt("Totalscore3", totalScore.get(2)).apply();
            prefEditor.putInt("Totalscore4", totalScore.get(3)).apply();

            //Players current score
            prefEditor.putInt("Currentscore1", currentScore.get(0)).apply();
            prefEditor.putInt("Currentscore2", currentScore.get(1)).apply();
            prefEditor.putInt("Currentscore3", currentScore.get(2)).apply();
            prefEditor.putInt("Currentscore4", currentScore.get(3)).apply();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.game, container, false);
        Log.d(TAG, "In onCreateView");
        db = FirebaseFirestore.getInstance();
        prefs = getActivity().getSharedPreferences("PREFERENCE", getActivity().MODE_PRIVATE);
        prefEditor = prefs.edit();

        button1 = view.findViewById(R.id.par_1_point_btn);
        button2 = view.findViewById(R.id.par_2_point_btn);
        button3 = view.findViewById(R.id.par_3_point_btn);
        button4 = view.findViewById(R.id.par_4_point_btn);
        par_1_view = view.findViewById(R.id.par_1_name);
        par_2_view = view.findViewById(R.id.par_2_name);
        par_3_view = view.findViewById(R.id.par_3_name);
        par_4_view = view.findViewById(R.id.par_4_name);

        noCurrentGame = view.findViewById(R.id.noCurrentGame);

        button1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog(1);
            }
        });
        button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog(2);
            }
        });
        button3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog(3);
            }
        });
        button4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog(4);
            }
        });

        /*if(isOngoing = getActivity().getSharedPreferences("PREFERENCE", getActivity().MODE_PRIVATE).getBoolean("IsOngoing", false)){
        }*/

        if (getArguments() != null) {
            noCurrentGame.setVisibility(View.INVISIBLE);
            initializeFromArgs();
            initializeFields();
        }
        else{
            if(!prefs.getBoolean("IsOngoing", false)){
                button1.setVisibility(View.INVISIBLE);
                button2.setVisibility(View.INVISIBLE);
                button3.setVisibility(View.INVISIBLE);
                button4.setVisibility(View.INVISIBLE);
                par_1_view.setVisibility(View.INVISIBLE);
                par_2_view.setVisibility(View.INVISIBLE);
                par_3_view.setVisibility(View.INVISIBLE);
                par_4_view.setVisibility(View.INVISIBLE);
            }
            else{
                noCurrentGame.setVisibility(View.INVISIBLE);
                initializeFromShared();
                initializeFields();
            }
        }

        return view;
    }

    public void initializeFields(){
        par_1_view.setText(playersUsername.get(0));
        par_2_view.setText(playersUsername.get(1));
        par_3_view.setText(playersUsername.get(2));
        par_4_view.setText(playersUsername.get(3));
        button1.setText(Integer.toString(currentScore.get(0)));
        button2.setText(Integer.toString(currentScore.get(1)));
        button3.setText(Integer.toString(currentScore.get(2)));
        button4.setText(Integer.toString(currentScore.get(3)));
    }

    public void calcPoints(int a, int count){
        switch (a){
            case 1:
                currentScore.set(0, currentScore.get(0) + count);
                button1.setText(Integer.toString(currentScore.get(0)));
                break;
            case 2:
                currentScore.set(1, currentScore.get(1) + count);
                button2.setText(Integer.toString(currentScore.get(1)));
                break;
            case 3:
                currentScore.set(2, currentScore.get(2) + count);
                button3.setText(Integer.toString(currentScore.get(2)));
                break;
            case 4:
                currentScore.set(3, currentScore.get(3) + count);
                button4.setText(Integer.toString(currentScore.get(3)));
                break;
        }
    }

    public void selectWinner(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter the winner");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!isEmpty(input)){
                    String winner = input.getText().toString();
                    boolean found = false;
                    for(String username : playersUsername){
                        if(username.equals(winner)){
                            found = true;
                            saveToDatabase(username);
                        }
                    }
                    if(!found){
                        Toast.makeText(getActivity(), "Did not find the entered username.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getActivity(), "Field cannot be empty.", Toast.LENGTH_SHORT).show();
                }
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

    public void saveToDatabase(String winner){
        String gameName = "Game" + gameNum;

        Map<String, Object> newGame = new HashMap<>();
        newGame.put("winner", winner);
        newGame.put("participants", playersUsername);
        newGame.put("score", currentScore);
        newGame.put("gamename", gameName);

        final List<Integer> newTotalScore = new ArrayList<>();
        newTotalScore.add(totalScore.get(0) + currentScore.get(0));
        newTotalScore.add(totalScore.get(1) + currentScore.get(1));
        newTotalScore.add(totalScore.get(2) + currentScore.get(2));
        newTotalScore.add(totalScore.get(3) + currentScore.get(3));

        CollectionReference leagues = db.collection("leagues");
        leagues.document(leagueId).collection("games")
                .add(newGame)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        Map<String, Object> newScore = new HashMap<>();
                        newScore.put("totalscore", newTotalScore);
                        db.collection("leagues").document(leagueId)
                                .update(newScore);
                        prefEditor.putBoolean("IsOngoing", false).apply();
                        Toast.makeText(getActivity(), "Game saved successfully.", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(getActivity(), "Could not save game.", Toast.LENGTH_SHORT).show();
                    }
                });
        }

    public void showDialog(final int button){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose a rule");
        String[] rules = {
                "+1 First Blood",
                "+1 Saving another player",
                "+1 Casting your commander 4 or more times",
                "+1 Killing a player with commander damage",
                "-1 Killing everyone in the same turn",
                "-1 Attacking person in last place (not tied)",
                "-1 Searching for 40 seconds & every 30 seconds after that",
                "-1 Not casting your commander"};

        builder.setItems(rules, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        calcPoints(button, 1);
                        break;
                    case 1:
                        calcPoints(button, 1);
                        break;
                    case 2:
                        calcPoints(button, 1);
                        break;
                    case 3:
                        calcPoints(button, 1);
                        break;
                    case 4:
                        calcPoints(button, -1);
                        break;
                    case 5:
                        calcPoints(button, -1);
                        break;
                    case 6:
                        calcPoints(button, -1);
                        break;
                    case 7:
                        calcPoints(button, -1);
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.game_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_complete:
                selectWinner();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}