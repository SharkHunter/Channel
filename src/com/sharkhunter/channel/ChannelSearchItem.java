package com.sharkhunter.channel;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelSearchItem extends VirtualFolder{
	private String id;
	private String str;
	private Channel ch;
	private long accessed;
	
	public ChannelSearchItem(String id,String str,Channel ch) {
		super(str,null);
		this.id=id;
		this.str=str;
		this.ch=ch;
		touch();
	}
	
	public Channel getChannel() {
		return ch;
	}
	
	public boolean equal(Channel ch,String id,String str) {
		return this.ch.name().equals(ch.name())&&
				id.equals(this.id)&&
				str.equals(this.str);
	}
	
	public long access() {
		return accessed;
	}
	
	public void discoverChildren() {
		touch();
		Channels.debug("perform research on "+toString());
		ch.research(str, id, this);
	}
	
	public String toString() {
		return ch.name()+","+id+","+str;
	}
	
	public void touch() {
		accessed=System.currentTimeMillis();
	}
}
