package pro.smjx.travelmate;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pro.smjx.travelmate.events.Expense;

public abstract class Utilities {
    private static double exp;
    private static List<String> keys = new ArrayList<>();

    public static int getDaysLeft(long endTime) {
        int day;
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long difference = endTime - currentTime + 86400000;
        if (difference > 0) {
            day = (int) Math.ceil(difference / 86400000);
        } else
            day = 0;
        return day;
    }


    public static double getExpenses(final DatabaseReference eventRef) {
        exp = 0;
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                keys.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    keys.add(ds.getKey());
                    Log.e("key", "onDataChange: " + ds.getKey());
                }
                DatabaseReference newRef;
                for (String getFrmKey : keys) {
                    newRef = eventRef.child(getFrmKey);
                    newRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Expense expense = dataSnapshot.getValue(Expense.class);
                            exp = exp + expense.getAmount();
                            Log.e("str", "onDataChange: inner" + exp);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return exp;
    }

    public static void deleteEvent(Context context, String clickedItemId) {

        if (clickedItemId != null) {
            FirebaseUser user;
            DatabaseReference rootRef, userRef, eventItemRef;
            user = FirebaseAuth.getInstance().getCurrentUser();
            rootRef = FirebaseDatabase.getInstance().getReference();
            eventItemRef = rootRef.child(user.getUid()).child(FINALS.EVENTS).child(clickedItemId);
            eventItemRef.removeValue();
            Toast.makeText(context, R.string.event_deleted, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.error_occured, Toast.LENGTH_SHORT).show();

        }
    }
}
