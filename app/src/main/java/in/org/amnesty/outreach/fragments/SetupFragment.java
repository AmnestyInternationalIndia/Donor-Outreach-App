package in.org.amnesty.outreach.fragments;


import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.activity.HomeActivity;
import in.org.amnesty.outreach.asynctasks.BaseAsyncTask;
import in.org.amnesty.outreach.helpers.Utils;
import in.org.amnesty.outreach.services.DownloadService;

public class SetupFragment extends BaseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    //public static final String TAG = SetupFragment.class.getCanonicalName();

    public static final int TYPE_CITY = 0;

    public static final int TYPE_DOWNLOAD = 1;

    private ArrayAdapter<City> mCityListAdapter;

    private ArrayList<City> mCityList;

    private Button mDownloadButton;

    private int mCurrentSelectedPosition;

    private DownloadProgressReceiver mSyncBroadcastReceiver;

    private TextView mStatusTextView;

    private TextView mCurrentItemTextView;

    private TextView mTotalItemTextView;

    private ProgressBar mDownloadProgressBar;

    private LocalBroadcastManager mLocalBroadcastManager;

    private int mCurrentType;

    private LinearLayout mCitySetupLayout;

    private LinearLayout mDownloadProgressLayout;

    private LinearLayout mDownloadProgressCounterLayout;

    private RelativeLayout mProgressLayout;

    private RelativeLayout mContentLayout;

    public SetupFragment() {
        // Required empty public constructor
    }

    public static SetupFragment newInstance(Bundle arguments) {
        SetupFragment fragment = new SetupFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(Utils.Constants.STATE_SELECTED_POSITION);
        }

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getParentActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View setupView = inflater.inflate(R.layout.fragment_setup, container, false);

        Toolbar toolbar = (Toolbar) setupView.findViewById(R.id.toolbar);
        getParentActivity().setSupportActionBar(toolbar);

        mCitySetupLayout = (LinearLayout) setupView.findViewById(R.id.citySetupLayout);
        mDownloadProgressLayout = (LinearLayout) setupView.findViewById(R.id.downloadProgressLayout);
        mDownloadProgressCounterLayout = (LinearLayout) setupView.findViewById(R.id.downloadProgressCounterLayout);
        mContentLayout = (RelativeLayout) setupView.findViewById(R.id.contentLayout);
        RelativeLayout mNoContentLayout = (RelativeLayout) setupView.findViewById(R.id.noContentLayout);
        mProgressLayout = (RelativeLayout) setupView.findViewById(R.id.progressLayout);
        mStatusTextView = (TextView) setupView.findViewById(R.id.statusTextView);
        mCurrentItemTextView = (TextView) setupView.findViewById(R.id.currentItemTextView);
        mDownloadProgressBar = (ProgressBar) setupView.findViewById(R.id.downloadProgressBar);
        mTotalItemTextView = (TextView) setupView.findViewById(R.id.totalItemTextView);
        Bundle arguments = getArguments();

        if (arguments != null) {
            mCurrentType = arguments.getInt(BaseTabFragment.BUNDLE_CURRENT_TYPE, TYPE_CITY);

            switch (mCurrentType) {
                case TYPE_CITY:
                    getParentActivity().setTitle(getParentActivity().getString(R.string.activity_title_select_city));
                    ListView mCityListView = (ListView) setupView.findViewById(R.id.cityList);
                    mDownloadButton = (Button) setupView.findViewById(R.id.downloadButton);
                    mCityList = new ArrayList<>();
                    mCityListAdapter = new ArrayAdapter<>(getParentActivity(), R.layout.base_list_item_city,
                            mCityList);
                    mCityListView.setAdapter(mCityListAdapter);
                    mCityListView.setEmptyView(mNoContentLayout);
                    mCityListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    mCityListView.setOnItemClickListener(this);
                    mDownloadProgressLayout.setVisibility(View.GONE);
                    break;
                case TYPE_DOWNLOAD:
                    getParentActivity().setTitle(getParentActivity().getString(R.string.activity_title_downloading));
                    mCitySetupLayout.setVisibility(View.GONE);
                    break;
            }
        }

        getParentActivity().setCurrentFragmentTag(HomeActivity.TAG_SETUP_FRAGMENT);
        return setupView;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_START);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_PROGRESS);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        mSyncBroadcastReceiver = new DownloadProgressReceiver();
        mLocalBroadcastManager.registerReceiver(mSyncBroadcastReceiver, intentFilter);

        switch (mCurrentType) {
            case TYPE_CITY:
                mCitySetupLayout.setVisibility(View.VISIBLE);
                mDownloadProgressLayout.setVisibility(View.GONE);
                new CityLoaderTask(getParentActivity(), getParentActivity().getDriveService()).execute();
                break;
            case TYPE_DOWNLOAD:
                mCitySetupLayout.setVisibility(View.GONE);
                mDownloadProgressLayout.setVisibility(View.VISIBLE);

                if (!Utils.Preferences.getBooleanPrefs(getParentActivity(), Utils.Preferences.IS_DOWNLOADING)) {
                    Intent downloadIntent = new Intent(DownloadService.ACTION_DOWNLOAD_START);
                    getParentActivity().sendBroadcast(downloadIntent);
                } else {
                    int totalItems = Utils.Preferences.getIntPrefs(getParentActivity(), Utils.Preferences.TOTAL_ITEMS);
                    int totalDownloadedItems = Utils.Preferences
                            .getIntPrefs(getParentActivity(), Utils.Preferences.TOTAL_DOWNLOADED_ITEMS);
                    mDownloadProgressCounterLayout.setVisibility(View.VISIBLE);
                    mStatusTextView.setText(getParentActivity().getString(R.string.message_downloading));
                    if (totalDownloadedItems == 0) {
                        totalDownloadedItems++;
                    }
                    mCurrentItemTextView.setText(String.valueOf(totalDownloadedItems));
                    mTotalItemTextView.setText(String.valueOf(totalItems));
                }
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mDownloadButton.setOnClickListener(this);
        mDownloadButton.setVisibility(View.VISIBLE);
        mCurrentSelectedPosition = position;

        City city = mCityList.get(mCurrentSelectedPosition);
        Utils.Preferences.setStringPrefs(getParentActivity(), Utils.Preferences.SELECTED_CITY_NAME, city.mName);
        Utils.Preferences
                .setStringPrefs(getParentActivity(), Utils.Preferences.SELECTED_CITY_FOLDER_ID, city.mFolderId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.downloadButton:
                getParentActivity().setTitle(getParentActivity().getString(R.string.activity_title_downloading));
                mCitySetupLayout.setVisibility(View.GONE);
                mDownloadProgressLayout.setVisibility(View.VISIBLE);
                Intent downloadIntent = new Intent(DownloadService.ACTION_DOWNLOAD_START);
                getParentActivity().sendBroadcast(downloadIntent);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mSyncBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Utils.Constants.STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    public class CityLoaderTask extends BaseAsyncTask {

        private static final String SEARCH_QUERY =
                "mimeType = 'application/vnd.google-apps.folder' and title contains 'city-'";
        private Drive mDriveService;

        public CityLoaderTask(Activity activity, Drive driveService) {
            super(activity);
            mDriveService = driveService;
        }

        @Override
        protected void doInBackground() throws IOException {
            List<File> result = new ArrayList<>();

            Drive.Files.List request = mDriveService.files().list()
                                                    .setQ(SEARCH_QUERY);

            do {
                try {
                    FileList files = request.execute();
                    result.addAll(files.getItems());
                    request.setPageToken(files.getNextPageToken());
                } catch (IOException e) {
                    e.printStackTrace();
                    request.setPageToken(null);
                }
            } while (request.getPageToken() != null &&
                    request.getPageToken().length() > 0);

            for (File file : result) {
                City city = new City();
                city.mName = Utils.Text.capitalize(file.getTitle().replace("city-", ""));
                city.mFolderDownloadUrl = file.getDownloadUrl();
                city.mFolderId = file.getId();
                mCityList.add(city);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mCityListAdapter.notifyDataSetChanged();
            mProgressLayout.setVisibility(View.GONE);
            mContentLayout.setVisibility(View.VISIBLE);
        }
    }

    private class City {
        public String mName;
        public String mFolderDownloadUrl;
        public String mFolderId;

        @Override
        public String toString() {
            return mName;
        }
    }

    private class DownloadProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equalsIgnoreCase(DownloadService.ACTION_DOWNLOAD_PROGRESS)) {

                int totalItems = Utils.Preferences.getIntPrefs(getParentActivity(), Utils.Preferences.TOTAL_ITEMS);
                int totalDownloadedItems =
                        Utils.Preferences.getIntPrefs(getParentActivity(), Utils.Preferences.TOTAL_DOWNLOADED_ITEMS);

                mDownloadProgressCounterLayout.setVisibility(View.VISIBLE);
                mStatusTextView.setText(context.getString(R.string.message_downloading));
                if (totalDownloadedItems == 0) {
                    totalDownloadedItems++;
                }
                mCurrentItemTextView.setText(String.valueOf(totalDownloadedItems));
                mTotalItemTextView.setText(String.valueOf(totalItems));
            } else {
                mStatusTextView.setText(context.getString(R.string.message_download_complete));
                mDownloadProgressCounterLayout.setVisibility(View.GONE);
                mDownloadProgressBar.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getParentActivity().switchFragment(HomeActivity.FRAGMENT_HOME, null);
                    }
                }, 1000);
            }
        }
    }
}
