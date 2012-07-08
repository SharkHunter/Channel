package com.sharkhunter.channel;

import java.util.ArrayList;
import java.util.HashMap;

import net.pms.dlna.DLNAResource;

public class ChannelSubUtil {

	public static String backtrackedName(DLNAResource start,String[] prop) {
		String realName=ChannelUtil.backTrack(start,ChannelUtil.getNameIndex(prop),
				ChannelUtil.getPropertyValue(prop, "name_separator"));
		// Maybe we should mangle the name?
		String nameMangle=ChannelUtil.getPropertyValue(prop, "name_mangle");
		realName=ChannelUtil.mangle(nameMangle, realName);
		return realName;
	}

	public static HashMap<String,Object> subSelect(DLNAResource start,String imdb,
			String[] subtitle,Channel ch) {
		if(subtitle==null) 
			return null;
		for(int i=0;i<subtitle.length;i++) {
			ChannelSubs subs=Channels.getSubs(subtitle[i]);
			if(subs==null)
				continue;
			if(!subs.langSupported()||!subs.selectable())
				continue;
			return subSelect(start,imdb,subs,subtitle,ch);
		}
		return null;
	}

	public static HashMap<String,Object> subSelect(DLNAResource start,String imdb,String subSite,
			String[] subtitle,Channel ch) {
		if(ChannelUtil.empty(subSite)) // fallback solution
			return subSelect(start,imdb,subtitle,ch);
		ChannelSubs subs=Channels.getSubs(subSite);
		if(subs==null)
			return null;
		if(!subs.langSupported()||!subs.selectable())
			return null;
		return subSelect(start,imdb,subs,subtitle,ch);
	}

	private static HashMap<String,Object> subSelect(DLNAResource start,String imdb,ChannelSubs subs,
			String[] subtitle,Channel ch) {
		String realName=backtrackedName(start,subtitle);
		Channels.debug("backtracked name "+realName);
		HashMap<String,String> subName;
		int subScript=0;
		while((subName=ch.getSubMap(realName,subScript))!=null) {
			subScript++;
			if(!ChannelUtil.empty(imdb))
				subName.put("imdb", ChannelUtil.ensureImdbtt(imdb));
			HashMap<String,Object> res=subs.select(subName);
			if(res!=null&&!res.isEmpty()) {
				// Add this special matchname to make
				// the choices "sortable"
				res.put("__match_name__", (Object)realName);
				return res;
			}
		}
		return null;
	}

	public static ArrayList<String> subSites(String[] subtitle) {
		if(subtitle==null) 
			return null;
		ArrayList<String> res=new ArrayList<String>();
		for(int i=0;i<subtitle.length;i++) {
			ChannelSubs subs=Channels.getSubs(subtitle[i]);
			if(subs==null)
				continue;
			if(!subs.langSupported()||!subs.selectable())
				continue;
			res.add(subs.getName());
		}
		if(Channels.openSubs()!=null)
			res.add(Channels.openSubs().getName());
		return res;
	}

}
