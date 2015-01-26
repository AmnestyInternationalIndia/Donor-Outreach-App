package in.org.amnesty.outreach.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.adapters.VideoAdapter;
import in.org.amnesty.outreach.helpers.Utils;

public class HomeFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final int LOADER_ID = 1001;



    private RecyclerView mVideoRecyclerView;

    private VideoAdapter mVideoRecyclerAdapter;

    private GridLayoutManager mLayoutManager;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(Bundle arguments) {
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initLoader(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_home, null);

        mVideoRecyclerView = (RecyclerView) homeView.findViewById(R.id.videoRecyclerView);
        mVideoRecyclerView.setHasFixedSize(true);

        mLayoutManager = new GridLayoutManager(getParentActivity(), 2);
        mLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
        mVideoRecyclerView.setLayoutManager(mLayoutManager);

        mVideoRecyclerAdapter = new VideoAdapter(getParentActivity(), null);
        mVideoRecyclerView.setAdapter(mVideoRecyclerAdapter);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getChildFragmentManager().findFragmentById(
                R.id.navigationDrawer);
        mNavigationDrawerFragment.setUp(homeView.findViewById(R.id.navigationDrawer), (DrawerLayout) homeView.findViewById(R.id
                .drawerLayout), (Toolbar) homeView.findViewById(R.id.toolbar));

        setHasOptionsMenu(true);
        return homeView;
    }

    @Override
    public void onResume() {
        super.onResume();
        initLoader(true);
    }


    private void initLoader(boolean mReset) {
        if (mReset) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
            return;
        }
        getLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MediaStore.Video.Media.DATA + " like ?";
        String[] selectionArgs = new String[]{"%" + Utils.Constants.DEFAULT_APP_FOLDER + "%"};
        String[] projection = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE};
        return new CursorLoader(getActivity(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs,
                MediaStore.Video.Media.DATE_TAKEN + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!Utils.isCursorEmpty(data)) {
            mVideoRecyclerAdapter.swapCursor(data);
            mVideoRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

    }
}
