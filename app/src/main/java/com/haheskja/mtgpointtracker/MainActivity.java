package com.haheskja.mtgpointtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    TextView toolbarTitle;
    public static boolean dataChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.mipmap.baseline_menu_white_24);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarTitle = findViewById(R.id.toolbartitle);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        switch (menuItem.getItemId()) {
                            case R.id.nav_leagues:
                                startLeagueFragment();
                                break;
                            case R.id.nav_game:
                                startGameFragment(null);
                                break;
                            case R.id.nav_logout:
                                signOut();
                                break;
                        }
                        return true;
                    }
                });

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.content_frame) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            LeagueFragment firstFragment = new LeagueFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            FragmentManager manager = getSupportFragmentManager();

            FragmentTransaction transaction = manager.beginTransaction();

            transaction.add(R.id.content_frame, firstFragment);
            transaction.addToBackStack(firstFragment.getClass().getName());
            transaction.commit();

            navigationView.setCheckedItem(R.id.nav_leagues);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {

            @Override
            public void onBackStackChanged() {
                Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (f != null){
                    updateTitleAndDrawer (f);
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        Log.d("Testingtest", currentFragment.getClass().getName());
        if(dataChanged && currentFragment.getClass().getName().equals("com.haheskja.mtgpointtracker.LeagueFragment")){
            dataChanged = false;
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.detach(currentFragment);
            fragmentTransaction.attach(currentFragment);
            fragmentTransaction.commit();
        }
    }

    private void updateTitleAndDrawer (Fragment fragment){
        String fragClassName = fragment.getClass().getName();

        if (fragClassName.equals(GameFragment.class.getName())){
            navigationView.setCheckedItem(R.id.nav_game);

        }
        else if (fragClassName.equals(LeagueFragment.class.getName())){
            navigationView.setCheckedItem(R.id.nav_leagues);

        }
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void startLeagueFragment(){
        LeagueFragment newFragment = new LeagueFragment();
        replaceFragment(newFragment);

    }

    public void startGameFragment(Intent data){
        GameFragment newFragment = new GameFragment();
        if(data != null){
            Log.d("GameFragment", "Putting arguments");
            Bundle bundle = new Bundle();

            //Forward league name, id and gamenumber
            bundle.putString("LeagueName", data.getStringExtra("LeagueName"));
            bundle.putString("LeagueId", data.getStringExtra("LeagueId"));
            bundle.putInt("GameNum", data.getIntExtra("GameNum", 0));
            bundle.putInt("NumGames", data.getIntExtra("NumGames", 0));

            //Forward player username
            bundle.putString("Par1", data.getStringExtra("Par1"));
            bundle.putString("Par2", data.getStringExtra("Par2"));
            bundle.putString("Par3", data.getStringExtra("Par3"));
            bundle.putString("Par4", data.getStringExtra("Par4"));

            //Forward players totalscore
            bundle.putInt("Totalscore1", data.getIntExtra("Totalscore1", 0));
            bundle.putInt("Totalscore2", data.getIntExtra("Totalscore2", 0));
            bundle.putInt("Totalscore3", data.getIntExtra("Totalscore3", 0));
            bundle.putInt("Totalscore4", data.getIntExtra("Totalscore4", 0));

            newFragment.setArguments(bundle);
        }
        getSupportFragmentManager().popBackStackImmediate(newFragment.getClass().getName(), 0);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, newFragment);
        ft.addToBackStack(newFragment.getClass().getName());
        ft.commit();
        navigationView.setCheckedItem(R.id.nav_game);
    }

    private void replaceFragment (Fragment fragment){
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped){
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }


    public void finishGame(String leagueId, String leagueName){
        startLeagueFragment();
        Intent i = new Intent(this, GameLog.class);


        i.putExtra("LeagueId", leagueId);
        i.putExtra("LeagueName", leagueName);
        startActivityForResult(i, 555);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == 555){
            if(resultCode==RESULT_OK){
                startGameFragment(data);
            }
        }
    }

    public void createLeague(){
        Intent i = new Intent(this, LeagueNewActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.menu_add:
                createLeague();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
