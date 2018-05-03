package com.yeban207.navition;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Circle;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.yeban207.msg.MsgEvents;
import com.yeban207.msg.MsgLocation;
import com.yeban207.navition.MyOrientationListener.OnOrientationListener;
import com.yeban207.navition7getapplication.R;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MyFragmentBaiduMap extends Fragment implements OnClickListener {

	private Context context;
	private MapView mMapView;
	private BaiduMap map;

	// 定位
	private LocationClient mLocationClient;
	private MyLocationListener mLocationListener;

	private LatLng mLocation;

	// 是否是群主相关
	boolean isFirstLocation = true;
	private boolean isConnect;
	private boolean isOnwer;
	// 定位的添加marker相关
	private String mDeviceId;
	private Map<String, MarkerOptions> optionsMap;
	private Map<String, Marker> markersMap;
	// 方向相关
	private MyOrientationListener myOrientationListener;
	private float currentX;
	private BitmapDescriptor mIconLocation;
	private Circle mCricle;
	private ImageButton img_bt_me;
	private TextView tv_count;
	private Button bt_resert;
	private Handler mHandler;
	
	static class MHandler extends Handler{
		private TextView tv_count;
		public MHandler(TextView tv){
			this.tv_count = tv;
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			tv_count.setText("人数:"+msg.arg1);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context = getActivity().getApplicationContext();
		EventBus.getDefault().register(this);

		BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
		mDeviceId = defaultAdapter.getName();
		optionsMap = new HashMap<String, MarkerOptions>();
		markersMap = new HashMap<String, Marker>();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		SDKInitializer.initialize(context);
		// 初始化view
		View view = initView(inflater, container);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		// 初始map
		initMap();
		// 初始化定位
		initLocation();

	}

	private View initView(LayoutInflater inflater, ViewGroup container) {
		View view = inflater.inflate(R.layout.fg_baidumap, container, false);
		mMapView = (MapView) view.findViewById(R.id.id_bmapView);
		img_bt_me = (ImageButton) view.findViewById(R.id.id_bt_me);
		tv_count = (TextView) view.findViewById(R.id.tv_count);
		bt_resert = (Button) view.findViewById(R.id.bt_resert);
		img_bt_me.setOnClickListener(this);
		bt_resert.setOnClickListener(this);
		mHandler = new MHandler(tv_count);
		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.id_bt_me:
			LatLng latlng = mLocation;
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
			map.animateMapStatus(msu);
			break;
		case R.id.bt_resert:
			tv_count.setText("人数:0");
			if (!markersMap.isEmpty()) {
				for (Marker marker : markersMap.values()) {
					marker.remove();
					Log.v("xyz", marker.toString() + "移除了啊");
				}
				markersMap.clear();
				// map.clear();
			}
			if (!optionsMap.isEmpty()) {
				optionsMap.clear();
			}
			break;
		default:
			break;
		}
	}

	private void initMap() {

		map = mMapView.getMap();
		mMapView.removeViewAt(1);// 去掉logo
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(18.0f);
		map.setMapStatus(msu);

		mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.navigation48);

	}

	private void initLocation() {
		mLocationClient = new LocationClient(context);
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener);
		LocationClientOption option = new LocationClientOption();

		// 可选，默认gcj02，设置返回的定位结果坐标系
		option.setCoorType("bd09ll");

		// 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
		int span = 9000;
		option.setScanSpan(span);
		// 可选，设置是否需要地址信息，默认不需要
		option.setIsNeedAddress(true);
		// 可选，默认false,设置是否使用gps
		option.setOpenGps(true);
		// 可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
		option.setLocationNotify(true);
		// 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
		option.setIsNeedLocationDescribe(true);
		// 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
		option.setIsNeedLocationPoiList(true);
		// 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
		option.setIgnoreKillProcess(false);
		// 可选，默认false，设置是否收集CRASH信息，默认收集
		option.SetIgnoreCacheException(false);
		// 可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
		option.setEnableSimulateGps(false);

		mLocationClient.setLocOption(option);

		myOrientationListener = new MyOrientationListener(context);
		myOrientationListener.setmOnOrientationListener(new OnOrientationListener() {

			@Override
			public void onOrientationChanged(float x) {
				currentX = x;
			}
		});

	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		map.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted())
			mLocationClient.start();
		// 开启方向传感器
		myOrientationListener.start();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		map.setMyLocationEnabled(false);
		if (mLocationClient.isStarted())
			mLocationClient.stop();
		// 关闭传感器
		myOrientationListener.stop();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mMapView.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mMapView.onResume();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mMapView.onPause();
	}

	class MyLocationListener implements BDLocationListener {

		@Override
		public void onConnectHotSpotMessage(String arg0, int arg1) {

		}

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
			MyLocationData data = new MyLocationData.Builder()//
					.direction(currentX)//
					.accuracy(location.getRadius())// 定位精度 大致位于圈内
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude()).build();//

			// 设置自图标
			MyLocationConfiguration myLoctionConfig = new MyLocationConfiguration(
					com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.NORMAL, true, mIconLocation);
			map.setMyLocationConfiguration(myLoctionConfig);

			// mLocation = new LatLng(location.getLatitude(),
			// location.getLongitude());
			Toast.makeText(context, "定位刷新了", Toast.LENGTH_SHORT).show();
			Log.d("xyz",
					"我的位置:" + Float.toString((float) data.latitude) + "," + Float.toString((float) data.longitude));
			LatLng newPosition = new LatLng(location.getLatitude(), location.getLongitude());
			if (!newPosition.equals(mLocation)) {
				if (mCricle != null) {
					mCricle.remove();
				}
				mLocation = newPosition;

				mCricle = addCricle();
			}

			map.setMyLocationData(data);

			// 是群主 广播自己的位置
			new SendUDP().start();

			if (isFirstLocation) {
				LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
				map.animateMapStatus(msu);
				isFirstLocation = false;
				Toast.makeText(context, location.getAddrStr(), Toast.LENGTH_LONG).show();
			}

			if (!markersMap.isEmpty()) {
				Log.v("xyz", "markersMap" + markersMap.toString());
				
				// map.clear();
				for (Marker marker : markersMap.values()) {
					marker.remove();
					Log.v("xyz", marker.toString() + "移除了啊");
				}
			}
			Log.v("xyz",""+!optionsMap.isEmpty());
			
			if (!optionsMap.isEmpty()) {
				Log.d("xyz", "添加markersMap执行了；optionsMap" + optionsMap.toString());
				int count = optionsMap.keySet().size();
				for (MarkerOptions option : optionsMap.values()) {
					Marker addOverlay = (Marker) map.addOverlay(option);
					markersMap.put(option.getTitle(), addOverlay);
					Message msg = Message.obtain();
					msg.arg1 = count;
					mHandler.sendMessage(msg);
					Log.v("xyz","人数有设置吗？"+count);
					Log.d("xyz", "应该向markermap中添加了吧" + (!markersMap.isEmpty()) + markersMap.toString());
				}
			}
		}

	}

	private Circle addCricle() {
		CircleOptions options = new CircleOptions();
		options.center(mLocation).//
				radius(150)//
				.fillColor(0x210000ff)//
				.stroke(new Stroke(10, 0x60ff0000));//
		return (Circle) map.addOverlay(options);
	}

	private Marker addMarker(LatLng point, String id) {
		Log.v("xyz", "添加了一个marker标记");
		MarkerOptions option = new MarkerOptions();
		BitmapDescriptor fromResource = null;
		if (id.equals("0")) {
			fromResource = BitmapDescriptorFactory.fromResource(R.drawable.marker0);
		} else {
			fromResource = BitmapDescriptorFactory.fromResource(R.drawable.marker1);
		}
		option.position(point).icon(fromResource).title("大学").draggable(false);
		Marker addMarker = (Marker) map.addOverlay(option);
		return addMarker;
	}

	private MarkerOptions makeOption(LatLng point, String id, String name) {
		Log.v("xyz", "添加了一个marker标记");
		MarkerOptions option = new MarkerOptions();
		BitmapDescriptor fromResource = null;
		if (id.equals("0")) {
			fromResource = BitmapDescriptorFactory.fromResource(R.drawable.marker0);
		} else {
			fromResource = BitmapDescriptorFactory.fromResource(R.drawable.marker1);
		}
		option.position(point).icon(fromResource).title(name).draggable(false);
		return option;
	}

	@Subscribe(threadMode = ThreadMode.BACKGROUND)
	public void receMsg(MsgEvents event) {
		isConnect = event.isConnect();
		isOnwer = event.isOnwer();
		Log.e("xyz", "收到连接状态" + isConnect + ":" + isOnwer);
	}

	@Subscribe(threadMode = ThreadMode.BACKGROUND)
	public void receMsg(MsgLocation event) {
		// optionsMap = new HashMap<String, MarkerOptions>();
		// markersMap = new HashMap<String, Marker>();
		String id = event.getId();
		String name = event.getName();
		LatLng latLng = event.getLatLng();
		Log.e("xyz", "收到位置信息" + event.toString());
		if (!mDeviceId.equals(name)) {
			MarkerOptions makeOption = makeOption(latLng, id, name);
			optionsMap.put(name, makeOption);
			// Marker marker = addOptionMarker(makeOption);
			// markersMap.put(name, marker);
			// tv_count.setText("人数:"+optionsMap.size());
			Log.e("xyz", "添加到了map中去了" + optionsMap.toString() + ";" + markersMap.toString());
		}
		Log.v("xyz",""+optionsMap.isEmpty());
	}

	private Marker addOptionMarker(MarkerOptions option) {
		// TODO Auto-generated method stub
		return (Marker) map.addOverlay(option);
	}

	class SendUDP extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// TODO 发生msg公告线程
			InetAddress byName = null;

			try {
				StringBuilder sb = new StringBuilder().append("loc:");
				Log.i("xyz", "发送了自己的位置信息给");
				if (isOnwer) {
					byName = InetAddress.getByName("192.168.49.255");
					sb.append("0:" + mDeviceId + ":");
					Log.i("xyz", "所有人");
				} else if (isConnect) {
					byName = InetAddress.getByName("192.168.49.1");
					sb.append(mDeviceId + ":");
					Log.i("xyz", "群主");
				} else {
					byName = InetAddress.getByName("192.168.1.103");
					sb.append("1:" + mDeviceId + ":" + "");
					Log.i("xyz", "服务器");
				}
				sb.append(mLocation.latitude + ":" + mLocation.longitude);
				byte[] data = sb.toString().getBytes();
				DatagramPacket pk = new DatagramPacket(data, data.length, byName, 10001);
				DatagramSocket ds = new DatagramSocket();
				ds.send(pk);
				Log.i("xyz", "发送了自己的位置信息结束" + sb.toString());
				ds.close();
			} catch (Exception e) {
			}
			Log.i("xyz", "发送自己位置结束了" + Thread.currentThread().getId() + isConnect + isOnwer);
		}
	}

}
