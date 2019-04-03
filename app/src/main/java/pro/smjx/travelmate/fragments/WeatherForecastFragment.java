package pro.smjx.travelmate.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import pro.smjx.travelmate.R;
import pro.smjx.travelmate.Preferences;
import pro.smjx.travelmate.adapters.WeatherForecastAdapter;
import pro.smjx.travelmate.interfaces.ServiceHelperInterface;
import pro.smjx.travelmate.weather.WeatherForecast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherForecastFragment extends Fragment {

    private ServiceHelperInterface service;
    private Preferences preferences;
    private RecyclerView recyclerView;
    private WeatherForecast weatherForecast;

    public WeatherForecastFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_weather_forecast, container, false);

        preferences = new Preferences(getActivity());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(preferences.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ServiceHelperInterface.class);

        String url = String.format("forecast?lat=%f&lon=%f&units=%s&appid=%s&cnt=%d", preferences.getLatitude(), preferences.getLongitude(), preferences.getTempUnit(), preferences.OWM_API_KEY, preferences.NUMBER_OF_FORECAST_DATA);

        Log.e("FR", url);
        Call<WeatherForecast> weatherCall = service.getWeatherForecast(url);

        weatherCall.enqueue(new Callback<WeatherForecast>() {
            @Override
            public void onResponse(Call<WeatherForecast> call, Response<WeatherForecast> response) {
                if (response.code() == 200) {

                    weatherForecast = response.body();
                    WeatherForecastAdapter adapter = new WeatherForecastAdapter(getActivity(), weatherForecast);
                    recyclerView = v.findViewById(R.id.recyclerView);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                } else {
                    Toast.makeText(getActivity(), getString(R.string.could_not_download_forecast), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherForecast> call, Throwable t) {
                Toast.makeText(getActivity(), getString(R.string.failed_to_download_forecast), Toast.LENGTH_SHORT).show();

            }
        });

        return v;
    }

}
