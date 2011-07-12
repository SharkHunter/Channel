package com.sharkhunter.channel;

import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ChannelCookie {
	
	private static String trimUrl(String u) {
		String u1=u.replace("http://", "").replace("https://", "");
		int p=u1.indexOf("/");
		if(p!=-1)
			u1=u1.substring(0,p);
		p=u1.indexOf('.');
		if((p!=-1)&&(u1.startsWith("www"))) // skip wwwxxx.
			u1=u1.substring(p+1);
		return u1;
	}
	
	private static long parseTTD(String expStr) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		java.util.Date d;
		try {
			d = sdfDate.parse(expStr);
			return d.getTime();
		} catch (ParseException e) {
			return System.currentTimeMillis()+(24*60*60*2*1000);
		}
	}
	
	public static String getCookie(String url) {
		ChannelAuth a=Channels.getCookie(trimUrl(url));
		if(a==null)
			return null;
		return a.authStr;
	}
	
	public static void parseCookie(URLConnection connection,ChannelAuth a,String url) throws Exception {
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
	 		Channels.debug("cookie "+cookie);
	 		if(a!=null&&ChannelUtil.cookieMethod(a.method))
	 			if(!ChannelUtil.empty(a.authStr)&&cookie.equals(a.authStr))
	 				continue;
	 		if(fields.length>1)
	 			if(fields[1].contains("expires")) {
	 				String[] exp=fields[1].split(",");
	 				if(exp.length>1)
	 					ttd=parseTTD(exp[1]);
	 			}
	 		if(a==null)
	 			a=new ChannelAuth();
	 		a.method=ChannelLogin.SIMPLE_COOKIE;
	 		a.authStr=cookie;
	 		a.ttd=ttd;
	 		Channels.debug("adding cookie "+cookie+" to url "+trimUrl(url));
	 		update|=Channels.addCookie(trimUrl(url), a);	
		}
		if(update)
			Channels.mkCookieFile();
	}
}
