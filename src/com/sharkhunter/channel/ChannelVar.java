package com.sharkhunter.channel;

import java.util.ArrayList;
import java.util.List;

import net.pms.io.OutputParams;

public class ChannelVar {
	
	private String name;
	private String var_name;
	private String currVal;
	private String defaultVal;
	private String[] values;
	private Channel parent;	
	
	private String prefix;
	private String suffix;
	
	private String action;
	
	private int type;
	
	private String instance;
	
	public final static int VAR_TYPE_STD=0;
	public final static int VAR_TYPE_INC=1;
	private final static String[] INC_VALUES={"","+1","-1","+5","-5","+10","-10"};
	
	private final static String VAR_DELIM="@#"; 

	public ChannelVar(ArrayList<String> data,Channel parent) {
		this.parent=parent;
		type=VAR_TYPE_STD;
		suffix="";
		prefix="";
		currVal="0";
		defaultVal="";
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
			if(keyval[0].equalsIgnoreCase("default")) 
				defaultVal=keyval[1];
			if(keyval[0].equalsIgnoreCase("suffix"))
				suffix=keyval[1];
			if(keyval[0].equalsIgnoreCase("prefix"))
				prefix=keyval[1];
			if(keyval[0].equalsIgnoreCase("action")) // action script for stream vars
				action=keyval[1];
			if(keyval[0].equalsIgnoreCase("type")) {
				if(keyval[1].equalsIgnoreCase("inc")) 
					type=VAR_TYPE_INC;
				if(keyval[1].equalsIgnoreCase("std"))
					type=VAR_TYPE_STD;
			}
		}
		if(type==VAR_TYPE_INC) { 
			if(values==null)
				values=INC_VALUES;
			else {
				String[] old=values;
				values=new String[(values.length*2)+1];
				int j=1;
				for(int i=0;i<old.length;i++) {
					values[j]="+"+old[i];
					values[j+1]="-"+old[i];
					j+=2;
				}
			}
			if(ChannelUtil.empty(defaultVal))
				defaultVal=currVal;
			else
				currVal=defaultVal;
			values[0]="Clear current : "+currVal;
		}
	}
	
	public int type() {
		return type;
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
	
	private static int getInt(String i) {
		try {
			return Integer.parseInt(i);
		}	
		catch (Exception e) {
			return 0;
		}
	}
	
	public void setValue(String v) {
		if(v==null) //  no need to set this
			return;
		String[] tmp=v.split("@");
		if(tmp.length==2) {
			if(!ChannelUtil.empty(instance)) {
				if(!tmp[1].equals(instance))
					return;
			}
			v=tmp[0];
		}
		if(type==VAR_TYPE_INC) {
			if(v.startsWith("Clear current")) // clear it
				currVal=defaultVal;
			else {
				int val=getInt(currVal);
				int factor=getInt(v);
				currVal=String.valueOf(val+factor);
			}
			values[0]="Clear current : "+currVal;
		}
		else
			currVal=v;
		Channels.setChVar(parent.getName(), name, ChannelUtil.append(currVal,"@",instance));
	}
	
	public void setValue(int i) {
		if(type==VAR_TYPE_INC) {
			if(i==0) // clear it
				currVal=defaultVal;
			else {
				int val=getInt(currVal);
				int factor=getInt(values[i]);
				currVal=String.valueOf(val+factor);
			}
			values[0]="Clear current : "+currVal;
		}
		else
			currVal=values[i];
		Channels.setChVar(parent.getName(), name, ChannelUtil.append(currVal,"@",instance));
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
	
	public void setChannel(Channel ch) {
		parent=ch;
	}
	
	public void action(String player,List<String> list,OutputParams params) {
		if(action==null) // no action
			return;
		if(ChannelBuiltIn.action(action,player,currVal,list,params))
			return;
		String data=player+" "+currVal;
		String res=ChannelScriptMgr.runScript(action, data, parent, true);
		Channels.debug("extrnal var script "+action+" returned "+res);
		if(ChannelUtil.empty(res))
			return;
		if(res.equals(data)) // no script found (probably)
			return;
		String[] tmp=res.split("\n");
		for(int i=0;i<tmp.length;i++) {
			String[] kv=tmp[i].split("=",2);
			list.add(kv[0]);
			if(kv.length>1)
				list.add(kv[1]);
		}
	}
	
	public void setInstance(String inst) {
		instance=inst;
	}
}
