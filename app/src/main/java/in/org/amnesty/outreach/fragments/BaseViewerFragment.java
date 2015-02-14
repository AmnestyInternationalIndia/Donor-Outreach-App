package in.org.amnesty.outreach.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import java.io.File;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.activity.HomeActivity;
import in.org.amnesty.outreach.helpers.Utils;

public class BaseViewerFragment extends BaseFragment {

    public static final String FILE_NAME = "fileName";

    private VideoView mVideoView;

    private int mCurrentType;

    public BaseViewerFragment() {
        // Required empty public constructor
    }

    public static BaseViewerFragment newInstance(Bundle arguments) {
        BaseViewerFragment fragment = new BaseViewerFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentType = savedInstanceState.getInt(Utils.Constants.CURRENT_FRAGMENT_TYPE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View pdfViewerView = inflater.inflate(R.layout.fragment_base_viewer, container, false);
        PDFView mPdfView = (PDFView) pdfViewerView.findViewById(R.id.pdfView);

        mVideoView = (VideoView) pdfViewerView.findViewById(R.id.videoView);
        ImageView mImageView = (ImageView) pdfViewerView.findViewById(R.id.imageView);
        MediaController mediaController = new MediaController(getParentActivity());

        Toolbar toolbar = (Toolbar) pdfViewerView.findViewById(R.id.toolbar);
        getParentActivity().setSupportActionBar(toolbar);
        getParentActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle arguments = getArguments();

        if (arguments != null) {
            String fileName = arguments.getString(FILE_NAME);
            getParentActivity().setTitle(fileName);
            mCurrentType = arguments.getInt(BaseTabFragment.BUNDLE_CURRENT_TYPE, BaseTabFragment.TYPE_VIDEO);
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    Utils.Constants.DEFAULT_APP_FOLDER_WITH_SLASH + fileName;
            mVideoView.setVisibility(View.GONE);
            mImageView.setVisibility(View.GONE);
            mPdfView.setVisibility(View.GONE);
            switch (mCurrentType) {
                case BaseTabFragment.TYPE_VIDEO:
                    Uri videoUri = Uri.parse(
                            Environment.getExternalStorageDirectory().getAbsolutePath() +
                                    Utils.Constants.DEFAULT_APP_FOLDER_WITH_SLASH + fileName);

                    mVideoView.setVideoURI(videoUri);
                    mVideoView.setMediaController(mediaController);
                    mVideoView.start();
                    mVideoView.setVisibility(View.VISIBLE);
                    break;
                case BaseTabFragment.TYPE_IMAGE:
                    Uri imageUri = Uri.parse(
                            Environment.getExternalStorageDirectory().getAbsolutePath() +
                                    Utils.Constants.DEFAULT_APP_FOLDER_WITH_SLASH + fileName);
                    mImageView.setImageURI(imageUri);
                    mImageView.setVisibility(View.VISIBLE);
                    break;
                case BaseTabFragment.TYPE_PDF:
                    Utils.Logs.d(filePath);
                    mPdfView.fromFile(new File(filePath)).defaultPage(1)
                            .showMinimap(true)
                            .enableSwipe(true)
                            .onLoad(new OnLoadCompleteListener() {
                                @Override
                                public void loadComplete(int i) {

                                }
                            })
                            .onPageChange(new OnPageChangeListener() {
                                @Override
                                public void onPageChanged(int i, int i2) {

                                }
                            })
                            .load();
                    mPdfView.setVisibility(View.VISIBLE);
                    break;
            }
        }

        setHasOptionsMenu(true);
        getParentActivity().setCurrentFragmentTag(HomeActivity.TAG_VIEWER_FRAGMENT);
        return pdfViewerView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mVideoView != null) {
                    if (mVideoView.isPlaying()) {
                        mVideoView.stopPlayback();
                    }
                }
                getParentActivity().switchFragment(HomeActivity.FRAGMENT_HOME, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Utils.Constants.CURRENT_FRAGMENT_TYPE, mCurrentType);
    }
}
