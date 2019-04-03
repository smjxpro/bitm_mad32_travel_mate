package pro.smjx.travelmate.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import pro.smjx.travelmate.FINALS;
import pro.smjx.travelmate.R;
import pro.smjx.travelmate.adapters.ReviewAdapter;
import pro.smjx.travelmate.interfaces.PlaceDetailsServiceHelperInterface;
import pro.smjx.travelmate.nearbyplaces.NearbyPlaceScopes;
import pro.smjx.travelmate.placedetails.PlaceDetailsResponse;
import pro.smjx.travelmate.placedetails.Review;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceDetailsActivity extends AppCompatActivity {

    private ImageView imageIv;
    private TextView nameTv, addressTv, phoneTv, webTv, ratingTv, reviewTv, showAllTv;
    private NearbyPlaceScopes selectedPlaceScopes;
    private String selectedPlace, photoUrl, phoneNo, webAdd;
    private Retrofit retrofit;
    private TableRow callTr;
    private LinearLayout webLl;
    private List<Review> reviewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        retrofit = new Retrofit.Builder()
                .baseUrl(FINALS.MAPS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Intent intent = getIntent();
        selectedPlace = intent.getStringExtra("selected");
        selectedPlaceScopes = (NearbyPlaceScopes) intent.getSerializableExtra("send_serial");
        photoUrl = "";
        phoneNo = "";
        webAdd = "";
        if (selectedPlace.isEmpty() || selectedPlaceScopes == null) {
            showWarnDialogue("Something  is wrong", true);
        } else
            doWorksNow();
    }

    private void doWorksNow() {
        setTitle(selectedPlaceScopes.getName());
        imageIv = findViewById(R.id.imageIv);
        nameTv = findViewById(R.id.nameTv);
        addressTv = findViewById(R.id.addressTv);
        phoneTv = findViewById(R.id.phoneTv);
        webTv = findViewById(R.id.webTv);
        ratingTv = findViewById(R.id.ratingTv);
        callTr = findViewById(R.id.callTr);
        webLl = findViewById(R.id.webLl);
        reviewTv = findViewById(R.id.reviewTv);
        showAllTv = findViewById(R.id.showAllTv);

        nameTv.setText(selectedPlaceScopes.getName());
        addressTv.setText(selectedPlaceScopes.getAddress());
        if (!selectedPlaceScopes.getPhotoRef().isEmpty()) {
            photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=200&photoreference=" + selectedPlaceScopes.getPhotoRef() + "&key=" + FINALS.MAPS_API_KEY;
        }

        String callUrl = String.format("place/details/json?placeid=%s&key=%s", selectedPlaceScopes.getPlaceId(), FINALS.MAPS_API_KEY);
        Log.e("photourl", photoUrl);
        Log.e("errurl", callUrl);
        PlaceDetailsServiceHelperInterface placeDetailsService = retrofit.create(PlaceDetailsServiceHelperInterface.class);
        Call<PlaceDetailsResponse> call = placeDetailsService.getPlaceDetails(callUrl);
        call.enqueue(new Callback<PlaceDetailsResponse>() {
            @Override
            public void onResponse(Call<PlaceDetailsResponse> call, Response<PlaceDetailsResponse> response) {
                if (response.code() == 200) {
                    PlaceDetailsResponse placeDetailsResponse = response.body();
                    if (placeDetailsResponse == null)
                        showWarnDialogue("This place data is secret", true);
                    else {
                        if (photoUrl.isEmpty()) {
                            if (!placeDetailsResponse.getResult().getIcon().isEmpty())
                                photoUrl = placeDetailsResponse.getResult().getIcon();
                        }
                        if (!photoUrl.isEmpty())
                            Picasso.get().load(Uri.parse(photoUrl)).into(imageIv);
                        else
                            imageIv.setVisibility(View.GONE);
                        if (placeDetailsResponse.getResult().getFormattedPhoneNumber() != null) {
                            phoneNo = placeDetailsResponse.getResult().getInternationalPhoneNumber();
                            phoneTv.setText(placeDetailsResponse.getResult().getFormattedPhoneNumber());
                            callTr.setVisibility(View.VISIBLE);
                        }
                        if (placeDetailsResponse.getResult().getWebsite() != null) {
                            webAdd = placeDetailsResponse.getResult().getWebsite();
                            webTv.setText(placeDetailsResponse.getResult().getWebsite());
                            webLl.setVisibility(View.VISIBLE);
                        }


                        if (placeDetailsResponse.getResult().getFormattedAddress() != null)
                            addressTv.setText(placeDetailsResponse.getResult().getFormattedAddress());

                        if (placeDetailsResponse.getResult().getRating() != null) {
                            reviewList = placeDetailsResponse.getResult().getReviews();
                            ratingTv.setText(String.valueOf(placeDetailsResponse.getResult().getRating()));
                            reviewTv.setText(String.valueOf(reviewList.size()));
                        } else {
                            showAllTv.setVisibility(View.GONE);
                            ratingTv.setText("No ratings yet");
                            reviewTv.setText("No reviews yet");
                        }
                    }
                } else
                    showWarnDialogue("Error downloading data!", true);
            }

            @Override
            public void onFailure(Call<PlaceDetailsResponse> call, Throwable t) {
                showWarnDialogue("Data fetching failed", true);
            }
        });
    }

    private void showWarnDialogue(String msg, final boolean exit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please check")
                .setIcon(R.drawable.warn)
                .setMessage(msg)
                .setCancelable(false)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (exit)
                            finish();
                    }
                })
                .show();
    }

    public void callPhone(View view) {
        if (phoneNo.isEmpty())
            showWarnDialogue("Phone number not found for this place to call", false);
        else {

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNo));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                showWarnDialogue("No component found to make phone call!", false);
            }
        }
    }

    public void sendSms(View view) {
        if (phoneNo.isEmpty())
            showWarnDialogue("Phone number not found for this place to send message!", false);
        else {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phoneNo));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                showWarnDialogue("No component found to send sms!", false);
            }
        }
    }

    public void goToWeb(View view) {
        if (phoneNo.isEmpty())
            showWarnDialogue("Web address not found for this place to open!", false);
        else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(webAdd));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                showWarnDialogue("No component found to open web link!", false);
            }
        }
    }

    public void viewDirection(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("get_lat", String.valueOf(selectedPlaceScopes.getLat()));
        intent.putExtra("get_lng", String.valueOf(selectedPlaceScopes.getLng()));
        startActivity(intent);
    }

    public void seeReviews(View view) {
        if (reviewList.size() == 0)
            showWarnDialogue("No reviews found for this " + selectedPlace, false);
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AlertDialog dialog = builder.setTitle("Reviews of " + selectedPlaceScopes.getName())
                    .setIcon(R.drawable.review)
                    .setView(R.layout.review_list)
                    .setCancelable(false)
                    .setNegativeButton("Close", null)
                    .create();
            dialog.show();
            ListView listView = dialog.findViewById(R.id.listView);
            listView.setAdapter(new ReviewAdapter(this, reviewList));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (reviewList.get(position).getText() == null) {
                        showWarnDialogue("No comment attached on this review", false);
                    } else {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(PlaceDetailsActivity.this);
                        builder1.setTitle(reviewList.get(position).getAuthorName())
                                .setIcon(R.drawable.review)
                                .setMessage(reviewList.get(position).getText())
                                .setNegativeButton("Close", null)
                                .show();
                    }

                }
            });
        }
    }
}
