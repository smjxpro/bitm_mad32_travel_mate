package pro.smjx.travelmate.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import pro.smjx.travelmate.R;
import pro.smjx.travelmate.Utilities;
import pro.smjx.travelmate.events.Event;

public class EventAdapter extends ArrayAdapter<Event> {
    private Context context;
    private TextView nameTv, createdOnValTv, startsOnValTv, daysLeftTv;
    private Date processDate = new Date();
    private SimpleDateFormat dateFormat;
    private List<Event> events;

    public EventAdapter(@NonNull Context context, List<Event> events) {
        super(context, R.layout.events_layout, events);
        this.context = context;
        this.events = events;
        dateFormat = new SimpleDateFormat("MMM dd yyyy");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.events_layout, parent, false);
        nameTv = convertView.findViewById(R.id.nameTv);
        createdOnValTv = convertView.findViewById(R.id.createdOnValTv);
        startsOnValTv = convertView.findViewById(R.id.startsOnValTv);
        daysLeftTv = convertView.findViewById(R.id.daysLeftTv);

        nameTv.setText(events.get(position).getName());

        int day = Utilities.getDaysLeft(events.get(position).getDepartureDate());
        String daysLeft;
        if (events.get(position).getEndingDate() > 0) {
            daysLeft = context.getString(R.string.finished);
        } else {
            if (day == 0)
                daysLeft = context.getString(R.string.running);
            else if (day < 2)
                daysLeft = context.getString(R.string.tomorrow);
            else
                daysLeft = day + " " + context.getString(R.string.days) + " " + context.getString(R.string.left);
        }

        daysLeftTv.setText(daysLeft);

        processDate.setTime(events.get(position).getDepartureDate());
        startsOnValTv.setText(dateFormat.format(processDate));

        processDate.setTime(events.get(position).getCreatingDate());
        createdOnValTv.setText(dateFormat.format(processDate));
        return convertView;
    }
}
