package pro.smjx.travelmate.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pro.smjx.travelmate.FINALS;
import pro.smjx.travelmate.R;
import pro.smjx.travelmate.Utilities;
import pro.smjx.travelmate.adapters.EventAdapter;
import pro.smjx.travelmate.events.Event;
import pro.smjx.travelmate.interfaces.ActivityHelperInterface;
import pro.smjx.travelmate.interfaces.DrawerHelperInterface;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment {

    private ActivityHelperInterface activityHelperInterface;
    private DrawerHelperInterface drawerHelperInterface;
    private ListView listView;
    private Button button;
    private TextView createdEventsTv, loadingTv;
    private List<Event> events = new ArrayList<>();
    private FirebaseUser user;
    private boolean loadAdaper;
    private DatabaseReference rootRef, userRef, eventRef;
    private String clickedItemId;
    Context context;

    public EventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        activityHelperInterface = (ActivityHelperInterface) context;
        drawerHelperInterface = (DrawerHelperInterface) context;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_drawer, container, false);
        listView = v.findViewById(R.id.listView);
        createdEventsTv = v.findViewById(R.id.createdEventsTv);
        loadingTv = v.findViewById(R.id.loadingTv);
        button = v.findViewById(R.id.button);

        loadAdaper = true;
        user = FirebaseAuth.getInstance().getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = rootRef.child(user.getUid());
        eventRef = userRef.child(FINALS.EVENTS);
        userRef.keepSynced(true);

        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (loadAdaper) {
                    events.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        events.add(ds.getValue(Event.class));
                    }
                    if (events.size() > 0) {
                        listView.setAdapter(new EventAdapter(getActivity(), events));
                        createdEventsTv.setVisibility(View.VISIBLE);
                        loadingTv.setVisibility(View.GONE);
                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                clickedItemId = events.get(position).getId();
                                PopupMenu menu = new PopupMenu(context, view);
                                MenuInflater menuInflater = menu.getMenuInflater();
                                menuInflater.inflate(R.menu.edit_delete, menu.getMenu());
                                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.deleteMenu:
                                                deleteDialogue();
                                                break;
                                            case R.id.editMenu:
                                                Bundle bundle = new Bundle();
                                                bundle.putString("id", clickedItemId);
                                                activityHelperInterface.loadFragmentWithValue(new EditEventFragment(), true, bundle);
                                                break;
                                        }
                                        return false;
                                    }
                                });
                                menu.show();
                                return true;
                            }
                        });
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                clickedItemId = events.get(position).getId();
                                Bundle bundle = new Bundle();
                                bundle.putString("id", clickedItemId);
                                activityHelperInterface.loadFragmentWithValue(new EventDetailsFragment(), true, bundle);
                            }
                        });
                    } else {
                        LinearLayout containerLayout = v.findViewById(R.id.containerLayout);
                        listView.setVisibility(View.GONE);
                        loadingTv.setVisibility(View.GONE);
                        containerLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getActivity().setTitle(R.string.all_events);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewEvent();
            }
        });
        return v;
    }

    private void deleteDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false)
                .setMessage(R.string.are_you_sure)
                .setTitle(R.string.confirm)
                .setIcon(R.drawable.warn)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utilities.deleteEvent(context, clickedItemId);
                    }
                }).setNegativeButton("No", null).show();
    }

    @Override
    public void onStop() {
        userRef.keepSynced(true);
        loadAdaper = false;
        super.onStop();
    }

    @Override
    public void onPause() {
        userRef.keepSynced(true);
        loadAdaper = false;
        super.onPause();
    }

    private void createNewEvent() {
        activityHelperInterface.loadFragment(new NewEventFragment(), true);
    }

}
