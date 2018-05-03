package com.yeban207.navition;


import com.yeban207.navition7getapplication.R;
import com.yeban207.udpserverservice.UdpServerService;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends Activity implements OnCheckedChangeListener {

	// UI相关的控件
	private RadioGroup rb_tab_bar;
	private RadioButton rb_message;
	// fragment相关
	private Fragment f4 = new MyFragmentSetting("第四个fragment啦");
	private MyFragmentOKHttp mFgokHttp = new MyFragmentOKHttp();
	private MyFragmentBaiduMap mFgBMap = new MyFragmentBaiduMap();
	private MyFragmentWifiDirect mFgWd = new MyFragmentWifiDirect();
	private FragmentManager fManager;
	//按回退次数
	private int backCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 隐藏上部导航栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		// fragment的管理器
		fManager = getFragmentManager();
		initFragment();
		// 找到按钮组
		rb_tab_bar = (RadioGroup) findViewById(R.id.rg_tab_bar);
		rb_tab_bar.setOnCheckedChangeListener(this);
		// 找到第一个按钮 并选中
		rb_message = (RadioButton) findViewById(R.id.rb_message);
		rb_message.setChecked(true);

		startUdpServerService();

	}

	private void initFragment() {
		// TODO Auto-generated method stub
		FragmentTransaction transaction = fManager.beginTransaction();
		f4 = new MyFragmentSetting("第四个fragment啦");
		mFgokHttp = new MyFragmentOKHttp();
		mFgBMap = new MyFragmentBaiduMap();
		mFgWd = new MyFragmentWifiDirect();
		transaction.add(R.id.ly_content, mFgBMap);
		transaction.add(R.id.ly_content, mFgWd);
		transaction.add(R.id.ly_content, mFgokHttp);
		transaction.add(R.id.ly_content, f4);
		transaction.commit();
	}

	/**
	 * 在一进入软件打开就启动服务
	 */
	private void startUdpServerService() {
		// TODO Auto-generated method stub
		Intent mUdpIntent = new Intent(MainActivity.this, UdpServerService.class);
		// mUdpIntent.setAction("com.yeban207.UDPserverservice");
		// mUdpIntent.setPackage("com.yeban207.udpserverservice");
		startService(mUdpIntent);
		Log.i("xyz", "开始启动UDP服务");
	}

	private void hideAllFragment(FragmentTransaction transaction) {
		if (mFgBMap != null)
			transaction.hide(mFgBMap);
		if (mFgWd != null)
			transaction.hide(mFgWd);
		if (mFgokHttp != null)
			transaction.hide(mFgokHttp);
		if (f4 != null)
			transaction.hide(f4);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		FragmentTransaction transaction = fManager.beginTransaction();
		hideAllFragment(transaction);
		switch (checkedId) {
		case R.id.rb_channel:
			if (mFgBMap == null) {
				mFgBMap = new MyFragmentBaiduMap();
				transaction.add(R.id.ly_content, mFgBMap);
			} else {
				transaction.show(mFgBMap);
			}
			break;
		case R.id.rb_message:
			Log.d("xyz", "点击第二个按钮1");
			if (mFgWd == null) {
				Log.d("xyz", "点击第二个按钮2");
				mFgWd = new MyFragmentWifiDirect();
				Log.d("xyz", "点击第二个按钮3");
				transaction.add(R.id.ly_content, mFgWd);
			} else {
				transaction.show(mFgWd);
			}
			break;
		case R.id.rb_better:
			if (mFgokHttp == null) {
				mFgokHttp = new MyFragmentOKHttp();
				transaction.add(R.id.ly_content, mFgokHttp);
			} else {
				transaction.show(mFgokHttp);
			}
			break;
		case R.id.rb_setting:
			if (f4 == null) {
				f4 = new MyFragmentSetting("第四个fragment啦");
				transaction.add(R.id.ly_content, f4);
			} else {
				transaction.show(f4);
			}
			break;

		default:
			break;
		}
		transaction.commit();
	}

	// 屏蔽了back按钮了
	@Override
	public void onBackPressed() {
		backCount++;
		if(backCount >= 5){
			super.onBackPressed();
		}
	}

}
