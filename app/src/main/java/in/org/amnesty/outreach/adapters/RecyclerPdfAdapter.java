package in.org.amnesty.outreach.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import in.org.amnesty.outreach.R;
import in.org.amnesty.outreach.activity.HomeActivity;
import in.org.amnesty.outreach.fragments.BaseTabFragment;
import in.org.amnesty.outreach.fragments.BaseViewerFragment;
import in.org.amnesty.outreach.helpers.Utils;

public class RecyclerPdfAdapter extends RecyclerView.Adapter<RecyclerPdfAdapter.ViewHolder> {

    private ArrayList<java.io.File> mFiles;

    private Context mContext;

    public RecyclerPdfAdapter(Context context, ArrayList<java.io.File> files) {
        mContext = context;
        mFiles = files;
    }

    @Override
    public int getItemCount() {
        return (mFiles != null) ? mFiles.size() : 0;
    }

    public void swapDataSet(ArrayList<java.io.File> files) {
        mFiles = files;
        notifyDataSetChanged();
    }


    @Override
    public RecyclerPdfAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.base_list_item_pdf, parent, false);
        return new ViewHolder(view, new ViewHolder.OnButtonClickListener() {
            @Override
            public void onShare(int position) {
                String fileName = mFiles.get(position).getName();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                Uri imageUri = Uri.parse(
                        Environment.getExternalStorageDirectory().getAbsolutePath() +
                                Utils.Constants.DEFAULT_APP_FOLDER_WITH_SLASH + fileName);
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                shareIntent.setType("application/pdf");
                mContext.startActivity(Intent.createChooser(shareIntent, mContext.getResources().getText(R.string
                        .label_send_to)));
            }

            @Override
            public void onRead(int position) {
                String fileName = mFiles.get(position).getName();
                Bundle arguments = new Bundle();
                arguments.putString(BaseViewerFragment.FILE_NAME, fileName);
                arguments.putInt(BaseTabFragment.BUNDLE_CURRENT_TYPE, BaseTabFragment.TYPE_PDF);
                ((HomeActivity) mContext).switchFragment(HomeActivity.FRAGMENT_BASE_VIEWER, arguments);
            }
        });
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        String fileName = mFiles.get(position).getName();
        viewHolder.mFileNameView.setText(Utils.Storage.removeExtension(fileName));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mThumbnailView;
        public TextView mFileNameView;
        public Button mShareFile;
        public Button mReadPdf;
        public OnButtonClickListener mOnButtonClickListener;

        public ViewHolder(View v, OnButtonClickListener onButtonClickListener) {
            super(v);
            mOnButtonClickListener = onButtonClickListener;
            mThumbnailView = (ImageView) v.findViewById(R.id.thumbnailImageView);
            mFileNameView = (TextView) v.findViewById(R.id.fileNameView);
            mReadPdf = (Button) v.findViewById(R.id.readFile);
            mShareFile = (Button) v.findViewById(R.id.shareFile);
            mShareFile.setOnClickListener(this);
            mFileNameView.setOnClickListener(this);
            mReadPdf.setOnClickListener(this);
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
                case R.id.readFile:
                    mOnButtonClickListener.onRead(getPosition());
                    break;
            }
        }

        public static interface OnButtonClickListener {
            public void onShare(int position);

            public void onRead(int position);
        }
    }
}