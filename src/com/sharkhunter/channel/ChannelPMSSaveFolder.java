package com.sharkhunter.channel;

import java.io.InputStream;

import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;

public class ChannelPMSSaveFolder extends VirtualFolder {
	
	private Channel ch;
	private String url;
	private String name;
	private String thumb;
	private String proc;
	private int asx;
	private ChannelScraper scraper;
	private int type;
	private long childDone;
	private String imdb;
	
	private static final long AUTO_PLAY_FACTOR=(1000*15);
	
	public ChannelPMSSaveFolder(Channel ch,String name,String url,String thumb,
								String proc,int asx,int type,
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
		childDone=0;
		imdb=null;
	}
	
	public void setImdb(String i) {
		imdb=i;
	}
	
	public boolean refreshChildren() { // Always update
		return true;
	}
	
	public void discoverChildren() {
		ChannelMediaStream cms=new ChannelMediaStream(ch,"SAVE&PLAY",url,thumb,proc,type,asx,scraper,name,name);
		cms.setImdb(imdb);
		addChild(cms);
		cms=new ChannelMediaStream(ch,"PLAY",url,thumb,proc,type,asx,scraper,name,null);
		cms.setImdb(imdb);
		addChild(cms);
		if(Channels.doSubs()) {
			cms=new ChannelMediaStream(ch,"SAVE&PLAY - No Subs",url,thumb,proc,type,asx,scraper,name,name);
			cms.noSubs();
			cms.setImdb(imdb);
			addChild(cms);
			cms=new ChannelMediaStream(ch,"PLAY - No Subs",url,thumb,proc,type,asx,scraper,name,null);
			cms.noSubs();
			cms.setImdb(imdb);
			addChild(cms);
		}
		final ChannelOffHour oh=Channels.getOffHour();
		if(oh!=null) {
			final boolean add=!oh.scheduled(url);
			final String rName=name;
			addChild(new VirtualVideoAction((add?"ADD to ":"DELETE from ")+
											"offhour download", true) { //$NON-NLS-1$
				public boolean enable() {
					String rUrl=url;
					if(scraper!=null)
						rUrl=scraper.scrape(ch, url, proc, type, this,false,null);
					oh.update(rUrl, rName, add);
					return add;
				}
			});
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
	
	public void childDone() {
		childDone=System.currentTimeMillis();
	}
	
	public boolean preventAutoPlay() {
		// Normally childDone is 0 and 0+15000 is never larger
		// then now.
		return (childDone+AUTO_PLAY_FACTOR)>System.currentTimeMillis();
	}
}
