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

import pro.smjx.travelmate.R;
import pro.smjx.travelmate.interfaces.ActivityHelperInterface;
import pro.smjx.travelmate.interfaces.MainActivityHelperInterface;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

     private EditText loginEmailEt, loginPassEt;
     private TextView loginStatusTv, loginErrorTv;
    private Button loginBtn, regBtn;


    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ActivityHelperInterface activityHelperInterface;
    private MainActivityHelperInterface mainActivityHelperInterface;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        getActivity().setTitle(R.string.login);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            mainActivityHelperInterface.goToNextActivity();
        } else {

            loginEmailEt = v.findViewById(R.id.loginEmailEt);
            loginPassEt = v.findViewById(R.id.loginPassEt);
            loginBtn = v.findViewById(R.id.loginBtn);
            regBtn = v.findViewById(R.id.regBtn);
            loginStatusTv = v.findViewById(R.id.loginStatusTv);
            loginErrorTv = v.findViewById(R.id.loginErrorTv);
            regBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityHelperInterface.loadFragment(new RegistrationFragment(), true);
                }
            });
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final String email = loginEmailEt.getText().toString();
                    final String pass = loginPassEt.getText().toString();

                    if (email.isEmpty()) {
                        loginEmailEt.setError(getString(R.string.can_not_be_empty));
                    } else if (pass.isEmpty()) {
                        loginPassEt.setError(getString(R.string.can_not_be_empty));
                    } else {

                        loginStatusTv.setVisibility(View.VISIBLE);
                        loginErrorTv.setVisibility(View.GONE);

                        Task<AuthResult> resultTask = mAuth.signInWithEmailAndPassword(email, pass);
                        resultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    user = mAuth.getCurrentUser();

                                    Toast.makeText(getActivity(), getText(R.string.login_success), Toast.LENGTH_SHORT).show();
                                    mAuth.signInWithEmailAndPassword(email, pass);
                                    mainActivityHelperInterface.goToNextActivity();
                                } else {

                                    loginErrorTv.setText(getText(R.string.error_occured));
                                    loginStatusTv.setVisibility(View.GONE);
                                    loginErrorTv.setVisibility(View.VISIBLE);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loginErrorTv.setText(e.getLocalizedMessage());
                                loginStatusTv.setVisibility(View.GONE);
                                loginErrorTv.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            });

        }
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityHelperInterface = (ActivityHelperInterface) context;
        mainActivityHelperInterface = (MainActivityHelperInterface) context;
    }
}
