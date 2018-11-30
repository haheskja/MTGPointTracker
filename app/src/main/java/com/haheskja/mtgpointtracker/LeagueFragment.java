package com.haheskja.mtgpointtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class LeagueFragment extends ListFragment{
    private static final String TAG = "LeagueFragment";
    private TextView toolbarTitle;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<League> leagueList;
    private List<String> leagueStringList;
    private List<Integer> scoreList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();


        Log.d(TAG, "Updating List");
        db.collection("leagues")
                .whereArrayContains("participantsid", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            leagueList = new ArrayList<>();
                            leagueStringList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                League league = document.toObject(League.class);
                                league.setId(document.getId());
                                leagueList.add(league);
                                leagueStringList.add(league.getName());
                                Log.d(TAG, league.getName() + " <- Name " + document.getId() + " => " + document.getData());
                            }
                            //ArrayAdapter
                            if(isAdded()){
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, leagueStringList);
                                setListAdapter(arrayAdapter);
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i = new Intent(getActivity(), GameLog.class);

        i.putExtra("LeagueId", leagueList.get(position).getId());
        i.putExtra("LeagueName", leagueList.get(position).getName());
        getActivity().startActivityForResult(i, 555);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        toolbarTitle = getActivity().findViewById(R.id.toolbartitle);
        toolbarTitle.setText("Leagues");
        return inflater.inflate(R.layout.league, container, false);
    }
}