package pro.smjx.travelmate;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    private Handler handler;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        int transitionType = event.getGeofenceTransition();
        List<Geofence> triggeringGeofences = event.getTriggeringGeofences();
        String transitionName = "";
        List<String> areas = new ArrayList<>();
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                transitionName = "entered";
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                transitionName = "exited";
                break;
        }

        for (Geofence g : triggeringGeofences) {
            areas.add(g.getRequestId());
        }

        final String fullMessage = "You have " + transitionName + " " + TextUtils.join(", ", areas);
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GeofenceTransitionsIntentService.this, fullMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
