package com.sharkhunter.channel;

import net.pms.dlna.virtual.VirtualFolder;

import org.apache.commons.lang.StringEscapeUtils;

public class ChannelPMSSwitch extends VirtualFolder implements ChannelFilter{
	
	private ChannelSwitch cs;
	private String filter;
	private String url;
	private String imdb;
	private Channel dstCh;
	
	private boolean thumbScriptRun;
	private boolean favorized;
	
	
	public ChannelPMSSwitch(Channel dst,ChannelSwitch cs,String name,String filter,String url,String thumb) {
		super(name==null?"":StringEscapeUtils.unescapeHtml(name),thumb);
		this.cs=cs;
		this.filter=filter;
		this.url=url;
		this.dstCh=dst;
		favorized=false;
		thumbScriptRun=false;
	}
	
	public void discoverChildren() {
		dstCh.action(cs, null, url, thumbnailIcon, this);
	}


	@Override
	public boolean filter(String str) {
		return true;
	}
	
	

}
