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
public class ChangePassFragment extends Fragment implements FragmentHelperInterface {


    private ActivityHelperInterface activityHelperInterface;
    private DrawerHelperInterface drawerHelperInterface;
    private EditText currentPassEt, newPassEt, confNewPassEt;
    private TextView statusTv, errorTv;
    private Button confirmBtn, cancelBtn;
    private FirebaseAuth auth;
    private FirebaseUser user;

    public ChangePassFragment() {
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
        View v = inflater.inflate(R.layout.fragment_change_pass, container, false);

        getActivity().setTitle(R.string.change_password);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        currentPassEt = v.findViewById(R.id.currentPassEt);
        newPassEt = v.findViewById(R.id.newPassEt);
        confNewPassEt = v.findViewById(R.id.confNewPassEt);
        statusTv = v.findViewById(R.id.statusTv);
        errorTv = v.findViewById(R.id.errorTv);
        confirmBtn = v.findViewById(R.id.confirmBtn);
        cancelBtn = v.findViewById(R.id.cancelBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityHelperInterface.loadFragment(new ProfileFragment(), true);
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = currentPassEt.getText().toString();
                final String newPass = newPassEt.getText().toString();
                String confNewPass = confNewPassEt.getText().toString();

                if (pass.isEmpty()) {
                    currentPassEt.setError(getText(R.string.can_not_be_empty));
                } else if (newPass.isEmpty()) {
                    newPassEt.setError(getText(R.string.can_not_be_empty));
                } else if (confNewPass.isEmpty()) {
                    confNewPassEt.setError(getText(R.string.can_not_be_empty));
                } else if (!confNewPass.equals(newPass)) {
                    errorOrStatus(getString(R.string.new_passes_not_match), true);
                } else {

                    errorOrStatus(getString(R.string.loading), false);
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), pass);
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            Toast.makeText(getActivity(), R.string.changed_successfully, Toast.LENGTH_SHORT).show();
                                            activityHelperInterface.reloadActivity(FINALS.CHANGE_PASS);
                                        } else {
                                            errorOrStatus(getString(R.string.error_occured), true);
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        errorOrStatus(getString(R.string.error_occured), true);

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
