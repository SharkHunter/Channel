package com.sharkhunter.channel;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

public class ChannelSimple implements ChannelProps {

	private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ChannelSimple.class);
	private String name;
	private String url;
	private String[] prop;

	private ChannelMatcher matcher;
	private Channel parent;

	public ChannelSimple(ArrayList<String> data,Channel parent) {
		this.parent=parent;
		parse(data);
	}

	public ChannelSimple(Channel parent) {
		this.parent=parent;
	}

	public void setProp(String p) {
		prop=p.trim().split(",");
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
					LOGGER.debug("{Channel} Unknown macro {}", keyval[1]);
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
				matcher.setChannel(parent);
			}
			if(keyval[0].equalsIgnoreCase("order")) {
				if(matcher==null)
					matcher=new ChannelMatcher(null,keyval[1],this);
				else
					matcher.setOrder(keyval[1]);
				matcher.setChannel(parent);
			}
		}
		if(matcher!=null)
			matcher.processProps(prop);
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

	@Override
	public boolean escape(String base) {
		return ChannelUtil.getProperty(prop, base+"_escape");

	}

	@Override
	public boolean unescape(String base) {
		return ChannelUtil.getProperty(prop, base+"_unescape");

	}

	@Override
	public String mangle(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_mangle");
	}
}
