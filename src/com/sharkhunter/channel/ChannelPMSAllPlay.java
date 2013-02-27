package com.sharkhunter.channel;

import java.io.InputStream;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;

public class ChannelPMSAllPlay extends VirtualFolder {
	
	public ChannelPMSAllPlay(String name,String thumb) {
		super(name+" - ALL",thumb);
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}
	
	public void clearID() {
		setId(null);
	}
}
