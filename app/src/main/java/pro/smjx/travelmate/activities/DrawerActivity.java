package pro.smjx.travelmate.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pro.smjx.travelmate.FINALS;
import pro.smjx.travelmate.R;
import pro.smjx.travelmate.fragments.ChangeEmailFragment;
import pro.smjx.travelmate.fragments.ChangePassFragment;
import pro.smjx.travelmate.fragments.EventFragment;
import pro.smjx.travelmate.fragments.EditNameFragment;
import pro.smjx.travelmate.fragments.ProfileFragment;
import pro.smjx.travelmate.interfaces.ActivityHelperInterface;
import pro.smjx.travelmate.interfaces.DrawerHelperInterface;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ActivityHelperInterface, DrawerHelperInterface {

    private FirebaseUser user;
    private FirebaseAuth auth;
    private int frameLayoutId;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private MenuItem homeMenu;
    private boolean showHomeMenu;
    private boolean farFromOrigin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        setTitle(R.string.app_name);

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();
        if (user == null) {
            goHome();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        frameLayoutId = R.id.frameLayout;

        Intent intent = getIntent();
        int intentValue = intent.getIntExtra("fragment_name", 0);
        if (intentValue > 0) {
            farFromOrigin = true;
            showHomeMenu = true;
        } else {
            farFromOrigin = false;
            showHomeMenu = false;
        }
        switch (intentValue) {
            case FINALS.UPDATE_PROFILE:
                loadFragment(new EditNameFragment(), true);
                break;
            case FINALS.CHANGE_EMAIL:
                loadFragment(new ChangeEmailFragment(), true);
                break;
            case FINALS.CHANGE_PASS:
                loadFragment(new ChangePassFragment(), true);
                break;
            case FINALS.SHOW_DRAWER:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
                loadFragment(new EventFragment(), true);
                break;
            default:
                loadFragment(new EventFragment(), true);
                break;
        }
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View v = navigationView.getHeaderView(0);

        TextView userNameTv, userEmailTv;
        userNameTv = v.findViewById(R.id.userNameTv);
        userEmailTv = v.findViewById(R.id.userEmailTv);

        userNameTv.setText(user.getDisplayName());
        userEmailTv.setText(user.getEmail());

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        Intent intent = getIntent();
        int intentValue = intent.getIntExtra("fragment_name", 0);
        if (intentValue > 0) {
            farFromOrigin = true;
            showHomeMenu = true;
        } else {
            farFromOrigin = false;
            showHomeMenu = false;
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (farFromOrigin) {
            loadFragment(new EventFragment(), true);
            farFromOrigin = false;
        } else {

            showExitDialogue(getText(R.string.are_you_sure).toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        homeMenu = menu.findItem(R.id.menuHome);
        if (showHomeMenu)
            homeMenu.setVisible(true);
        else
            homeMenu.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        homeMenu.setVisible(true);
        switch (item.getItemId()) {
            case R.id.menuHome:
                loadFragment(new EventFragment(), true);
                homeMenu.setVisible(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.profile:
                loadFragment(new ProfileFragment(), true);
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.events:
                loadFragment(new EventFragment(), true);
                break;
            case R.id.weather:
                startActivity(new Intent(this, WeatherActivity.class));
                finish();
                break;
            case R.id.map:
                startActivity(new Intent(this, MapsActivity.class));
                finish();
                break;
            case R.id.nearby:
                startActivity(new Intent(this, NearbyPlacesActivity.class));
                finish();
                break;
            case R.id.logout:
                logout();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        goHome();
    }

    private void goHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void showExitDialogue(String exitConfirmMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(exitConfirmMsg);
        builder.setTitle(R.string.confirm);
        builder.setIcon(R.drawable.warn);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
            }
        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //
                        // goHome();
                    }
                })
                .setCancelable(true)
        ;
        builder.show();
    }

    @Override
    public void loadFragment(Fragment fragment, boolean replace) {
        farFromOrigin = true;

        if (user != null) {
            fm = getSupportFragmentManager();
            ft = fm.beginTransaction();
            if (replace)
                ft.replace(frameLayoutId, fragment);
            else
                ft.add(frameLayoutId, fragment);
            this.ft.commit();
        } else {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show();
            goToHome();
        }
    }

    @Override
    public void loadFragmentWithValue(Fragment fragment, boolean replace, Bundle bundle) {
        farFromOrigin = true;
        if (user != null) {

            fm = getSupportFragmentManager();
            ft = fm.beginTransaction();
            fragment.setArguments(bundle);
            if (replace)
                ft.replace(frameLayoutId, fragment);
            else
                ft.add(frameLayoutId, fragment);
            ft.commit();
        } else {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show();
            goToHome();
        }
    }

    @Override
    public void reloadActivity(int fragmentIntValue) {

        if (fragmentIntValue > 0)
            startActivity(new Intent(this, DrawerActivity.class).putExtra("fragment_name", fragmentIntValue));
        else
            startActivity(new Intent(this, DrawerActivity.class));
        finish();

    }

    @Override
    public void goToHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void confirmLoggedUser() {
        user = auth.getCurrentUser();
        if (user == null) {
            goHome();
        }
    }
}