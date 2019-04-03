package pro.smjx.travelmate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pro.smjx.travelmate.R;
import pro.smjx.travelmate.fragments.LoginFragment;
import pro.smjx.travelmate.interfaces.ActivityHelperInterface;
import pro.smjx.travelmate.interfaces.MainActivityHelperInterface;


public class MainActivity extends AppCompatActivity implements ActivityHelperInterface, MainActivityHelperInterface {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private int frameLayoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayoutId = R.id.frameLayout;

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null) {
            loadFragment(new LoginFragment(), true);
        } else {
            goToNextActivity();
        }
    }

    @Override
    public void reloadActivity(int fragmentIntValue) {
        if (fragmentIntValue > 0)
            startActivity(new Intent(this, MainActivity.class).putExtra("fragment_name", fragmentIntValue));
        else
            startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void goToNextActivity() {
        startActivity(new Intent(this, DrawerActivity.class));
        finish();
    }


    @Override
    public void loadFragment(Fragment fragment, boolean replace) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        if (replace)
            ft.replace(frameLayoutId, fragment);
        else
            ft.add(frameLayoutId, fragment);
        ft.commit();
    }

    @Override
    public void loadFragmentWithValue(Fragment fragment, boolean replace, Bundle bundle) {

    }

}
