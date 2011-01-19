package com.sharkhunter.channel;

import java.util.ArrayList;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.WebAudioStream;
import net.pms.dlna.WebVideoStream;

public class ChannelMedia implements ChannelProps{
	public boolean Ok;
	private ChannelMatcher matcher;
	private String name;
	private Channel parent;
	private String[] prop;
	private String thumbURL;
	
	public ChannelMedia(ArrayList<String> data,Channel parent) {
		Ok=false;
		matcher=null;
		this.parent=parent;
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
						matcher=new ChannelMatcher(keyval[1],null,null);
					else
						matcher.setMatcher(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("order")) {
				if(matcher==null)
					matcher=new ChannelMatcher(null,keyval[1],null);
				else
					matcher.setOrder(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("name"))
				name=keyval[1];
			if(keyval[0].equalsIgnoreCase("prop"))	
				prop=keyval[1].trim().split(",");
			if(keyval[0].equalsIgnoreCase("img"))
				thumbURL=keyval[1];
		}
	}
	
	public ChannelMatcher getMatcher() {
		return matcher;
	}
	
	public void add(DLNAResource res,String nName,String url,String thumb) {
		if(thumbURL!=null&&thumbURL.length()!=0) {
			if(ChannelUtil.getProperty(prop, "use_conf_thumb"))
				thumb=thumbURL;
		}
		if(name!=null&&name.length()!=0) {
			String sep=ChannelUtil.getPropertyValue(prop, "name_separator");
			if(ChannelUtil.getProperty(prop, "prepend_name"))
				nName=ChannelUtil.append(name,sep,nName);
			else if(ChannelUtil.getProperty(prop, "append_name"))
				nName=ChannelUtil.append(nName,sep,name);
			else {
				if(nName==null)
					nName=name;
				else if(!ChannelUtil.getProperty(prop, "ignore_name"))
					nName=name;
			}
		}
		thumb=ChannelUtil.getThumb(thumb, thumbURL, parent);
		if(parent.getFormat()==Channel.FORMAT_VIDEO)
			res.addChild(new WebVideoStream(nName,url,thumb));
		else if(parent.getFormat()==Channel.FORMAT_AUDIO)
			res.addChild(new WebAudioStream(nName,url,thumb));
	}
	
	public String separator(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_separator");
	}
}
