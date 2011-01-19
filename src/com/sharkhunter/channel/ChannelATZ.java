package com.sharkhunter.channel;

import java.io.InputStream;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelATZ extends VirtualFolder {
	
	private ChannelFolder folder;
	
	public ChannelATZ(ChannelFolder cf) {
		super(cf.getName()==null?"A-Z":cf.getName(),cf.getThumb());
		folder=cf;
	}
	
	public void discoverChildren() {
		for(char i='A';i<='Z';i++)
			addChild(new ChannelPMSFolder(folder,i));
		addChild(new ChannelPMSFolder(folder,'#'));
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
