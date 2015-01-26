package in.org.amnesty.outreach.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.activity.HomeActivity;
import in.org.amnesty.outreach.fragments.VideoPlayerFragment;

public class VideoAdapter extends CursorRecyclerViewAdapter<VideoAdapter.ViewHolder> {

    private Context mContext;

    public VideoAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.base_list_item_video, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, new ViewHolder.OnButtonClickListener() {
            @Override
            public void onShare(int position) {
                getCursor().moveToPosition(position);
            }

            @Override
            public void onPlay(int position) {
                Cursor cursor = getCursor();
                cursor.moveToPosition(position);

                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                Bundle arguments = new Bundle();
                arguments.putString(VideoPlayerFragment.VIDEO_FILE_NAME, fileName);
                ((HomeActivity) mContext).switchFragment(HomeActivity.FRAGMENT_VIDEO_PLAYER, arguments);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        int videoId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
        String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap mThumbnailBitmap = MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(), videoId,
                MediaStore.Video.Thumbnails.MICRO_KIND,
                options);
        viewHolder.mThumbnailView.setImageBitmap(mThumbnailBitmap);
        viewHolder.mFileNameView.setText(fileName);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mThumbnailView;
        public TextView mFileNameView;
        public Button mShareVideo;
        public Button mPlayVideo;
        public OnButtonClickListener mOnButtonClickListener;

        public ViewHolder(View v, OnButtonClickListener onButtonClickListener) {
            super(v);
            mOnButtonClickListener = onButtonClickListener;
            mThumbnailView = (ImageView) v.findViewById(R.id.thumbnailImageView);
            mFileNameView = (TextView) v.findViewById(R.id.fileNameView);
            mShareVideo = (Button) v.findViewById(R.id.shareFile);
            mPlayVideo = (Button) v.findViewById(R.id.playFile);

            mFileNameView.setOnClickListener(this);
            mShareVideo.setOnClickListener(this);
            mPlayVideo.setOnClickListener(this);
        }

        public static interface OnButtonClickListener {
            public void onShare(int position);
            public void onPlay(int position);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fileNameView:
                    v.setSelected(true);
                    break;
                case R.id.shareFile:
                    mOnButtonClickListener.onShare(getPosition());
                    break;
                case R.id.playFile:
                    mOnButtonClickListener.onPlay(getPosition());
                    break;
            }
        }
    }
}