package com.sharkhunter.channel;

public abstract class ChannelHook {
	
	public static final int URL=0x01;
	public static final int NAME=0x02;
	public static final int THUMB=0x04;
	public static final int SCRAPED=0x08;
	
	public static final int DONE=URL|SCRAPED;
	
	public String url;
	public String name;
	public String thumb;
	public int result;
	
	public abstract int pre(String script,String tag,String baseUrl);
	
	public abstract int post(String script,String tag,String baseUrl);	
}
