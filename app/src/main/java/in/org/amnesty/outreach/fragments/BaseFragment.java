package in.org.amnesty.outreach.fragments;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import in.org.amnesty.outreach.activity.HomeActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {


    /**
     * Context.
     */
    private ActionBarActivity mActivity;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        mActivity = (ActionBarActivity) activity;
        super.onAttach(activity);
    }

    public HomeActivity getParentActivity() {
        return (HomeActivity) mActivity;
    }
}
