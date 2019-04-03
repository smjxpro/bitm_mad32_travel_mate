package pro.smjx.travelmate.interfaces;

import pro.smjx.travelmate.GoogleApiSimplifier;
import pro.smjx.travelmate.weather.CurrentWeather;
import pro.smjx.travelmate.weather.WeatherForecast;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ServiceHelperInterface {

    @GET()
    Call<CurrentWeather> getCurrentWeather(@Url String urlString);

    @GET()
    Call<WeatherForecast> getWeatherForecast(@Url String urls);

    @GET()
    Call<GoogleApiSimplifier> getGoogleSimplifier(@Url String urls);
}
