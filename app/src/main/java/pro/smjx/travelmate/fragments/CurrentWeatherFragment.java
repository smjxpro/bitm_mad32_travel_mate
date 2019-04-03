package pro.smjx.travelmate.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import pro.smjx.travelmate.R;
import pro.smjx.travelmate.Preferences;
import pro.smjx.travelmate.WeatherUtility;
import pro.smjx.travelmate.interfaces.ServiceHelperInterface;
import pro.smjx.travelmate.weather.CurrentWeather;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.Math.ceil;


/**
 * A simple {@link Fragment} subclass.
 */
public class CurrentWeatherFragment extends Fragment {

    private ServiceHelperInterface service;
    private Preferences preferences;


    public CurrentWeatherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_current_weather, container, false);

        final TextView degreeTv = v.findViewById(R.id.degreeTv);
        final TextView minTv = v.findViewById(R.id.minTv);
        final TextView maxTv = v.findViewById(R.id.maxTv);
        final TextView sunriseTv = v.findViewById(R.id.sunriseTv);
        final TextView sunsetTv = v.findViewById(R.id.sunSetTv);
        final TextView humidityTv = v.findViewById(R.id.humidityTv);
        final TextView pressureTv = v.findViewById(R.id.pressureTv);
        final TextView currentDateTv = v.findViewById(R.id.currentDateTv);
        final TextView currentDayTv = v.findViewById(R.id.currentDayTv);
        final TextView currentCityTv = v.findViewById(R.id.currentCityTv);
        final TextView weatherSituationTv = v.findViewById(R.id.weatherSituationTv);
        final ImageView weatherImageIv = v.findViewById(R.id.weatherImageIv);


        preferences = new Preferences(getActivity());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(preferences.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ServiceHelperInterface.class);

        String url = String.format("weather?lat=%f&lon=%f&units=%s&appid=%s", preferences.getLatitude(), preferences.getLongitude(), preferences.getTempUnit(), preferences.OWM_API_KEY);

        Call<CurrentWeather> weatherCall = service.getCurrentWeather(url);


        weatherCall.enqueue(new Callback<CurrentWeather>() {
            @Override
            public void onResponse(Call<CurrentWeather> call, Response<CurrentWeather> response) {
                if (response.code() == 200) {
                    try {
                        CurrentWeather currentWeather = response.body();
                        degreeTv.setText(String.valueOf((int) ceil(currentWeather.getMain().getTemp())) + getText(R.string.degree).toString()
                                + preferences.getTempUnitSign());
                        currentDateTv.setText(WeatherUtility.getDateFromMS(currentWeather.getDt()));
                        currentDayTv.setText(WeatherUtility.getDayFromMS(currentWeather.getDt()));
                        currentCityTv.setText(currentWeather.getName());


                        minTv.setText(String.valueOf(currentWeather.getMain().getTempMin()) + getText(R.string.degree).toString()
                                + preferences.getTempUnitSign());
                        maxTv.setText(String.valueOf(currentWeather.getMain().getTempMax()) + getText(R.string.degree).toString()
                                + preferences.getTempUnitSign());
                        humidityTv.setText(String.valueOf(currentWeather.getMain().getHumidity()) + getText(R.string.percent_sign));
                        pressureTv.setText(String.valueOf(currentWeather.getMain().getPressure()) + getText(R.string.pressure_sign));

                        sunriseTv.setText(WeatherUtility.getTimeFromMS(currentWeather.getSys().getSunrise()));
                        sunsetTv.setText(WeatherUtility.getTimeFromMS(currentWeather.getSys().getSunset()));

                        for (CurrentWeather.Weather w : currentWeather.getWeather()) {
                            weatherSituationTv.setText(w.getDescription());
                            Picasso.get().load(Uri.parse("https://openweathermap.org/img/w/" + w.getIcon() + ".png")).into(weatherImageIv);
                        }

                    } catch (NullPointerException e) {

                    }

                } else {
                    Toast.makeText(getActivity(), getText(R.string.can_not_download_weather), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CurrentWeather> call, Throwable t) {

                Toast.makeText(getActivity(), getText(R.string.failed_to_download_weather), Toast.LENGTH_SHORT).show();
            }
        });
        // Inflate the layout for this fragment
        return v;
    }
}
