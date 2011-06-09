package com.sharkhunter.channel;

public abstract class ChannelHook {
	
	public final static int NONE=0;
	public final static int ALL=1;
	public final static int PART=2;
	
	public abstract int pre(String tag,String baseUrl);
	
	public abstract int post(String tag,String baseUrl);	
}
