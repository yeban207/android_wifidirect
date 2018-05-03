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

		// 1.������ 8000
		// 2.��Ƶ¼������ CHANNEL_CONFIGURATION_MONO������ CHANNEL_IN_STEREO˫����
		// 3.��Ƶ���ݸ�ʽ:PCM 16λÿ����������֤�豸֧�֡�PCM 8λÿ����������һ���ܵõ��豸֧�֡�
		minBufferSize = AudioRecord.getMinBufferSize(8000, //
				AudioFormat.CHANNEL_CONFIGURATION_MONO, //
				AudioFormat.ENCODING_PCM_16BIT);

		// 1.��Ƶ��ȡԴ MediaRecorder.AudioSource.MIC
		// 2.������ 8000
		// 3.��Ƶ¼������ CHANNEL_CONFIGURATION_MONO������ CHANNEL_IN_STEREO˫����
		// 4.��Ƶ��Сminbuffersize

		// new AudioRecord�����ʱ345ms
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, //
				8000, //
				AudioFormat.CHANNEL_CONFIGURATION_MONO, //
				AudioFormat.ENCODING_PCM_16BIT, //
				minBufferSize);

		if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
			// ��ʼ��ʧ��
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
