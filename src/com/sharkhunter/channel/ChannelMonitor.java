package com.sharkhunter.channel;

import java.net.MalformedURLException;
import java.util.ArrayList;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;

public class ChannelMonitor {
	
	private ChannelFolder cf;
	private ArrayList<String> oldEntries;
	private String name;
	private boolean scanning;

	ChannelMonitor(ChannelFolder cf,ArrayList<String> oldEntries,String name) {
		this.cf=cf;
		this.oldEntries=oldEntries;
		this.name=name;
		scanning=false;
	}
	
	public void scan() {
		if(scanning)
			return;
		scanning=true;
		Channels.debug("scanning "+name);
		VirtualFolder dummy=new VirtualFolder(null,null);
		try {
			cf.match(dummy);
		} catch (MalformedURLException e) {
			scanning=false;
			return;
		}
		ArrayList<DLNAResource> crawl=new ArrayList<DLNAResource>();
		for(DLNAResource r : dummy.getChildren()) {
			if(oldEntries.contains(r.getName().trim())) 
				continue;
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
		String[] tmp=modeStr.split("\\+");
		int[] modes=new int[tmp.length];
		Channels.debug("do crawl for "+res);
		for(int i=0;i<tmp.length;i++) {
			if(tmp[i].equalsIgnoreCase("fla"))
				modes[i]=ChannelCrawl.CRAWL_FLA;
			else if(tmp[i].equalsIgnoreCase("hml"))
				modes[i]=ChannelCrawl.CRAWL_HML;
			else
				modes[i]=-1;
		}
		ChannelCrawl crawler=new ChannelCrawl();
		DLNAResource r=crawler.crawl(res, modes);
		boolean all=crawler.allSeen();
		if(r==null)
			return;
		if(!(r instanceof ChannelMediaStream))
			return;
		ChannelMediaStream cms=(ChannelMediaStream)r;
		cms.scrape();
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
}
