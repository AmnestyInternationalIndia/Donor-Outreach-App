package in.org.amnesty.outreach.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

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

public class CitySelectorFragment extends BaseFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

	//public static final String TAG = CitySelectorFragment.class.getCanonicalName();
    private ListView mCityListView;
    private ArrayAdapter mCityListAdapter;
    private ArrayList<City> mCityList;
    private RelativeLayout mEmptyView;
    private Button mDownloadButton;
    private int mSelectedPosition;

	public static CitySelectorFragment newInstance(Bundle arguments) {
		CitySelectorFragment fragment =  new CitySelectorFragment();
        fragment.setArguments(arguments);
        return fragment;
	}

	public CitySelectorFragment () {
		// Required empty public constructor
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container,
	                          Bundle savedInstanceState) {
		View citySelectorView = inflater.inflate(R.layout.fragment_city_selector, container, false);
        Toolbar toolbar = (Toolbar) citySelectorView.findViewById(R.id.toolbar);
        getParentActivity().setSupportActionBar(toolbar);
        getParentActivity().setTitle("Select City");
        mCityListView = (ListView) citySelectorView.findViewById(R.id.cityList);
        mEmptyView = (RelativeLayout) citySelectorView.findViewById(R.id.empty);
        mDownloadButton = (Button) citySelectorView.findViewById(R.id.downloadButton);
        mCityList = new ArrayList<City>();
        mCityListAdapter = new ArrayAdapter(getParentActivity(), android.R.layout.simple_list_item_single_choice,
                mCityList);
        mCityListView.setAdapter(mCityListAdapter);
        mCityListView.setEmptyView(mEmptyView);
        mCityListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mCityListView.setOnItemClickListener(this);
        getParentActivity().setCurrentFragmentTag(HomeActivity.TAG_CITY_SELECTOR_FRAGMENT);
		return citySelectorView;
	}

    @Override
    public void onResume() {
        super.onResume();

        new CityLoaderTask(getParentActivity(), getParentActivity().getDriveService()).execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mDownloadButton.setOnClickListener(this);
        mDownloadButton.setVisibility(View.VISIBLE);
        mSelectedPosition = position;

        City city =  mCityList.get(mSelectedPosition);
        Utils.PreferenceUtils.setStringPrefs(getParentActivity(), Utils.PreferenceUtils.SELECTED_CITY_NAME, city.mName);
        Utils.PreferenceUtils.setStringPrefs(getParentActivity(), Utils.PreferenceUtils.SELECTED_CITY_FOLDER_ID, city.mFolderId);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.downloadButton:
                getParentActivity().switchFragment(HomeActivity.FRAGMENT_DOWNLOAD_PROGRESS, null);
                break;
        }
    }

    public class CityLoaderTask extends BaseAsyncTask {

        private Drive mDriveService;

        public CityLoaderTask(Activity activity, Drive driveService) {
            super(activity);
            mDriveService = driveService;
        }

        @Override
        protected void doInBackground() throws IOException {
            List<File> result = new ArrayList<File>();

            Drive.Files.List request =  mDriveService.files().list().setQ("mimeType = 'application/vnd.google-apps.folder' and title contains 'city-'");

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

            for(File file : result) {
                City city = new City();
                city.mName = Utils.capitalize(file.getTitle().replace("city-", ""));
                city.mFolderDownloadUrl = file.getDownloadUrl();
                city.mFolderId = file.getId();
                mCityList.add(city);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mCityListAdapter.notifyDataSetChanged();
        }
    }

    public class City {

        public String mName;
        public String mFolderDownloadUrl;
        public long mFolderSize;
        public String mFolderId;

        @Override
        public String toString() {
            return mName;
        }
    }
}
