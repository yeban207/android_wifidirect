package com.yeban207.msg;

public class MsgEvents {
	// ��־λ ������Ա
	private boolean isOnwer = false;
	private boolean isConnect = false;

	public MsgEvents() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MsgEvents(boolean isOnwer, boolean isConnect) {
		super();
		this.isOnwer = isOnwer;
		this.isConnect = isConnect;
	}

	public boolean isOnwer() {
		return isOnwer;
	}

	public void setOnwer(boolean isOnwer) {
		this.isOnwer = isOnwer;
	}

	public boolean isConnect() {
		return isConnect;
	}

	public void setConnect(boolean isConnect) {
		this.isConnect = isConnect;
	}

}
