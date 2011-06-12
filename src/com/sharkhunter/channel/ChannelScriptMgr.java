package com.sharkhunter.channel;

import java.io.File;
import java.util.ArrayList;

public class ChannelScriptMgr {
	
	private static boolean isExtScript(String file) {
		File f=new File(file);
		return (f.exists()&&f.canExecute());
	}
	
	public static String runScript(String script,String url,Channel ch) {
		if(ChannelUtil.empty(script))
			return url;
		// 1st up check local NIPL
		ArrayList<String> sData=Channels.getScript(script);
		if(sData!=null) { // found local script use it
			return ChannelNaviXProc.simple(url,sData);
		}
		// 2nd remote NIPL
		if(script.startsWith("http://")) {
			return ChannelNaviXProc.parse(url,script,ch.getFormat(),"",ch);
		}
		// 3rd external script,assume full path
		if(isExtScript(script)) {
			return ChannelUtil.execute(script,url,ch.getFormat());
		}
		// no try in a special place
		if(isExtScript(Channels.cfg().scriptFile(script)))
			return ChannelUtil.execute(Channels.cfg().scriptFile(script),url,ch.getFormat());
		// down here we're out of options return the orignal url
		return url;
	}
}
