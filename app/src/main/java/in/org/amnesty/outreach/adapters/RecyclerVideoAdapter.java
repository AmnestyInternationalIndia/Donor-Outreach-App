package in.org.amnesty.outreach.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import in.org.amnesty.outreach.fragments.BaseTabFragment;
import in.org.amnesty.outreach.fragments.BaseViewerFragment;
import in.org.amnesty.outreach.helpers.Utils;

public class RecyclerVideoAdapter extends CursorRecyclerViewAdapter<RecyclerVideoAdapter.ViewHolder> {

    private Context mContext;

    public RecyclerVideoAdapter(Context context, Cursor cursor) {
        super(cursor);
        mContext = context;
    }

    @Override
    public RecyclerVideoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.base_list_item_video, parent, false);
        return new ViewHolder(view, new ViewHolder.OnButtonClickListener() {
            @Override
            public void onShare(int position) {
                getCursor().moveToPosition(position);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                Uri imageUri = Uri.parse(
                        Environment.getExternalStorageDirectory().getAbsolutePath() +
                                Utils.Constants.DEFAULT_APP_FOLDER_WITH_SLASH + getCursor().getColumnIndex(MediaStore
                                .Images.Media.DISPLAY_NAME));
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setType("video/mp4");
                mContext.startActivity(Intent.createChooser(shareIntent, mContext.getResources().getText(R.string
                        .label_send_to)));
            }

            @Override
            public void onPlay(int position) {
                Cursor cursor = getCursor();
                cursor.moveToPosition(position);

                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                Bundle arguments = new Bundle();
                arguments.putString(BaseViewerFragment.FILE_NAME, fileName);
                arguments.putInt(BaseTabFragment.BUNDLE_CURRENT_TYPE, BaseTabFragment.TYPE_VIDEO);
                ((HomeActivity) mContext).switchFragment(HomeActivity.FRAGMENT_BASE_VIEWER, arguments);
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        int videoId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
        String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap mThumbnailBitmap = MediaStore.Video.Thumbnails.getThumbnail(mContext.getContentResolver(), videoId,
                MediaStore.Video.Thumbnails.MINI_KIND,
                options);
        viewHolder.mThumbnailView.setImageBitmap(mThumbnailBitmap);
        viewHolder.mFileNameView.setText(Utils.Storage.removeExtension(fileName));
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

        public static interface OnButtonClickListener {
            public void onShare(int position);

            public void onPlay(int position);
        }
    }
}