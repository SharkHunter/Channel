package com.sharkhunter.channel;

import java.util.ArrayList;

public class ChannelVar {
	
	private String name;
	private String var_name;
	private String currVal;
	private String[] values;
	private Channel parent;	
	
	private String prefix;
	private String suffix;
	
	private final static String VAR_DELIM="@#"; 

	public ChannelVar(ArrayList<String> data,Channel parent) {
		this.parent=parent;
		suffix="";
		prefix="";
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;
			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2)
				continue;
			if(keyval[0].equalsIgnoreCase("disp_name"))
				name=keyval[1];
			if(keyval[0].equalsIgnoreCase("var_name"))
				var_name=keyval[1];
			if(keyval[0].equalsIgnoreCase("values")) {
				values=keyval[1].split(",");
				currVal=values[0];
			}
			if(keyval[0].equalsIgnoreCase("suffix"))
				suffix=keyval[1];
			if(keyval[0].equalsIgnoreCase("prefix"))
				prefix=keyval[1];
		}
	}
	
	public String displayName() {
		return name;
	}
	
	public String varName() {
		return VAR_DELIM+var_name+VAR_DELIM;
	}

	public String value() {
		return currVal;
	}
	
	public void setValue(String v) {
		currVal=v;
		Channels.setChVar(parent.getName(), name, currVal);
	}
	
	public void setValue(int i) {
		currVal=values[i];
		Channels.setChVar(parent.getName(), name, currVal);
	}
	
	public String resolve(String str) {
		return str.replaceAll(varName(), currVal);
	}
	
	public String niceValue(String val) {
		String s=ChannelUtil.append(prefix, " ", val);
		return ChannelUtil.append(s, " ", suffix);
	}	
	
	public String[] values() {
		return values;
	}
	
}
