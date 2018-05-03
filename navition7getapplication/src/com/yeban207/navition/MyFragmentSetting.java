package com.yeban207.navition;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yeban207.navition7getapplication.R;
import com.yeban207.pushserver.Params;
import com.yeban207.splash.LoginActivity;
import com.yeban207.utils.MePusher;

public class MyFragmentSetting extends Fragment {

	private String content;
	private Context context;
	//UI相关
	private View view;
	private EditText ed_username;
	private EditText ed_guidername;
	private EditText edt_modify_buffersize;
	private Button bt_modify_jian;
	private Button bt_modify_plus;
	private Button bt_modify_save;
	
	private String userName;
	private String guiderName;
	private Integer buffer_size;

	public MyFragmentSetting(String content) {
		this.content = content;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity().getApplicationContext();
		SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		userName = sp.getString("et_user", "");
		guiderName = sp.getString("et_guider", "");
		buffer_size = sp.getInt("buffer_size", 640);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fg_setting, container, false);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
	}

	private void initView() {
		ed_username = (EditText) view.findViewById(R.id.edt_username);
		if(userName != null ){
			ed_username.setText(userName);
		}
		
		ed_guidername = (EditText) view.findViewById(R.id.edt_guider_username);
		if(guiderName != null ){
			ed_guidername.setText(guiderName);
		}
		
		edt_modify_buffersize = (EditText) view.findViewById(R.id.edt_modify_buffersize);
		edt_modify_buffersize.setText(buffer_size.toString());
		
		bt_modify_jian = (Button) view.findViewById(R.id.bt_modify_jian);
		bt_modify_jian.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
				Editor edit = sp.edit();
				buffer_size = sp.getInt("buffer_size", 640);
				if(buffer_size>=640){
					buffer_size-=100;
					edt_modify_buffersize.setText(buffer_size.toString());
					edit.putInt("buffer_size", buffer_size);
					edit.commit();
				}
				edit.clear();
			}
		});
		bt_modify_plus = (Button) view.findViewById(R.id.bt_modify_plus);
		bt_modify_plus.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
				Editor edit = sp.edit();
				buffer_size = sp.getInt("buffer_size", 640);
				if(buffer_size<=2000){
					buffer_size+=100;
					edt_modify_buffersize.setText(buffer_size.toString());
					edit.putInt("buffer_size", buffer_size);
					edit.commit();
				}
				edit.clear();
			}
		});
		
		bt_modify_save = (Button) view.findViewById(R.id.bt_modify_save);
		bt_modify_save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (ed_username.getText().toString().length() == 0) {
					Toast.makeText(context, "请输入用户名", Toast.LENGTH_SHORT).show();
					ed_username.requestFocus();
					return;
				}
				if(ed_guidername.getText().toString().length() == 0){
					Toast.makeText(context, "请输入导游名", Toast.LENGTH_SHORT).show();
					ed_guidername.requestFocus();
					return;
				}
				if(edt_modify_buffersize.getText().toString().length() == 0){
					Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show();
					edt_modify_buffersize.requestFocus();
					return;
				}
				SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
				Editor edit = sp.edit();
				edit.putString("et_user", ed_username.getText().toString());
				edit.putString("et_guider", ed_guidername.getText().toString());
				edit.putInt("buffer_size", Integer.parseInt(edt_modify_buffersize.getText().toString()));
				edit.commit();
				MePusher.sendMsgToServer(context, sp.getString("et_guider", ""), "regist:"+sp.getString("et_user", ""));
				//Intent intent = new Intent(getActivity(), MainActivity.class);
				//startActivity(intent);
			}
		});
		
	}
}
