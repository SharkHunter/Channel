package com.sharkhunter.channel;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import net.pms.PMS;

public class ChannelSimple implements ChannelProps {
	
	private String name;
	private String url;
	private int format;
	private int type;
	private String[] prop;
	
	private ChannelMatcher matcher;
	private Channel parent;
	
	public ChannelSimple(ArrayList<String> data,Channel parent) {
		this.parent=parent;
		parse(data);
	}
	
	public void parse(ArrayList<String> data) {		
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;

			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2)
				continue;
			if(keyval[0].equalsIgnoreCase("macro")) {
				ChannelMacro m=parent.getMacro(keyval[1]);
				if(m!=null)
					parse(m.getMacro());
				else
					PMS.debug("unknown macro "+keyval[1]);
			}	
			if(keyval[0].equalsIgnoreCase("name"))
				name=keyval[1];
			if(keyval[0].equalsIgnoreCase("url"))
				url=keyval[1];		
			if(keyval[0].equalsIgnoreCase("prop"))	
				prop=keyval[1].trim().split(",");
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
		}
	}
	
	public ChannelMatcher getMatcher() {
		return matcher;
	}
	
	public String fetch() {
		try {
			URL urlobj=new URL(url.replaceAll(" ", "%20"));
			URLConnection conn=urlobj.openConnection();
			return ChannelUtil.fetchPage(conn,null,"",null);
		}
		catch (Exception e) {
			return null;
		}
	}

	public String separator(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_separator");
	}
	
	public boolean onlyFirst() {
		return ChannelUtil.getProperty(prop, "only_first");
	}
	
	public String append(String base) {
		return ChannelUtil.getPropertyValue(prop,"append_"+base);
	}
	public String prepend(String base) {
		return ChannelUtil.getPropertyValue(prop,"prepend_"+base);
	}
}
