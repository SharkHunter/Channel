package com.sharkhunter.channel;

import java.io.InputStream;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import net.pms.dlna.DLNAMediaSubtitle;
import org.apache.commons.lang.StringUtils;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;

public class ChannelPMSSubSelector extends VirtualFolder {
	
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

	private String matchName;
	private String site;
	
	private ChannelStreamVars streamVars;
	
	private static final String PNS="PLAY (no subs match)";
	
	public ChannelPMSSubSelector(Channel ch,String name,String nextUrl,
				 String thumb,String proc,int type,int asx,
				 ChannelScraper scraper,String dispName,
				 String saveName,String imdb) {
		this(ch,name,nextUrl,thumb,proc,type,asx,scraper,dispName,saveName,imdb,null);
	}
	
	public ChannelPMSSubSelector(Channel ch,String name,String nextUrl,
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
		matchName=null;
		this.stash=stash;
		site=null;
		streamVars=null;
	}
	
	public void setSite(String s) {
		site=s;
		ChannelSubs subs=Channels.getSubs(site);
		if(!ChannelUtil.empty(subs.getImg()))
			thumbnailIcon=subs.getImg();
	}
	
	public void setStreamVars(ChannelStreamVars vars) {
		streamVars=vars;
	}

	public void discoverChildren() {
		if(scraper==null)
			return;
		DLNAResource start=this;
		if(start.getParent() instanceof ChannelPMSSubSiteSelector) // compensate
			start=start.getParent();
		Channels.debug("sub sel site "+site);
		HashMap<String,Object> choices=scraper.subSelect(start, imdb,site);
		if(choices==null)
			return;
		// extract the match name (remove to avoid it to be sorted)
		matchName=(String)choices.get("__match_name__");
		choices.remove("__match_name__");
		// Add a special PLAY at the top and bottom to make it esaier
		// to play even if nothing is good enough...
		ChannelMediaStream cms=new ChannelMediaStream(ch,PNS,url,null,proc,type,asx,scraper,dispName,saveName);
		cms.setImdb(imdb);
		cms.setStash(stash);
		cms.setStreamVars(streamVars);
		addChild(cms);
		if(choices.isEmpty()) // no idea to continue
			return;
		// sort the result
		TreeMap<Integer,TreeSet<String>> m=sortMap(choices);
		for(Integer id : m.keySet()) {
			TreeSet<String> l=m.get(id);
			for(String key : l) {
				Object obj=choices.get(key);
				String thumb=ChannelSubs.icon(obj, null);
				cms=new ChannelMediaStream(ch,key,url,thumb,proc,type,asx,scraper,dispName,saveName);
				cms.setEmbedSub(obj);
				cms.setImdb(imdb);
				cms.setStash(stash);
				if(Channels.cfg().useStreamVar()) {
					ChannelStreamVars sVar=new ChannelStreamVars(streamVars);
					sVar.setInstance(key.hashCode());
					cms.setStreamVars(sVar);
				}
				addChild(cms);
			}
		}
		cms=new ChannelMediaStream(ch,PNS,url,null,proc,type,asx,scraper,dispName,saveName);
		cms.setImdb(imdb);
		cms.setStash(stash);
		cms.setStreamVars(streamVars);
		addChild(cms);
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}	
	
	private TreeMap<Integer,TreeSet<String>> sortMap(HashMap<String,Object> map) {
		if(ChannelUtil.empty(matchName)) // make matchName ain't null
			matchName="";
		// The sort logic here
		// remove all "odd" chars like .,_ and space
		// also make common case to make it caseinsensitive
		// do this for the keys as well
		// Find out where the keys differ and sort them in a treemap
		// based on the differ score (higher is better).
		// Those keys that get the same diff use a TreeSet to get natural sorting there
		matchName=matchName.replaceAll("[ ._]", "").toLowerCase();
		TreeMap<Integer, TreeSet<String>> m=new TreeMap<Integer,TreeSet<String>>();
		for(String key : map.keySet()) {
			String key1=key.replaceAll("[ ._]", "").toLowerCase();
			// Multiply by -1 since TreeMap uses ascending order
			Integer pos=new Integer(-1*StringUtils.indexOfDifference(matchName, key1));
			TreeSet<String> slot=m.get(pos);
			if(slot==null)
				slot=new TreeSet<String>();
			slot.add(key);
			m.put(pos,slot);
		}
		return m;
	}
}
