package com.yeban207.navition;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.baidu.mapapi.model.LatLng;
import com.yeban207.adapters.FindAdapter;
import com.yeban207.aduio.AudRec;
import com.yeban207.aduio.AudioTry;
import com.yeban207.broadcastreceiver.WifiDBroadcastReceiver;
import com.yeban207.msg.MsgEvents;
import com.yeban207.msg.MsgLocation;
import com.yeban207.msg.ReceiveTextMsg;
import com.yeban207.navition7getapplication.R;
import com.yeban207.utils.MePusher;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * 
 * @author Administrator ִ��˳���� onAttach() onCreate() onCreateView()
 *         onActivityCreated()
 *
 *         onStart() onResume()
 *
 */

public class MyFragmentWifiDirect extends Fragment implements OnClickListener {
	// UI��صĿؼ�
	private View view;
	private Button bt_find;
	private Button bt_connect;
	private Button bt_send;
	private ToggleButton togBtn;
	private TextView tv_ip;
	private TextView tv_msg;
	private EditText et_send;
	private Button bt_BroadOrPlay;
	private Button bt_stop;
	private TextView tv_groupdevice;
	private TextView tv_findMsg;
	private ListView listView;
	//
	private Context context;

	// �㲥��ص�
	private IntentFilter mWifiDirectFilter;
	private WifiP2pManager.Channel mChannel;
	private WifiP2pManager mManager;
	private WifiDBroadcastReceiver wifiDBroadcastReceiver;

	private List<WifiP2pDevice> peers = new ArrayList<>();
	private FindAdapter adapter;

	private UDPReciverBroad udpReciverBroad;
	// ��־λ ������Ա
	private boolean isOnwer = false;
	private boolean isConnect = false;
	// Ⱥ����IP��ַ
	private InetAddress groupOwnerAddress;

	//private AmrAudioEncoder amrEncoder;
	//private AmrAudioPlayer audioPlayer;
	private PeerListListener peerListener;
	private GroupInfoListener groupInfoListener;
	private ConnectionInfoListener mInfoListener;
	
	//¼�����
	public static boolean m_flagRec = false;
	public static boolean m_flagTry = false;
	private AudioManager audioManager = null;
	private AudioTry audioTry;
	private List<String> targetUsers = new ArrayList<>();;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context = getActivity().getApplicationContext();
		registerBroadcast();
		
		audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setMicrophoneMute(true);

		Log.d("xyz", "onCreateִ���ˡ�����");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("xyz", "onCreateViewִ���ˡ�����");
		view = inflater.inflate(R.layout.fg_wifidirect, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO onActivityCreated
		super.onActivityCreated(savedInstanceState);
		initViews();
		initBroadReceivers();

		Log.d("xyz", "onActivityCreatedִ���ˡ�����");
	}

	@Override
	public void onDestroy() {
		// TODO ����
		super.onDestroy();
		context.unregisterReceiver(wifiDBroadcastReceiver);
		context.unregisterReceiver(udpReciverBroad);
	}

	private void initBroadReceivers() {
		// TODO WIFI�ļ���
		mWifiDirectFilter = new IntentFilter();
		mWifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mWifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mWifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mWifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		// mWifiDirectFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

		mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(context, context.getMainLooper(), null);
		peerListener = new PeerListListener() {

			@Override
			public void onPeersAvailable(WifiP2pDeviceList arg0) {
				// TODO �����豸�ص�
				peers.clear();
				peers.addAll(arg0.getDeviceList());
				if (peers.size() == 0) {
					show("�����б�Ϊ��");
					tv_findMsg.setText("������Χ�豸" + peers.size() + "��");
					return;
				} else {
					// String text = peers.toString();
					tv_findMsg.setText("������Χ�豸" + peers.size() + "��");
					adapter = new FindAdapter(context, peers);
					listView.setAdapter(adapter);

					// ����������listview�ĵ���¼�
					listView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							// TODO Auto-generated method stub
							WifiP2pDevice device = (WifiP2pDevice) parent.getItemAtPosition(position);
							connectDevice(device);
						}
					});
				}
			}
		};

		groupInfoListener = new GroupInfoListener() {

			@Override
			public void onGroupInfoAvailable(WifiP2pGroup group) {
				// TODO Auto-generated method stub
				Log.e("xyz", "��ô��");
				if(group == null || !isOnwer){
					Log.e("xyz", "groupΪ��");
					tv_groupdevice.setText("�����û�����0 ��");
					return;
				}
				Collection<WifiP2pDevice> clientList = group.getClientList();
				//Log.e("xyz", clientList.toString());
				if(clientList.isEmpty()){
					tv_groupdevice.setText("�����û�����0 ��");
				}else {
					tv_groupdevice.setText("�����û�����" + clientList.size() + "��");
				}
			}
		};

		mInfoListener = new ConnectionInfoListener() {

			@Override
			public void onConnectionInfoAvailable(WifiP2pInfo info) {
				groupOwnerAddress = info.groupOwnerAddress;
				if (info.groupFormed && info.isGroupOwner) {
					show("Ⱥ����");
					setOnwerOrOther(1, groupOwnerAddress.getHostAddress());
					isConnect = true;
				} else if (info.groupFormed) {
					show("ȺԱ");
					setOnwerOrOther(2, null);
					isConnect = true;

				} else {
					show("�Ͽ���");
					setOnwerOrOther(3, null);
					isConnect = false;
					m_flagRec = false;
					m_flagTry = false;
				}
				// ��������״̬
				EventBus.getDefault().post(new MsgEvents(isOnwer, isConnect));
				Log.e("xyz", "��������״̬" + isConnect + isOnwer);
			}
		};

		// TODO ע��㲥
		wifiDBroadcastReceiver = new WifiDBroadcastReceiver(mManager, mChannel, peerListener, mInfoListener,
				groupInfoListener);
		context.registerReceiver(wifiDBroadcastReceiver, mWifiDirectFilter);
	}

	@Override
	public void onClick(View v) {
		// TODO ��ť���
		int id = v.getId();
		switch (id) {
		case R.id.bt_find:
			show("�������");
			discoveryOther();

			break;

		case R.id.bt_connect:
			show("�������");
			//connectFirst();
			if(isConnect&&!isOnwer){
				Log.e("xyz", "��������");
				if(!isOnwer){
					Log.e("xyz", "��������");
					//outGroup();
					cancelGroup();
				}
			} else {
				if(isOnwer) {
					cancelGroup();
				} else {
					creatGroup();
				}
			}
			
			break;
		case R.id.bt_send:
			show("�������");
			new SendMsgThread().start();
			sendMsg();
			break;
		case R.id.bt_broadcast_or_play:
			show("����㲥");
			if (isOnwer) {
				startRecord();
				bt_BroadOrPlay.setEnabled(false);
			} else if (!isOnwer && groupOwnerAddress != null) {
				//CommonConfig.SERVER_IP_ADDRESS = groupOwnerAddress.getHostAddress();
				startPlay();
				bt_BroadOrPlay.setEnabled(false);
			}
			bt_stop.setEnabled(true);
			break;
		case R.id.bt_stop:
			show("���ֹͣ");
			if (isOnwer) {
				stopRecord();
				bt_stop.setEnabled(false);
			} else if (!isOnwer && groupOwnerAddress != null) {
				stopPlay();
				bt_stop.setEnabled(false);
			}
			bt_BroadOrPlay.setEnabled(true);
			break;
		}
	}

	// ��ʼ¼��
	private void startRecord() {
		pushToTalk();
	}
	public boolean pushToTalk() {
		if (m_flagRec == false) {
			m_flagRec = true;
			audioManager.setMicrophoneMute(false);
			AudRec audRec = new AudRec("192.168.49.255", 25555);
			if (audRec.inint()) {
				audRec.start();
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

	// ֹͣ¼��
	private void stopRecord() {
		releaseToListen();
	}
	public void releaseToListen() {
		if (m_flagRec == true) {
			m_flagRec = false;
			audioManager.setMicrophoneMute(true);
		}
	}

	// ����
	private void startPlay() {
		if (m_flagTry == false) {
			audioTry = new AudioTry();
			audioTry.init();
			audioTry.start();
			m_flagTry = true;
		} else {
			m_flagTry = false;
			audioTry = null;
		}
	}

	// ֹͣ����
	private void stopPlay() {
		if (m_flagTry == true) {
			m_flagTry = false;
			audioTry = null;
		}
	}
	
	/**
	 * ȡ����
	 */
	private void cancelGroup(){
		mManager.removeGroup(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				show("ȡ����");
				isOnwer = false;
				m_flagRec = false;
				m_flagTry = false;
			}
			
			@Override
			public void onFailure(int arg0) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	
	/**
	 * ������
	 */
	private void creatGroup(){
		mManager.createGroup(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				show("��ʼ������");
				//isOnwer = true;
			}
			
			@Override
			public void onFailure(int arg0) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	/**
	 * ����
	 */
	private void outGroup(){
		mManager.cancelConnect(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				show("����");
				Log.e("xyz", "��������1");
			}
			
			@Override
			public void onFailure(int arg0) {
				// TODO Auto-generated method stub
				Log.e("xyz", "��������2");
			}
		});
	}
	
	/**
	 * ���ӵ�һ���豸
	 */
	@SuppressWarnings("unused")
	private void connectFirst() {
		if (peers.size() == 0) {
			show("û�п��õ��豸");
			return;
		}
		WifiP2pDevice device = (WifiP2pDevice) peers.get(0);
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		mManager.connect(mChannel, config, new ActionListener() {
			@Override
			public void onSuccess() { // WiFiDirectBroadcastReceiverwill notify
										// us. Ignore for // now.
				show("��ʼ�����豸");
			}

			@Override
			public void onFailure(int reason) {
				show("�����豸ʧ��");
			}
		});
	}

	/**
	 * ��������豸
	 * 
	 * @param device
	 *            ��Ҫ���ӵ��豸
	 */
	private void connectDevice(WifiP2pDevice device) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		mManager.connect(mChannel, config, new ActionListener() {
			@Override
			public void onSuccess() { // WiFiDirectBroadcastReceiverwill notify
										// us. Ignore for // now.
				show("��ʼ�����豸");
			}

			@Override
			public void onFailure(int reason) {
				show("�����豸ʧ��");
			}
		});
	}

	/**
	 * �����豸
	 */
	private void discoveryOther() {
		mManager.discoverPeers(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				show("��ʼ�������ɹ�");
			}

			@Override
			public void onFailure(int arg0) {
				show("����ʧ��");
			}
		});
	}

	/*
	 * @Override public void onPeersAvailable(WifiP2pDeviceList peerlist) { //
	 * TODO ��ÿ��õ��б� show("����"); peers.clear();
	 * peers.addAll(peerlist.getDeviceList()); if (peers.size() == 0) {
	 * show("�]�������������豸"); return; } else { String text = peers.toString();
	 * tv_findMsg.setText("hahah"); show(text); } }
	 */
	/**
	 * �ͻ��˺�Ⱥ��������
	 * 
	 * @param key
	 */
	private void setOnwerOrOther(int key, String ip) {
		switch (key) {
		case 1:
			et_send.setVisibility(View.VISIBLE);
			bt_BroadOrPlay.setText("�㲥");
			togBtn.setChecked(true);
			togBtn.setTextColor(Color.RED);
			tv_ip.setText("Ⱥ��:" + ip);
			bt_send.setVisibility(View.VISIBLE);
			tv_groupdevice.setVisibility(View.VISIBLE);
			bt_connect.setText("�Ƴ���");
			isOnwer = true;
			break;
		case 2:
			et_send.setVisibility(View.GONE);
			bt_BroadOrPlay.setText("����");
			bt_connect.setText("����");
			togBtn.setChecked(true);
			togBtn.setTextColor(Color.GREEN);
			tv_ip.setText("ȺԱ");
			bt_send.setVisibility(View.GONE);
			tv_groupdevice.setVisibility(View.GONE);
			isOnwer = false;
			break;
		case 3:
			togBtn.setChecked(false);
			togBtn.setTextColor(Color.GRAY);
			tv_ip.setText("����:");
			bt_connect.setText("����");
			break;
		}
	}

	private void show(String msg) {
		// TODO ��ʾ
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		Log.i("xyz", msg);
	}

	private void initViews() {
		// TODO �ؼ��ĳ�ʼ��
		bt_find = (Button) view.findViewById(R.id.bt_find);
		bt_connect = (Button) view.findViewById(R.id.bt_connect);
		bt_send = (Button) view.findViewById(R.id.bt_send);
		togBtn = (ToggleButton) view.findViewById(R.id.TogBtn);
		tv_ip = (TextView) view.findViewById(R.id.tv_ip);
		tv_msg = (TextView) view.findViewById(R.id.tv_msg);
		tv_msg.setSelected(true);
		tv_msg.setText("�����⹫��:"
				+ context.getSharedPreferences("udpreceiver", Context.MODE_PRIVATE).getString("receMsg", "ע��") + "������");
		et_send = (EditText) view.findViewById(R.id.et_send);
		bt_BroadOrPlay = (Button) view.findViewById(R.id.bt_broadcast_or_play);
		bt_stop = (Button) view.findViewById(R.id.bt_stop);
		tv_groupdevice = (TextView) view.findViewById(R.id.tv_groupdevice);
		tv_findMsg = (TextView) view.findViewById(R.id.tv_findmsg);
		listView = (ListView) view.findViewById(R.id.listview);
		// ��������set������Ӧ����¼�
		bt_find.setOnClickListener(this);
		bt_connect.setOnClickListener(this);
		bt_send.setOnClickListener(this);
		bt_BroadOrPlay.setOnClickListener(this);
		bt_stop.setOnClickListener(this);
		
		EventBus.getDefault().register(this);
	}

	/**
	 * ע��udp�㲥����
	 */
	private void registerBroadcast() {
		udpReciverBroad = new UDPReciverBroad();
		IntentFilter intentFilter = new IntentFilter("com.yeban207.udpserver");
		context.registerReceiver(udpReciverBroad, intentFilter);
	}

	/**
	 * �㲥������
	 * 
	 * @author Administrator
	 *
	 */
	class UDPReciverBroad extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO �㲥������
			String msg = intent.getStringExtra("receMsg");
			// context.getSharedPreferences(arg0, arg1)
			tv_msg.setText("�����⹫��:" + msg + "������");
		}

	}
	
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void receTextMsgEventBus(ReceiveTextMsg msg){
		Intent intent = new Intent("com.yeban207.udpserver");
		MsgLocation msgLocation;
		String message = msg.getMessage();
		String[] split = message.split(":");
		if (split[0].equals("regist")){
			targetUsers.add(split[1]);
		} else if (split[0].equals("not")) {// ����
			// 1 ������Կ��ǰѹ�������ݷŵ�xml�ļ���ȥ 2�ڿ����ù㲥����ͨ�� ���ݼ�ʱ������ ����
			context.getSharedPreferences("udpreceiver", Context.MODE_PRIVATE).edit()
					.putString("receMsg", split[1]).commit();
			intent.putExtra("receMsg", split[1]);
			// intent.putExtra("MSG", receData);
			context.sendBroadcast(intent);
		} else if (split[0].equals("loc")) {// λ����Ϣ
			
			if (split[1].equals("0")) {// �㲥��Ϣ
				LatLng latLng = new LatLng(Double.parseDouble(split[3]), Double.parseDouble(split[4]));
				msgLocation = new MsgLocation(split[2], latLng, "0");
			} else if (split[1].equals("1")) {// ��������Ϣ
				LatLng latLng = new LatLng(Double.parseDouble(split[4]), Double.parseDouble(split[5]));
				msgLocation = new MsgLocation(split[3], latLng, "1");
			} else {// ֱ����Ϣ
				LatLng latLng = new LatLng(Double.parseDouble(split[2]), Double.parseDouble(split[3]));
				msgLocation = new MsgLocation(split[1], latLng, "2");
			}
			EventBus.getDefault().post(msgLocation);
			Log.e("xyz", "�յ��e�˵�λ����Ϣ" + msgLocation.getLatLng().toString());
		}
	}
	
	private void sendMsg(){
		if (et_send.getText().toString().length() == 0) {
			Toast.makeText(context, "�����빫����Ϣ", Toast.LENGTH_SHORT).show();
			et_send.requestFocus();
			return;
		}
		String message = et_send.getText().toString();
		message = "not:" + message;
		if(targetUsers.isEmpty() ){
			return;
		}
		for (int i = 0; i < targetUsers.size(); i++) {
			MePusher.sendMsgToServer(context, targetUsers.get(i), message);
		}
	}

	class SendMsgThread extends Thread {
		@Override
		public void run() {
			// TODO ����msg�����߳�

			try {
				String string = et_send.getText().toString();
				string = "not:" + string;
				byte[] data = string.getBytes();
				DatagramPacket pk = new DatagramPacket(data, data.length, InetAddress.getByName("192.168.49.255"),
						10001);
				DatagramSocket ds = new DatagramSocket();
				ds.send(pk);
				Log.i("xyz", "���Ͱ�");
				ds.close();
			} catch (Exception e) {
			}
			Log.i("xyz", "���ͽ�����" + Thread.currentThread().getId());
		}
	}

}
