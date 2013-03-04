package com.sharkhunter.channel;

import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ChannelCookie {
	
	private static long parseTTD(String expStr,SimpleDateFormat sdfDate) throws ParseException {
		java.util.Date d;
		String[] tmp=expStr.trim().split(" ");
		if(tmp.length<2) {
			// no timezone??
			d = sdfDate.parse(expStr);
			return d.getTime();
		}
		// Timezon convertion trick...
		String tz=tmp[2];
		sdfDate.setTimeZone(TimeZone.getTimeZone(tz));
		d = sdfDate.parse(expStr);
		Calendar c=Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone(tz));
		c.get(Calendar.DATE);
		c.setTime(d);
		c.setTimeZone(TimeZone.getDefault());
		c.get(Calendar.DATE);
		return c.getTimeInMillis();		
	}
	
	private static long parseTTD(String expStr) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		SimpleDateFormat sdfDate1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");
		try {
			return parseTTD(expStr,sdfDate);
		} catch (ParseException e) {
			try {
				return parseTTD(expStr,sdfDate1);
			} catch (ParseException e1) {
				Channels.debug("bad ttd parse "+e1);
			}
		}
		return System.currentTimeMillis()+(24*60*60*2*1000);
	}
	
	public static String getCookie(String url) {
		ChannelAuth a=Channels.getCookie(ChannelUtil.trimURL(url));
		if(a==null)
			return null;
		long t=a.ttd;
		if(t != 0 && t < System.currentTimeMillis())
			return null;
		return a.authStr;
	}
	
	public static String parseCookie(URLConnection connection,ChannelAuth a,String url) throws Exception {
		return parseCookie(connection,a,url,false);
	}
	
	public static String parseCookie(URLConnection connection,ChannelAuth a,String url,boolean skipUpdate) throws Exception {
		String hName="";
		long ttd=System.currentTimeMillis()+(24*60*60*2*1000);
		boolean update=false;
		for (int j=1; (hName = connection.getHeaderFieldKey(j))!=null; j++) {
		 	String cStr=connection.getHeaderField(j);
		 	if (!hName.equalsIgnoreCase("Set-Cookie")) 
		 		continue;
		 	String[] fields = cStr.split(";\\s*");
	 		String cookie=fields[0];
	 		if(ChannelUtil.empty(cookie))
	 			continue;
	 		int pos;
	 		if((pos=cookie.indexOf(";"))!=-1)
	 			cookie = cookie.substring(0, pos);
	 		if(a!=null&&ChannelUtil.cookieMethod(a.method))
	 			if(!ChannelUtil.empty(a.authStr)&&cookie.equals(a.authStr))
	 				continue;
	 		if(fields.length>1) {
	 			for(int i=1;i<fields.length;i++) {
	 				if(fields[i].contains("expires")) {
	 					String[] exp=fields[i].split(",",2);
	 					if(exp.length>1)
	 						ttd=parseTTD(exp[1]);
	 				}
	 			}
	 		}
	 		if(a==null)
	 			a=new ChannelAuth();
	 		a.method=ChannelLogin.SIMPLE_COOKIE;
	 		a.authStr=ChannelUtil.append(a.authStr,"; ",cookie);
	 		a.ttd=ttd;
	 		if(skipUpdate)
	 			return a.authStr;
	 		Channels.debug("adding (stat) cookie "+cookie+" to url "+ChannelUtil.trimURL(url));
	 		update|=fixCookie(ChannelUtil.trimURL(url), cookie);	
		}
		if(update)
			Channels.mkCookieFile();
		return null;
	}
	
	private static boolean fixCookie(String url, String cookie) {
		ChannelAuth b=new ChannelAuth();
		b.authStr=cookie;
		return Channels.addCookie(url, b);
	}
}
