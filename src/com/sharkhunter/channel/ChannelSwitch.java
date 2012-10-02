package com.sharkhunter.channel;

import java.io.IOException;
import java.util.ArrayList;

import net.pms.dlna.DLNAResource;

public class ChannelSwitch implements ChannelProps {
	public boolean Ok;
	
	private String name;
	private String action;
	private String script;
	private String[] prop;
	public ChannelMatcher matcher;
	private int format;
	
	private Channel parent;
	private ChannelFolder parentFolder;
	
	public ChannelSwitch(String action) {
		this.action=action;
	}
	
	public ChannelSwitch(ArrayList<String> data,Channel parent) {
		Ok=false;
		this.parent=parent;
		format=-1;
		parentFolder=null;
		parse(data);
		Ok=true;
	}
	
	public void parse(ArrayList<String> data) {
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;
			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2)
				continue;
			if(keyval[0].equalsIgnoreCase("name"))
				name=keyval[1];
			if(keyval[0].equalsIgnoreCase("action"))
				action=keyval[1];
			if(keyval[0].equalsIgnoreCase("script"))
				script=keyval[1].trim();		
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
			if(keyval[0].equalsIgnoreCase("prop"))	
				prop=keyval[1].trim().split(",");
			if(keyval[0].equalsIgnoreCase("format"))
				format=ChannelUtil.getFormat(keyval[1],format);
		}
		if(matcher!=null)
			matcher.processProps(prop);
	}
	
	public void parentFormat(int f) {
		if(format==-1)
			format=f;
	}
	
	public void setParentFolder(ChannelFolder cf) {
		parentFolder=cf;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAction() {
		return action;
	}
	
	public int getFormat() {
		return format;
	}
	
	public ChannelFolder getParentFolder() {
		return parentFolder;
	}
	
	public String[] getProps() {
		return prop;
	}
	
	public String runScript(String url) {
		return ChannelScriptMgr.runScript(script, url, parent);
	}
	
	@Override
	public String separator(String base) {
		return ChannelUtil.separatorToken(ChannelUtil.getPropertyValue(prop, base+"_separator"));
	}

	@Override
	public boolean onlyFirst() {
		return false;
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
	
	public String mangle(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_mangle");
	}
	
	public void monitor(DLNAResource res) {
		String pm=ChannelUtil.getPropertyValue(prop, "monitor_type");
		if(pm==null||!pm.equalsIgnoreCase("parent")) {
			// Impossible to monitor this combo?
			Channels.debug("bad switch monitor");
			return;
		}
		String data=parentFolder.mkFav(null,res.getName(),null,null);
		Channels.debug("about to monitor "+res.getName()+" "+data);
		try {
			Channels.monitor(res,parentFolder,
					         data.replace("favorite {", "monitor {"),
					         ChannelUtil.getPropertyValue(prop, "monitor_templ"));
		} catch (IOException e) {
		}
	}
	
	public String rawEntry() {
		StringBuilder sb=new StringBuilder();
		sb.append("switch {");
		sb.append("\n");
		if(!ChannelUtil.empty(name)) {
			sb.append("name=");
			sb.append(name);
			sb.append("\n");
		}
		if(matcher!=null) {
			sb.append("matcher=");
			sb.append(matcher.regString());
			sb.append("\n");
			matcher.orderString(sb);
			sb.append("\n");
		}
		if(prop!=null) {
			sb.append("prop=");
			ChannelUtil.list2file(sb,prop);
			sb.append("\n");
		}
		if(script!=null) {
			sb.append("script=");
			sb.append(script);
			sb.append("\n");
		}
		if(action!=null) {
			sb.append("action=");
			sb.append(action);
			sb.append("\n");
		}
		if(format!=-1) {
			sb.append("format=");
			sb.append(ChannelUtil.format2str(format));
			sb.append("\n");
		}
		sb.append("\n}\n");
		return sb.toString();
	}
	
}
