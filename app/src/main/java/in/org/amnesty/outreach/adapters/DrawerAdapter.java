package in.org.amnesty.outreach.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import in.org.amnesty.outreach.R;

public class DrawerAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;

	public static DrawerAdapter getInstance(LayoutInflater layoutInflater) {
		return new DrawerAdapter(layoutInflater);
	}

	public DrawerAdapter(LayoutInflater layoutInflater) {
		this.layoutInflater = layoutInflater;
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	public static class ViewHolder {
		public static TextView innerTextView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		switch (position) {
		default:
			convertView =  layoutInflater.inflate(R.layout.drawer_list_item, null);
			ViewHolder.innerTextView = (TextView) convertView.findViewById(R.id.drawerItemTextView);
			ViewHolder.innerTextView.setText(getText(position));
			ViewHolder.innerTextView.setCompoundDrawablesWithIntrinsicBounds(getInnerImage(position), 0, 0, 0);
			break;
		}
		return convertView;
	}

	public int getText(int position) {
		switch (position) {
		case 0:
			return R.string.drawer_item_home;
		case 1:
			return R.string.drawer_item_video;
		default:
			return R.string.drawer_item_pdf;
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
}