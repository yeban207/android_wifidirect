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
 * @author Administrator 执行顺序是 onAttach() onCreate() onCreateView()
 *         onActivityCreated()
 *
 *         onStart() onResume()
 *
 */

public class MyFragmentWifiDirect extends Fragment implements OnClickListener {
	// UI相关的控件
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

	// 广播相关的
	private IntentFilter mWifiDirectFilter;
	private WifiP2pManager.Channel mChannel;
	private WifiP2pManager mManager;
	private WifiDBroadcastReceiver wifiDBroadcastReceiver;

	private List<WifiP2pDevice> peers = new ArrayList<>();
	private FindAdapter adapter;

	private UDPReciverBroad udpReciverBroad;
	// 标志位 主或者员
	private boolean isOnwer = false;
	private boolean isConnect = false;
	// 群主的IP地址
	private InetAddress groupOwnerAddress;

	//private AmrAudioEncoder amrEncoder;
	//private AmrAudioPlayer audioPlayer;
	private PeerListListener peerListener;
	private GroupInfoListener groupInfoListener;
	private ConnectionInfoListener mInfoListener;
	
	//录音相关
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

		Log.d("xyz", "onCreate执行了。。。");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d("xyz", "onCreateView执行了。。。");
		view = inflater.inflate(R.layout.fg_wifidirect, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO onActivityCreated
		super.onActivityCreated(savedInstanceState);
		initViews();
		initBroadReceivers();

		Log.d("xyz", "onActivityCreated执行了。。。");
	}

	@Override
	public void onDestroy() {
		// TODO 销毁
		super.onDestroy();
		context.unregisterReceiver(wifiDBroadcastReceiver);
		context.unregisterReceiver(udpReciverBroad);
	}

	private void initBroadReceivers() {
		// TODO WIFI的监听
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
				// TODO 发现设备回调
				peers.clear();
				peers.addAll(arg0.getDeviceList());
				if (peers.size() == 0) {
					show("搜索列表为空");
					tv_findMsg.setText("发现周围设备" + peers.size() + "个");
					return;
				} else {
					// String text = peers.toString();
					tv_findMsg.setText("发现周围设备" + peers.size() + "个");
					adapter = new FindAdapter(context, peers);
					listView.setAdapter(adapter);

					// 这里设置了listview的点击事件
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
				Log.e("xyz", "怎么啦");
				if(group == null || !isOnwer){
					Log.e("xyz", "group为空");
					tv_groupdevice.setText("在线用户数：0 个");
					return;
				}
				Collection<WifiP2pDevice> clientList = group.getClientList();
				//Log.e("xyz", clientList.toString());
				if(clientList.isEmpty()){
					tv_groupdevice.setText("在线用户数：0 个");
				}else {
					tv_groupdevice.setText("在线用户数：" + clientList.size() + "个");
				}
			}
		};

		mInfoListener = new ConnectionInfoListener() {

			@Override
			public void onConnectionInfoAvailable(WifiP2pInfo info) {
				groupOwnerAddress = info.groupOwnerAddress;
				if (info.groupFormed && info.isGroupOwner) {
					show("群主啊");
					setOnwerOrOther(1, groupOwnerAddress.getHostAddress());
					isConnect = true;
				} else if (info.groupFormed) {
					show("群员");
					setOnwerOrOther(2, null);
					isConnect = true;

				} else {
					show("断开了");
					setOnwerOrOther(3, null);
					isConnect = false;
					m_flagRec = false;
					m_flagTry = false;
				}
				// 发送连接状态
				EventBus.getDefault().post(new MsgEvents(isOnwer, isConnect));
				Log.e("xyz", "发送连接状态" + isConnect + isOnwer);
			}
		};

		// TODO 注册广播
		wifiDBroadcastReceiver = new WifiDBroadcastReceiver(mManager, mChannel, peerListener, mInfoListener,
				groupInfoListener);
		context.registerReceiver(wifiDBroadcastReceiver, mWifiDirectFilter);
	}

	@Override
	public void onClick(View v) {
		// TODO 按钮点击
		int id = v.getId();
		switch (id) {
		case R.id.bt_find:
			show("点击搜索");
			discoveryOther();

			break;

		case R.id.bt_connect:
			show("点击建组");
			//connectFirst();
			if(isConnect&&!isOnwer){
				Log.e("xyz", "有退组吗");
				if(!isOnwer){
					Log.e("xyz", "有退组吗");
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
			show("点击发送");
			new SendMsgThread().start();
			sendMsg();
			break;
		case R.id.bt_broadcast_or_play:
			show("点击广播");
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
			show("点击停止");
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

	// 开始录音
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

	// 停止录音
	private void stopRecord() {
		releaseToListen();
	}
	public void releaseToListen() {
		if (m_flagRec == true) {
			m_flagRec = false;
			audioManager.setMicrophoneMute(true);
		}
	}

	// 播放
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

	// 停止播放
	private void stopPlay() {
		if (m_flagTry == true) {
			m_flagTry = false;
			audioTry = null;
		}
	}
	
	/**
	 * 取消组
	 */
	private void cancelGroup(){
		mManager.removeGroup(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				show("取消组");
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
	 * 创建组
	 */
	private void creatGroup(){
		mManager.createGroup(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				show("开始创建组");
				//isOnwer = true;
			}
			
			@Override
			public void onFailure(int arg0) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	/**
	 * 退组
	 */
	private void outGroup(){
		mManager.cancelConnect(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				show("退组");
				Log.e("xyz", "有退组吗1");
			}
			
			@Override
			public void onFailure(int arg0) {
				// TODO Auto-generated method stub
				Log.e("xyz", "有退组吗2");
			}
		});
	}
	
	/**
	 * 连接第一个设备
	 */
	@SuppressWarnings("unused")
	private void connectFirst() {
		if (peers.size() == 0) {
			show("没有可用的设备");
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
				show("开始连接设备");
			}

			@Override
			public void onFailure(int reason) {
				show("连接设备失败");
			}
		});
	}

	/**
	 * 点击连接设备
	 * 
	 * @param device
	 *            需要连接的设备
	 */
	private void connectDevice(WifiP2pDevice device) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		mManager.connect(mChannel, config, new ActionListener() {
			@Override
			public void onSuccess() { // WiFiDirectBroadcastReceiverwill notify
										// us. Ignore for // now.
				show("开始连接设备");
			}

			@Override
			public void onFailure(int reason) {
				show("连接设备失败");
			}
		});
	}

	/**
	 * 发现设备
	 */
	private void discoveryOther() {
		mManager.discoverPeers(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				show("开始了搜索成功");
			}

			@Override
			public void onFailure(int arg0) {
				show("搜索失败");
			}
		});
	}

	/*
	 * @Override public void onPeersAvailable(WifiP2pDeviceList peerlist) { //
	 * TODO 获得可用的列表 show("啊哈"); peers.clear();
	 * peers.addAll(peerlist.getDeviceList()); if (peers.size() == 0) {
	 * show("沒有搜索到可用设备"); return; } else { String text = peers.toString();
	 * tv_findMsg.setText("hahah"); show(text); } }
	 */
	/**
	 * 客户端和群主的设置
	 * 
	 * @param key
	 */
	private void setOnwerOrOther(int key, String ip) {
		switch (key) {
		case 1:
			et_send.setVisibility(View.VISIBLE);
			bt_BroadOrPlay.setText("广播");
			togBtn.setChecked(true);
			togBtn.setTextColor(Color.RED);
			tv_ip.setText("群主:" + ip);
			bt_send.setVisibility(View.VISIBLE);
			tv_groupdevice.setVisibility(View.VISIBLE);
			bt_connect.setText("移除组");
			isOnwer = true;
			break;
		case 2:
			et_send.setVisibility(View.GONE);
			bt_BroadOrPlay.setText("收听");
			bt_connect.setText("退组");
			togBtn.setChecked(true);
			togBtn.setTextColor(Color.GREEN);
			tv_ip.setText("群员");
			bt_send.setVisibility(View.GONE);
			tv_groupdevice.setVisibility(View.GONE);
			isOnwer = false;
			break;
		case 3:
			togBtn.setChecked(false);
			togBtn.setTextColor(Color.GRAY);
			tv_ip.setText("类型:");
			bt_connect.setText("建组");
			break;
		}
	}

	private void show(String msg) {
		// TODO 显示
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		Log.i("xyz", msg);
	}

	private void initViews() {
		// TODO 控件的初始化
		bt_find = (Button) view.findViewById(R.id.bt_find);
		bt_connect = (Button) view.findViewById(R.id.bt_connect);
		bt_send = (Button) view.findViewById(R.id.bt_send);
		togBtn = (ToggleButton) view.findViewById(R.id.TogBtn);
		tv_ip = (TextView) view.findViewById(R.id.tv_ip);
		tv_msg = (TextView) view.findViewById(R.id.tv_msg);
		tv_msg.setSelected(true);
		tv_msg.setText("请留意公告:"
				+ context.getSharedPreferences("udpreceiver", Context.MODE_PRIVATE).getString("receMsg", "注意") + "！！！");
		et_send = (EditText) view.findViewById(R.id.et_send);
		bt_BroadOrPlay = (Button) view.findViewById(R.id.bt_broadcast_or_play);
		bt_stop = (Button) view.findViewById(R.id.bt_stop);
		tv_groupdevice = (TextView) view.findViewById(R.id.tv_groupdevice);
		tv_findMsg = (TextView) view.findViewById(R.id.tv_findmsg);
		listView = (ListView) view.findViewById(R.id.listview);
		// 必须设置set才能响应点击事件
		bt_find.setOnClickListener(this);
		bt_connect.setOnClickListener(this);
		bt_send.setOnClickListener(this);
		bt_BroadOrPlay.setOnClickListener(this);
		bt_stop.setOnClickListener(this);
		
		EventBus.getDefault().register(this);
	}

	/**
	 * 注册udp广播接收
	 */
	private void registerBroadcast() {
		udpReciverBroad = new UDPReciverBroad();
		IntentFilter intentFilter = new IntentFilter("com.yeban207.udpserver");
		context.registerReceiver(udpReciverBroad, intentFilter);
	}

	/**
	 * 广播接收类
	 * 
	 * @author Administrator
	 *
	 */
	class UDPReciverBroad extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO 广播接收器
			String msg = intent.getStringExtra("receMsg");
			// context.getSharedPreferences(arg0, arg1)
			tv_msg.setText("请留意公告:" + msg + "！！！");
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
		} else if (split[0].equals("not")) {// 公告
			// 1 这里可以考虑把公告的内容放到xml文件中去 2在考虑用广播进行通信 数据及时更新呢 哈哈
			context.getSharedPreferences("udpreceiver", Context.MODE_PRIVATE).edit()
					.putString("receMsg", split[1]).commit();
			intent.putExtra("receMsg", split[1]);
			// intent.putExtra("MSG", receData);
			context.sendBroadcast(intent);
		} else if (split[0].equals("loc")) {// 位置信息
			
			if (split[1].equals("0")) {// 广播消息
				LatLng latLng = new LatLng(Double.parseDouble(split[3]), Double.parseDouble(split[4]));
				msgLocation = new MsgLocation(split[2], latLng, "0");
			} else if (split[1].equals("1")) {// 服务器消息
				LatLng latLng = new LatLng(Double.parseDouble(split[4]), Double.parseDouble(split[5]));
				msgLocation = new MsgLocation(split[3], latLng, "1");
			} else {// 直连消息
				LatLng latLng = new LatLng(Double.parseDouble(split[2]), Double.parseDouble(split[3]));
				msgLocation = new MsgLocation(split[1], latLng, "2");
			}
			EventBus.getDefault().post(msgLocation);
			Log.e("xyz", "收到別人的位置信息" + msgLocation.getLatLng().toString());
		}
	}
	
	private void sendMsg(){
		if (et_send.getText().toString().length() == 0) {
			Toast.makeText(context, "请输入公告消息", Toast.LENGTH_SHORT).show();
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
			// TODO 发生msg公告线程

			try {
				String string = et_send.getText().toString();
				string = "not:" + string;
				byte[] data = string.getBytes();
				DatagramPacket pk = new DatagramPacket(data, data.length, InetAddress.getByName("192.168.49.255"),
						10001);
				DatagramSocket ds = new DatagramSocket();
				ds.send(pk);
				Log.i("xyz", "发送啊");
				ds.close();
			} catch (Exception e) {
			}
			Log.i("xyz", "发送结束了" + Thread.currentThread().getId());
		}
	}

}
