package com.sharkhunter.channel;

import java.util.List;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.io.OutputParams;

public class ChannelBuiltIn {
	
	public static boolean action(String act,String player,String value,List<String> list,
				OutputParams par) {
		if(act.equals("maxBrStream"))
			return maxBr(player,value,list);
		if(act.equals("bufferDelay"))
			return delay(value,par);
		if(act.equals("subDelay"))
			return subDelay(player,value,list);
		return false;
	}
	
	private static int getInt(String i,int def) {
		try {
			return Integer.parseInt(i);
		}	
		catch (Exception e) {
			return def;
		}
	}
	
	private static boolean subDelay(String player,String value,List<String> list) {
		if(player.equalsIgnoreCase("mencoder")&&!value.equals("0")) {
			list.add("-subdelay");
			list.add(value);
		}
		return true;
	}
	
	private static boolean delay(String value,OutputParams par) {
		int time=getInt(value,-1);
		if(time>0) {
			par.waitbeforestart=time;
		}
		return true;
	}
	
	private static boolean maxBr(String player,String value,List<String> list) {
		// Value is % of max allowed
		PmsConfiguration conf=PMS.getConfiguration();
		int percent=getInt(value,100);
		int br=getInt(conf.getMaximumBitrate(),100);
		int effBr=br*(int)(percent/100);
		String arg=null;
		if(player.equals("ffmpeg"))
			arg="-b";
		if(ChannelUtil.empty(arg))
			return false;
		return true;
	}

}
