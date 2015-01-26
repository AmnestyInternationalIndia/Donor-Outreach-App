package in.org.amnesty.outreach.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import in.org.amnesty.outreach.OutreachApplication;
import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.adapters.DrawerAdapter;
import in.org.amnesty.outreach.helpers.Utils;

public class NavigationDrawerFragment extends BaseFragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final int FIRST_POSITION = 0;
    private int mCurrentSelectedPosition = FIRST_POSITION;
    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
    private ImageLoader mImageLoader;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }

        setupCache();
        selectItem(mCurrentSelectedPosition);
    }

    public void setupCache() {
        mImageLoader = OutreachApplication.getInstance().getImageLoader();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        initListHeader(inflater);
        initListeners();

        if (Utils.PreferenceUtils.getBooleanPrefs(getParentActivity(), Utils.PreferenceUtils.FIRST_LOGIN)) {
            mDrawerLayout.openDrawer(mDrawerLayout);
            Utils.PreferenceUtils.setBooleanPrefs(getParentActivity(), Utils.PreferenceUtils.FIRST_LOGIN, false);
        }

        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void setUp(View fragmentContainerView, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = fragmentContainerView;
        mDrawerLayout = drawerLayout;

        //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        getParentActivity().setSupportActionBar(toolbar);
        getParentActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getParentActivity().getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(getParentActivity(), mDrawerLayout, toolbar,
                R.string.app_name, R.string.app_name) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getParentActivity().getSupportActionBar().setTitle(R.string.app_name);
                getParentActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getParentActivity().getSupportActionBar().setTitle(R.string.app_name);
                getParentActivity().invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void initListeners() {

        DrawerAdapter drawerAdapter = DrawerAdapter.getInstance(getActivity().getLayoutInflater());
        mDrawerListView.setAdapter(drawerAdapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
    }

    private void initListHeader(LayoutInflater layoutInflater) {
        View view = layoutInflater.inflate(R.layout.drawer_list_header, null, false);
        TextView userId = (TextView) view.findViewById(R.id.drawerUserId);
        TextView userName = (TextView) view.findViewById(R.id.drawerUserName);
        NetworkImageView userImage = (NetworkImageView) view.findViewById(R.id.drawerUserImage);
        userId.setText(Utils.PreferenceUtils.getStringPrefs(getParentActivity(),
                Utils.PreferenceUtils.ACCOUNT_USER_ID));
        userName.setText(Utils.PreferenceUtils.getStringPrefs(getParentActivity(), Utils.PreferenceUtils.ACCOUNT_USER_NAME));
        String imageUrl = Utils.PreferenceUtils.getStringPrefs(getParentActivity(), Utils.PreferenceUtils.ACCOUNT_USER_IMAGE_URL);
        userImage.setImageUrl(imageUrl, mImageLoader);
        userImage.setErrorImageResId(R.drawable.ic_action_action_account_circle);
        mDrawerListView.addHeaderView(view, null, false);

    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }
}
