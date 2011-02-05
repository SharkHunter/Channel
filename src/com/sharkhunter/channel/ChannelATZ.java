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
		for(char i='A';i<='Z';i++) {
			if(folder.getType()==ChannelFolder.TYPE_ATZ)
				addChild(new ChannelPMSFolder(folder,i,url));
			else if(folder.getType()==ChannelFolder.TYPE_ATZ_LINK) {
				addChild(new ChannelPMSFolder(folder,String.valueOf(i),null,
						   "/"+String.valueOf(i),folder.getThumb()));
			}
		}
		if(folder.getType()==ChannelFolder.TYPE_ATZ)
			addChild(new ChannelPMSFolder(folder,'#',url));
		else if(folder.getType()==ChannelFolder.TYPE_ATZ_LINK) {
			String otherStr=folder.getProp("other_string");
			otherStr=(otherStr==null?"/#":"/"+otherStr);
			addChild(new ChannelPMSFolder(folder,"#",null,otherStr,folder.getThumb()));
		}
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
