package pro.smjx.travelmate;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class AppHelper extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
