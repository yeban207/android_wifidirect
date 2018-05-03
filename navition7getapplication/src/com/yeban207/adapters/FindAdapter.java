package com.yeban207.adapters;



import java.util.List;

import com.yeban207.navition7getapplication.R;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FindAdapter extends BaseAdapter {

	private Context context;
	private List<WifiP2pDevice> mList;


	public FindAdapter(Context context, List<WifiP2pDevice> mList) {
		this.context = context;
		this.mList = mList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.listview_find_item, null);
			viewHolder = new ViewHolder();

			viewHolder.findDeviceName = (TextView) convertView.findViewById(R.id.tv_find_name);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.findDeviceName.setText(mList.get(position).deviceName);

		return convertView;
	}

	private class ViewHolder {
		TextView findDeviceName;
	}

}
