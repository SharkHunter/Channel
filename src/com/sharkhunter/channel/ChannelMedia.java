package com.sharkhunter.channel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.WebAudioStream;
import net.pms.dlna.WebStream;
import net.pms.dlna.WebVideoStream;
import net.pms.formats.Format;

public class ChannelMedia implements ChannelProps{
	public static final int TYPE_NORMAL=0;
	public static final int TYPE_ASX=1;
	
	public boolean Ok;
	private ChannelMatcher matcher;
	private String name;
	private Channel parent;
	private String[] prop;
	private String thumbURL;
	private int type;
	
	public ChannelMedia(ArrayList<String> data,Channel parent) {
		Ok=false;
		matcher=null;
		this.parent=parent;
		type=ChannelMedia.TYPE_NORMAL;
		parse(data);
		Ok=true;
	}
	
	public void parse(ArrayList<String> data) {
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;
			String[] keyval=line.split("=",2);
			if(keyval.length<2) // ignore weird lines
				continue;
			if(keyval[0].equalsIgnoreCase("macro")) {
				ChannelMacro m=parent.getMacro(keyval[1]);
				if(m!=null)
					parse(m.getMacro());
				else
					PMS.debug("unknown macro "+keyval[1]);
			}	
			if(keyval[0].equalsIgnoreCase("matcher")) {
					if(matcher==null)
						matcher=new ChannelMatcher(keyval[1],null,this);
					else
						matcher.setMatcher(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("order")) {
				if(matcher==null)
					matcher=new ChannelMatcher(null,keyval[1],this);
				else
					matcher.setOrder(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("name"))
				name=keyval[1];
			if(keyval[0].equalsIgnoreCase("prop"))	
				prop=keyval[1].trim().split(",");
			if(keyval[0].equalsIgnoreCase("img"))
				thumbURL=keyval[1];
			if(keyval[0].equalsIgnoreCase("type")) {
				if(keyval[1].trim().equalsIgnoreCase("asx"))
					type=ChannelMedia.TYPE_ASX;
			}
		}
	}
	
	public ChannelMatcher getMatcher() {
		return matcher;
	}
	
	private String parseASX(String url) {
		String page;
		try {
			page = ChannelUtil.fetchPage(new URL(url));
		} catch (MalformedURLException e) {
			parent.debug("asx fetch failed "+e);
			return url;
		}
		parent.debug("page "+page);
		int first=page.indexOf("href=");
		if(first==-1)
			return url;
		int last=page.indexOf('\"', first+6);
		if(last==-1)
			return url;
		return page.substring(first+6,last);
	}
	
	public void add(DLNAResource res,String nName,String url,String thumb) {
		if(thumbURL!=null&&thumbURL.length()!=0) {
			if(ChannelUtil.getProperty(prop, "use_conf_thumb"))
				thumb=thumbURL;
		}
		if(name!=null&&name.length()!=0) {
			nName=ChannelUtil.concatField(name,nName,prop,"name");
			if(nName==null)
				nName=name;
			else if(ChannelUtil.getProperty(prop, "ignore_match"))
				nName=name;
		}
		thumb=ChannelUtil.getThumb(thumb, thumbURL, parent);
		parent.debug("found media "+nName+" thumb "+thumb+" url "+url);
		// asx is weird and one would expect mencoder to support it no
		if(type==ChannelMedia.TYPE_ASX)  
			url=parseASX(url);
		url=ChannelUtil.pendData(url,prop,"url");
		if(parent.getFormat()==Channel.FORMAT_VIDEO)
			res.addChild(new WebVideoStream(nName,url,thumb));
		else if(parent.getFormat()==Channel.FORMAT_AUDIO)
			res.addChild(new WebAudioStream(nName,url,thumb));
		else if(parent.getFormat()==Channel.FORMAT_IMAGE) {
			String auth=parent.getAuth();
			res.addChild(new ChannelImageStream(nName,url,thumb,auth));
		}
	}
	
	public String separator(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_separator");
	}
}
