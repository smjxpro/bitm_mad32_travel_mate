package pro.smjx.travelmate.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pro.smjx.travelmate.FINALS;
import pro.smjx.travelmate.GoogleApiSimplifier;
import pro.smjx.travelmate.R;
import pro.smjx.travelmate.Preferences;
import pro.smjx.travelmate.fragments.CurrentWeatherFragment;
import pro.smjx.travelmate.fragments.WeatherForecastFragment;
import pro.smjx.travelmate.interfaces.ServiceHelperInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Preferences preferences;
    private FusedLocationProviderClient providerClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private int getAutoLocation;
    private int searching = 0;
    private boolean dialoguePending;

    private void restartActivity() {
        startActivity(new Intent(this, WeatherActivity.class).putExtra("search", 1));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        dialoguePending = false;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            onBackPressed();
        }
        preferences = new Preferences(this);

        viewPager = findViewById(R.id.viewPager);

        tabLayout = findViewById(R.id.tabLayout);

        getAutoLocation = 1;

        Intent intent = getIntent();
        if (intent.getAction() != null) {
            try {
                if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
                    String query = intent.getStringExtra(SearchManager.QUERY);

                    searching++;
                    Retrofit googleRetrofit = new Retrofit.Builder()
                            .baseUrl(preferences.GOOGLE_BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    ServiceHelperInterface googleService = googleRetrofit.create(ServiceHelperInterface.class);
                    String url = String.format("json?address=%s&key=%s", query, preferences.GOOGLE_API_KEY);
                    Call<GoogleApiSimplifier> apiSimplifierCall = googleService.getGoogleSimplifier(url);

                    apiSimplifierCall.enqueue(new Callback<GoogleApiSimplifier>() {
                        @Override
                        public void onResponse(Call<GoogleApiSimplifier> call, Response<GoogleApiSimplifier> response) {
                            if (response.code() == 200) {
                                GoogleApiSimplifier apiSimplifier = response.body();

                                if (apiSimplifier.getStatus().equals("OK")) {
                                    double lat = apiSimplifier.getResults().get(0).getGeometry().getLocation().getLat();
                                    double longgit = apiSimplifier.getResults().get(0).getGeometry().getLocation().getLng();

                                    preferences.setLatitude(lat);
                                    preferences.setLongitude(longgit);

                                    loadFragmentsOfViewPager();
                                } else {
                                    Toast.makeText(WeatherActivity.this, getString(R.string.could_not_get_city), Toast.LENGTH_SHORT).show();
                                    loadFragmentsOfViewPager();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<GoogleApiSimplifier> call, Throwable t) {

                        }
                    });

                    getAutoLocation = 0;
                }
            } catch (NullPointerException e) {
                askPerm();
                //nullpointer
            }
        }
        if (intent.getIntExtra("search", 0) > 0) {
            getAutoLocation = 0;
        }

        if (getAutoLocation > 0) {
            providerClient = LocationServices.getFusedLocationProviderClient(this);
            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        askPerm();
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        ///location update sequencly
                    }
                }
            };

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                askPerm();
                return;
            }

            providerClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location == null) {
                        askPerm();
                        return;
                    }
                    preferences.setLatitude(location.getLatitude());
                    preferences.setLongitude(location.getLongitude());
                }
            });
            providerClient.requestLocationUpdates(locationRequest, locationCallback, null);
            restartActivity();
        }

        if (searching == 0) {
            loadFragmentsOfViewPager();
        }
    }

    private void loadFragmentsOfViewPager() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText(R.string.current));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.forecast));

        viewPager.setAdapter(new CustomPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.setCurrentItem(preferences.getTabPosition());
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                preferences.setTabPosition(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.option_menu, menu);

        SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.searchMenu).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem celsiusItem = menu.findItem(R.id.celsiusMenu);
        MenuItem fahrenheitItem = menu.findItem(R.id.fahrenheitMenu);
        switch (preferences.getTempUnit()) {
            case "imperial":
                celsiusItem.setVisible(true);
                fahrenheitItem.setVisible(false);
                break;
            default:
                celsiusItem.setVisible(false);
                fahrenheitItem.setVisible(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exitMenu:
                onBackPressed();
                break;
            case R.id.fahrenheitMenu:
                preferences.setTempUnit("f");
                break;
            default:
                preferences.setTempUnit("c");
                break;
        }
        if (item.getItemId() != R.id.exitMenu)
            restartActivity();
        return true;
    }

    private class CustomPagerAdapter extends FragmentPagerAdapter {

        private int totalTab;

        public CustomPagerAdapter(FragmentManager fm, int totalTab) {
            super(fm);
            this.totalTab = totalTab;
        }

        @Override
        public int getCount() {
            return totalTab;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CurrentWeatherFragment();
                case 1:
                    return new WeatherForecastFragment();
                default:
                    return null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("calling", "onRequestPermissionsResult: calling");
        showDialogue();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void askPerm() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    private void showDialogue() {
        dialoguePending = true;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.note_c).setMessage(R.string.turn_on_location)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(WeatherActivity.this, DrawerActivity.class).putExtra("fragment_name", FINALS.SHOW_DRAWER));
                        finish();
                    }
                }).setCancelable(false).setIcon(R.drawable.warn).show();
    }
}
