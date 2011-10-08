package com.sharkhunter.channel;

import java.io.InputStream;

import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;
import net.pms.formats.Format;

public class ChannelPMSSaveFolder extends VirtualFolder {
	
	private Channel ch;
	private String url;
	private String name;
	private String thumb;
	private String proc;
	private int asx;
	private ChannelScraper scraper;
	private int f;
	private long childDone;
	private String imdb;
	private boolean subs;
	private boolean rawSave;
	private String videoFormat;
	
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
		this.f=type;
		childDone=0;
		imdb=null;
		subs=true;
		rawSave=false;
		videoFormat=null;
	}
	
	public void setImdb(String i) {
		imdb=i;
	}
	
	public void setDoSubs(boolean b) {
		subs=b;
	}
	
	public boolean refreshChildren() { // Always update
		return true;
	}
	
	public void setSaveMode(boolean raw) {
		rawSave=raw;
	}
	
	public void discoverChildren() {
		final ChannelPMSSaveFolder me=this;
		final ChannelOffHour oh=Channels.getOffHour();
		if(oh!=null) {
			final boolean add=!oh.scheduled(url);
			final String rName=name;
			addChild(new VirtualVideoAction((add?"ADD to ":"DELETE from ")+
											"offhour download", true) { //$NON-NLS-1$
				public boolean enable() {
					if(me.preventAutoPlay())
						return false;
					String rUrl=url;
					if(scraper!=null)
						rUrl=scraper.scrape(ch, url, proc, f, this,false,null);
					oh.update(rUrl, rName, add);
					return add;
				}
			});
		}
		if(ChannelNaviXUpdate.active()) {
			final String rName=name;
			addChild(new VirtualVideoAction("Upload to NaviX",true) { //$NON-NLS-1$
				public boolean enable() {
					try {
						if(me.preventAutoPlay())
							return false;
						ChannelNaviXUpdate.updateMedia(rName, url, proc, f,thumb,imdb);
					} catch (Exception e) {
					}
					return true;
				}
			});
		}
		ChannelMediaStream cms=new ChannelMediaStream(ch,"SAVE&PLAY",url,thumb,proc,f,asx,scraper,name,name);
		cms.setImdb(imdb);
		cms.setRender(this.defaultRenderer);
		cms.setSaveMode(rawSave);
		cms.setFallbackFormat(videoFormat);
		addChild(cms);
		cms=new ChannelMediaStream(ch,"PLAY",url,thumb,proc,f,asx,scraper,name,null);
		cms.setImdb(imdb);
		cms.setRender(this.defaultRenderer);
		cms.setSaveMode(rawSave);
		cms.setFallbackFormat(videoFormat);
		addChild(cms);
		if(Channels.doSubs()&&subs&&(f==Format.VIDEO)) {
			cms=new ChannelMediaStream(ch,"SAVE&PLAY - No Subs",url,thumb,proc,f,asx,scraper,name,name);
			cms.noSubs();
			cms.setImdb(imdb);
			cms.setRender(this.defaultRenderer);
			cms.setSaveMode(rawSave);
			cms.setFallbackFormat(videoFormat);
			addChild(cms);
			cms=new ChannelMediaStream(ch,"PLAY - No Subs",url,thumb,proc,f,asx,scraper,name,null);
			cms.noSubs();
			cms.setImdb(imdb);
			cms.setRender(this.defaultRenderer);
			cms.setSaveMode(rawSave);
			cms.setFallbackFormat(videoFormat);
			addChild(cms);
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
	
	public void setFallbackFormat(String s) {
		videoFormat=s;
	}
}
