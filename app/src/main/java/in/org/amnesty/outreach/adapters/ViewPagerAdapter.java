package in.org.amnesty.outreach.adapters;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.fragments.BaseTabFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final int DEFAULT_PAGE_COUNT = 3;

    private Context mContext;

    public ViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        switch (position) {
            case BaseTabFragment.TYPE_VIDEO:
                bundle.putInt(BaseTabFragment.BUNDLE_CURRENT_TYPE, BaseTabFragment.TYPE_VIDEO);
                return BaseTabFragment.newInstance(bundle);
            case BaseTabFragment.TYPE_IMAGE:
                bundle.putInt(BaseTabFragment.BUNDLE_CURRENT_TYPE, BaseTabFragment.TYPE_IMAGE);
                return BaseTabFragment.newInstance(bundle);
            case BaseTabFragment.TYPE_PDF:
                bundle.putInt(BaseTabFragment.BUNDLE_CURRENT_TYPE, BaseTabFragment.TYPE_PDF);
                return BaseTabFragment.newInstance(bundle);
            default:
                return BaseTabFragment.newInstance(new Bundle());
        }
    }

    @Override
    public int getCount() {
        return DEFAULT_PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case BaseTabFragment.TYPE_VIDEO:
                return mContext.getString(R.string.label_videos);
            case BaseTabFragment.TYPE_IMAGE:
                return mContext.getString(R.string.label_images);
            default:
                return mContext.getString(R.string.label_pdf);
        }
    }
}
