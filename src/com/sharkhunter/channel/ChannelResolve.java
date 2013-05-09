package com.sharkhunter.channel;

import net.pms.dlna.WebStream;

public class ChannelResolve extends WebStream {
	
	private Channel ch;
	
	ChannelResolve(String name,String url,String thumb,Channel ch,int format) {
		super(name,url,thumb,format);
		this.ch=ch;
	}
	
	public String write() {
		return "resolve@"+getUrl()+">"+ch.getName()+">"+getFluxName()+">"+
			ChannelUtil.format2str(getSpecificType())+
			">"+getThumbURL();
	}

}
