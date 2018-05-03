package com.yeban207.udpserverservice;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.greenrobot.eventbus.EventBus;

import com.baidu.mapapi.model.LatLng;
import com.yeban207.msg.MsgLocation;
import com.yeban207.navition.MainActivity;
import com.yeban207.navition7getapplication.R;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UdpServerService extends Service {
	private Intent intent;
	// private MyNanoHttp myNanoHttp;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		intent = new Intent("com.yeban207.udpserver");
		//new ReceiveData().start();
		Log.i("xyz", "UDP服务真的启动了");
		// new ServerThread(getApplicationContext()).start();//开启语音服务器了

		/*
		 * myNanoHttp = new MyNanoHttp(10001); try { myNanoHttp.start();
		 * Log.d("xyz", "web服务器启动了"); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); Log.d("xyz",
		 * "web服务器启动了异常"); }
		 */
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i("xyz", "服务结束了啊");
	}

	/**
	 * 显示通知函数
	 * 
	 * @param notice
	 */
	private void showNotification(String notice) {
		NotificationManager mManger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		Builder builder = new Notification.Builder(this);
		builder.setTicker("注意通知通知");
		// 设置标题
		builder.setContentTitle("注意通知");
		// 设置内容
		builder.setContentText(notice);
		// 设置默认的声音 震动
		builder.setDefaults(Notification.DEFAULT_ALL);
		// 小图标
		builder.setSmallIcon(R.drawable.f6);
		// 点击意图
		builder.setContentIntent(pendingIntent);
		// 构建通知对象
		Notification notification = builder.build();
		// 通知取消

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// notification.flags |= Notification.FLAG_INSISTENT;
		// startForeground(1, notification);
		// 发通知
		mManger.notify(1, notification);
		Log.d("xyz", "发通知了");
	}

	/**
	 * 接收公告的消息线程 （也可以是接收服务器的消息线程 ，因为我会把接收服务器的消息也是这个udp协议吧，因为这样就简单些呢，哈哈哈）
	 * 
	 * @author Administrator
	 *
	 */
	class ReceiveData extends Thread {

		private MsgLocation msgLocation = null;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] buf = new byte[1024 * 2];
			DatagramSocket socket = null;
			DatagramPacket rs = new DatagramPacket(buf, buf.length);
			try {
				socket = new DatagramSocket(10001);
				while (true) {
					Log.i("xyz", "服务线程启动，阻塞了码");
					socket.receive(rs);
					Log.i("xyz", "有阻塞了呢");
					String receData = new String(rs.getData(), 0, rs.getLength(), "UTF-8");
					Log.e("xyz", receData);
					String[] split = receData.split(":");
					if (split[0].equals("not")) {// 公告
						// 1 这里可以考虑把公告的内容放到xml文件中去 2在考虑用广播进行通信 数据及时更新呢 哈哈
						getApplicationContext().getSharedPreferences("udpreceiver", Context.MODE_PRIVATE).edit()
								.putString(split[1], receData).commit();
						showNotification(split[1]);
						intent.putExtra("receMsg", split[1]);
						// intent.putExtra("MSG", receData);
						sendBroadcast(intent);
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				if (socket != null)
					socket.close();
				Log.i("xyz", "绑定端口异常啦");
			}
			Log.i("xyz", "线程结束");
		}
	}

	// http服务器
	/*
	 * class MyNanoHttp extends NanoHTTPD {
	 * 
	 * public MyNanoHttp(int port) { super(port); // TODO Auto-generated
	 * constructor stub }
	 * 
	 * public MyNanoHttp(String hostName, int port) { super(hostName, port); }
	 * 
	 * public Response server(IHTTPSession session) { Log.d("xyz", "有房客来了");
	 * Method method = session.getMethod(); if
	 * (NanoHTTPD.Method.GET.equals(method)) { // get方式 String queryParams =
	 * session.getQueryParameterString(); Log.e("DEMO", "params:" +
	 * queryParams); } else if (NanoHTTPD.Method.POST.equals(method)) { //
	 * post请求 } return super.serve(session);
	 * 
	 * } }
	 */

}
