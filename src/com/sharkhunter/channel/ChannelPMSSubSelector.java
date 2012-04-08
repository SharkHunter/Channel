package com.sharkhunter.channel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

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
	
	private String matchName;
	
	public ChannelPMSSubSelector(Channel ch,String name,String nextUrl,
			  					 String thumb,String proc,int type,int asx,
			  					 ChannelScraper scraper,String dispName,
			  					 String saveName,String imdb) {
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
	}

	public void discoverChildren() {
		if(scraper==null)
			return;
		HashMap<String,Object> choices=scraper.subSelect(this, imdb);
		if(choices==null)
			return;
		// extract the match name (remove to avoid it to be sorted)
		matchName=(String)choices.get("__match_name__");
		choices.remove("__match_name__");
		if(choices.isEmpty()) // no idea to continue
			return;
		// Add a special PLAY at the top and bottom to make it esaier
		// to play even if nothing is good enough...
		ChannelMediaStream cms=new ChannelMediaStream(ch,"PLAY (no subs match)",url,null,proc,type,asx,scraper,dispName,saveName);
		cms.setImdb(imdb);
		addChild(cms);
		// sort the result
		TreeMap<Integer,TreeSet<String>> m=sortMap(choices);
		for(Integer id : m.keySet()) {
			TreeSet<String> l=m.get(id);
			for(String key : l) {
				cms=new ChannelMediaStream(ch,key,url,null,proc,type,asx,scraper,dispName,saveName);
				cms.setEmbedSub(choices.get(key));
				cms.setImdb(imdb);
				addChild(cms);
			}
		}
		cms=new ChannelMediaStream(ch,"PLAY (no subs match)",url,null,proc,type,asx,scraper,dispName,saveName);
		cms.setImdb(imdb);
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
