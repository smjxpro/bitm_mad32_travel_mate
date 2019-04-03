package pro.smjx.travelmate.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import pro.smjx.travelmate.FINALS;
import pro.smjx.travelmate.MapItems;
import pro.smjx.travelmate.R;
import pro.smjx.travelmate.interfaces.NearbyPlacesServiceInterface;
import pro.smjx.travelmate.nearbyplaces.NearbyPlaceScopes;
import pro.smjx.travelmate.nearbyplaces.NearbyPlacesResponse;
import pro.smjx.travelmate.nearbyplaces.PlacesNearby;
import pro.smjx.travelmate.nearbyplaces.RadiusNearby;
import pro.smjx.travelmate.nearbyplaces.Result;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NearbyPlacesActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Spinner placeSp, rangeSp;
    private List<String> places, radiuses;
    private String selectedPlace, selectedRadius, photoUrl;
    private GoogleMap map;
    private GoogleMapOptions mapOptions;
    private GeoDataClient geoDataClient;
    private PlaceDetectionClient placeDetectionClient;
    private ClusterManager<MapItems> clusterManager;
    private NearbyPlacesServiceInterface service;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient providerClient;
    private double lat, lng;
    private List<Result> results = new ArrayList<>();
    private List<String> allNames = new ArrayList<>();
    private LatLng latLng;
    private FrameLayout mapContainer;
    private Retrofit retrofit;
    private List<NearbyPlaceScopes> nearbyPlaceScopes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_places);
        placeSp = findViewById(R.id.placeSp);
        rangeSp = findViewById(R.id.rangeSp);
        mapContainer = findViewById(R.id.mapContainer);
        retrofit = new Retrofit.Builder()
                .baseUrl(FINALS.MAPS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        lat = 23.7509;
        lng = 90.3935;
        selectedRadius = "";
        selectedPlace = "";
        places = new ArrayList<>();
        radiuses = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askPerm();
            return;
        }
        for (PlacesNearby p : getPlaces())
            places.add(p.getPlaceName());
        for (RadiusNearby r : getRadius())
            radiuses.add(r.getRadiusTitle());
        ArrayAdapter<String> placeAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_row,
                R.id.textView,
                places);
        placeSp.setAdapter(placeAdapter);
        ArrayAdapter<String> radiusAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_row,
                R.id.textView,
                radiuses);
        rangeSp.setAdapter(radiusAdapter);
        placeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPlace = getPlaces().get(position).getPlaceCode();
                if (!selectedPlace.isEmpty() && !selectedRadius.isEmpty())
                    findPlaces(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        rangeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRadius = getRadius().get(position).getRadius();
                if (!selectedPlace.isEmpty() && !selectedRadius.isEmpty())
                    findPlaces(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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

    private List<RadiusNearby> getRadius() {
        List<RadiusNearby> allRadius = new ArrayList<>();
        allRadius.add(new RadiusNearby("100 m", "100"));
        allRadius.add(new RadiusNearby("200 m", "200"));
        allRadius.add(new RadiusNearby("300 m", "300"));
        allRadius.add(new RadiusNearby("500 m", "500"));
        allRadius.add(new RadiusNearby("1 km", "1000"));
        allRadius.add(new RadiusNearby("2 km", "2000"));
        allRadius.add(new RadiusNearby("5 km", "5000"));
        allRadius.add(new RadiusNearby("10 km", "10000"));
        allRadius.add(new RadiusNearby("20 km", "20000"));
        allRadius.add(new RadiusNearby("50 km", "50000"));
        return allRadius;
    }

    private List<PlacesNearby> getPlaces() {
        List<PlacesNearby> allPlaces = new ArrayList<>();
        allPlaces.add(new PlacesNearby("ATM", "atm"));
        allPlaces.add(new PlacesNearby("Bank", "bank"));
        allPlaces.add(new PlacesNearby("Cafe", "cafe"));
        allPlaces.add(new PlacesNearby("Hospital", "hospital"));
        allPlaces.add(new PlacesNearby("Mosque", "mosque"));
        allPlaces.add(new PlacesNearby("Park", "park"));
        allPlaces.add(new PlacesNearby("Police Stations", "police"));
        allPlaces.add(new PlacesNearby("Restaurant", "restaurant"));
        return allPlaces;
    }

    public void findPlaces(View view) {
        if (selectedPlace.isEmpty())
            showWarnDialogue("Please select place type");
        else if (selectedRadius.isEmpty())
            showWarnDialogue("Please select range");
        else {


            locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1000000);
            locationRequest.setFastestInterval(500000);
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
                        }
                    }
                }
            };


            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                askPerm();
                return;
            }
            providerClient = LocationServices.getFusedLocationProviderClient(this);
            providerClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location == null) {
                        askPerm();
                        return;
                    } else {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    }
                }
            });

            providerClient.requestLocationUpdates(locationRequest, locationCallback, null);


            mapOptions = new GoogleMapOptions();
            mapOptions.zoomControlsEnabled(true);
            SupportMapFragment mapFragment = SupportMapFragment.newInstance(mapOptions);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mapContainer, mapFragment);
            ft.commit();
            mapFragment.getMapAsync(this);
        }
    }

    private void showWarnDialogue(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error)
                .setIcon(R.drawable.warn)
                .setMessage(msg)
                .setNegativeButton("Close", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, DrawerActivity.class));
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        clusterManager = new ClusterManager<MapItems>(this, map);
        map.setOnMarkerClickListener(clusterManager);
        map.setOnCameraIdleListener(clusterManager);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showAllNamesInDialogue();
            }
        });

        latLng = new LatLng(lat, lng);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(Double.parseDouble(selectedRadius))
                .fillColor(0x434644ad)
                .strokeColor(0x434644ad)
                .strokeWidth(5);
        map.addCircle(circleOptions);


        service = retrofit.create(NearbyPlacesServiceInterface.class);
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        AlertDialog dialog = build.setTitle("Loading...")
                .setMessage("Loading...\nPlease wait...")
                .setCancelable(false).create();
        mapContainer.setVisibility(View.VISIBLE);
        nearbyPlaceScopes.clear();
        addOtherPlaces("", dialog);
        dialog.show();

    }

    private void showAllNamesInDialogue() {
        if (nearbyPlaceScopes == null)
            return;
        if (nearbyPlaceScopes.size() == 0)
            return;

        allNames.clear();
        int counter = 0;
        for (int i = 0; i < nearbyPlaceScopes.size(); i++) {
            counter++;
            allNames.add(counter + ". " + nearbyPlaceScopes.get(i).getName() + "\n" + nearbyPlaceScopes.get(i).getAddress() + "\n");
        }
        CharSequence[] charSequenceItems = allNames.toArray(new CharSequence[allNames.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("All " + selectedPlace)
                .setCancelable(false)
                .setNegativeButton("Close", null)
                .setItems(charSequenceItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showFullDetailsOf(which);
                    }
                })
                .create();
        builder.show();
    }

    private void showFullDetailsOf(int which) {

        if (nearbyPlaceScopes.get(which) == null)
            showWarnDialogue("Error displaying this " + selectedPlace);
        else {
            Intent intent = new Intent(NearbyPlacesActivity.this, PlaceDetailsActivity.class);
            intent.putExtra("send_serial", nearbyPlaceScopes.get(which));
            intent.putExtra("selected", selectedPlace);
            startActivity(intent);
        }
    }

    private void addOtherPlaces(String nextToken, final AlertDialog dialog) {
        String callUrl = "";
        if (nextToken.isEmpty())
            callUrl = String.format("place/nearbysearch/json?location=%f,%f&radius=%s&type=%s&key=%s", latLng.latitude, latLng.longitude, selectedRadius, selectedPlace, FINALS.MAPS_API_KEY);
        else
            callUrl = String.format("place/nearbysearch/json?pagetoken=%s&key=%s", nextToken, FINALS.MAPS_API_KEY);

        Call<NearbyPlacesResponse> call = service.getNearbyPlaces(callUrl);
        call.enqueue(new Callback<NearbyPlacesResponse>() {
            @Override
            public void onResponse(Call<NearbyPlacesResponse> call, Response<NearbyPlacesResponse> response) {
                if (response.code() == 200) {
                    NearbyPlacesResponse nearbyPlacesResponse = response.body();
                    if (nearbyPlacesResponse.getStatus().equals("OK")) {
                        results.clear();
                        results = nearbyPlacesResponse.getResults();

                        String photoRef;
                        for (Result r : results) {
                            photoRef = "";
                            if (r.getPhotos() != null)
                                if (r.getPhotos().size() > 0)
                                    if (!r.getPhotos().get(0).getPhotoReference().isEmpty())
                                        photoRef = r.getPhotos().get(0).getPhotoReference();
                            nearbyPlaceScopes.add(new NearbyPlaceScopes(r.getId(), r.getPlaceId(), r.getVicinity(), r.getName(), photoRef, r.getGeometry().getLocation().getLat(), r.getGeometry().getLocation().getLng()));
                        }
                    } else {
                        dialog.dismiss();
                        showWarnDialogue("Nothing found!");
                    }

                    putPlacesOnMap();
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    showWarnDialogue("Error loading data!");
                }

            }

            @Override
            public void onFailure(Call<NearbyPlacesResponse> call, Throwable t) {
                dialog.dismiss();
                showWarnDialogue("Internet connection error!");
            }
        });
    }

    private void putPlacesOnMap() {
        List<MapItems> items = new ArrayList<>();
        for (int i = 0; i < nearbyPlaceScopes.size(); i++) {
            MapItems item = new MapItems(new LatLng(nearbyPlaceScopes.get(i).getLat(), nearbyPlaceScopes.get(i).getLng()), nearbyPlaceScopes.get(i).getName());
            items.add(item);
        }
        clusterManager.addItems(items);
        clusterManager.cluster();
    }
}
