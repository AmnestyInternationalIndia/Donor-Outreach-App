package in.org.amnesty.outreach.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.activity.HomeActivity;
import in.org.amnesty.outreach.helpers.Utils;

public class VideoPlayerFragment extends BaseFragment {

    public static final String VIDEO_FILE_NAME = "videoFileName";

    private VideoView mVideoView;

    public VideoPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static VideoPlayerFragment newInstance(Bundle arguments) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View videoPlayerView = inflater.inflate(R.layout.fragment_player, container, false);

        mVideoView = (VideoView) videoPlayerView.findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(getParentActivity());
        Toolbar toolbar = (Toolbar) videoPlayerView.findViewById(R.id.toolbar);
        getParentActivity().setSupportActionBar(toolbar);
        getParentActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle arguments = getArguments();
        Uri videoUri;

        if (arguments != null) {
            String videoFileName = arguments.getString(VIDEO_FILE_NAME);
            getParentActivity().setTitle(videoFileName);
            videoUri = Uri.parse(
                    Environment.getExternalStorageDirectory().getAbsolutePath() +
                            Utils.Constants.DEFAULT_APP_FOLDER_WITH_SLASH + videoFileName);

            if (videoUri != null) {
                mVideoView.setVideoURI(videoUri);
                mVideoView.setMediaController(mediaController);
                mVideoView.start();
            }
        }

        getParentActivity().setCurrentFragmentTag(HomeActivity.TAG_VIDEO_PLAYER_FRAGMENT);
        return videoPlayerView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mVideoView.suspend();
                getParentActivity().switchFragment(HomeActivity.FRAGMENT_HOME, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
