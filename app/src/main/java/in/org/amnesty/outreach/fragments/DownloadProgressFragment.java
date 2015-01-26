package in.org.amnesty.outreach.fragments;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.activity.HomeActivity;
import in.org.amnesty.outreach.helpers.Utils;
import in.org.amnesty.outreach.services.DownloadService;

/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadProgressFragment extends BaseFragment {

    private DownloadProgressReceiver mSyncBroadcastReceiver;
    private TextView mStatusTextView;
    private TextView mCurrentItemTextView;
    private ProgressBar mDownloadProgressBar;
    private LocalBroadcastManager mLocalBroadcastManager;


    public static DownloadProgressFragment newInstance(Bundle arguments) {
		DownloadProgressFragment fragment = new DownloadProgressFragment();
        fragment.setArguments(arguments);
		return fragment;
	}

	public DownloadProgressFragment () {
		// Required empty public constructor
	}


	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container,
	                          Bundle savedInstanceState) {
		View downloadProgressView = inflater.inflate(R.layout.fragment_download_progress, container, false);
        Toolbar toolbar = (Toolbar) downloadProgressView.findViewById(R.id.toolbar);
        getParentActivity().setSupportActionBar(toolbar);
        getParentActivity().setTitle("Downloading files");
        mStatusTextView = (TextView) downloadProgressView.findViewById(R.id.statusTextView);
        mCurrentItemTextView = (TextView) downloadProgressView.findViewById(R.id.currentItemTextView);
        mDownloadProgressBar = (ProgressBar) downloadProgressView.findViewById(R.id.downloadProgressBar);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getParentActivity());
        getParentActivity().setCurrentFragmentTag(HomeActivity.TAG_DOWNLOAD_PROGRESS_FRAGMENT);
        return downloadProgressView;
	}

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_START);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_PROGRESS);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_COMPLETE);
        mSyncBroadcastReceiver = new DownloadProgressReceiver();
        mLocalBroadcastManager.registerReceiver(mSyncBroadcastReceiver, intentFilter);

        Intent downloadIntent = new Intent(DownloadService.ACTION_DOWNLOAD_START);
        getParentActivity().sendBroadcast(downloadIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mSyncBroadcastReceiver);
    }

    private class DownloadProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equalsIgnoreCase(DownloadService.ACTION_DOWNLOAD_PROGRESS)) {

                String currentFileName = intent.getStringExtra(DownloadService.KEY_CURRENT_ITEM);
                String totalFiles = intent.getStringExtra(DownloadService.KEY_TOTAL_ITEM);

                mStatusTextView.setText("Downloading ...");

                if(!TextUtils.isEmpty(currentFileName)) {
                    mCurrentItemTextView.setText(currentFileName);
                }
            } else {
                mStatusTextView.setText("Download complete.");
                mCurrentItemTextView.setVisibility(View.GONE);
                mDownloadProgressBar.setVisibility(View.GONE);
                Utils.PreferenceUtils.setBooleanPrefs(context, Utils.PreferenceUtils.APP_INITIALIZATION_STATUS, true);

                // add greetings for first login
                getParentActivity().switchFragment(HomeActivity.FRAGMENT_HOME, null);
            }
        }
    }
}
