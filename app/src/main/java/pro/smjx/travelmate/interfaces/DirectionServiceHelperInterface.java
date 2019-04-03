package pro.smjx.travelmate.interfaces;


import pro.smjx.travelmate.direction.DirectionResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface DirectionServiceHelperInterface {
    @GET
    Call<DirectionResponse> getDirections(@Url String urlString);

}
