package com.sharkhunter.channel;

import java.io.InputStream;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelATZ extends VirtualFolder {
	
	private ChannelFolder folder;
	private String url;
	
	public ChannelATZ(ChannelFolder cf) {
		this(cf,"");
	}
	
	public ChannelATZ(ChannelFolder cf,String url) {
		super(cf.getName()==null?"A-Z":cf.getName(),cf.getThumb());
		folder=cf;
		this.url=url;
	}
	
	public void discoverChildren() {
		for(char i='A';i<='Z';i++)
			addChild(new ChannelPMSFolder(folder,i,url));
		addChild(new ChannelPMSFolder(folder,'#',url));
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}
}
