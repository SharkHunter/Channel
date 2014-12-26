package com.sharkhunter.channel;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;

public class ChannelMonitor {
	
	private ChannelFolder cf;
	private ArrayList<String> oldEntries;
	private String name;
	private boolean scanning;
	private String templ;
	private String search;
	private boolean try_search;

	ChannelMonitor(ChannelFolder cf,ArrayList<String> oldEntries,String name) {
		this.cf=cf;
		this.oldEntries=oldEntries;
		this.name=name;
		templ=null;
		search=null;
		scanning=false;
		try_search = false;
	}
	
	public void setTemplate(String t) {
		templ=t;
	}
	
	public void setSearch(String s) {
		search=s;
	}

	public void setTrySearch(boolean b) { try_search = b; }
	
	public void scan() {
		if(scanning)
			return;
		scanning=true;
		Channels.debug("scanning "+name);
		VirtualFolder dummy=new VirtualFolder("",null);
		try {
			if(!ChannelUtil.empty(search))
				cf.search(search, dummy);
			else {
				if(try_search) {
					Channel ch = cf.getChannel();
					ch.searchAll(name, dummy);
				}
				cf.match(dummy);
			}
		} catch (MalformedURLException e) {
			scanning=false;
			return;
		}
		ArrayList<DLNAResource> crawl=new ArrayList<DLNAResource>();
		for(DLNAResource r : dummy.getChildren()) {
			if(ChannelUtil.empty(templ)) {
				// standard simple matching
				if(oldEntries.contains(r.getName().trim())) 
					continue;
			}
			else {
				if(!oldEntries.isEmpty()&&templateMatch(r.getName().trim()))
					continue;
			}
			Channels.addNewMonitoredMedia(r,getName().trim());
			crawl.add(r);
		}
		if(Channels.cfg().crawl())
			doCrawl(crawl,getName().trim());
		scanning=false;
	}
	
	private void doCrawl(ArrayList<DLNAResource> res,String folder) {
		if(res.isEmpty())
			return;
		String modeStr=cf.getProp("crawl_mode");
		ChannelCrawl crawler=new ChannelCrawl();
		DLNAResource r=crawler.startCrawl(res, modeStr);
		boolean all=crawler.allSeen();
		if(r==null && !(r instanceof ChannelMediaStream)) // nothing found give up
			return;
		ChannelMediaStream cms=(ChannelMediaStream)r;
		cms.scrape(null);
		String url=cms.getSystemName();
		Channels.debug("crawled to "+url);
		String outFile=Channels.fileName(crawler.goodName(), false);
		if(ChannelUtil.empty(ChannelUtil.extension(outFile)))
			outFile=outFile+Channels.cfg().getCrawlFormat();
		Thread t=ChannelUtil.newBackgroundDownload(outFile,url);
		t.start();
		if(all)
			rescan(folder);
		cms.donePlaying();
	}
	
	private void rescan(String folder) {
		Channels.clearNewMediaFolder(folder);
		Runnable r=new Runnable() {
    		public void run() {
    			ChannelUtil.sleep(2000);
    			scan();
    		}
    	};
    	new Thread(r).start();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean addEntry(String newEntry) {
		Channels.debug("add entry "+newEntry+" old "+oldEntries.contains(newEntry));
		if(!oldEntries.contains(newEntry)) {
			oldEntries.add(newEntry);
			return true;
		}
		return false;
	}
	
	private boolean templateMatch(String entry) {
		HashMap<String,String> vars=new HashMap<String,String>();
		vars.put("entry", entry);
		vars.put("nodebug", "1");
		boolean res=true;
		for(String old : oldEntries) {
			String match=ChannelNaviXProc.simple(old, templ, vars);
			if(match.equals("__EXACT_MATCH__"))
				return true;
			res&=ChannelUtil.empty(match);
		}
		return res;
	}
	
}
