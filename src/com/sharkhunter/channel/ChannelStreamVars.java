package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.LoggerFactory;
import net.pms.dlna.DLNAResource;
import net.pms.io.OutputParams;

public class ChannelStreamVars {
	private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ChannelStreamVars.class);
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
    	String str,ver="unknown";
    	StringBuilder sb=new StringBuilder();
		BufferedReader in=new BufferedReader(new FileReader(f));
		try {
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
		} finally {
			in.close();
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
    	LOGGER.debug("{Channel} Adding stream vars version {}", ver);
    	Channels.debug("Adding stream vars version "+ver);
	}

	public void add(DLNAResource res,Channel ch) {
		for(String var: vars.keySet()) {
			ChannelVar v=vars.get(var);
			v.setChannel(ch);
			v.setInstance(instance);
			String[] tmp=ch.trashVar(var);
			if(tmp!=null) {
				Channels.debug("add stream var "+v.displayName()+" inst "+tmp[0]+" val "+tmp[1]);
				if(tmp[0]!=null&&tmp[0].equals(instance)) { // instance found verify that it's correct
					v.initValue(tmp[1]);
				}
			}
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

	public String instance() {
		return instance;
	}
}
