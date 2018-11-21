package com.haheskja.mtgpointtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
    EditText leagueName, par_1, par_2, par_3, par_4;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaguenew);

        //Set the top toolbar as the actionbar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Home button
        android.support.v7.app.ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.baseline_arrow_back_white_36dp);

        leagueName = findViewById(R.id.input_name);
        par_1 = findViewById(R.id.input_par_1);
        par_2 = findViewById(R.id.input_par_2);
        par_3 = findViewById(R.id.input_par_3);
        par_4 = findViewById(R.id.input_par_4);
    }

    public void createLeague(View view){
        final String name = leagueName.getText().toString();
        final List<String> participantList = new ArrayList<>();
        final List<String> participantIDList = new ArrayList<>();
        final List<Integer> totalscore = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            totalscore.add(0);
        }
        participantList.add(par_1.getText().toString());
        participantList.add(par_2.getText().toString());
        participantList.add(par_3.getText().toString());
        participantList.add(par_4.getText().toString());

        Query firstQuery = db.collection("users").whereEqualTo("username", participantList.get(0));
        Query secondQuery = db.collection("users").whereEqualTo("username", participantList.get(1));
        Query thirdQuery = db.collection("users").whereEqualTo("username", participantList.get(2));
        Query fourthQuery = db.collection("users").whereEqualTo("username", participantList.get(3));

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
                    }
                }

                Map<String, Object> newLeague = new HashMap<>();
                newLeague.put("name", name);
                newLeague.put("participants", participantList);
                newLeague.put("participantsid", participantIDList);
                newLeague.put("totalscore", totalscore);


                db.collection("leagues")
                        .add(newLeague)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {

                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                Toast.makeText(LeagueNewActivity.this, name + " was added successfully.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                                Toast.makeText(LeagueNewActivity.this, "Couldn't add new league",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
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

    /*private class getJSON extends AsyncTask<String, Void,String> {
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... urls) {
            String retur = "";
            String s = "";
            String output = "";
            for (String url : urls) {
                try{
                    URL urlen = new URL(urls[0]);
                    HttpURLConnection conn = (HttpURLConnection)urlen.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");
                    if(conn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed: HTTP errorcode: " + conn.getResponseCode());
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    System.out.println("Output from Server .... \n");
                    while((s = br.readLine()) != null) {
                        output = output + s;
                    }
                    conn.disconnect();
                    try{
                        //Where info should be put into objects
                        JSONArray mat = new JSONArray(output);
                        for (int i = 0; i < mat.length(); i++) {
                            JSONObject jsonobject = mat.getJSONObject(i);
                            String name = jsonobject.getString("name");
                            retur = retur + name+ "\n";
                        }
                        return retur;
                    }
                    catch(JSONException e) {e.printStackTrace();
                    }
                    return retur;
                } catch(Exception e) {
                    return "Noe gikk feil";
                }
            }
            return retur;
        }
        @Override
        protected void onPostExecute(String ss) {
            textView.setText(ss);}
    }
    */
}

