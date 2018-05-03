package com.yeban207.navition;

import java.io.IOException;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.yeban207.navition7getapplication.R;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class MyFragmentOKHttp extends Fragment implements OnClickListener {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fg_okhttptest, container, false);
		initView(view);
		return view;
	}

	private void initView(View view) {
		Button bt_httpget = (Button) view.findViewById(R.id.id_bt_httpget);
		bt_httpget.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.id_bt_httpget:
			// 创建okHttpClient对象
			OkHttpClient mOkHttpClient = new OkHttpClient();
			// 创建一个Request
			final Request request = new Request.Builder().url("https://www.baidu.com").build();
			// new call
			Call call = mOkHttpClient.newCall(request);
			// 请求加入调度
			call.enqueue(new Callback() {
				@Override
				public void onFailure(Request request, IOException e) {
				}

				@Override
				public void onResponse(final Response response) throws IOException {
					 String htmlStr = response.body().string();
					 Log.d("xyz", htmlStr);
				}
			});
			break;

		default:
			break;
		}

	}
}
