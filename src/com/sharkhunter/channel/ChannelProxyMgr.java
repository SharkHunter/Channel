package com.sharkhunter.channel;

import java.util.HashMap;

public class ChannelProxyMgr {
	private HashMap<String,ChannelProxy> proxies;

	public ChannelProxyMgr() {
		proxies=new HashMap<String,ChannelProxy>();
	}
	
	public void put(String name,ChannelProxy p) {
		if(proxies.get(name)==null)
			proxies.put(name, p);
	}
	
	public ChannelProxy get(String name) {
		return proxies.get(name);
	}
	
	public ChannelProxy find(String country,String[] prop) {
		for(String key : proxies.keySet()) {
			ChannelProxy p=proxies.get(key);
		/*	if(!p.country().equalsIgnoreCase(country))
				continue;
			if(!p.support(prop))
				continue;*/
			return p;
		}
		return null;
	}
}
