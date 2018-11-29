package com.haheskja.mtgpointtracker;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class GameFragment extends Fragment {
    private static final String TAG = "GameFragment";
    TinyDB tinydb;


    //Objects
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    SharedPreferences prefs;
    SharedPreferences.Editor prefEditor;

    //XML fields
    Button button1, button2, button3, button4;
    TextView par_1_view, par_2_view, par_3_view, par_4_view, noCurrentGame, toolbarTitle;

    //Game fields
    List<Integer> totalScore;
    List<Integer> currentScore;
    List<Integer> currentScoreOld;
    List<String> playersUsername;
    String leagueName, leagueId;
    int gameNum;
    int numGames;
    int returnNumber = 0;
    boolean hasUndone = false;
    boolean isLoaded = false;

    ArrayList<Rule> par1rules;
    ArrayList<Rule> par2rules;
    ArrayList<Rule> par3rules;
    ArrayList<Rule> par4rules;

    ArrayList<Rule> par1rulesOld;
    ArrayList<Rule> par2rulesOld;
    ArrayList<Rule> par3rulesOld;
    ArrayList<Rule> par4rulesOld;

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
        numGames = prefs.getInt("NumGames", 0);

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

        par1rules = tinydb.getListObject("Par1Rules");
        par2rules = tinydb.getListObject("Par2Rules");
        par3rules = tinydb.getListObject("Par3Rules");
        par4rules = tinydb.getListObject("Par4Rules");

    }

    public void initializeFromArgs(){
        Log.d(TAG, "In initializeFromArgs");

        //Store league name, id and gamenumber
        leagueName = getArguments().getString("LeagueName", "");
        leagueId = getArguments().getString("LeagueId", "");
        gameNum = getArguments().getInt("GameNum", 0);
        numGames = getArguments().getInt("NumGames", 0);

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


        createRules();

        //Give players a current score
        currentScore = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            currentScore.add(0);
        }

        //Put the game to Ongoing
        prefEditor.putBoolean("IsOngoing", true).apply();
    }

    public void createRules(){
        Rule[] rules = {
                new Rule("+1 First Blood", 1, true, false),
                new Rule("+1 Saving another player", 1, false, false),
                new Rule("+1 Casting your commander 4 or more times", 1, false, true),
                new Rule("+1 Killing a player with commander damage", 1, false, false),
                new Rule("-1 Killing everyone in the same turn", -1, true, false),
                new Rule("-1 Attacking person in last place (not tied)", -1, false, true),
                new Rule("-1 Searching for 40 seconds & every 30 seconds after that", -99, false, false),
                new Rule("-1 Not casting your commander", -1, false, true),
        };

        par1rules = new ArrayList<>(Arrays.asList(rules));
        par2rules = new ArrayList<>(Arrays.asList(rules));
        par3rules = new ArrayList<>(Arrays.asList(rules));
        par4rules = new ArrayList<>(Arrays.asList(rules));
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
            prefEditor.putInt("NumGames", numGames).apply();

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

            //Players current ruleslist
            tinydb.putListObject("Par1Rules", par1rules);
            tinydb.putListObject("Par2Rules", par2rules);
            tinydb.putListObject("Par3Rules", par3rules);
            tinydb.putListObject("Par4Rules", par4rules);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.game, container, false);
        Log.d(TAG, "In onCreateView");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        tinydb = new TinyDB(getActivity());
        prefs = getActivity().getSharedPreferences(mAuth.getCurrentUser().getUid(), getActivity().MODE_PRIVATE);
        prefEditor = prefs.edit();

        button1 = view.findViewById(R.id.par_1_point_btn);
        button2 = view.findViewById(R.id.par_2_point_btn);
        button3 = view.findViewById(R.id.par_3_point_btn);
        button4 = view.findViewById(R.id.par_4_point_btn);
        par_1_view = view.findViewById(R.id.par_1_name);
        par_2_view = view.findViewById(R.id.par_2_name);
        par_3_view = view.findViewById(R.id.par_3_name);
        par_4_view = view.findViewById(R.id.par_4_name);
        toolbarTitle = getActivity().findViewById(R.id.toolbartitle);
        toolbarTitle.setText("Current game");
        noCurrentGame = view.findViewById(R.id.noCurrentGame);

        button1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog(1, par1rules);
            }
        });
        button2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog(2, par2rules);
            }
        });
        button3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog(3, par3rules);
            }
        });
        button4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog(4, par4rules);
            }
        });


        if (getArguments() != null) {
            noCurrentGame.setVisibility(View.INVISIBLE);
            initializeFromArgs();
            initializeFields();
            isLoaded = true;
            setArguments(null);
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
                isLoaded = true;
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

    public int getUserNumber(String user){
        for(int i = 0; i < playersUsername.size(); i++){
            if(user.equals(playersUsername.get(i))){
                int b = i+1;
                return b;
            }
        }
        return 0;
    }

    public void passInfo(String user1, String user2, String user3, String user4){
        Set<String> spinner1Comp = new HashSet<String>();
        spinner1Comp.add(user2);
        spinner1Comp.add(user3);
        spinner1Comp.add(user4);

        Set<String> spinner2Comp = new HashSet<String>();
        spinner2Comp.add(user1);
        spinner2Comp.add(user3);
        spinner2Comp.add(user4);

        Set<String> spinner3Comp = new HashSet<String>();
        spinner3Comp.add(user1);
        spinner3Comp.add(user2);
        spinner3Comp.add(user4);

        Set<String> spinner4Comp = new HashSet<String>();
        spinner4Comp.add(user1);
        spinner4Comp.add(user2);
        spinner4Comp.add(user3);

        if(spinner1Comp.contains(user1) || spinner2Comp.contains(user2) || spinner3Comp.contains(user3) || spinner4Comp.contains(user4)){
            Toast.makeText(getActivity(), "No dropdowns can be equal.", Toast.LENGTH_SHORT).show();
        }
        else{
            calcPoints(getUserNumber(user1), 4);
            calcPoints(getUserNumber(user2), 3);
            calcPoints(getUserNumber(user3), 2);
            calcPoints(getUserNumber(user4), 1);
            saveToDatabase(user1);
        }
    }

    public void selectWinner(){
        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View textEntryView = factory.inflate(R.layout.select_winners, null);
        final Spinner spinner1 = textEntryView.findViewById(R.id.spinnerFirst);
        final Spinner spinner2 = textEntryView.findViewById(R.id.spinnerSecond);
        final Spinner spinner3 = textEntryView.findViewById(R.id.spinnerThird);
        final Spinner spinner4 = textEntryView.findViewById(R.id.spinnerFourth);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, playersUsername);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, playersUsername);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, playersUsername);
        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, playersUsername);
        spinner1.setAdapter(adapter1);
        spinner2.setAdapter(adapter2);
        spinner3.setAdapter(adapter3);
        spinner4.setAdapter(adapter4);



        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Who won the match?").setView(textEntryView).setPositiveButton("Save",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        passInfo(spinner1.getSelectedItem().toString(), spinner2.getSelectedItem().toString(),
                                spinner3.getSelectedItem().toString(), spinner4.getSelectedItem().toString());
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {

                    }
                });
        alert.show();

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
                        Map<String, Object> newScore = new HashMap<>();
                        newScore.put("totalscore", newTotalScore);

                        if(numGames == gameNum){
                            newScore.put("ongoing", false);
                        }
                        db.collection("leagues").document(leagueId)
                                .update(newScore);
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        Toast.makeText(getActivity(), "Game saved successfully.", Toast.LENGTH_SHORT).show();
                        finishGame();
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

    public void finishGame(){
        prefEditor.putBoolean("IsOngoing", false).apply();
        Activity act = getActivity();
        if (act instanceof MainActivity){
            ((MainActivity) act).finishGame(leagueId, leagueName);
        }
    }

    public void showDialog(final int button, final List<Rule> rulesList){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose a rule");
        final String[] rules = new String[rulesList.size()];
        for(int i = 0; i < rulesList.size(); i++){
            rules[i] = rulesList.get(i).getText();
        }


        builder.setItems(rules, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveLastAction();
                if(rulesList.get(which).getPointsGiven() == -99){
                    pointsFromTimeOnClick(button);
                }
                else {
                    calcPoints(button, rulesList.get(which).getPointsGiven());
                    changeLists(rulesList, which);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void pointsFromTimeOnClick(final int button){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter search time in seconds:");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!isEmpty(input)){
                    pointsFromTime(button, Integer.valueOf(input.getText().toString().trim()));
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

    public void pointsFromTime(int button, int number){
        int points = 0;
        if(number > 40){
            points--;
            number += -40;
        }
        while(number > 30){
            points--;
            number -= 30;
        }
        calcPoints(button, points);
    }

    public void changeLists(List<Rule> rulesList, int which){
        if(rulesList.get(which).isRemoveFromAll()){
            par1rules.remove(which);
            par2rules.remove(which);
            par3rules.remove(which);
            par4rules.remove(which);
        }
        else if(rulesList.get(which).isRemoveFromSelf()){
            rulesList.remove(which);
        }
    }

    public void saveLastAction(){
        hasUndone = false;
        par1rulesOld = new ArrayList<>(par1rules);
        par2rulesOld = new ArrayList<>(par2rules);
        par3rulesOld = new ArrayList<>(par3rules);
        par4rulesOld = new ArrayList<>(par4rules);
        currentScoreOld = new ArrayList<>(currentScore);
    }

    public void undoLastAction(){
        if(par1rulesOld == null && par2rulesOld == null && par3rulesOld == null && par4rulesOld == null || hasUndone){
            Toast.makeText(getActivity(), "No action to undo.", Toast.LENGTH_SHORT).show();
            return;
        }
        hasUndone = true;
        Toast.makeText(getActivity(), "Undoing.", Toast.LENGTH_SHORT).show();
        par1rules = par1rulesOld;
        par2rules = par2rulesOld;
        par3rules = par3rulesOld;
        par4rules = par4rulesOld;
        currentScore = currentScoreOld;
        initializeFields();
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
                if(prefs.getBoolean("IsOngoing", false) || isLoaded){
                    selectWinner();
                }
                break;
            case R.id.menu_undo:
                undoLastAction();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}