package com.sharkhunter.channel;

import java.io.InputStream;
import java.util.ArrayList;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelGroupFolder extends VirtualFolder{
	
	ArrayList<ChannelPMSFolder> members;
	
	public ChannelGroupFolder(String name,ArrayList<ChannelPMSFolder> data,String thumb) {
		super(name,thumb);
		members=data;
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}
	
	public void discoverChildren() {
		for(int i=0;i<members.size();i++)
			addChild(members.get(i));
	}
}
