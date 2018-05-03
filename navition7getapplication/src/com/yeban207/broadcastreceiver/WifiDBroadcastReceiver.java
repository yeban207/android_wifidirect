package com.yeban207.broadcastreceiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.widget.Toast;

public class WifiDBroadcastReceiver extends BroadcastReceiver {
	private Context context;

	//private MyFragmentWifiDirect fg;
	private WifiP2pManager mManager;
	private WifiP2pManager.Channel mChannel;
	private PeerListListener peerListener;
	private ConnectionInfoListener mInfoListener;
	private GroupInfoListener mGroupListener;

	public WifiDBroadcastReceiver(WifiP2pManager mManager, Channel mChannel, PeerListListener peerlistener,
			ConnectionInfoListener mInfoListener, GroupInfoListener mGroupListener) {
		// TODO Auto-generated constructor stub
		this.mManager = mManager;
		this.mChannel = mChannel;
		this.peerListener = peerlistener;
		this.mInfoListener = mInfoListener;
		this.mGroupListener = mGroupListener;
	}

	@SuppressLint("InlinedApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context = context;
		String action = intent.getAction();
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				// ֱ����
				show("ֱ������");
			} else {
				// ֱ����ʧ��
				show("ֱ����ʧ��");
			}
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			show("���ֿ����豸");
			if (mManager != null) {
				mManager.requestPeers(mChannel, peerListener);
				show("�������б�");
			}

		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			show("���ӷ����仯");
			if (mManager == null) {
				return;
			}
			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			WifiP2pGroup group = (WifiP2pGroup) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
			if (networkInfo.isConnected() && group != null) {
				show("�������������豸", 5);
				mManager.requestConnectionInfo(mChannel, mInfoListener);
				mManager.requestGroupInfo(mChannel, mGroupListener);
			} else {
				show("�Ͽ�����");
				mManager.requestConnectionInfo(mChannel, mInfoListener);
				mManager.requestGroupInfo(mChannel, mGroupListener);
				return;
			}
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
			show("�����豸�����仯");
		}
	}

	private void show(String msg) {
		// TODO Auto-generated method stub
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		Log.i("xyz", msg);
	}

	private void show(String msg, int time) {
		// TODO Auto-generated method stub
		Toast.makeText(context, msg, time).show();
		Log.i("xyz", msg);
	}

}
