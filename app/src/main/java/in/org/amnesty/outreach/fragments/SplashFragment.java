package in.org.amnesty.outreach.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.common.SignInButton;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;

import java.io.IOException;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.activity.HomeActivity;
import in.org.amnesty.outreach.asynctasks.BaseAsyncTask;
import in.org.amnesty.outreach.helpers.Utils;


public class SplashFragment extends BaseFragment implements View.OnClickListener {

    //public static final String TAG = SplashFragment.class.getCanonicalName();

    public static final int ONE_SECOND = 1;
    public static final int ZERO_SECONDS = 0;

    private SignInButton mSignInButton;
    private boolean mIsGoogleConnected;

    public SplashFragment() {
        // Required empty public constructor
    }

    public static SplashFragment newInstance(Bundle arguments) {
        SplashFragment fragment = new SplashFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View splashView = inflater.inflate(R.layout.fragment_splash, null);
        mSignInButton = (SignInButton) splashView.findViewById(R.id.sign_in_button);
        ProgressBar mProgressBar = (ProgressBar) splashView.findViewById(R.id.progressBar);

        getParentActivity().setProgressBar(mProgressBar);

        getParentActivity().setCurrentFragmentTag(HomeActivity.TAG_SPLASH_FRAGMENT);

        return splashView;
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean mIsSetupComplete = Utils.PreferenceUtils.getBooleanPrefs(getActivity(),
                Utils.PreferenceUtils.APP_INITIALIZATION_STATUS);
        mIsGoogleConnected = Utils.PreferenceUtils.getBooleanPrefs(getActivity(),
                Utils.PreferenceUtils.GOOGLE_CONNECT_STATUS);

        if (mIsGoogleConnected) {
            if (!mIsSetupComplete) {
                mSignInButton.setVisibility(View.INVISIBLE);
                next(HomeActivity.FRAGMENT_CITY_SELECTOR);
            } else {
                mSignInButton.setVisibility(View.INVISIBLE);
                next(HomeActivity.FRAGMENT_HOME);
            }
        }

        checkGoogleConnect();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
               getParentActivity().chooseAccount();
        }
    }


    private void checkGoogleConnect() {
        if (!mIsGoogleConnected) {
            mSignInButton.setVisibility(View.VISIBLE);
            mSignInButton.setSize(SignInButton.SIZE_WIDE);
            mSignInButton.setOnClickListener(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case Activity.RESULT_OK:
                switch (requestCode) {
                    case HomeActivity.REQUEST_ACCOUNT_PICKER:
                        new PersonLoaderTask(getActivity(),
                                getParentActivity().getPlusService()).execute();
                        break;
                }
        }
    }

    public void next(final int next) {
        int secondsDelayed;
        if (Utils.PreferenceUtils.getBooleanPrefs(getParentActivity(), Utils.PreferenceUtils.SPLASH_STATUS)) {
            secondsDelayed = ONE_SECOND;
        } else {
            secondsDelayed = ZERO_SECONDS;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (next) {
                    case HomeActivity.FRAGMENT_CITY_SELECTOR:
                        getParentActivity().switchFragment(HomeActivity.FRAGMENT_CITY_SELECTOR, null);
                        break;
                    default:
                        getParentActivity().switchFragment(HomeActivity.FRAGMENT_HOME, null);
                        break;

                }
            }
        }, secondsDelayed * 1000);
    }

    public class PersonLoaderTask extends BaseAsyncTask {

        private Plus mPlusService;
        private Person mPerson;
        private Context mContext;

        public PersonLoaderTask(Activity activity, Plus plusService) {
            super(activity);
            mPlusService = plusService;
            mContext = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.showProgressDialog();
        }

        @Override
        protected void doInBackground() throws IOException {
            mPerson = mPlusService.people().get("me").execute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mActivity.hideProgressDialog();
            if (mPerson != null) {
                Utils.PreferenceUtils.setStringPrefs(mContext, Utils.PreferenceUtils.ACCOUNT_USER_NAME, mPerson.getDisplayName());
                Utils.PreferenceUtils.setStringPrefs(mContext, Utils.PreferenceUtils.ACCOUNT_USER_IMAGE_URL, mPerson.getImage().getUrl());
                Utils.PreferenceUtils.setStringPrefs(mContext,
                        Utils.PreferenceUtils.ACCOUNT_REGISTRATION_DATE, Utils.getFormattedDate(System.currentTimeMillis(),
                        "dd-MMMM-yyyy"));
                Utils.PreferenceUtils.setBooleanPrefs(getParentActivity(),
                        Utils.PreferenceUtils.GOOGLE_CONNECT_STATUS,true);
                next(HomeActivity.FRAGMENT_CITY_SELECTOR);
            }
        }
    }
}
