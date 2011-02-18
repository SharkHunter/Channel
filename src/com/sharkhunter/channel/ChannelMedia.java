package com.sharkhunter.channel;

import java.util.ArrayList;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;

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
	
	public void add(DLNAResource res,String nName,String url,String thumb,boolean autoASX) {
		if(!ChannelUtil.empty(thumbURL)) {
			if(ChannelUtil.getProperty(prop, "use_conf_thumb"))
				thumb=thumbURL;
		}
		if(!ChannelUtil.empty(name)) {
			nName=ChannelUtil.concatField(name,nName,prop,"name");
			if(nName==null)
				nName=name;
			else if(ChannelUtil.getProperty(prop, "ignore_match"))
				nName=name;
		}
		thumb=ChannelUtil.getThumb(thumb, thumbURL, parent);
		parent.debug("found media "+nName+" thumb "+thumb+" url "+url);
		// asx is weird and one would expect mencoder to support it no
		boolean asx=autoASX||(type==ChannelMedia.TYPE_ASX)||
							 (ChannelUtil.getProperty(prop, "auto_asx"));
		url=ChannelUtil.pendData(url,prop,"url");
		if(Channels.save())  { // Add save version
			ChannelPMSSaveFolder sf=new ChannelPMSSaveFolder(parent,nName,url,thumb,null,asx,
					                parent.getFormat(),null);
			res.addChild(sf);
		}
		else {
			res.addChild(new ChannelMediaStream(parent,nName,url,thumb,null,parent.getFormat(),
					asx,null));
		}
	}
	
	public String separator(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_separator");
	}
}
