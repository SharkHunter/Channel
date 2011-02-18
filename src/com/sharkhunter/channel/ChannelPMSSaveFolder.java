package com.sharkhunter.channel;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelPMSSaveFolder extends VirtualFolder {
	
	private Channel ch;
	private String url;
	private String name;
	private String thumb;
	private String proc;
	private boolean asx;
	private ChannelScraper scraper;
	private int type;
	
	public ChannelPMSSaveFolder(Channel ch,String name,String url,String thumb,
								String proc,boolean asx,int type,
								ChannelScraper scraper) {
		super(name,thumb);
		this.url=url;
		this.name=(ChannelUtil.empty(name)?"download":name);
		this.thumb=thumb;
		this.proc=proc;
		this.ch=ch;
		this.asx=asx;
		this.scraper=scraper;
		this.type=type;
	}
	
	public void discoverChildren() {
		addChild(new ChannelMediaStream(ch,"SAVE&PLAY",url,thumb,proc,type,asx,scraper,name));
		addChild(new ChannelMediaStream(ch,"PLAY",url,thumb,proc,type,asx,scraper,null));
	}
}
