package com.sharkhunter.channel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import net.pms.dlna.WebAudioStream;
import net.pms.dlna.WebStream;
import net.pms.dlna.WebVideoStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;

public class ChannelNaviX extends VirtualFolder {
	private String url;
	private Channel parent;
	
	public ChannelNaviX(Channel ch,String name,String thumb,String url) {
		super(name,thumb);
		this.url=url;
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
				addChild(new ChannelNaviX(parent,name,thumb,nextUrl));
			}
			else {
				int f=getFormat(type);
				parent.debug("add media "+f+" name "+name+" url "+nextUrl);
				if(f!=-1)
					addChild(new ChannelMediaStream(parent,name,nextUrl,thumb,proc,f));
			}
			/*else if(type.equalsIgnoreCase("video")) {
				String realUrl=ChannelNaviXProc.parse(parent,nextUrl,proc);
				if(realUrl!=null&&realUrl.length()!=0) 
					addChild(new WebVideoStream(name,realUrl,thumb));
			}
			else if(type.equalsIgnoreCase("audio")) {
				String realUrl=ChannelNaviXProc.parse(parent,nextUrl,proc);
				if(realUrl!=null&&realUrl.length()!=0) 
					addChild(new WebAudioStream(name,realUrl,thumb));
			}
			else if(type.equalsIgnoreCase("image")) {
				String realUrl=ChannelNaviXProc.parse(parent,nextUrl,proc);
				if(realUrl!=null&&realUrl.length()!=0) 
					addChild(new ChannelImageStream(name,realUrl,thumb,parent.getAuth()));
			}*/
		}
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
}
