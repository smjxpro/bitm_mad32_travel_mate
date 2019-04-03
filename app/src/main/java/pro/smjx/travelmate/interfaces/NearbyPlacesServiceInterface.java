package pro.smjx.travelmate.interfaces;


import pro.smjx.travelmate.nearbyplaces.NearbyPlacesResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;


public interface NearbyPlacesServiceInterface {
    @GET
    Call<NearbyPlacesResponse> getNearbyPlaces(@Url String urlString);

}

