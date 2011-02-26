package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelNaviXNookie {
	private static HashMap<String, Long> stash=new HashMap<String,Long>();
	private static File nFile;
	
	public static void init(File f) throws Exception {
		stash.clear();
		nFile=f;
		if(!f.exists()) // no file, nothing to do
			return;
		BufferedReader in = new BufferedReader(new FileReader(f));
		String str;
		while ((str = in.readLine()) != null) {
			str=str.trim();
			if(ChannelUtil.ignoreLine(str))
				continue;
			String[] vals=str.split(",");
			if(vals.length<3) // weird line,ignore
				continue;
			long now=(long)(System.currentTimeMillis()/1000);
			String key=vals[0];
			long ttd=convTime(vals[1]);
			if(ttd!=0) {
				if(now>ttd) // nookie timed out
					continue;
			}
			Long l=stash.get(key);
			if(l!=null&&l.longValue()<ttd)
				continue;
			stash.put(key, new Long(ttd));
			ChannelNaviXProc.nookies.put(key, vals[2]);
		}	
		// We dump all the nookies back to file since some might have been dropped
		dumpNookies();
	}
	
	private static void dumpNookies() throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter(nFile,false));
		for(String key : stash.keySet()) {
			String v=ChannelNaviXProc.nookies.get(key);
			Long l=stash.get(key);
			String line="\n\r"+key+","+l.toString()+","+v+"\n\r";
			out.write(line);
		}
		out.flush();
		out.close();
	}
	
	private static long convTime(String str) {
		Long l;
		try {
			l=Long.valueOf(str);
		}
		catch (Exception e) {
			l=new Long(0);
		}
		return l.longValue();
	}
	
	public static boolean expired(String key) {
		Long l=stash.get(key);
		if(l==null) // if the key is gone it is of course expired
			return true;
		long now=System.currentTimeMillis()/1000;
		boolean exp=(now>l.longValue());
		if(exp) // expired remove from stash
			stash.remove(key);
		return exp;
	}
	
	private static long calcTTD(String expTime) {
		if(ChannelUtil.empty(expTime))
			return 0;
		expTime=expTime.trim();
		Pattern re=Pattern.compile("(\\d+)(\\D+)");
		Matcher m=re.matcher(expTime);
		if(!m.find())
			return 0;
		long t=convTime(m.group(1));
		if(m.group(2).equals("d")) // days
			t=t*86400;
		else if(m.group(2).equals("h")) // hours
			t=t*3600;
		else if(m.group(2).equals("m")) // minutes
			t=t*60;
		return t;
	}
	
	public static void store(String key,String val,String expTime) throws Exception {
		long l=calcTTD(expTime);
		long now=System.currentTimeMillis()/1000;
		if(l!=0)
			l+=now;
		stash.put(key, new Long(l));
		String line="\n\r"+key+","+String.valueOf(l)+","+val+"\n\r";
		BufferedWriter out = new BufferedWriter(new FileWriter(nFile,true));
		out.write(line);
		out.flush();
		out.close();
	}
}
