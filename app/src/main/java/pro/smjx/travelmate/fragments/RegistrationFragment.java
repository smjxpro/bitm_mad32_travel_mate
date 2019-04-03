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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import pro.smjx.travelmate.R;
import pro.smjx.travelmate.interfaces.ActivityHelperInterface;
import pro.smjx.travelmate.interfaces.FragmentHelperInterface;
import pro.smjx.travelmate.interfaces.MainActivityHelperInterface;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegistrationFragment extends Fragment implements FragmentHelperInterface {

    ActivityHelperInterface activityHelperInterface;
    MainActivityHelperInterface mainActivityHelperInterface;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView regStatusTv, regErrorTv;
    private EditText regNameEt, regEmailEt, regPassEt;

    private Button regBtn, loginBtn;

    public RegistrationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fakeView = inflater.inflate(R.layout.fragment_register, container, false);
        getActivity().setTitle(R.string.registration);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null) {
            mainActivityHelperInterface.goToNextActivity();
        } else {

            regNameEt = fakeView.findViewById(R.id.regNameEt);
            regEmailEt = fakeView.findViewById(R.id.regEmailEt);
            //regPhoneEt = fakeView.findViewById(R.id.regPhoneEt);
            regPassEt = fakeView.findViewById(R.id.regPassEt);
            loginBtn = fakeView.findViewById(R.id.loginBtn);

            regStatusTv = fakeView.findViewById(R.id.regStatusTv);
            regErrorTv = fakeView.findViewById(R.id.regErrorTv);

            regBtn = fakeView.findViewById(R.id.regBtn);
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityHelperInterface.loadFragment(new LoginFragment(), true);
                }
            });
            regBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String name = regNameEt.getText().toString();
                    String email = regEmailEt.getText().toString();
                    String pass = regPassEt.getText().toString();

                    if (name.isEmpty()) {
                        regNameEt.setError(getText(R.string.can_not_be_empty));
                    } else if (email.isEmpty()) {
                        regEmailEt.setError(getText(R.string.can_not_be_empty));
                    }

                    else if (pass.isEmpty()) {
                        regPassEt.setError(getText(R.string.can_not_be_empty));
                    } else {
                        errorOrStatus(getString(R.string.registering), false);

                        Task<AuthResult> resultTask = mAuth.createUserWithEmailAndPassword(email, pass);
                        resultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    regStatusTv.setText(getText(R.string.reg_success_setting_name));
                                    user = mAuth.getCurrentUser();
                                    UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                                    user.updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getActivity(), getText(R.string.reg_success), Toast.LENGTH_SHORT).show();
                                            mainActivityHelperInterface.goToNextActivity();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            errorOrStatus(getString(R.string.could_not_set_name), true);
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

        }

        return fakeView;
    }

    @Override
    public void onAttach(Context context) {
        activityHelperInterface = (ActivityHelperInterface) context;
        mainActivityHelperInterface = (MainActivityHelperInterface) context;
        super.onAttach(context);
    }

    @Override
    public void errorOrStatus(String text, boolean error) {

        if (error) {
            regErrorTv.setText(text);
            regErrorTv.setVisibility(View.VISIBLE);
            regStatusTv.setVisibility(View.GONE);
        } else {
            regStatusTv.setText(text);
            regStatusTv.setVisibility(View.VISIBLE);
            regErrorTv.setVisibility(View.GONE);
        }
    }
}
