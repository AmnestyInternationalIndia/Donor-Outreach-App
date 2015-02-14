package in.org.amnesty.outreach.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.PlusScopes;

import java.io.IOException;
import java.util.Arrays;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.fragments.BaseTabFragment;
import in.org.amnesty.outreach.fragments.BaseViewerFragment;
import in.org.amnesty.outreach.fragments.HomeFragment;
import in.org.amnesty.outreach.fragments.SetupFragment;
import in.org.amnesty.outreach.fragments.SplashFragment;
import in.org.amnesty.outreach.helpers.Utils;
import in.org.amnesty.outreach.receivers.DownloadBroadcastReceiver;


public class HomeActivity extends ActionBarActivity {

    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 9001;
    public static final int REQUEST_ACCOUNT_PICKER = 9020;
    public static final int REQUEST_AUTHORIZATION = 9002;

    public static final int FRAGMENT_SPLASH = 0;
    public static final int FRAGMENT_SETUP = 1;
    public static final int FRAGMENT_HOME = 2;
    public static final int FRAGMENT_BASE_VIEWER = 3;

    public static final String TAG_SPLASH_FRAGMENT = "splashFragment";
    public static final String TAG_HOME_FRAGMENT = "homeFragment";
    public static final String TAG_SETUP_FRAGMENT = "setupFragment";
    public static final String TAG_VIEWER_FRAGMENT = "viewerFragment";

    private static final String KEY_CURRENT_FRAGMENT_TAG = "currentFragmentTag";
    private static final String ANDROID_PERMISSION_SEND_DOWNLOAD_COMPLETED_INTENTS =
            "android.permission.SEND_DOWNLOAD_COMPLETED_INTENTS";

    private final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();

    private final AndroidJsonFactory jsonFactory = AndroidJsonFactory.getDefaultInstance();

    private GoogleAccountCredential mGoogleCredential;

    private Plus mPlusService;

    private Drive mDriveService;

    private String mCurrentFragmentTag;

    private ProgressDialog mProgressDialog;

    private DownloadBroadcastReceiver mDownloadProgressReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState != null) {
            mCurrentFragmentTag = savedInstanceState.getString(KEY_CURRENT_FRAGMENT_TAG);
        }

        mGoogleCredential =
                GoogleAccountCredential.usingOAuth2(this, Arrays.asList(PlusScopes.PLUS_ME, DriveScopes.DRIVE));

        mGoogleCredential.setSelectedAccountName(
                Utils.Preferences.getStringPrefs(this, Utils.Preferences.ACCOUNT_USER_ID));

        mPlusService =
                new Plus.Builder(httpTransport, jsonFactory, mGoogleCredential)
                        .setApplicationName("OutReach/1.0").build();

        mDriveService =
                new Drive.Builder(httpTransport, jsonFactory, mGoogleCredential)
                        .setApplicationName("OutReach/1.0").build();

        mProgressDialog = new ProgressDialog(this, AlertDialog.THEME_HOLO_DARK);

        mDownloadProgressReceiver = new DownloadBroadcastReceiver();
    }

    public void setCurrentFragmentTag(String tag) {
        mCurrentFragmentTag = tag;
    }

    public void chooseAccount() {
        startActivityForResult(mGoogleCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        showProgressDialog(null);
    }

    public void showProgressDialog(String message) {
        mProgressDialog.setCancelable(false);
        if (TextUtils.isEmpty(message)) {
            mProgressDialog.setMessage(getString(R.string.message_connection_progress));
        } else {
            mProgressDialog.setMessage(message);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        mProgressDialog.hide();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!Utils.Device.enabled(this, Utils.Device.INTERNET)) {
            Toast.makeText(this, getString(R.string.no_internet_access), Toast.LENGTH_SHORT).show();
        }

        boolean appFolderExists;
        try {
            appFolderExists = Utils.Storage.existsFolder(this, Utils.Constants.DEFAULT_APP_FOLDER);

            if(!appFolderExists) {
                Utils.Storage.clearMediaStorage(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.no_external_storage), Toast.LENGTH_LONG).show();
        }

        registerReceiver(mDownloadProgressReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ANDROID_PERMISSION_SEND_DOWNLOAD_COMPLETED_INTENTS, null);
        Bundle bundle = new Bundle();
        if (Utils.Device.enabled(this, Utils.Device.PLAY_SERVICES)) {
            if (Utils.Preferences.getBooleanPrefs(this, Utils.Preferences.APP_INITIALIZATION_STATUS)) {
                if (!TextUtils.isEmpty(mCurrentFragmentTag)) {
                    switchFragment(mCurrentFragmentTag, null);
                } else {
                    switchFragment(FRAGMENT_HOME, null);
                }
            } else if (Utils.Preferences.getBooleanPrefs(this, Utils.Preferences.IS_DOWNLOADING)) {
                bundle.putInt(BaseTabFragment.BUNDLE_CURRENT_TYPE, SetupFragment.TYPE_DOWNLOAD);
                switchFragment(FRAGMENT_SETUP, bundle);
            } else if (Utils.Preferences.getBooleanPrefs(this, Utils.Preferences.GOOGLE_CONNECT_STATUS)
                    && !Utils.Preferences.getBooleanPrefs(this, Utils.Preferences.APP_INITIALIZATION_STATUS)) {
                bundle.putInt(BaseTabFragment.BUNDLE_CURRENT_TYPE, SetupFragment.TYPE_CITY);
                switchFragment(FRAGMENT_SETUP, bundle);
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
            case FRAGMENT_SETUP:
                fragmentTransaction.replace(R.id.container, SetupFragment.newInstance(arguments),
                        TAG_SETUP_FRAGMENT);
                break;
            case FRAGMENT_BASE_VIEWER:
                fragmentTransaction.replace(R.id.container, BaseViewerFragment.newInstance(arguments),
                        TAG_VIEWER_FRAGMENT);
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mDownloadProgressReceiver);
    }

    private void switchFragment(final String fragmentTag, final Bundle arguments) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);

        if (fragment == null) {
            fragment = getFragmentByTag(fragmentTag, arguments);
            fragmentTransaction.replace(R.id.container, fragment, fragmentTag);
            fragmentTransaction.commitAllowingStateLoss();
        } else {
            if (fragment.isVisible()) {
                return;
            }

            fragmentTransaction.replace(R.id.container, fragment, fragmentTag);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private Fragment getFragmentByTag(final String fragmentTag, final Bundle arguments) {
        if (fragmentTag.equalsIgnoreCase(TAG_SPLASH_FRAGMENT)) {
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
                    Utils.Device.enabled(this, Utils.Device.PLAY_SERVICES);
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != Activity.RESULT_OK) {
                    chooseAccount();
                } else {
                    hideProgressDialog();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    hideProgressDialog();
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mGoogleCredential.setSelectedAccountName(accountName);
                        Utils.Preferences.setStringPrefs(this, Utils.Preferences.ACCOUNT_USER_ID, accountName);
                    }
                } else {
                    hideProgressDialog();
                }
                break;
        }

        if (!TextUtils.isEmpty(mCurrentFragmentTag)) {
            getSupportFragmentManager().findFragmentByTag(mCurrentFragmentTag).onActivityResult(requestCode, resultCode,
                    data);
        }
    }

    public Drive getDriveService() {
        return mDriveService;
    }

    public Plus getPlusService() {
        return mPlusService;
    }

}
