package com.sharkhunter.channel;

import net.pms.dlna.DLNAResource;

public interface ChannelScraper {	
	public String scrape(Channel ch,String url,String processorUrl,int format,DLNAResource start, 
			             boolean noSub,String imdb);
	public long delay();
}
