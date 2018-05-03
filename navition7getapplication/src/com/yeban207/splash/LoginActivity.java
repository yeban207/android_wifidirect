package com.yeban207.splash;

import com.yeban207.navition.MainActivity;
import com.yeban207.navition7getapplication.R;
import com.yeban207.utils.MePusher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private SharedPreferences sp;
	private EditText et_user;
	private EditText et_guider;
	private Button bt_regist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		sp = this.getSharedPreferences("config", MODE_PRIVATE);
		if (sp.getBoolean("isFirst", false)) {
			Login();
		}
		setContentView(R.layout.activity_login);
		initView();

	}

	private void initView() {
		et_user = (EditText) findViewById(R.id.et_user);
		et_guider = (EditText) findViewById(R.id.et_guider);
		bt_regist = (Button) findViewById(R.id.bt_regist);
		bt_regist.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LoginActivity.this.start();
			}
		});
		
	}
	
	protected void start() {
		// TODO Auto-generated method stub
		if (et_user.getText().toString().length() == 0) {
			Toast.makeText(this.getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
			et_user.requestFocus();
			return;
		}
		if(et_guider.getText().toString().length() == 0){
			Toast.makeText(this.getApplicationContext(), "请输入导游名", Toast.LENGTH_SHORT).show();
			et_guider.requestFocus();
			return;
		}
		String guiderName = et_guider.getText().toString();
		String userName = et_user.getText().toString();
		sp = this.getSharedPreferences("config", MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putBoolean("isFirst", true);
		edit.putString("et_user", userName);
		edit.putString("et_guider", guiderName);
		edit.putInt("buffer_size", 640);
		edit.commit();
		MePusher.saveAccountInfo(getApplicationContext(), "192.168.1.103", "9966", "9999", et_user.getText().toString());
		Login();
	}

	private void Login() {
		sp = this.getSharedPreferences("config", MODE_PRIVATE);
		MePusher.sendToServer(getApplicationContext());
		MePusher.sendMsgToServer(getApplicationContext(), sp.getString("et_guider", ""), "regist:"+sp.getString("et_user", ""));
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		this.startActivity(intent);
		this.finish();
	}

}
