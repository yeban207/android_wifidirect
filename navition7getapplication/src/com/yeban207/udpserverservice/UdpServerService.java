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
		Log.i("xyz", "UDP�������������");
		// new ServerThread(getApplicationContext()).start();//����������������

		/*
		 * myNanoHttp = new MyNanoHttp(10001); try { myNanoHttp.start();
		 * Log.d("xyz", "web������������"); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); Log.d("xyz",
		 * "web�������������쳣"); }
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
		Log.i("xyz", "��������˰�");
	}

	/**
	 * ��ʾ֪ͨ����
	 * 
	 * @param notice
	 */
	private void showNotification(String notice) {
		NotificationManager mManger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		Builder builder = new Notification.Builder(this);
		builder.setTicker("ע��֪֪ͨͨ");
		// ���ñ���
		builder.setContentTitle("ע��֪ͨ");
		// ��������
		builder.setContentText(notice);
		// ����Ĭ�ϵ����� ��
		builder.setDefaults(Notification.DEFAULT_ALL);
		// Сͼ��
		builder.setSmallIcon(R.drawable.f6);
		// �����ͼ
		builder.setContentIntent(pendingIntent);
		// ����֪ͨ����
		Notification notification = builder.build();
		// ֪ͨȡ��

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// notification.flags |= Notification.FLAG_INSISTENT;
		// startForeground(1, notification);
		// ��֪ͨ
		mManger.notify(1, notification);
		Log.d("xyz", "��֪ͨ��");
	}

	/**
	 * ���չ������Ϣ�߳� ��Ҳ�����ǽ��շ���������Ϣ�߳� ����Ϊ�һ�ѽ��շ���������ϢҲ�����udpЭ��ɣ���Ϊ�����ͼ�Щ�أ���������
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
					Log.i("xyz", "�����߳���������������");
					socket.receive(rs);
					Log.i("xyz", "����������");
					String receData = new String(rs.getData(), 0, rs.getLength(), "UTF-8");
					Log.e("xyz", receData);
					String[] split = receData.split(":");
					if (split[0].equals("not")) {// ����
						// 1 ������Կ��ǰѹ�������ݷŵ�xml�ļ���ȥ 2�ڿ����ù㲥����ͨ�� ���ݼ�ʱ������ ����
						getApplicationContext().getSharedPreferences("udpreceiver", Context.MODE_PRIVATE).edit()
								.putString(split[1], receData).commit();
						showNotification(split[1]);
						intent.putExtra("receMsg", split[1]);
						// intent.putExtra("MSG", receData);
						sendBroadcast(intent);
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				if (socket != null)
					socket.close();
				Log.i("xyz", "�󶨶˿��쳣��");
			}
			Log.i("xyz", "�߳̽���");
		}
	}

	// http������
	/*
	 * class MyNanoHttp extends NanoHTTPD {
	 * 
	 * public MyNanoHttp(int port) { super(port); // TODO Auto-generated
	 * constructor stub }
	 * 
	 * public MyNanoHttp(String hostName, int port) { super(hostName, port); }
	 * 
	 * public Response server(IHTTPSession session) { Log.d("xyz", "�з�������");
	 * Method method = session.getMethod(); if
	 * (NanoHTTPD.Method.GET.equals(method)) { // get��ʽ String queryParams =
	 * session.getQueryParameterString(); Log.e("DEMO", "params:" +
	 * queryParams); } else if (NanoHTTPD.Method.POST.equals(method)) { //
	 * post���� } return super.serve(session);
	 * 
	 * } }
	 */

}
