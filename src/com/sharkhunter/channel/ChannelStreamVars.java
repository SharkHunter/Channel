package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.io.OutputParams;

public class ChannelStreamVars {
	private HashMap<String,ChannelVar> vars;
	private String instance;
	
	public ChannelStreamVars() {
		vars=new HashMap<String,ChannelVar>();
	}
	
	public ChannelStreamVars(ChannelStreamVars def) {
		vars=new HashMap<String,ChannelVar>(def.vars);
		instance=def.instance;
	}
	
	public void parse(File f) throws Exception {
		BufferedReader in=new BufferedReader(new FileReader(f));
    	String str,ver="unknown";
    	StringBuilder sb=new StringBuilder();
    	while ((str = in.readLine()) != null) {
    		str=str.trim();
    		if(ChannelUtil.ignoreLine(str))
				continue;
    		if(str.startsWith("version")) {
    			String[] v=str.split("\\s*=\\s*");
     	    	if(v.length<2)
     	    		continue;
     	    	ver=v[1];
     	    	continue; // don't append these
     	    }	
    		sb.append(str);
     	    sb.append("\n");
    	}
    	String[] lines=sb.toString().split("\n");
    	for(int i=0;i<lines.length;i++) {
    	    str=lines[i].trim();
    	    if(str.startsWith("var {")) {
    	    	ArrayList<String> data=ChannelUtil.gatherBlock(lines, i+1);
    			i+=data.size();
    			ChannelVar v=new ChannelVar(data,null);
    			vars.put(v.displayName(), v);
    	    }
    	}
    	PMS.minimal("Adding stream vars version "+ver);
    	Channels.debug("Adding stream vars version "+ver);
	}
	
	public void add(DLNAResource res,Channel ch) {
		for(String var: vars.keySet()) {
			ChannelVar v=vars.get(var);
			v.setChannel(ch);
			String[] tmp=ch.trashVar(var);
			if(tmp!=null) {
				Channels.debug("add stream var "+v.displayName()+" inst "+tmp[0]+" val "+tmp[1]);
				if(!ChannelUtil.empty(tmp[0])) // instance found verify that it's correct
					if(!tmp[0].equals(instance))
						continue;
				v.initValue(tmp[1]);
			}
			v.setInstance(instance);
			res.addChild(new ChannelPMSVar(var,v));
		}
	}
	
	public void resolve(String player,List<String> list,OutputParams params) {
		for(String var: vars.keySet()) {
			ChannelVar v=vars.get(var);
			v.action(player,list,params);
		}
	}
	
	public void setInstance(int i) {
		setInstance(String.format("%x", i));
	}
	
	public void setInstance(String inst) {
		instance=inst;
	}
}
