package com.sharkhunter.channel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ChannelScriptMgr {
	
	private static boolean isExtScript(String file) {
		File f=new File(file);
		return (f.exists()&&f.canExecute());
	}
	
	public static String runScript(String script,String url,Channel ch) {
		return runScript(script,url,ch,"",false);
	}
	
	public static String runScript(String script,String url,Channel ch,
								   boolean no_format) {
		return runScript(script,url,ch,"",no_format);
	}
	
	public static String runScript(String script,String url,Channel ch,String page) {
		return runScript(script,url,ch,page,false);
	}
	
	public static String runScript(String script,String url,Channel ch,
								   String page,boolean no_format) {
		if(ChannelUtil.empty(script))
			return url;
		// 1st up check local NIPL
		ArrayList<String> sData=Channels.getScript(script);
		if(sData!=null) { // found local script use it
			HashMap<String,String> vars=new HashMap<String,String>();
			vars.put("htmRaw", page);
			return ChannelNaviXProc.simple(url,sData,vars);
		}
		// 2nd remote NIPL
		if(script.startsWith("http://")) {
			int format=-1;
			if(ch!=null)
				format=ch.getFormat();
			return ChannelNaviXProc.parse(url,script,format,"",ch);
		}
		// 3rd external script,assume full path
		if(isExtScript(script)) {
			if(no_format)
				return ChannelUtil.execute(script,url,"");
			else
				return ChannelUtil.execute(script,url,ch.getFormat());
		}
		// no try in a special place
		String f=Channels.cfg().scriptFile(script);
		if(isExtScript(f)) {
			if(no_format)
				return ChannelUtil.execute(f,url,"");
			else 
				return ChannelUtil.execute(f,url,ch.getFormat());
		}
		// down here we're out of options return the orignal url
		return url;
	}
}
