package in.org.amnesty.outreach.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import in.org.amnesty.outreach.OutreachApplication;
import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.adapters.DrawerAdapter;
import in.org.amnesty.outreach.helpers.Utils;

public class BaseDrawerFragment extends BaseFragment {

    private static final int FIRST_POSITION = 0;
    private int mCurrentSelectedPosition = FIRST_POSITION;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private ImageLoader mImageLoader;
    private Button mLogoutButton;
    private LinearLayout mDrawerContainerLayout;

    public BaseDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(Utils.Constants.STATE_SELECTED_POSITION);
        }

        setupCache();
        selectItem(mCurrentSelectedPosition);
    }

    public void setupCache() {
        mImageLoader = OutreachApplication.getInstance().getImageLoader();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View drawerView = inflater.inflate(R.layout.fragment_base_drawer, null);

        mDrawerListView = (ListView) drawerView.findViewById(R.id.drawerListView);

        mLogoutButton = (Button) drawerView.findViewById(R.id.logoutButton);

        mDrawerContainerLayout = (LinearLayout) drawerView.findViewById(R.id.drawerContainerLayout);

        initListHeader(drawerView);

        initListeners();

        if (Utils.Preferences.getBooleanPrefs(getParentActivity(), Utils.Preferences.FIRST_LOGIN)) {
            mDrawerLayout.openDrawer(mDrawerContainerLayout);
            Utils.Preferences.setBooleanPrefs(getParentActivity(), Utils.Preferences.FIRST_LOGIN, false);
        }

        return drawerView;
    }

    public void setUp(DrawerLayout drawerLayout, Toolbar toolbar) {

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

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //closeDrawer();

                new AlertDialog.Builder(getParentActivity()).setMessage
                        (getParentActivity().getString(R.string
                                .message_logout)).setPositiveButton(getParentActivity().getString(R.string
                                .button_logout),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                new AsyncTask<Void, Void, Void>() {

                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                        getParentActivity().showProgressDialog(getParentActivity().getString(R.string
                                                .message_logout_progress));
                                    }

                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        Utils.Preferences.clearPrefs(getParentActivity());
                                        try {
                                            Utils.Storage.deleteDefaultAppDirectory(getParentActivity());
                                            Utils.Storage.clearMediaStorage(getParentActivity());
                                        } catch (Utils.ExternalStorageNotFoundException e) {
                                            e.printStackTrace();
                                        }

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        getParentActivity().hideProgressDialog();
                                        getParentActivity().finish();
                                    }
                                }.execute();
                            }
                        }).create().show();

            }
        });
    }

    private void initListHeader(View view) {
        TextView userId = (TextView) view.findViewById(R.id.drawerUserId);
        TextView userName = (TextView) view.findViewById(R.id.drawerUserName);
        NetworkImageView userImage = (NetworkImageView) view.findViewById(R.id.drawerUserImage);
        userId.setText(Utils.Preferences.getStringPrefs(getParentActivity(),
                Utils.Preferences.ACCOUNT_USER_ID));
        userName.setText(Utils.Preferences.getStringPrefs(getParentActivity(), Utils.Preferences.ACCOUNT_USER_NAME));
        String imageUrl = Utils.Preferences
                .getStringPrefs(getParentActivity(), Utils.Preferences.ACCOUNT_USER_IMAGE_URL);
        userImage.setImageUrl(imageUrl, mImageLoader);
        userImage.setErrorImageResId(R.drawable.ic_action_action_account_circle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }

        closeDrawer();

//        if (mCallbacks != null) {
//            mCallbacks.onNavigationDrawerItemSelected(position);
//        }
    }

    private void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawerContainerLayout);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Utils.Constants.STATE_SELECTED_POSITION, mCurrentSelectedPosition);
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
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);

    }

//    public static interface NavigationDrawerCallbacks {
//        void onNavigationDrawerItemSelected(int position);
//    }
}
