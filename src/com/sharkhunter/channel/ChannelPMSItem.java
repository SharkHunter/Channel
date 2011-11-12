package com.sharkhunter.channel;

import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.commons.lang.StringEscapeUtils;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelPMSItem extends VirtualFolder implements ChannelFilter{
	
	private ChannelItem ci;
	private String filter;
	private String url;
	
	public ChannelPMSItem(ChannelItem ci,String name,String filter,String url,String thumb) {
		super(StringEscapeUtils.unescapeHtml(name),thumb);
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
	
	public boolean isRefreshNeeded() {
		return true;
	}
	
	public boolean isTranscodeFolderAvailable() {
		return false;
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
