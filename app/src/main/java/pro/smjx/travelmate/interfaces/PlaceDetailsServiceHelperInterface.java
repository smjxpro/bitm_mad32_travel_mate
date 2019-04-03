package pro.smjx.travelmate.interfaces;


import pro.smjx.travelmate.placedetails.PlaceDetailsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface PlaceDetailsServiceHelperInterface {
    @GET
    Call<PlaceDetailsResponse> getPlaceDetails(@Url String urlString);
}
