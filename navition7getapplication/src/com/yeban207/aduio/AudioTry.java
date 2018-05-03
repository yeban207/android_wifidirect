package com.yeban207.aduio;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;

import com.yeban207.application.MyApplication;
import com.yeban207.navition.MyFragmentWifiDirect;

import android.content.Context;
import android.graphics.AvoidXfermode.Mode;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioTry extends Thread {
	private int minbuffersize;
	private AudioTrack mAudioTrack;
	private byte[] minbufferbytes;
	private final int UDPLOCALPORT = 25555;
	private LinkedList<byte[]> data_q = null;
	private byte[] q_head;
	private byte[] playdata;
	private String remoteIP = null;
	private String localIP = null;

	@SuppressWarnings("deprecation")
	public boolean init() {
		localIP = new String(getLocalIP());
		minbuffersize = AudioTrack.getMinBufferSize(8000, //
				AudioFormat.CHANNEL_CONFIGURATION_MONO, //
				AudioFormat.ENCODING_PCM_16BIT);
		Log.d("xyz", ""+minbuffersize);
		// 1.音频类型 STREAM_VOICE_CALL or STREAM_MUSIC 听筒or扬声器
		// 2.采样频率 8000
		// 3.数据格式 16bit
		// 4.数据大小
		// 5.音频模式 流的数据
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, //
				8000, //
				AudioFormat.CHANNEL_CONFIGURATION_MONO, //
				AudioFormat.ENCODING_PCM_16BIT, //
				minbuffersize, //
				AudioTrack.MODE_STREAM);

		if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
			//
			Log.e("xyz", "播放器初始化失败");
			return false;
		}
		//TODO 这里需要仔细修改的
		Context context = MyApplication.getContext();
		int buffer_size = context.getSharedPreferences("config", Context.MODE_PRIVATE).getInt("buffer_size", 640);
		
		minbufferbytes = new byte[buffer_size];// 注意这里必须用640.minbuffersize返回的是1648
		return true;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		mAudioTrack.play();
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket(UDPLOCALPORT);
			DatagramPacket dp = new DatagramPacket(minbufferbytes, minbufferbytes.length);
			while (MyFragmentWifiDirect.m_flagTry) {
				ds.receive(dp);
				remoteIP = dp.getAddress().getHostAddress();
				if (data_q == null) {
					data_q = new LinkedList<byte[]>();
				}
				data_q.add(minbufferbytes);
				synchronized (data_q) {
					if (!data_q.isEmpty()) {
						q_head = data_q.removeFirst();
						playdata = q_head;
						q_head = null;
					}
				}
				if (playdata != null) {
					if (!localIP.equals(remoteIP)) {
						mAudioTrack.write(playdata, 0, playdata.length);
					}
				}
				playdata = null;

			}

			// if (ds != null) {
			ds.close();
			// }

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
			mAudioTrack = null;
		}

	}

	public String getLocalIP() {
		String IP_Address;
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface intf = en.nextElement();
				Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
				while (enumIpAddr.hasMoreElements()) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
						IP_Address = inetAddress.getHostAddress();
						return IP_Address;
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
