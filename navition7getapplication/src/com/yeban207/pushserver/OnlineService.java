package com.yeban207.pushserver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

import org.ddpush.im.v1.client.appuser.Message;
import org.ddpush.im.v1.client.appuser.UDPClientBase;
import org.greenrobot.eventbus.EventBus;

import com.baidu.mapapi.model.LatLng;
import com.yeban207.msg.MsgLocation;
import com.yeban207.msg.ReceiveTextMsg;
import com.yeban207.navition.MainActivity;
import com.yeban207.navition7getapplication.R;
import com.yeban207.pushserver.other.DateTimeUtil;
import com.yeban207.pushserver.other.TickAlarmReceiver;
import com.yeban207.pushserver.other.Util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

public class OnlineService extends Service {

	protected PendingIntent tickPendIntent;
	protected TickAlarmReceiver tickAlarmReceiver = new TickAlarmReceiver();
	WakeLock wakeLock;
	MyUdpClient myUdpClient;
	Notification n;

	private Intent intent;

	public class MyUdpClient extends UDPClientBase {

		public MyUdpClient(byte[] uuid, int appid, String serverAddr, int serverPort) throws Exception {
			super(uuid, appid, serverAddr, serverPort);
		}

		@Override
		public boolean hasNetworkConnection() {
			return Util.hasNetwork(OnlineService.this);
		}

		@Override
		public void trySystemSleep() {
			tryReleaseWakeLock();
		}

		@Override
		public void onPushMessage(Message message) {
			if (message == null) {
				return;
			}
			if (message.getData() == null || message.getData().length == 0) {
				return;
			}
			if (message.getCmd() == 16) { // 0x10 通用推送信息
				notifyUser(16, "DDPush通用推送信息", "时间：" + DateTimeUtil.getCurDateTime(), "收到通用推送信息");
			}
			if (message.getCmd() == 17) {// 0x11 分组推送信息
				long msg = ByteBuffer.wrap(message.getData(), 5, 8).getLong();
				notifyUser(17, "DDPush分组推送信息", "" + msg, "收到通用推送信息");
			}
			if (message.getCmd() == 32) {// 0x20 自定义推送信息
				String str = null;
				try {
					str = new String(message.getData(), 5, message.getContentLength(), "UTF-8");
				} catch (Exception e) {
					str = Util.convert(message.getData(), 5, message.getContentLength());
				}
				EventBus.getDefault().post(new ReceiveTextMsg(str));
				/*if (str.split(":")[0].equals("not") && !getSharedPreferences("udpreceiver", Context.MODE_PRIVATE)
						.getString("receMsg", "").equals(str.substring(4))) {
					notifyUser(32, "收到一条新的公告", "" + str.substring(4), "收到推送信息");
				}*/
				dealMsg(str, 0);
			}
			setPkgsInfo();
		}

	}

	public OnlineService() {
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		this.setTickAlarm();

		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OnlineService");

		intent = new Intent("com.yeban207.udpserver");
		new ReceiveData().start();
		Log.i("xyz", "UDP服务真的启动了");

		resetClient();

		notifyRunning();
	}

	protected void resetClient() {
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME, Context.MODE_PRIVATE);
		String serverIp = account.getString(Params.SERVER_IP, "");
		String serverPort = account.getString(Params.SERVER_PORT, "");
		String pushPort = account.getString(Params.PUSH_PORT, "");
		String userName = account.getString(Params.USER_NAME, "");
		if (serverIp == null || serverIp.trim().length() == 0 || serverPort == null || serverPort.trim().length() == 0
				|| pushPort == null || pushPort.trim().length() == 0 || userName == null
				|| userName.trim().length() == 0) {
			return;
		}
		if (this.myUdpClient != null) {
			try {
				myUdpClient.stop();
			} catch (Exception e) {
			}
		}
		try {
			myUdpClient = new MyUdpClient(Util.md5Byte(userName), 1, serverIp, Integer.parseInt(serverPort));
			myUdpClient.setHeartbeatInterval(50);
			myUdpClient.start();
			/*
			 * SharedPreferences.Editor editor = account.edit();
			 * editor.putString(Params.SENT_PKGS, "0");
			 * editor.putString(Params.RECEIVE_PKGS, "0"); editor.commit();
			 */
		} catch (Exception e) {
			Toast.makeText(this.getApplicationContext(), "操作失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
		}
		Toast.makeText(this.getApplicationContext(), "ddpush：终端重置", Toast.LENGTH_LONG).show();
	}

	private void setPkgsInfo() {
		// TODO Auto-generated method stub
		if (this.myUdpClient == null) {
			return;
		}
		long sent = myUdpClient.getSentPackets();
		long received = myUdpClient.getReceivedPackets();
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = account.edit();
		editor.putString(Params.SENT_PKGS, "" + sent);
		editor.putString(Params.RECEIVE_PKGS, "" + received);
		editor.commit();
	}

	public void notifyUser(int id, String title, String content, String tickerText) {
		NotificationManager notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		Notification n = new Notification.Builder(OnlineService.this).setAutoCancel(true).setTicker(tickerText)
				.setContentTitle(title).setContentText(content).setContentIntent(pi)
				.setSmallIcon(R.drawable.ic_launcher).setWhen(System.currentTimeMillis()).build();
		// n.contentIntent = pi;

		// n.setLatestEventInfo(this, title, content, pi);
		n.defaults = Notification.DEFAULT_ALL;
		n.flags |= Notification.FLAG_SHOW_LIGHTS;
		n.flags |= Notification.FLAG_AUTO_CANCEL;

		// n.icon = R.drawable.ic_launcher;
		// n.when = System.currentTimeMillis();
		// n.tickerText = tickerText;
		notificationManager.notify(id, n);
	}

	protected void notifyRunning() {
		NotificationManager notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		n = new Notification.Builder(OnlineService.this).setTicker("助手运行").setContentTitle("游玩助手")
				.setContentText("正在运行").setOngoing(true).setContentIntent(pi).setSmallIcon(R.drawable.ic_launcher)
				.setWhen(System.currentTimeMillis()).build();
		// n.contentIntent = pi;
		// n.setLatestEventInfo(this, "DDPushDemoUDP", "姝ｅ湪杩愯", pi);
		// n.defaults = Notification.DEFAULT_ALL;
		// n.flags |= Notification.FLAG_SHOW_LIGHTS;
		// n.flags |= Notification.FLAG_AUTO_CANCEL;
		// n.flags |= Notification.FLAG_ONGOING_EVENT;
		// n.flags |= Notification.FLAG_NO_CLEAR;
		// n.iconLevel = 5;

		// n.icon = R.drawable.ic_launcher;
		// n.when = System.currentTimeMillis();
		// n.tickerText = "DDPushDemoUDP姝ｅ湪杩愯";
		notificationManager.notify(0, n);
	}

	protected void cancelNotifyRunning() {
		NotificationManager notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}

	@Override
	public int onStartCommand(Intent param, int flags, int startId) {
		// TODO Auto-generated method stub
		if (param == null) {
			return START_STICKY;
		}
		String cmd = param.getStringExtra("CMD");
		if (cmd == null) {
			cmd = "";
		}
		if (cmd.equals("TICK")) {
			if (wakeLock != null && wakeLock.isHeld() == false) {
				wakeLock.acquire();
			}
		}
		if (cmd.equals("RESET")) {
			if (wakeLock != null && wakeLock.isHeld() == false) {
				wakeLock.acquire();
			}
			resetClient();
		}
		if (cmd.equals("TOAST")) {
			String text = param.getStringExtra("TEXT");
			if (text != null && text.trim().length() != 0) {
				Toast.makeText(this, text, Toast.LENGTH_LONG).show();
			}
		}

		setPkgsInfo();

		return START_STICKY;
	}

	protected void tryReleaseWakeLock() {
		if (wakeLock != null && wakeLock.isHeld() == true) {
			wakeLock.release();
		}
	}

	protected void setTickAlarm() {
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, TickAlarmReceiver.class);
		int requestCode = 0;
		tickPendIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// 灏忕背2s鐨凪IUI鎿嶄綔绯荤粺锛岀洰鍓嶆渶鐭箍鎾棿闅斾负5鍒嗛挓锛屽皯浜?鍒嗛挓鐨刟larm浼氱瓑鍒?鍒嗛挓鍐嶈Е鍙戯紒2014-04-28
		long triggerAtTime = System.currentTimeMillis();
		int interval = 300 * 1000;
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, tickPendIntent);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		cancelNotifyRunning();
		this.tryReleaseWakeLock();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void dealMsg(String msg,int id){
		if(id == 0){
			String[] split = msg.split(":");
			if (split[0].equals("not") && !getSharedPreferences("udpreceiver", Context.MODE_PRIVATE)
					.getString("receMsg", "").equals(split[1])) {
				notifyUser(32, "收到一条新的公告", "" + split[1], "收到推送信息");
				getSharedPreferences("udpreceiver", Context.MODE_PRIVATE).edit().putString("receMsg", split[1]).commit();
				return;
			}
		} else if(id == 1){
			if(!getSharedPreferences("udpreceiver", Context.MODE_PRIVATE).getString("receMsg", "").equals(msg))
			{
				notifyUser(32, "收到一条新的公告", "" + msg, "收到推送信息");
				getSharedPreferences("udpreceiver", Context.MODE_PRIVATE).edit().putString("receMsg", msg).commit();
			}
		}
			
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
						/*getApplicationContext().getSharedPreferences("udpreceiver", Context.MODE_PRIVATE).edit()
								.putString(split[1], receData).commit();
						showNotification(split[1]);*/
						dealMsg(split[1], 1);
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
	
}
