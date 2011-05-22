package com.sharkhunter.channel;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ChannelProxy {
	private Proxy p;
	private boolean state;
	private long lastCheck;
	private InetAddress ia;
	
	private static final long VerifyInterval=(60*1000*5);
	
	public ChannelProxy(String name,ArrayList<String> data,File dPath) throws UnknownHostException {
		String addr=null,port=null;
		p=null;
		state=false;
		lastCheck=0;
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2) // ignore weird lines
				continue;
			if(keyval[0].equalsIgnoreCase("addr"))
				addr=keyval[1];
			if(keyval[0].equalsIgnoreCase("port")) {
				port=keyval[1];
			}
		}
		Integer j=new Integer(port);
		InetAddress ia=InetAddress.getByName(addr);
		p=new Proxy(Proxy.Type.HTTP,(SocketAddress)new InetSocketAddress(ia,j.intValue()));
	}
	
	public boolean isUp() {
		long now=System.currentTimeMillis();
		if(now<(lastCheck+VerifyInterval))
			return state;
/*		try {
			state=ia.isReachable(7000);
		} catch (IOException e) {
			state=false;
		}*/
		state=true;	
		lastCheck=System.currentTimeMillis();
		return state;
	}
	
	public Proxy getProxy() {
		return p;
	}
	
	public void invalidate() {
		lastCheck=System.currentTimeMillis();
		state=false;
	}
}
