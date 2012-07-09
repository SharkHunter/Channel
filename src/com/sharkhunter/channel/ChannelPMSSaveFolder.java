package com.sharkhunter.channel;

import java.io.InputStream;
import java.util.HashMap;

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
	private String embedSub;
	private HashMap<String,String> stash;

	
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
		embedSub=null;
	}
	
	public void setImdb(String i) {
		imdb=i;
	}
	
	public void setDoSubs(boolean b) {
		subs=b;
	}
	
	public void setSaveMode(boolean raw) {
		rawSave=raw;
	}
	
	public void setEmbedSub(String str) {
		embedSub=str;
	}
	
	public void setStash(HashMap<String,String> map) {
		stash=map;
	}
	
	private String displayName(String n) {
		if(Channels.cfg().longSaveName())
			return ChannelUtil.append(n, " ", name);
		else
			return n;
	}
		
	public void discoverChildren() {
		final ChannelPMSSaveFolder me=this;
		final ChannelOffHour oh=Channels.getOffHour();
		final String rName=name;
		boolean save=Channels.save();
		boolean doSubs=Channels.doSubs()&&subs&&(f==Format.VIDEO);
		ChannelMediaStream cms;
		ChannelStreamVars streamVars=new ChannelStreamVars(Channels.defStreamVar());
		streamVars.add(this, ch);
		streamVars.setInstance(String.valueOf(name.hashCode()));
		if(oh!=null) {
			final boolean add=!oh.scheduled(url);
			addChild(new VirtualVideoAction((add?"ADD to ":"DELETE from ")+
											"offhour download", true) { //$NON-NLS-1$
				public boolean enable() {
					if(me.preventAutoPlay())
						return false;
					String rUrl=url;
					if(scraper!=null)
						rUrl=scraper.scrape(ch, url, proc, f, this,false,null,
											embedSub,stash);
					oh.update(rUrl, rName, add);
					me.childDone();
					return add;
				}
			});
		}
		if(ChannelNaviXUpdate.active()) {
			addChild(new VirtualVideoAction("Upload to NaviX",true) { //$NON-NLS-1$
				public boolean enable() {
					try {
						if(me.preventAutoPlay())
							return false;
						String rUrl=url;
						if(scraper!=null)
							rUrl=scraper.scrape(ch, url, proc, f, this,true,null,
												embedSub,stash);
						ChannelNaviXUpdate.updateMedia(ch,rName, rUrl, proc, f,thumb,imdb);
					} catch (Exception e) {
					}
					me.childDone();
					return true;
				}
			});
		}
		if(save) {
			addChild(new VirtualVideoAction("Download",true) {
				public boolean enable() {
					try {
						if(me.preventAutoPlay())
							return false;
						String rUrl=url;
						if(scraper!=null)
							rUrl=scraper.scrape(ch, url, proc, f, this,false,null,
												embedSub,stash);
						if(ChannelUtil.empty(rUrl))
							return false;
						Thread t=ChannelUtil.backgroundDownload(rName, rUrl, false);
						t.start();
					} catch (Exception e) {
					}
					me.childDone();
					return true;
				}
			});
			if(!doSubs||Channels.cfg().oldSub()) {
				cms=new ChannelMediaStream(ch,displayName("SAVE&PLAY"),url,thumb,
						proc,f,asx,scraper,name,name);
				cms.setImdb(imdb);
				cms.setRender(this.defaultRenderer);
				cms.setSaveMode(rawSave);
				cms.setEmbedSub(embedSub);
				cms.setFallbackFormat(videoFormat);
				cms.setStreamVars(streamVars);
				cms.setStash(stash);
				addChild(cms);
			}
			if(doSubs) {
				if(Channels.cfg().oldSub()) {
					ChannelPMSSubSelector subSel=new ChannelPMSSubSelector(ch,displayName("Select subs - SAVE&PLAY"),
							url,thumb,proc,f,asx,
							scraper,name,name,imdb,stash);
					subSel.setStreamVars(streamVars);
					addChild(subSel);
				}
				else {
					ChannelPMSSubSiteSelector subSel=new ChannelPMSSubSiteSelector(ch,displayName("Select subs - SAVE&PLAY"),
							url,thumb,proc,f,asx,
							scraper,name,name,imdb,stash);
					subSel.setStreamVars(streamVars);
					addChild(subSel);
				}
			}
		}
		if(!doSubs||Channels.cfg().oldSub()) {
			cms=new ChannelMediaStream(ch,displayName("PLAY"),url,thumb,
					proc,f,asx,scraper,name,null);
			cms.setImdb(imdb);
			cms.setRender(this.defaultRenderer);
			cms.setSaveMode(rawSave);
			cms.setEmbedSub(embedSub);
			cms.setFallbackFormat(videoFormat);
			cms.setStreamVars(streamVars);
			cms.setStash(stash);
			addChild(cms);
		}
		if(doSubs) {
			if(Channels.cfg().oldSub()) {
				ChannelPMSSubSelector subSel=new ChannelPMSSubSelector(ch,displayName("Select subs - PLAY"),
						url,thumb,proc,f,asx,
						scraper,name,name,imdb,stash);
				subSel.setStreamVars(streamVars);
				addChild(subSel);
			}
			else {
				ChannelPMSSubSiteSelector subSel=new ChannelPMSSubSiteSelector(ch,displayName("Select subs - PLAY"),
						url,thumb,proc,f,asx,
						scraper,name,name,imdb,stash);
				subSel.setStreamVars(streamVars);
				addChild(subSel);
			}
			if(save) {
				cms=new ChannelMediaStream(ch,displayName("SAVE&PLAY - No Subs"),url,
										   thumb,proc,f,asx,scraper,name,name);
				cms.noSubs();
				cms.setImdb(imdb);
				cms.setRender(this.defaultRenderer);
				cms.setSaveMode(rawSave);
				cms.setFallbackFormat(videoFormat);
				cms.setStreamVars(streamVars);
				cms.setStash(stash);
				addChild(cms);
			}
			cms=new ChannelMediaStream(ch,displayName("PLAY - No Subs"),url,thumb,
								       proc,f,asx,scraper,name,null);
			cms.noSubs();
			cms.setImdb(imdb);
			cms.setRender(this.defaultRenderer);
			cms.setSaveMode(rawSave);
			cms.setFallbackFormat(videoFormat);
			cms.setStreamVars(streamVars);
			cms.setStash(stash);
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
	
	public boolean isRefreshNeeded() {
		return true;
	}
    
    public boolean refreshChildren() {
    	refreshChildren(null);
    	return true;
    }
	
	public boolean refreshChildren(String str) {
		if(str==null)
			return false;
		getChildren().clear();
		//discoverChildren(str);
		return true;
	}
	
	public void resolve() {
		setDiscovered(false);
	}
}
