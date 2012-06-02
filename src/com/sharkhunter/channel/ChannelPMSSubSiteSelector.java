package com.sharkhunter.channel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelPMSSubSiteSelector extends VirtualFolder {
	
	private Channel ch;
	private String name;
	private String url;
	private String thumb;
	private String proc;
	private int type;
	private int asx;
	private ChannelScraper scraper;
	private String dispName;
	private String saveName;
	private String imdb;
	private HashMap<String,String> stash;
	
	public ChannelPMSSubSiteSelector(Channel ch,String name,String nextUrl,
			String thumb,String proc,int type,int asx,
			ChannelScraper scraper,String dispName,
			String saveName,String imdb) {
		this(ch,name,nextUrl,thumb,proc,type,asx,scraper,dispName,saveName,imdb,null);
	}

	public ChannelPMSSubSiteSelector(Channel ch,String name,String nextUrl,
			String thumb,String proc,int type,int asx,
			ChannelScraper scraper,String dispName,
			String saveName,String imdb,HashMap<String,String> stash) {
		super(name,thumb);
		url=nextUrl;
		this.name=name;
		this.thumb=thumb;
		this.proc=proc;
		this.type=type;
		this.ch=ch;
		this.asx=asx;
		this.scraper=scraper;
		this.saveName=saveName;
		this.dispName=dispName;
		this.imdb=imdb;
		this.stash=stash;
	}
	
	public void discoverChildren() {
		if(scraper==null)
			return;
		ArrayList<String> sites=scraper.subSites();
		if(sites==null)
			return;
		for(String site : sites) {
			ChannelPMSSubSelector subSel=new ChannelPMSSubSelector(ch,site,url,thumb,
											 proc,type,asx,scraper,dispName,saveName,
											 imdb,stash);
			subSel.setSite(site);
			addChild(subSel);
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
