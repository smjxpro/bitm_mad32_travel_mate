package pro.smjx.travelmate.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import pro.smjx.travelmate.FINALS;
import pro.smjx.travelmate.R;
import pro.smjx.travelmate.interfaces.ActivityHelperInterface;
import pro.smjx.travelmate.interfaces.DrawerHelperInterface;
import pro.smjx.travelmate.interfaces.FragmentHelperInterface;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditNameFragment extends Fragment implements FragmentHelperInterface {

    private FirebaseAuth auth;
    private FirebaseUser user;

    private TextView errorTv, loadingTv;
    ActivityHelperInterface activityHelperInterface;
    DrawerHelperInterface drawerHelperInterface;

    public EditNameFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_name, container, false);
        getActivity().setTitle(R.string.edit_name);
        drawerHelperInterface.confirmLoggedUser();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        TextView userNameTv;
        final EditText nameEt;
        Button submitBtn, cancelBtn;
        nameEt = v.findViewById(R.id.nameEt);
        submitBtn = v.findViewById(R.id.submitBtn);
        userNameTv = v.findViewById(R.id.userNameTv);
        errorTv = v.findViewById(R.id.errorTv);
        loadingTv = v.findViewById(R.id.loadingTv);
        cancelBtn = v.findViewById(R.id.cancelBtn);

        userNameTv.setText(user.getDisplayName());

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityHelperInterface.loadFragment(new ProfileFragment(), true);
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEt.getText().toString();
                if (name.isEmpty()) {
                    nameEt.setError(getText(R.string.can_not_be_empty));
                } else if (name.equals(user.getDisplayName())) {
                    nameEt.setError(getText(R.string.can_not_be_empty));
                } else {
                    errorOrStatus(getString(R.string.updating_name), false);
                    UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();

                    user.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            activityHelperInterface.reloadActivity(FINALS.UPDATE_PROFILE);
                            Toast.makeText(getActivity(), R.string.name_updated, Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            errorOrStatus(getString(R.string.could_not_update_name), true);
                        }
                    });
                }
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

    @Override
    public void errorOrStatus(String text, boolean error) {

        if (error) {
            errorTv.setText(text);
            errorTv.setVisibility(View.VISIBLE);
            loadingTv.setVisibility(View.GONE);
        } else {
            loadingTv.setText(text);
            loadingTv.setVisibility(View.VISIBLE);
            errorTv.setVisibility(View.GONE);
        }
    }
}
