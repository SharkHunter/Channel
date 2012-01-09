package com.sharkhunter.channel;

import net.pms.dlna.DLNAResource;

public class ChannelNaviXSearch implements SearchObj {
	
	private String url;
	private ChannelNaviX parent;
	
	public ChannelNaviXSearch(ChannelNaviX parent,String url) {
		this.url=url;
		this.parent=parent;
	}
	
	@Override
	public void search(String searchString, DLNAResource searcher) {
		if(ChannelUtil.empty(url))
			return;
		String realUrl=ChannelUtil.concatURL(url, searchString);
		Channels.addSearch(parent.getChannel(), "navix:"+url, searchString);
		parent.readPlx(realUrl,searcher);
	}

}