package com.sharkhunter.channel;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.pms.dlna.DLNAResource;

public class ChannelSubUtil {
	
	private final static String ARROW =" --> ";

	public static String backtrackedName(DLNAResource start,String[] prop) {
		String realName=ChannelUtil.backTrack(start,ChannelUtil.getNameIndex(prop),
				ChannelUtil.getPropertyValue(prop, "name_separator"));
		// Maybe we should mangle the name?
		String nameMangle=ChannelUtil.getPropertyValue(prop, "name_mangle");
		realName=ChannelUtil.mangle(nameMangle, realName);
		return realName;
	}

	public static HashMap<String,Object> subSelect(DLNAResource start,String imdb,
			String[] subtitle,Channel ch,String realName) {
		if(subtitle==null) 
			return null;
		for(int i=0;i<subtitle.length;i++) {
			ChannelSubs subs=Channels.getSubs(subtitle[i]);
			if(subs==null)
				continue;
			if(!subs.langSupported()||!subs.selectable())
				continue;
			return subSelect(start,imdb,subs,subtitle,ch, realName);
		}
		return null;
	}

    public static HashMap<String,Object> subSelect(DLNAResource start,String imdb,
                                                   String[] subtitle,Channel ch) {
        return subSelect(start,imdb,subtitle,ch, null);

    }

    public static HashMap<String,Object> subSelect(DLNAResource start,String imdb,String subSite,
                                                   String[] subtitle,Channel ch) {
        return subSelect(start,imdb,subtitle,ch, null);
    }

	public static HashMap<String,Object> subSelect(DLNAResource start,String imdb,String subSite,
			String[] subtitle,Channel ch, String realName) {
		if(ChannelUtil.empty(subSite)) // fallback solution
			return subSelect(start,imdb,subtitle,ch);
		ChannelSubs subs=Channels.getSubs(subSite);
		if(subs==null)
			return null;
		if(!subs.langSupported()||!subs.selectable())
			return null;
		return subSelect(start,imdb,subs,subtitle,ch, realName);
	}

	private static HashMap<String,Object> subSelect(DLNAResource start,String imdb,ChannelSubs subs,
			String[] subtitle,Channel ch, String realName) {
        if(ChannelUtil.empty(realName))
		     realName=backtrackedName(start,subtitle);
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
	
	private static String fixTimeNormal(String str) {
		int pos=str.lastIndexOf(':');
		if(pos==-1)
			return str;
		StringBuilder sb=new StringBuilder(str);
		sb.setCharAt(pos, ',');
		return sb.toString();
	}
	
	private static String fixTimeMs(String str) {
		long millis=Long.parseLong(str);
		long sec,min,hour;
		sec = millis / 1000;
		min = sec /60;
		hour = min / 60;
		sec = sec % 60;
		min = min % 60;
		millis = millis % 1000;
		return String.format("%02d:%02d:%02d,%03d", hour,min,sec,millis);
	}
	
	private static String fixTime(String str,boolean ms) {
		if(ms)
			return fixTimeMs(str);
		else
			return fixTimeNormal(str);
	}
	
	public static void writeSRT(OutputStreamWriter out,int id,String start,String stop,String text) throws IOException {
		writeSRT(out,id,start,stop,text,false);
	}
	
	public static void writeSRT(OutputStreamWriter out,int id,String start,String stop,String text,boolean ms) throws IOException {
		text=text.trim().replaceAll("\\\\n", "\n");
		out.write(String.valueOf(id));
		out.write("\n");
		out.write(fixTime(start,ms));
		out.write(ARROW);
		out.write(fixTime(stop,ms));
		out.write("\n");
		out.write(text);
		out.write("\n\n");
	}

}
