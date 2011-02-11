package com.sharkhunter.channel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import net.pms.dlna.WebAudioStream;
import net.pms.dlna.WebVideoStream;
import net.pms.dlna.virtual.VirtualFolder;

public class ChannelNaviX extends VirtualFolder implements ChannelProps{
	private String url;
	private Channel parent;
	
	public ChannelNaviX(Channel ch,String name,String thumb,String url) {
		super(name,thumb);
		this.url=url;
		parent=ch;
	}
	public void discoverChildren() {
		// The URL found in the cf points to a NaviX playlist
		// (or similar) fetch and parse
		URL urlobj=null;
		try {
			urlobj = new URL(url);
		} catch (MalformedURLException e) {
			parent.debug("navix error "+e);
			return;
		}
		String page=ChannelUtil.fetchPage(urlobj);
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
			if(line==null||line.length()==0) { // new block
				if(type!=null) {
					parent.debug("url "+nextUrl+" type "+type);
					if(playpath!=null)
						nextUrl=nextUrl+playpath;
					if(type.equalsIgnoreCase("playlist")) {
						addChild(new ChannelNaviX(parent,name,thumb,nextUrl));
					}
					else if(type.equalsIgnoreCase("video")) {
						String realUrl=ChannelNaviXProc.parse(parent,nextUrl,proc);
						if(realUrl!=null&&realUrl.length()!=0) 
							addChild(new WebVideoStream(name,realUrl,thumb));
					}
					else if(type.equalsIgnoreCase("audio")) {
						String realUrl=ChannelNaviXProc.parse(parent,nextUrl,proc);
						if(realUrl!=null&&realUrl.length()!=0) 
							addChild(new WebAudioStream(name,realUrl,thumb));
					}
				}
				name=null;
				nextUrl=null;
				thumb=null;
				proc=null;
				type=null;
				playpath=null;
				continue;
			}
			if(line.startsWith("#"))
				continue;
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
		if(type!=null) { // pick up last
			if(playpath!=null)
				nextUrl=nextUrl+playpath;
			if(type.equalsIgnoreCase("playlist")) {
				addChild(new ChannelNaviX(parent,name,thumb,nextUrl));
			}
			else if(type.equalsIgnoreCase("video")) {
				String realUrl=ChannelNaviXProc.parse(parent,nextUrl,proc);
				if(realUrl!=null&&realUrl.length()!=0) 
					addChild(new WebVideoStream(name,realUrl,thumb));
			}
			else if(type.equalsIgnoreCase("audio")) {
				String realUrl=ChannelNaviXProc.parse(parent,nextUrl,proc);
				if(realUrl!=null&&realUrl.length()!=0) 
					addChild(new WebAudioStream(name,realUrl,thumb));
			}
		}
	}
	
	public String separator(String base) {
		if(!base.equalsIgnoreCase("url"))
			return null;
		return "!!!!";
	}

}
