package com.sharkhunter.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAResource;

public interface ChannelScraper {	
	public String scrape(Channel ch,String url,String processorUrl,int format,DLNAResource start, 
			             boolean noSub,String imdb,Object embedSubs,
			             HashMap<String,String> extraMap,RendererConfiguration render);
	public long delay();
	public String backtrackedName(DLNAResource start);
	public HashMap<String, Object> subSelect(DLNAResource start,String imdb);
	public HashMap<String, Object> subSelect(DLNAResource start,String imdb,String site);
	public ArrayList<String> subSites();
	public boolean getBoolProp(String prop);
	public String lastPlayResolveURL(DLNAResource start);
}
