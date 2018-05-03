package com.yeban207.utils;

import java.util.HashMap;
import java.util.Map;

import org.ddpush.im.v1.client.appserver.Pusher;

import com.alibaba.fastjson.JSON;
import com.yeban207.pushserver.OnlineService;
import com.yeban207.pushserver.Params;
import com.yeban207.pushserver.other.Util;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class MePusher {

	public static void saveAccountInfo(Context context, String serverip, String serverPort, String pushPort,
			String userName) {
		SharedPreferences account = context.getSharedPreferences(Params.DEFAULT_PRE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = account.edit();
		editor.putString(Params.SERVER_IP, serverip);
		editor.putString(Params.SERVER_PORT, serverPort);
		editor.putString(Params.PUSH_PORT, pushPort);
		editor.putString(Params.USER_NAME, userName);
		editor.putString(Params.SENT_PKGS, "0");
		editor.putString(Params.RECEIVE_PKGS, "0");
		editor.commit();

		BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
		//String mDeviceId = defaultAdapter.getName();//获得设备名字
		defaultAdapter.setName(userName);//设置设备名字
		
	}

	public static void dealMsgFromServer(){
		String msg = null;
		
	}
	
	public static String toRegistJson(String user){
		Map<String,String> map = null;
		if(user != null){
			map = new HashMap<>();;
			map.put("id", "2");
			map.put("user", user);
			String str = JSON.toJSONString(map);
			return str;
		}
		return null;
	}
	public static String toMsgJson(String message){
		Map<String,String> map = null;
		if(message != null){
			map = new HashMap<>();;
			map.put("id", "0");
			map.put("msg", message);
			String str = JSON.toJSONString(map);
			return str;
		}
		return null;
	}
	
	public static void sendToServer(Context context) {
		// Toast.makeText(this.getApplicationContext(), "开始",
		// Toast.LENGTH_SHORT).show();
		Intent startSrv = new Intent(context, OnlineService.class);
		startSrv.putExtra("CMD", "RESET");
		context.startService(startSrv);
	}

	public static void sendMsgToServer(Context context, String targerUsers, String message) {

		SharedPreferences account = context.getSharedPreferences(Params.DEFAULT_PRE_NAME, Context.MODE_PRIVATE);
		String serverIp = account.getString(Params.SERVER_IP, "");
		String pushPort = account.getString(Params.PUSH_PORT, "");
		int port;
		try {
			port = Integer.parseInt(pushPort);
		} catch (Exception e) {
			Toast.makeText(context.getApplicationContext(), "推送端口格式错误：" + pushPort, Toast.LENGTH_SHORT).show();
			return;
		}
		byte[] uuid = null;

		String[] users = targerUsers.split(";");

		for (int i = 0; i < users.length; i++) {
			try {
				uuid = Util.md5Byte(users[i].trim());
			} catch (Exception e) {
				Toast.makeText(context.getApplicationContext(), "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
				return;
			}
			byte[] msg = null;
			try {
				msg = message.getBytes("UTF-8");
			} catch (Exception e) {
				Toast.makeText(context.getApplicationContext(), "错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
				return;
			}
			Thread t = new Thread(new send0x20Task(context, serverIp, port, uuid, msg));
			t.start();
		}
	}

	static class send0x20Task implements Runnable {
		private Context context;
		private String serverIp;
		private int port;
		private byte[] uuid;
		private byte[] msg;

		public send0x20Task(Context context, String serverIp, int port, byte[] uuid, byte[] msg) {
			this.context = context;
			this.serverIp = serverIp;
			this.port = port;
			this.uuid = uuid;
			this.msg = msg;
		}

		@Override
		public void run() {
			Pusher pusher = null;
			Intent startSrv = new Intent(context, OnlineService.class);
			startSrv.putExtra("CMD", "TOAST");
			try {
				boolean result;

				pusher = new Pusher(serverIp, port, 1000 * 5);
				result = pusher.push0x20Message(uuid, msg);
				if (result) {
					startSrv.putExtra("TEXT", "自定义信息发送成功");
				} else {
					startSrv.putExtra("TEXT", "发送失败！格式有误");
				}
			} catch (Exception e) {
				e.printStackTrace();
				startSrv.putExtra("TEXT", "发送失败！" + e.getMessage());
			} finally {
				if (pusher != null) {
					try {
						pusher.close();
					} catch (Exception e) {
					}
					;
				}
			}
			context.startService(startSrv);
		}
	}
}
