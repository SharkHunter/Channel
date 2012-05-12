package com.sharkhunter.channel;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ChannelIllegal {
	
	public static final int TYPE_NAME=0;
	public static final int TYPE_URL=1;
	
	private int type;
	private String regexp;
	private String[] prop;
	
	public ChannelIllegal(ArrayList<String> data) {
		type=TYPE_NAME;
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;
			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2)
				continue;
			if(keyval[0].equalsIgnoreCase("type")) {
				if(keyval[1].equalsIgnoreCase("name"))
					type=TYPE_NAME;
				if(keyval[1].equalsIgnoreCase("url"))
					type=TYPE_URL;
			}
			if(keyval[0].equalsIgnoreCase("matcher")) {
				regexp=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("prop"))
				prop=keyval[1].trim().split(",");
		}
	}
	
	private boolean handleString(String str) {
		if(ChannelUtil.getProperty(prop, "exact")) {
			if(ChannelUtil.getProperty(prop, "no_case"))
				return regexp.equalsIgnoreCase(str);
			else
				return regexp.equals(str);
		}
		else {
			if(ChannelUtil.getProperty(prop, "no_case"))
				return regexp.toLowerCase().contains(str.toLowerCase());
			else
				return regexp.contains(str);
		}
	}
	
	public boolean isIllegal(String str,int type) {
		if(ChannelUtil.empty(regexp)||type!=this.type||str==null)
			return false;
		if(ChannelUtil.getProperty(prop, "string")||
		   ChannelUtil.getProperty(prop, "exact")) {
			return handleString(str);
		}
		int flags=0;
		if(ChannelUtil.getProperty(prop, "no_case"))
			flags|=Pattern.CASE_INSENSITIVE;
		Pattern p=Pattern.compile(regexp,flags);
		return p.matcher(str).matches();
	}
}
