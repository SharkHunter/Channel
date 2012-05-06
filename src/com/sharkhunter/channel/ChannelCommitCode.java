package com.sharkhunter.channel;

import java.io.InputStream;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelCommitCode extends VirtualFolder {

	private ChannelPMSCode code;
	
	public ChannelCommitCode(ChannelPMSCode code,String thumb) {
		super("Commit",thumb);
		this.code=code;
	}
	
	public void discoverChildren(String str) {
		code.setCode(str);
		discoverChildren();
	}
	
	public void discoverChildren() {
		code.add(this);
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
