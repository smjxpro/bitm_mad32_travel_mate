package pro.smjx.travelmate.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pro.smjx.travelmate.R;
import pro.smjx.travelmate.interfaces.ActivityHelperInterface;
import pro.smjx.travelmate.interfaces.DrawerHelperInterface;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private ActivityHelperInterface activityHelperInterface;
    private DrawerHelperInterface drawerHelperInterface;

    private FirebaseUser user;
    private FirebaseAuth auth;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        getActivity().setTitle(R.string.profile);
        drawerHelperInterface.confirmLoggedUser();

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();


        TextView userNameTv,userEmailTv,userPasswordTv;
        Button editNameBtn, editEmailBtn, editPasswordBtn;

        userNameTv = v.findViewById(R.id.userNameTv);
        userEmailTv = v.findViewById(R.id.userEmailTv);
        userPasswordTv = v.findViewById(R.id.userPasswordTv);

        editNameBtn = v.findViewById(R.id.editNameBtn);
        editEmailBtn = v.findViewById(R.id.editEmailBtn);
        editPasswordBtn = v.findViewById(R.id.editPasswordBtn);


        if (user != null) {
            userNameTv.setText(user.getDisplayName().toString());
            userEmailTv.setText(user.getEmail().toString());
            userPasswordTv.setText("******");
        }


        editNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityHelperInterface.loadFragment(new EditNameFragment(), true);
            }
        });
        editEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityHelperInterface.loadFragment(new ChangeEmailFragment(), true);
            }
        });
        editPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityHelperInterface.loadFragment(new ChangePassFragment(), true);
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityHelperInterface = (ActivityHelperInterface) context;
        drawerHelperInterface = (DrawerHelperInterface) context;
    }
}
