package pro.smjx.travelmate.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import pro.smjx.travelmate.FINALS;
import pro.smjx.travelmate.GeofenceTransitionsIntentService;
import pro.smjx.travelmate.R;
import pro.smjx.travelmate.direction.DirectionResponse;
import pro.smjx.travelmate.direction.Route;
import pro.smjx.travelmate.direction.Step;
import pro.smjx.travelmate.interfaces.DirectionServiceHelperInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private boolean dialoguePending, zooomset, showingRoute;
    private GoogleMap mMap;
    private GoogleMapOptions mapOptions;
    private LatLng userClickdLoc, tempUserClickdLoc, latLng, tempLatLng, cameraPos;
    private float focus;
    private List<Route> routes;
    private List<String> allItems = new ArrayList<>();
    private FusedLocationProviderClient providerClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private DirectionServiceHelperInterface directionService;
    private Retrofit retrofit;
    private double lat, lng;
    private GeofencingClient geofencingClient;
    private List<Geofence> geofences = new ArrayList<>();
    private PendingIntent pendingIntent = null;
    private boolean doNothing, backToHome;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), 789);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofences);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private void modifyGeofence(boolean add) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (add) {
            geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent());
        } else {
            geofencingClient.removeGeofences(getGeofencePendingIntent());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        doNothing = false;
        dialoguePending = false;
        showingRoute = false;
        lat = 23.7509;
        lng = 90.3935;
        focus = 15;
        zooomset = false;
        backToHome = true;
        Intent intent = getIntent();
        if (intent.getStringExtra("get_lat") != null)
            if (!intent.getStringExtra("get_lat").isEmpty()) {
                backToHome = false;
                userClickdLoc = new LatLng(Double.parseDouble(intent.getStringExtra("get_lat")), Double.parseDouble(intent.getStringExtra("get_lng")));
            }
        mapOptions = new GoogleMapOptions();
        geofencingClient = LocationServices.getGeofencingClient(this);
        mapOptions.zoomControlsEnabled(true);
        retrofit = new Retrofit.Builder()
                .baseUrl(FINALS.MAPS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        directionService = retrofit.create(DirectionServiceHelperInterface.class);
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
                } else {
                    for (Location location : locationResult.getLocations()) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        onMapReady(mMap);
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askPerm();
            return;
        }
        if (!dialoguePending) {
            providerClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location == null) {
                        askPerm();
                        return;
                    } else {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        onMapReady(mMap);
                    }
                }
            });
            providerClient.requestLocationUpdates(locationRequest, locationCallback, null);
            SupportMapFragment mapFragment = SupportMapFragment.newInstance(mapOptions);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment);
            fragmentTransaction.commit();
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 789 && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            userClickdLoc = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
            showingRoute = false;
            dialoguePending = false;
            tempLatLng = null;
            Log.e("cllchk", "onActivityResult: ");
            onMapReady(mMap);
        }
    }

    private void askPerm() {
        if (!permGranted())
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    private boolean permGranted() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return false;
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e("calling", "onRequestPermissionsResult: calling");
        showDialogue();
    }

    private void showDialogue() {
        dialoguePending = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.note_c).setMessage(R.string.turn_on_location)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MapsActivity.this, DrawerActivity.class).putExtra("fragment_name", FINALS.SHOW_DRAWER));
                        finish();
                    }
                }).setCancelable(false).setIcon(R.drawable.warn).show();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        if (userClickdLoc != null) {
            if (tempUserClickdLoc != null)
                if (userClickdLoc == tempUserClickdLoc)
                    doNothing = true;
        }

        mMap = googleMap;
        if (!dialoguePending && !showingRoute) {
            if (mMap != null) {
                latLng = new LatLng(lat, lng);
                if (latLng.equals(tempLatLng) && doNothing) {
                } else {
                    tempLatLng = latLng;
                    focus = 15;

                    mMap.clear();
                    if (zooomset) {
                        focus = mMap.getCameraPosition().zoom;
                        cameraPos = mMap.getCameraPosition().target;
                    }

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                    if (userClickdLoc == null)
                        cameraPos = latLng;
                    else
                        tempUserClickdLoc = userClickdLoc;
                    if (cameraPos == null)
                        cameraPos = latLng;

                    mMap.addMarker(new MarkerOptions().position(latLng).title("Your position")).setDraggable(true);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPos, focus));
                    zooomset = true;
                    if (userClickdLoc != null) {
                        PolylineOptions polylineOptions = new PolylineOptions().add(latLng).add(userClickdLoc).width(2);
                        Polyline mainPoly = mMap.addPolyline(polylineOptions);
                        mainPoly.setTag("Distance: " + distance(latLng, userClickdLoc, true));
                        String callUrl = String.format("directions/json?origin=%f,%f&alternatives=true&destination=%f,%f&key=%s", latLng.latitude, latLng.longitude, userClickdLoc.latitude, userClickdLoc.longitude, FINALS.MAPS_API_KEY);
                        Call<DirectionResponse> call = directionService.getDirections(callUrl);
                        call.enqueue(new Callback<DirectionResponse>() {
                            @Override
                            public void onResponse(Call<DirectionResponse> call, Response<DirectionResponse> response) {
                                if (response.code() == 200) {
                                    DirectionResponse directionResponse = response.body();
                                    if (directionResponse.getStatus().equals("OK")) {
                                        routes = directionResponse.getRoutes();
                                        for (int i = 0; i < routes.size(); i++) {
                                            List<Step> steps = routes.get(i).getLegs().get(0).getSteps();
                                            for (int stI = 0; stI < steps.size(); stI++) {
                                                LatLng start = new LatLng(steps.get(stI).getStartLocation().getLat(), steps.get(stI).getStartLocation().getLng());
                                                LatLng end = new LatLng(steps.get(stI).getEndLocation().getLat(), steps.get(stI).getEndLocation().getLng());

                                                Polyline polyline = mMap.addPolyline(new PolylineOptions()
                                                        .add(start)
                                                        .add(end)
                                                        .color(getCurrentColor(i))
                                                        .width(5));
                                                polyline.setTag(Html.fromHtml(steps.get(stI).getHtmlInstructions()));
                                            }
                                        }
                                    } else {
                                        Toast.makeText(MapsActivity.this, R.string.error_occured, Toast.LENGTH_SHORT);
                                    }
                                } else {
                                    Toast.makeText(MapsActivity.this, R.string.can_not_download, Toast.LENGTH_SHORT);
                                }
                            }

                            @Override
                            public void onFailure(Call<DirectionResponse> call, Throwable t) {
                                Toast.makeText(MapsActivity.this, R.string.error_occured, Toast.LENGTH_SHORT);
                            }
                        });
                        mMap.addMarker(new MarkerOptions().position(userClickdLoc).title("Targeted place"));
                        CircleOptions circleOptions = new CircleOptions()
                                .center(userClickdLoc)
                                .radius(100)
                                .fillColor(0x434644ad)
                                .strokeColor(0x434644ad)
                                .strokeWidth(5);
                        mMap.addCircle(circleOptions);
                    }
                    mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(LatLng ulatLng) {
                            tempLatLng = null;
                            userClickdLoc = ulatLng;
                            showingRoute = false;
                            dialoguePending = false;
                            Geofence geofence = new Geofence.Builder()
                                    .setCircularRegion(userClickdLoc.latitude, userClickdLoc.longitude, 100)
                                    .setExpirationDuration(86400000)
                                    .setRequestId("targeted place")
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                                    .build();
                            geofences.clear();
                            geofences.add(geofence);
                            modifyGeofence(false);
                            modifyGeofence(true);
                            if (Integer.parseInt(distance(latLng, userClickdLoc, false)) > 100)
                                Toast.makeText(MapsActivity.this, "Direct distance:" + distance(latLng, userClickdLoc, true), Toast.LENGTH_SHORT).show();
                            onMapReady(mMap);
                        }
                    });
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            showRoutes();
                        }
                    });
                }
            }
            Log.e("mapready", "map ready: location");
        }
    }

    private int getCurrentColor(int i) {
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(255, 0, 0)); //red
        colors.add(Color.rgb(244, 164, 66)); //orange
        colors.add(Color.rgb(65, 244, 121)); //lime
        colors.add(Color.rgb(0, 0, 255)); //blue
        colors.add(Color.rgb(252, 27, 185)); //pink
        colors.add(Color.rgb(0, 255, 0)); //green
        colors.add(Color.rgb(123, 20, 158)); //violet
        if (colors.size() > i)
            return colors.get(i);
        return colors.get(0);
    }

    private String getCurrentColorName(int i) {
        List<String> names = new ArrayList<>();
        names.add("Red");
        names.add("Orange");
        names.add("Lime");
        names.add("Blue");
        names.add("Pink");
        names.add("Green");
        names.add("Violet");
        if (names.size() > i)
            return names.get(i);
        return names.get(0);
    }

    private void showRouteOf(int which) {
        if (routes.size() > which) {
            List<Step> dStep = routes.get(which).getLegs().get(0).getSteps();
            String msg = "";
            int stc = 0;
            for (Step s : dStep) {
                stc++;
                msg = msg + " Step " + stc + ": (" + m2km(s.getDistance().getValue()) + ")\n" + Html.fromHtml(s.getHtmlInstructions()) + "\n------\n";
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getCurrentColorName(which) + " route (" + m2km(routes.get(which).getLegs().get(0).getDistance().getValue()) + ")")
                    .setMessage(msg)
                    .setNegativeButton("Close", null)
                    .show();
        }
    }

    private void showRoutes() {
        if (routes == null)
            return;
        showingRoute = true;
        if (routes.size() == 1) {
            showRouteOf(0);
        } else if (routes.size() > 1) {
            allItems.clear();
            int counter;
            for (int i = 0; i < routes.size(); i++) {
                counter = i + 1;
                allItems.add("Route-" + counter + ": " + getCurrentColorName(i) + " color (" + m2km(routes.get(i).getLegs().get(0).getDistance().getValue()) + ")");
            }

            final CharSequence[] charSequenceItems = allItems.toArray(new CharSequence[allItems.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Routes:")
                    .setItems(charSequenceItems, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showRouteOf(which);
                        }
                    })
                    .setNegativeButton("Close", null)
                    .setCancelable(false)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            showingRoute = false;
                        }
                    })
                    .create();
            builder.show();
        }
    }

    public String m2km(int m) {
        String rKm = "";
        if (m < 1000)
            rKm = m + " m";
        else {
            int rest = m % 1000;
            int km = (m - rest) / 1000;
            rKm = km + " km";
            if (rest > 99) {
                char ss = String.valueOf(rest).charAt(0);
                rKm = km + "." + ss + " km";
            }
        }
        return rKm;
    }

    public String distance(LatLng latLng1, LatLng latLng2, boolean showUnit) {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(latLng1.latitude - latLng2.latitude);
        double lngDiff = Math.toRadians(latLng1.longitude - latLng2.longitude);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(latLng1.latitude)) * Math.cos(Math.toRadians(latLng2.latitude)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        int meterConversion = 1609;
        if (showUnit)
            return m2km((int) (distance * meterConversion));
        else
            return String.valueOf((int) (distance * meterConversion));
    }

    @Override
    public void onBackPressed() {
        if (backToHome)
            startActivity(new Intent(this, DrawerActivity.class));
        finish();
    }
}
