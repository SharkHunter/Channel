package com.sharkhunter.channel;

public class ChannelCred {

	public String user;
	public String pwd;
	public String channelName;
	public Channel ch;
	
	public ChannelCred(String u,String p,String name) {
		user=u;
		pwd=p;
		channelName=name;
		ch=null;
	}
}
