package com.yeban207.navition;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MyOrientationListener implements SensorEventListener {

	// ������������
	private SensorManager mSensorManager;
	// ������
	private Context mContext;
	// ������
	private Sensor mSensor;
	
	private float mLastX;

	// ���캯��
	public MyOrientationListener(Context context) {
		this.mContext = context;
	}

	// ��ʼ����
	@SuppressWarnings("deprecation")
	public void start() {
		// ��ô�����������
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager != null) {// �Ƿ�֧��
			// ��÷��򴫸���
			mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		}
		if (mSensor != null) {// ����ֻ��з��򴫸��������ȿ����Լ�ȥ���ã�ע�᷽�򴫸���
			mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
		}
	}

	// ��������
	public void stop() {
		// ȡ��ע��ķ��򴫸���
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		//�жϷ��صĴ����������ǲ��Ƿ��򴫸���
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            //ֻ��ȡx��ֵ
            float x = event.values[SensorManager.DATA_X];
            //Ϊ�˷�ֹ�����Եĸ���
            if(Math.abs(x-mLastX)>1.0){
                if(mOnOrientationListener!=null){
                	mOnOrientationListener.onOrientationChanged(x);
                }
            }
            mLastX = x;
        }
	}
	
	private OnOrientationListener mOnOrientationListener;
	
	 public void setmOnOrientationListener(OnOrientationListener mOnOrientationListener) {
		this.mOnOrientationListener = mOnOrientationListener;
	}


	//�ص�����
    public interface OnOrientationListener{
        void onOrientationChanged(float x);
    }
	
}
