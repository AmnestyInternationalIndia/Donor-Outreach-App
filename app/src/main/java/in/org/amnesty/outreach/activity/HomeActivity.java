package in.org.amnesty.outreach.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.PlusScopes;

import java.util.Arrays;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.fragments.CitySelectorFragment;
import in.org.amnesty.outreach.fragments.DownloadProgressFragment;
import in.org.amnesty.outreach.fragments.HomeFragment;
import in.org.amnesty.outreach.fragments.PdfViewerFragment;
import in.org.amnesty.outreach.fragments.SplashFragment;
import in.org.amnesty.outreach.fragments.VideoPlayerFragment;
import in.org.amnesty.outreach.helpers.Utils;


public class HomeActivity extends ActionBarActivity {

    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 9001;
    public static final int REQUEST_ACCOUNT_PICKER = 9020;
    public static final int REQUEST_AUTHORIZATION = 9002;

    public static final int FRAGMENT_SPLASH = 0;
    public static final int FRAGMENT_CITY_SELECTOR = 1;
    public static final int FRAGMENT_DOWNLOAD_PROGRESS = 2;
    public static final int FRAGMENT_HOME = 3;
    public static final int FRAGMENT_VIDEO_PLAYER = 4;
    public static final int FRAGMENT_PDF_VIEWER = 5;

    public static final String TAG_SPLASH_FRAGMENT = "splashFragment";
    public static final String TAG_DOWNLOAD_PROGRESS_FRAGMENT = "downloadProgressFragment" ;
    public static final String TAG_HOME_FRAGMENT = "homeFragment";
    public static final String TAG_CITY_SELECTOR_FRAGMENT = "citySelectorFragment";
    public static final String TAG_VIDEO_PLAYER_FRAGMENT = "videoPlayerFragment";
    public static final String TAG_PDF_VIEWER_FRAGMENT = "pdfViewerFragment";

    private static final String KEY_CURRENT_FRAGMENT_TAG = "currentFragmentTag";

    private final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    private final AndroidJsonFactory jsonFactory = AndroidJsonFactory.getDefaultInstance();
    private GoogleAccountCredential mGoogleCredential;
    private Plus mPlusService;
    private Drive mDriveService;
    private ProgressBar mProgressBar;
    private String mCurrentFragmentTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(savedInstanceState != null) {
            mCurrentFragmentTag = savedInstanceState.getString(KEY_CURRENT_FRAGMENT_TAG);
        }

        mGoogleCredential =
                GoogleAccountCredential.usingOAuth2(this, Arrays.asList(PlusScopes.PLUS_ME, DriveScopes.DRIVE));

        mGoogleCredential.setSelectedAccountName(Utils.PreferenceUtils.getStringPrefs(this, Utils.PreferenceUtils.ACCOUNT_USER_ID));

        mPlusService =
                new Plus.Builder(httpTransport, jsonFactory, mGoogleCredential)
                        .setApplicationName("OutReach/1.0").build();

        mDriveService =
                new Drive.Builder(httpTransport, jsonFactory, mGoogleCredential)
                        .setApplicationName("OutReach/1.0").build();
    }

    public void setCurrentFragmentTag(String tag){
        mCurrentFragmentTag = tag;
    }

    public void chooseAccount() {
        startActivityForResult(mGoogleCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    public void setProgressBar(ProgressBar progressBar){
        mProgressBar = progressBar;
    }

    public void showProgressDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressDialog() {
        mProgressBar.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!Utils.PhoneUtils.enabled(this, Utils.PhoneUtils.INTERNET)) {
            Toast.makeText(this, getString(R.string.no_internet_access), Toast.LENGTH_SHORT).show();
        }

        if (Utils.PhoneUtils.enabled(this, Utils.PhoneUtils.PLAY_SERVICES)) {
            if (Utils.PreferenceUtils.getBooleanPrefs(this, Utils.PreferenceUtils.APP_INITIALIZATION_STATUS)) {
                if(!TextUtils.isEmpty(mCurrentFragmentTag)){
                    switchFragment(mCurrentFragmentTag, null);
                } else {
                    switchFragment(FRAGMENT_HOME, null);
                }
            } else if(Utils.PreferenceUtils.getBooleanPrefs(this, Utils.PreferenceUtils.IS_DOWNLOADING)) {
                switchFragment(FRAGMENT_DOWNLOAD_PROGRESS, null);
            } else if(Utils.PreferenceUtils.getBooleanPrefs(this, Utils.PreferenceUtils.GOOGLE_CONNECT_STATUS)
                    && !Utils.PreferenceUtils.getBooleanPrefs(this, Utils.PreferenceUtils.APP_INITIALIZATION_STATUS)) {
                switchFragment(FRAGMENT_CITY_SELECTOR, null);
            } else {
                switchFragment(FRAGMENT_SPLASH, null);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_FRAGMENT_TAG, mCurrentFragmentTag);
    }

    public void switchFragment(final int fragmentNumber, final Bundle arguments) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (fragmentNumber) {
            case FRAGMENT_SPLASH:
                fragmentTransaction.replace(R.id.container, SplashFragment.newInstance(arguments), TAG_SPLASH_FRAGMENT);
                break;
            case FRAGMENT_HOME:
                fragmentTransaction.replace(R.id.container, HomeFragment.newInstance(arguments), TAG_HOME_FRAGMENT);
                break;
            case FRAGMENT_DOWNLOAD_PROGRESS:
                fragmentTransaction.replace(R.id.container, DownloadProgressFragment.newInstance(arguments),
                        TAG_DOWNLOAD_PROGRESS_FRAGMENT);
                break;
            case FRAGMENT_CITY_SELECTOR:
                fragmentTransaction.replace(R.id.container, CitySelectorFragment.newInstance(arguments),
                        TAG_CITY_SELECTOR_FRAGMENT);
                break;
            case FRAGMENT_VIDEO_PLAYER:
                fragmentTransaction.replace(R.id.container, VideoPlayerFragment.newInstance(arguments),
                        TAG_VIDEO_PLAYER_FRAGMENT);
                break;
            case FRAGMENT_PDF_VIEWER:
                fragmentTransaction.replace(R.id.container, PdfViewerFragment.newInstance(arguments),
                        TAG_PDF_VIEWER_FRAGMENT);
                break;
        }
        fragmentTransaction.commit();
    }

    private void switchFragment(final String fragmentTag, final Bundle arguments) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);

        if(fragment == null) {
            fragment = getFragmentByTag(fragmentTag, arguments);
            fragmentTransaction.replace(R.id.container, fragment, fragmentTag);
            fragmentTransaction.commit();
        } else {
            if(fragment.isVisible()) {
                return;
            }

            fragmentTransaction.replace(R.id.container, fragment, fragmentTag);
            fragmentTransaction.commit();
        }
    }

    private Fragment getFragmentByTag(final String fragmentTag, final Bundle arguments) {
        if(fragmentTag.equalsIgnoreCase(TAG_SPLASH_FRAGMENT)) {
            return SplashFragment.newInstance(arguments);
        } else {
            return HomeFragment.newInstance(arguments);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    Utils.PhoneUtils.enabled(this, Utils.PhoneUtils.PLAY_SERVICES);
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != Activity.RESULT_OK) {
                    chooseAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mGoogleCredential.setSelectedAccountName(accountName);
                        Utils.PreferenceUtils.setStringPrefs(this, Utils.PreferenceUtils.ACCOUNT_USER_ID, accountName);
                    }
                }
                break;
        }

        if (!TextUtils.isEmpty(mCurrentFragmentTag)) {
            getSupportFragmentManager().findFragmentByTag(mCurrentFragmentTag).onActivityResult(requestCode, resultCode,
                    data);
        }
    }

    public GoogleAccountCredential getGoogleCredential() {
        return mGoogleCredential;
    }

    public Drive getDriveService() {
        return mDriveService;
    }

    public Plus getPlusService() {
        return mPlusService;
    }

}
