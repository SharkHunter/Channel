package com.sharkhunter.channel;

import java.net.MalformedURLException;
import java.net.URL;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;

public class ChannelNaviX extends VirtualFolder implements ChannelScraper {
	private String url;
	private Channel parent;
	private String[] props;
	private int continues;
	private boolean contAll; 
	
	public ChannelNaviX(Channel ch,String name,String thumb,String url,String[] props) {
		super(name,ChannelUtil.getThumb(thumb,null,ch));
		this.url=url;
		this.props=props;
		contAll=false;
		continues=ChannelUtil.calcCont(props);
		if(continues==0)
			contAll=true;
		parent=ch;
	}
	
	private int getFormat(String type) {
		if(type.equalsIgnoreCase("image"))
			return Format.IMAGE;
		if(type.equalsIgnoreCase("video"))
			return Format.VIDEO;
		if(type.equalsIgnoreCase("audio"))
			return Format.AUDIO;
		return -1;
	}
	
	private void addMedia(String name,String nextUrl,String thumb,String proc,String type,String pp) {
		if(type!=null) {
			if(pp!=null)
				nextUrl=nextUrl+pp;
			parent.debug("url "+nextUrl+" type "+type+" processor "+proc);
			if(type.equalsIgnoreCase("playlist")) {
				String cn=ChannelUtil.getPropertyValue(props, "continue_name");
				String cu=ChannelUtil.getPropertyValue(props, "continue_url");
				parent.debug("cont "+continues+" name "+name);
				if(!ChannelUtil.empty(cn)) { // continue
					if(name.matches(cn)) {
						continues--;
						if((contAll||continues>0)&&(continues>Channels.ContSafetyVal)) {
							readPlx(nextUrl);
							return;
						}
					}
				}
				if(!ChannelUtil.empty(cu)) {
					if(nextUrl.matches(cu)) {
						continues--;
						if((contAll||continues>0)&&(continues>Channels.ContSafetyVal)) {
							readPlx(nextUrl);
							return;
						}
					}
				}
				addChild(new ChannelNaviX(parent,name,thumb,nextUrl,props));
			}
			else {
				int f=getFormat(type);
				parent.debug("add media "+f+" name "+name+" url "+nextUrl);
				if(f==-1) 
					return;
				if(Channels.save()) {
					ChannelPMSSaveFolder sf=new ChannelPMSSaveFolder(parent,name,nextUrl,thumb,proc,
							ChannelUtil.getProperty(props,"auto_asx"),f,this);
					addChild(sf);
				}
				else {
					addChild(new ChannelMediaStream(parent,name,nextUrl,thumb,proc,
							f,ChannelUtil.getProperty(props,"auto_asx"),this));
				}
			}
		}
	}
	
	private void readPlx(String str) {
		// The URL found in the cf points to a NaviX playlist
		// (or similar) fetch and parse
		URL urlobj=null;
		try {
			urlobj = new URL(str);
		} catch (MalformedURLException e) {
			parent.debug("navix error "+e);
			return;
		}
		String page;
		try {
			page = ChannelUtil.fetchPage(urlobj.openConnection());
		} catch (Exception e) {
			page="";
		}
		parent.debug("navix page "+page);
		String[] lines=page.split("\n");
		String name=null;
		String nextUrl=null;
		String thumb=null;
		String proc=null;
		String type=null;
		String playpath=null;
		for(int i=0;i<lines.length;i++) {
			String line=lines[i].trim();
			if(ChannelUtil.ignoreLine(line)) { // new block
				addMedia(name,nextUrl,thumb,proc,type,playpath);
				name=null;
				nextUrl=null;
				thumb=null;
				proc=null;
				type=null;
				playpath=null;
				continue;
			}
			if(line.startsWith("URL="))
				nextUrl=line.substring(4);
			else if(line.startsWith("name="))
				name=line.substring(5);
			else if(line.startsWith("thumb="))
				thumb=line.substring(6);
			else if(line.startsWith("processor="))
				proc=line.substring(10);
			else if(line.startsWith("type="))
				type=line.substring(5);	
			else if(line.startsWith("playpath="))
				playpath=line.substring(9);
		}
		// add last item
		addMedia(name,nextUrl,thumb,proc,type,playpath);
	}
	
	public void discoverChildren() {
		readPlx(url);
	}

	@Override
	public String scrape(Channel ch, String url, String processorUrl) {
		return ChannelNaviXProc.parse(ch,url,processorUrl);
	}
		
		
}
