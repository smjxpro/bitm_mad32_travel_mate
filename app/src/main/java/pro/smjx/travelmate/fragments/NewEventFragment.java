package pro.smjx.travelmate.fragments;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
public class NewEventFragment extends Fragment implements FragmentHelperInterface {

    private ActivityHelperInterface activityHelperInterface;
    private long startDt, currentDt;
    private int currentDay, currentYear, currentMonth;
    private Button dateBtn, createBtn, cancelBtn;
    private SimpleDateFormat simpleDateFormat;
    private TextView statusTv, errorTv;
    private DatabaseReference rootRef, userRef, eventRef;
    private FirebaseUser user;
    private EditText eventNameEt, budgetEt, deptEt, destEt;

    public NewEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityHelperInterface = (ActivityHelperInterface) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_new_event, container, false);
        getActivity().setTitle(getString(R.string.new_event));

        user = FirebaseAuth.getInstance().getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child(user.getUid());
        eventRef = userRef.child(FINALS.EVENTS);

        eventNameEt = v.findViewById(R.id.eventNameEt);
        deptEt = v.findViewById(R.id.deptEt);
        destEt = v.findViewById(R.id.destEt);

        budgetEt = v.findViewById(R.id.budgetEt);
        statusTv = v.findViewById(R.id.statusTv);
        errorTv = v.findViewById(R.id.errorTv);
        dateBtn = v.findViewById(R.id.dateBtn);
        createBtn = v.findViewById(R.id.createBtn);
        cancelBtn = v.findViewById(R.id.cancelBtn);

        simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");

        Calendar calendar = Calendar.getInstance();

        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        currentMonth = calendar.get(Calendar.MONTH);
        currentYear = calendar.get(Calendar.YEAR);

        startDt = 0;
        currentDt = calendar.getTimeInMillis() - 1;

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = eventNameEt.getText().toString();
                String startLoc = deptEt.getText().toString();
                String destination = destEt.getText().toString();
                String budgetString = budgetEt.getText().toString();
                if (budgetString.isEmpty()) budgetString = "0";
                double budget = Double.parseDouble(budgetString);
                if (startDt == 0) {
                    Toast.makeText(getActivity(), getString(R.string.select_date), Toast.LENGTH_LONG).show();
                    pickADate();
                } else if (startDt < currentDt) {
                    Toast.makeText(getActivity(), getString(R.string.cant_prev_date), Toast.LENGTH_LONG).show();
                    pickADate();
                } else if (name.isEmpty()) {
                    showWarnDialogue("Event name can not be empty");
                } else if (startLoc.isEmpty()) {
                    showWarnDialogue("You need to set departure location");
                } else if (destination.isEmpty()) {
                    showWarnDialogue("You need to set destination");
                } else if (budget <= 0) {
                    showWarnDialogue(getString(R.string.invalid_budget));
                } else {
                    errorOrStatus(getString(R.string.creating), false);
                    String eventKey = eventRef.push().getKey();
                    Event event = new Event(eventKey, name, startLoc, destination, startDt, currentDt, 0, budget, 0);
                    eventRef = eventRef.child(eventKey);
                    eventRef.setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), R.string.created_successfully, Toast.LENGTH_SHORT).show();
                                activityHelperInterface.loadFragment(new EventFragment(), true);
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
                activityHelperInterface.loadFragment(new EventFragment(), true);
            }
        });
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickADate();
            }
        });


        return v;
    }

    private void getLocationDetails(FragmentActivity activity, int i) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(activity), i);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(getActivity(), data);
            if (requestCode == 711) {

            } else {

            }
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
}
