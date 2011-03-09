package com.sharkhunter.channel;

public interface ChannelScraper {
	public String scrape(Channel ch,String url,String processorUrl,int format);
}
