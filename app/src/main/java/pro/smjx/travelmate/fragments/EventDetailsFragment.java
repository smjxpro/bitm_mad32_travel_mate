package pro.smjx.travelmate.fragments;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pro.smjx.travelmate.FINALS;
import pro.smjx.travelmate.R;
import pro.smjx.travelmate.adapters.ExpensesAdapter;
import pro.smjx.travelmate.events.Event;
import pro.smjx.travelmate.events.Expense;
import pro.smjx.travelmate.interfaces.ActivityHelperInterface;


public class EventDetailsFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView eventStatusTv, eventNameTv, expTv, percentTv, momTv, moreTv, editTv, deleteTv, closeTv, eventDetailsTv;
    private ActivityHelperInterface activityHelperInterface;
    private FirebaseUser user;
    private DatabaseReference rootRef, eventRef, expenseRef;
    private LinearLayout expLl, momLl, moreLl;
    private Context context;
    private double totalExp;
    private Calendar calendar;
    private boolean updateSynk;
    private Bitmap photo;
    private String eventId;
    private AlertDialog photoDialog;
    private List<Expense> expenses = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityHelperInterface = (ActivityHelperInterface) context;
        this.context = context;
        calendar = Calendar.getInstance();
    }

    public EventDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_event_details, container, false);

        updateSynk = true;
        progressBar = v.findViewById(R.id.progressBar);
        eventStatusTv = v.findViewById(R.id.eventStatusTv);
        eventNameTv = v.findViewById(R.id.eventNameTv);
        percentTv = v.findViewById(R.id.percentTv);
        //eventDetailsTv = v.findViewById(R.id.eventDetailsTv);

        eventId = this.getArguments().getString("id", "0");
        user = FirebaseAuth.getInstance().getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        eventRef = rootRef.child(user.getUid()).child(FINALS.EVENTS).child(eventId);
        expenseRef = rootRef.child(user.getUid()).child(FINALS.EXPENSES).child(eventId);
        eventRef.keepSynced(true);

        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Event event = dataSnapshot.getValue(Event.class);
                if (event == null) {

                    updateSynk = false;
                    eventRef.keepSynced(false);
                    activityHelperInterface.loadFragment(new EventFragment(), true);
                } else {
                    expTv = v.findViewById(R.id.expTv);
                    expLl = v.findViewById(R.id.expLl);
                    momLl = v.findViewById(R.id.momLl);
                    momTv = v.findViewById(R.id.momTv);
                    moreTv = v.findViewById(R.id.moreTv);
                    moreLl = v.findViewById(R.id.moreLl);
                    editTv = v.findViewById(R.id.editTv);
                    deleteTv = v.findViewById(R.id.deleteTv);
                    deleteTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(R.string.confirm)
                                    .setIcon(R.drawable.warn)
                                    .setMessage("Are you sure to delete this event?")
                                    .setNegativeButton("No", null)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            updateSynk = false;
                                            eventRef.keepSynced(false);
                                            expenseRef.removeValue();
                                            eventRef.removeValue();
                                            activityHelperInterface.loadFragment(new EventFragment(), true);
                                        }
                                    }).show();
                        }
                    });
                    editTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putString("id", eventId);
                            activityHelperInterface.loadFragmentWithValue(new EditEventFragment(), true, bundle);
                        }
                    });

                    TextView addNewExpTv, viewAllExpTv, addBudgetTv;
                    addNewExpTv = v.findViewById(R.id.addNewExpTv);
                    viewAllExpTv = v.findViewById(R.id.viewAllExpTv);
                    addBudgetTv = v.findViewById(R.id.addBudgetTv);
                    viewAllExpTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog dialog = new AlertDialog.Builder(context)
                                    .setView(R.layout.expense_list)
                                    .setCancelable(false).setNegativeButton("Close", null)
                                    .create();
                            dialog.show();
                            ListView lv = dialog.findViewById(R.id.lV);
                            lv.setAdapter(new ExpensesAdapter(context, expenses));

                        }
                    });
                    addNewExpTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            final AlertDialog dialog = builder.setCancelable(false)
                                    .setTitle("Add New Expense")
                                    .setView(R.layout.add_expenses)
                                    .create();
                            dialog.show();
                            final EditText detailsEt, amountEt;
                            final TextView errorTv;
                            Button addBtn, cancelBtn;
                            detailsEt = dialog.findViewById(R.id.detailsEt);
                            amountEt = dialog.findViewById(R.id.amountEt);
                            errorTv = dialog.findViewById(R.id.errorTv);
                            addBtn = dialog.findViewById(R.id.addBtn);
                            cancelBtn = dialog.findViewById(R.id.cancelBtn);
                            final LinearLayout lL = dialog.findViewById(R.id.lL);
                            cancelBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                            addBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String details = detailsEt.getText().toString();
                                    String amount = amountEt.getText().toString();
                                    if (amount.isEmpty()) amount = "0";
                                    double addAmount = Double.parseDouble(amount);
                                    if (addAmount <= 0) {
                                        setErrorText(errorTv, "Invalid amount!");
                                    } else if (addAmount + totalExp > event.getBudget()) {
                                        setErrorText(errorTv, "Exceeding budget! Please add funds to your budget!");
                                    } else if (details.isEmpty()) {
                                        setErrorText(errorTv, "Details required!");
                                    } else {
                                        lL.setVisibility(View.GONE);
                                        setErrorText(errorTv, "Processing...");
                                        String expenseKey = expenseRef.push().getKey();
                                        expenseRef.child(expenseKey).setValue(new Expense(expenseKey, details, addAmount, calendar.getTimeInMillis())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    setErrorText(errorTv, "Successful!");
                                                    int delay = 1500;
                                                    delayDismiss(dialog, delay);
                                                } else {
                                                    lL.setVisibility(View.VISIBLE);
                                                    setErrorText(errorTv, "An error occurred! Please try again.");
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                        }
                    });

                    addBudgetTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final AlertDialog dialog = new AlertDialog.Builder(context)
                                    .setCancelable(false)
                                    .setView(R.layout.add_budget)
                                    .setTitle("Add budget").create();
                            dialog.show();

                            Button addBtn = dialog.findViewById(R.id.addBtn);
                            Button cancelBtn = dialog.findViewById(R.id.cancelBtn);
                            cancelBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                            addBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final LinearLayout lL = dialog.findViewById(R.id.lL);
                                    final TextView errorTv = dialog.findViewById(R.id.errorTv);
                                    EditText addBudgetEt = dialog.findViewById(R.id.addBudgetEt);
                                    String budgetTxt = addBudgetEt.getText().toString();
                                    if (budgetTxt.isEmpty()) budgetTxt = "0";
                                    double budget = Double.parseDouble(budgetTxt);
                                    if (budget <= 0)
                                        setErrorText(errorTv, "Invalid budget amount!");
                                    else {
                                        lL.setVisibility(View.GONE);
                                        setErrorText(errorTv, "Processing...");
                                        double newBudget = budget + event.getBudget();
                                        eventRef.setValue(new Event(eventId, event.getName(), event.getStartingLocation(), event.getDestination(), event.getDepartureDate(), event.getCreatingDate(), event.getEndingDate(), newBudget, event.getExpenses())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    setErrorText(errorTv, "Added successfully!");
                                                    delayDismiss(dialog, 1500);
                                                } else {
                                                    setErrorText(errorTv, "An error occured! Please try again.");
                                                    lL.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        });
                                    }
                                }
                            });


                        }
                    });

                    /*moments */

                    TextView takePhotoTv = v.findViewById(R.id.takePhotoTv);
                    TextView galleryTv = v.findViewById(R.id.galleryTv);
                    TextView allMomTv = v.findViewById(R.id.allMomTv);
                    takePhotoTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            takePhoto();
                        }
                    });

                    galleryTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "content://media/internal/images/media"));
                            startActivity(intent);
                        }
                    });

                    allMomTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "content://media/internal/images/media"));
                            startActivity(intent);
                        }
                    });

                    if (event.getEndingDate() > 0) {
                        editTv.setVisibility(View.GONE);
                        closeTv.setVisibility(View.GONE);
                        addNewExpTv.setVisibility(View.GONE);
                        addBudgetTv.setVisibility(View.GONE);
                        takePhotoTv.setVisibility(View.GONE);

                    }
                    expTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (expLl.getVisibility() == View.VISIBLE) {
                                expLl.setVisibility(View.GONE);
                            } else {
                                expLl.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    momTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (momLl.getVisibility() == View.VISIBLE) {
                                momLl.setVisibility(View.GONE);
                            } else {
                                momLl.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    moreTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (moreLl.getVisibility() == View.VISIBLE) {
                                moreLl.setVisibility(View.GONE);
                            } else {
                                moreLl.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    if (getActivity() != null)
                        getActivity().setTitle(event.getName());
                    eventNameTv.setText(event.getName());
                    expenseRef = rootRef.child(user.getUid()).child(FINALS.EXPENSES).child(eventId);
                    expenseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            expenses.clear();
                            totalExp = 0.0;
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                Expense eeee = ds.getValue(Expense.class);
                                totalExp = eeee.getAmount() + totalExp;
                                expenses.add(eeee);
                            }
                            if (updateSynk) {
                                eventStatusTv.setText(getString(R.string.budget_status) + " (" + (int) totalExp + "/" + (int) event.getBudget() + ")");
                                int progress = (int) (100 * totalExp / event.getBudget());
                                if (progress > 100) progress = 100;
                                if (progress < 0) progress = 0;
                                progressBar.setProgress(progress);
                                percentTv.setText(progress + "%");
                            }
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
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        askPermission(111);
        super.onViewCreated(view, savedInstanceState);
    }

    private void askPermission(int pCode) {
        if (!hasPerm())
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, pCode);
    }

    private boolean hasPerm() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            return false;
        return true;
    }

    private void takePhoto() {
        if (hasPerm()) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 188);
        } else
            askPermission(101);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 188) {
            if (data != null)
                if (data.getExtras() != null)
                    if (data.getExtras().get("data") != null) {
                        photo = (Bitmap) data.getExtras().get("data");
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        photoDialog = builder
                                .setTitle("Preview")
                                .setView(R.layout.image_view)
                                .setCancelable(false)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        savePhoto();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .setNeutralButton("Take again", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        takAgain();
                                    }
                                })
                                .create();
                        photoDialog.show();
                        ImageView iv = photoDialog.findViewById(R.id.iv);
                        iv.setImageBitmap(photo);
                    }
        }
    }

    private void takAgain() {
        photoDialog.dismiss();
        takePhoto();
    }

    public void savePhoto() {
        photoDialog.dismiss();
        saveImage();
    }

    private void saveImage() {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + FINALS.MOMENTS_DIR_ROOT + eventId);
        myDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String fName = timeStamp + ".jpg";
        File file = new File(myDir, fName);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(getActivity(), "Image saved successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Could not save image!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101)
            takePhoto();
    }

    private void delayDismiss(final AlertDialog dialogHandle, int delay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                dialogHandle.dismiss();
            }
        }, delay);
    }

    private void setErrorText(TextView errorTv, String successful) {
        errorTv.setVisibility(View.VISIBLE);
        errorTv.setText(successful);
    }

    @Override
    public void onPause() {
        updateSynk = false;
        eventRef.keepSynced(false);
        super.onPause();
    }
}
