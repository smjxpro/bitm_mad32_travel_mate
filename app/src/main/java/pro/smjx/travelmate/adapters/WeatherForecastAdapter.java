package pro.smjx.travelmate.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import pro.smjx.travelmate.R;
import pro.smjx.travelmate.Preferences;
import pro.smjx.travelmate.WeatherUtility;
import pro.smjx.travelmate.weather.WeatherForecast;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.CustomViewHolder> {

    Context context;
    Preferences preferences;
    int totalItem;
    List<Integer> positions = new ArrayList<>();
    private WeatherForecast weatherForecast;

    public WeatherForecastAdapter(Context context, WeatherForecast weatherForecast) {
        this.context = context;
        this.weatherForecast = weatherForecast;
        this.totalItem = weatherForecast.getList().size();

        String date = "";
        int position = 0;
        for (WeatherForecast.NList dt : weatherForecast.getList()) {
            if (date.equals(WeatherUtility.getDateFromMSForecast(dt.getDt()))) {
                positions.add(position);
            } else {
                date = WeatherUtility.getDateFromMSForecast(dt.getDt());
            }
            position++;
        }
        preferences = new Preferences(context);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.date_adapter_view, parent, false);
        CustomViewHolder holder = new CustomViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {

        holder.dateFormTv.setVisibility(View.GONE);
        boolean found = false;
        for (int chk : positions) {
            if (chk == position) {
                found = true;
            }
        }
        if (!found) {
            holder.dateFormTv.setText(WeatherUtility.getDateFromMSForecast(weatherForecast.getList().get(position).getDt()));
            holder.dateFormTv.setVisibility(View.VISIBLE);
        }

        holder.maximumTv.setText(context.getText(R.string.max) + " " + String.valueOf(weatherForecast.getList().get(position).getMain().getTempMax()) + context.getText(R.string.degree) + preferences.getTempUnitSign());
        holder.timeTv.setText(context.getText(R.string.min) + " " + String.valueOf(weatherForecast.getList().get(position).getMain().getTempMin()) + context.getText(R.string.degree) + preferences.getTempUnitSign());

        holder.dateTv.setText(WeatherUtility.getTimeFromMSForecast(weatherForecast.getList().get(position).getDt()));


        for (WeatherForecast.Weather w : weatherForecast.getList().get(position).getWeather()) {
            Picasso.get().load(Uri.parse("https://openweathermap.org/img/w/" + w.getIcon() + ".png")).into(holder.imageIv);
            holder.weatherTv.setText(w.getDescription());
        }

    }

    @Override
    public int getItemCount() {
        return totalItem;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView imageIv;
        TextView dateFormTv;
        TextView dateTv;
        TextView weatherTv;
        TextView maximumTv;
        TextView timeTv;

        public CustomViewHolder(View itemView) {
            super(itemView);

            imageIv = itemView.findViewById(R.id.imageIv);
            dateFormTv = itemView.findViewById(R.id.dateFormTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            maximumTv = itemView.findViewById(R.id.maximumTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            weatherTv = itemView.findViewById(R.id.weatherTv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });


        }
    }

}
