package pro.smjx.travelmate.interfaces;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public interface ActivityHelperInterface {
    void loadFragment(Fragment fragment, boolean replace);

    void loadFragmentWithValue(Fragment fragment, boolean replace, Bundle bundle);

    void reloadActivity(int fragmentIntValue);
}
