package in.org.amnesty.outreach.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import in.org.amnesty.outreach.R;

public class DrawerAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    public DrawerAdapter(LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    public static DrawerAdapter getInstance(LayoutInflater layoutInflater) {
        return new DrawerAdapter(layoutInflater);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        switch (position) {
            default:
                convertView = layoutInflater.inflate(R.layout.drawer_list_item, null);
                ViewHolder.innerTextView = (TextView) convertView.findViewById(R.id.drawerItemTextView);
                ViewHolder.innerImageView = (ImageView) convertView.findViewById(R.id.drawerItemImageView);
                ViewHolder.innerTextView.setText(getText(position));
                ViewHolder.innerImageView.setImageResource(getInnerImage(position));
                break;
        }
        return convertView;
    }

    public int getText(int position) {
        switch (position) {
            case 0:
                return R.string.label_videos;
            case 1:
                return R.string.label_images;
            default:
                return R.string.label_starred;
        }
    }

    public int getInnerImage(int position) {
        switch (position) {
            case 0:
                return R.drawable.ic_action_action_account_circle;
            case 1:
                return R.drawable.ic_action_av_video_collection;
            default:
                return R.drawable.ic_action_editor_insert_drive_file;
        }
    }

    public static class ViewHolder {
        public static TextView innerTextView;
        public static ImageView innerImageView;
    }
}