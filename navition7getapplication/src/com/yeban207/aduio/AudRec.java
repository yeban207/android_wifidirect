package com.yeban207.aduio;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.yeban207.navition.MyFragmentWifiDirect;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudRec extends Thread {

	private String ip;
	private int port;
	private int minBufferSize;
	private AudioRecord mAudioRecord = null;
	private byte[] minbufbytes;
	private final int UDPLOCALPORT = 25556;

	public AudRec(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	@SuppressWarnings("deprecation")
	public boolean inint() {

		// 1.采样率 8000
		// 2.音频录制声道 CHANNEL_CONFIGURATION_MONO单声道 CHANNEL_IN_STEREO双声道
		// 3.音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
		minBufferSize = AudioRecord.getMinBufferSize(8000, //
				AudioFormat.CHANNEL_CONFIGURATION_MONO, //
				AudioFormat.ENCODING_PCM_16BIT);

		// 1.音频获取源 MediaRecorder.AudioSource.MIC
		// 2.采样率 8000
		// 3.音频录制声道 CHANNEL_CONFIGURATION_MONO单声道 CHANNEL_IN_STEREO双声道
		// 4.音频大小minbuffersize

		// new AudioRecord对象耗时345ms
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, //
				8000, //
				AudioFormat.CHANNEL_CONFIGURATION_MONO, //
				AudioFormat.ENCODING_PCM_16BIT, //
				minBufferSize);

		if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
			// 初始化失败
			return false;
		}
		minbufbytes = new byte[minBufferSize];

		return true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			mAudioRecord.startRecording();
			DatagramSocket ds = new DatagramSocket(UDPLOCALPORT);
			while (MyFragmentWifiDirect.m_flagRec) {
				mAudioRecord.read(minbufbytes, 0, minBufferSize);
				try {
					InetAddress address = InetAddress.getByName(ip);
					DatagramPacket dp = new DatagramPacket(minbufbytes, minbufbytes.length, address, port);
					ds.send(dp);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ds.close();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mAudioRecord != null) {
			mAudioRecord.stop();
			mAudioRecord.release();
			mAudioRecord = null;
		}
		minbufbytes = null;
	}

}
