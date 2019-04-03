package pro.smjx.travelmate.fragments;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import pro.smjx.travelmate.FINALS;
import pro.smjx.travelmate.R;
import pro.smjx.travelmate.events.Event;
import pro.smjx.travelmate.interfaces.ActivityHelperInterface;
import pro.smjx.travelmate.interfaces.FragmentHelperInterface;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditEventFragment extends Fragment implements FragmentHelperInterface {

    private FirebaseUser user;
    private DatabaseReference rootRef, eventRef;

    private Context context;
    private ActivityHelperInterface activityHelperInterface;
    private Button dateBtn, editBtn, cancelBtn;
    private TextView statusTv, errorTv;
    private long startDt, currentDt;
    private int currentDay, currentYear, currentMonth;
    private SimpleDateFormat simpleDateFormat;
    private EditText eventNameEt, budgetEt, deptEt, destEt;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        activityHelperInterface = (ActivityHelperInterface) context;
    }

    public EditEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final String eventId = this.getArguments().getString("id", "0");
        final View v = inflater.inflate(R.layout.fragment_edit_event, container, false);


        user = FirebaseAuth.getInstance().getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
        eventRef = rootRef.child(user.getUid()).child(FINALS.EVENTS).child(eventId);

        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final Event event = dataSnapshot.getValue(Event.class);

                if (event == null) {
                    Toast.makeText(getActivity(), R.string.event_not_found, Toast.LENGTH_SHORT).show();
                    activityHelperInterface.loadFragment(new EventFragment(), true);
                } else {
                    final Bundle bundle = new Bundle();
                    bundle.putString("id", event.getId());
                    startDt = event.getDepartureDate();
                    final Calendar calendar = Calendar.getInstance();
                    currentDt = calendar.getTimeInMillis() - 1;

                    if (event.getEndingDate() > 0) {
                        Toast.makeText(context, R.string.can_not_edit_expired_event, Toast.LENGTH_SHORT).show();
                        activityHelperInterface.loadFragmentWithValue(new EventDetailsFragment(), true, bundle);
                    } else {

                        statusTv = v.findViewById(R.id.statusTv);
                        errorTv = v.findViewById(R.id.errorTv);
                        dateBtn = v.findViewById(R.id.dateBtn);
                        editBtn = v.findViewById(R.id.editBtn);
                        cancelBtn = v.findViewById(R.id.cancelBtn);

                        eventNameEt = v.findViewById(R.id.eventNameEt);
                        deptEt = v.findViewById(R.id.deptEt);
                        destEt = v.findViewById(R.id.destEt);

                        budgetEt = v.findViewById(R.id.budgetEt);

                        simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");

                        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                        currentMonth = calendar.get(Calendar.MONTH);
                        currentYear = calendar.get(Calendar.YEAR);

                        startDt = event.getDepartureDate();
                        if (getActivity() != null)
                            getActivity().setTitle(event.getName());
                        calendar.setTimeInMillis(event.getDepartureDate());
                        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                        currentMonth = calendar.get(Calendar.MONTH);
                        currentYear = calendar.get(Calendar.YEAR);
                        eventNameEt.setText(event.getName());

                        deptEt.setText(event.getStartingLocation());
                        destEt.setText(event.getDestination());
                        budgetEt.setText(String.valueOf((int) event.getBudget()));
                        dateBtn.setText(simpleDateFormat.format(startDt));

                        editBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String name = eventNameEt.getText().toString();
                                String startLoc = deptEt.getText().toString();
                                String destination = destEt.getText().toString();
                                String budgetString = budgetEt.getText().toString();
                                if (budgetString.isEmpty()) budgetString = "0";
                                double budget = Double.parseDouble(budgetString);
                                if (name.isEmpty()) {
                                    showWarnDialogue("Event name can not be empty");
                                } else if (startLoc.isEmpty()) {
                                    showWarnDialogue("Please set departure location");
                                } else if (destination.isEmpty()) {
                                    showWarnDialogue("Please set destination");
                                } else if (budget <= 0) {
                                    showWarnDialogue("Budget can't be negative");
                                } else {
                                    errorOrStatus(getString(R.string.creating), false);
                                    Event event = new Event(eventId, name, startLoc, destination, startDt, currentDt, 0, budget, 0);
                                    eventRef.setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getActivity(), R.string.edited_successfully, Toast.LENGTH_SHORT).show();
                                                activityHelperInterface.loadFragmentWithValue(new EventDetailsFragment(), true, bundle);
                                            } else {
                                                errorOrStatus(getString(R.string.error_occured), true);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            errorOrStatus(e.getMessage(), true);
                                        }
                                    });
                                }
                            }
                        });
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                activityHelperInterface.loadFragmentWithValue(new EventDetailsFragment(), true, bundle);
                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(getActivity(), data);
            Toast.makeText(getActivity(), place.getName(), Toast.LENGTH_SHORT).show();
            if (requestCode == 337) {

            } else {

            }
        }
    }

    private void showWarnDialogue(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Please check")
                .setIcon(R.drawable.warn)
                .setMessage(msg)
                .setNegativeButton("Close", null)
                .show();
    }

    private void getLocationDetails(int i) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(getActivity()), i);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void errorOrStatus(String text, boolean error) {
        if (error) {
            errorTv.setText(text);
            errorTv.setVisibility(View.VISIBLE);
            statusTv.setVisibility(View.GONE);
        } else {
            statusTv.setText(text);
            statusTv.setVisibility(View.VISIBLE);
            errorTv.setVisibility(View.GONE);
        }
    }

    private void pickADate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar tempCalendar = Calendar.getInstance();
                        tempCalendar.set(year, month, dayOfMonth);
                        startDt = tempCalendar.getTimeInMillis();
                        currentYear = year;
                        currentDay = dayOfMonth;
                        currentMonth = month;
                        Date dateConfig = new Date(startDt);

                        dateBtn.setText(simpleDateFormat.format(dateConfig));
                    }
                },
                currentYear, currentMonth, currentDay
        );
        datePickerDialog.show();
    }
}
