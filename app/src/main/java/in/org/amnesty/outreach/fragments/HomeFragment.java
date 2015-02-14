package in.org.amnesty.outreach.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.adapters.ViewPagerAdapter;
import in.org.amnesty.outreach.helpers.Utils;
import in.org.amnesty.outreach.services.DownloadService;

public class HomeFragment extends BaseFragment {

    private static final String STATE_SELECTED_TAB = "selected_tab";
    private ViewPager mViewPager;
    private int mCurrentTab;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(Bundle arguments) {
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_home, null);


        getParentActivity().setTitle(R.string.app_name);

        mViewPager = (ViewPager) homeView.findViewById(R.id.viewPager);

        mViewPager.setAdapter(new ViewPagerAdapter(getParentActivity(), getChildFragmentManager()));
        mViewPager.setOffscreenPageLimit(3);

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip mPagerSlidingTabStrip = (PagerSlidingTabStrip) homeView.findViewById(R.id.tabs);
        mPagerSlidingTabStrip.setViewPager(mViewPager);

        BaseDrawerFragment mNavigationDrawerFragment = (BaseDrawerFragment) getChildFragmentManager().findFragmentById(
                R.id.navigationDrawer);

        mNavigationDrawerFragment.setUp((DrawerLayout) homeView.findViewById(R.id.drawerLayout), (Toolbar)
                homeView.findViewById(R.id.toolbar));

        if (savedInstanceState != null) {
            mCurrentTab = savedInstanceState.getInt(STATE_SELECTED_TAB, 0);
        }

        setHasOptionsMenu(true);
        return homeView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCurrentTab > 0) {
            mViewPager.setCurrentItem(mCurrentTab);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_home, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(Utils.Device.enabled(getParentActivity(), Utils.Device.INTERNET)) {
            Intent downloadIntent = new Intent(getParentActivity(), DownloadService.class);
            downloadIntent.setAction(DownloadService.ACTION_DOWNLOAD);
            getParentActivity().startService(downloadIntent);
        } else {
            Toast.makeText(getParentActivity(), getParentActivity().getString(R.string.no_internet_access), Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_TAB, mViewPager.getCurrentItem());
    }
}
