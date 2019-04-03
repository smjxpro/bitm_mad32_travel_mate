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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pro.smjx.travelmate.FINALS;
import pro.smjx.travelmate.R;
import pro.smjx.travelmate.interfaces.ActivityHelperInterface;
import pro.smjx.travelmate.interfaces.DrawerHelperInterface;
import pro.smjx.travelmate.interfaces.FragmentHelperInterface;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChangeEmailFragment extends Fragment implements FragmentHelperInterface {


    ActivityHelperInterface activityHelperInterface;
    DrawerHelperInterface drawerHelperInterface;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView statusTv, errorTv, currentEmailTv;

    public ChangeEmailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityHelperInterface = (ActivityHelperInterface) context;
        drawerHelperInterface = (DrawerHelperInterface) context;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_change_email, container, false);
        getActivity().setTitle(R.string.change_email_address);
        drawerHelperInterface.confirmLoggedUser();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        final EditText changeMailEt, passEt;
        Button confirmBtn, cancelBtn;

        changeMailEt = v.findViewById(R.id.changeMailEt);
        passEt = v.findViewById(R.id.passEt);
        confirmBtn = v.findViewById(R.id.confirmBtn);
        cancelBtn = v.findViewById(R.id.cancelBtn);
        statusTv = v.findViewById(R.id.statusTv);
        errorTv = v.findViewById(R.id.errorTv);
        currentEmailTv = v.findViewById(R.id.currentEmailTv);
        currentEmailTv.setText(user.getEmail());
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityHelperInterface.loadFragment(new ProfileFragment(), true);
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = changeMailEt.getText().toString();
                String pass = passEt.getText().toString();
                if (email.isEmpty()) {
                    changeMailEt.setError(getText(R.string.can_not_be_empty));
                } else if (pass.isEmpty()) {
                    passEt.setError(getText(R.string.can_not_be_empty));
                } else {
                    if (user.getEmail().equals(email)) {
                        errorOrStatus(getText(R.string.new_prev_mail_same).toString(), true);
                    } else {
                        errorOrStatus(getText(R.string.loading).toString(), false);
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), pass);
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
                                                activityHelperInterface.reloadActivity(FINALS.CHANGE_EMAIL);
                                            } else {
                                                errorOrStatus(getString(R.string.email_already_chosen), true);
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            errorOrStatus(getString(R.string.email_already_chosen), true);

                                        }
                                    });
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
            }
        });

        return v;
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
