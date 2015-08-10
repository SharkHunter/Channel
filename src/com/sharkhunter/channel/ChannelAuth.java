package com.sharkhunter.channel;

public class ChannelAuth {
	public int method;
	public String authStr;
	public long ttd;
	public ChannelProxy proxy;

	public ChannelAuth() {
	}

	public ChannelAuth(ChannelAuth a) {
		method=a.method;
		authStr=a.authStr;
		ttd=a.ttd;
		proxy=a.proxy;
	}
}
