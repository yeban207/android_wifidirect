package com.yeban207.msg;

import com.baidu.mapapi.model.LatLng;

public class MsgLocation {
	private String id;
	private String name;
	private LatLng latLng;
	public MsgLocation() {
		super();
		// TODO Auto-generated constructor stub
	}
	public MsgLocation(String name, LatLng latLng, String id) {
		super();
		this.name = name;
		this.latLng = latLng;
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LatLng getLatLng() {
		return latLng;
	}
	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return new String("{"+id+","+name+","+latLng.toString()+"}");
	}
	
}
