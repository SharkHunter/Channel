package com.sharkhunter.channel;

import java.util.HashMap;
import java.util.List;

import net.pms.dlna.DLNAResource;

public interface ChannelScraper {	
	public String scrape(Channel ch,String url,String processorUrl,int format,DLNAResource start, 
			             boolean noSub,String imdb,Object embedSubs);
	public long delay();
	public String backtrackedName(DLNAResource start);
	public HashMap<String, Object> subSelect(DLNAResource start,String imdb);
}
