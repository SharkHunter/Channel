package com.sharkhunter.channel;

import java.util.ArrayList;

public class WEB extends com.chocolatey.pmsencoder.WEB {
	private final static String[] def={"rtmp","rtmpe","synacast"};
	private ArrayList<String> extra;
	
	public WEB() {
		super();
		extra=new ArrayList<String>();
		for(int i=0;i<def.length;i++)
			extra.add(def[i]);
	}
	
	public String [] getId() {
		int len=extra.size();
		String[] x=super.getId();
		String[] res=new String[x.length+len];
		for(int i=0;i<len;i++)
			res[i]=extra.get(i);
		for(int i=0;i<x.length;i++)
			res[i+len]=x[i];
		return res;
	}
	
	public void addExtra(String str) {
		extra.add(str);
	}

}