package com.sharkhunter.channel;

import java.io.InputStream;
import java.net.MalformedURLException;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelPMSItem extends VirtualFolder implements ChannelFilter{
	
	private ChannelItem ci;
	private String filter;
	private String url;
	
	public ChannelPMSItem(ChannelItem ci,String name,String filter,String url,String thumb) {
		super(name,thumb);
		this.ci=ci;
		this.filter=filter;
		this.url=url;
	}
	
	public void discoverChildren() {
		try {
			ci.match(this,filter,url,"",thumbnailIcon);
		} catch (MalformedURLException e) {
		}
	}
	
	public boolean refreshChildren() { // Always update
		return true;
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}
	
	public boolean filter(String str) {
		return true;
	}
	
	public String getThumb() {
		return (thumbnailIcon);
	}
}
