package com.sharkhunter.channel;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.sun.net.ssl.HostnameVerifier;
import com.sun.syndication.io.impl.Base64;

import net.pms.PMS;

public class ChannelLogin {
	
	public static final int STD=0;
	public static final int COOKIE=1;
	public static final int APIKEY=2;
	public static final int SIMPLE_COOKIE=3;
	public static final int AUTO_COOKIE=4;
	public static final int BASIC=5;

	private Channel parent;
	private String user;
	private String pwd;
	private String url;
	private ChannelMatcher auth;
	private String params;
	private String authStr;
	private boolean loggedOn;
	private String tokenStr;
	private int type;
	private boolean mediaOnly;
	private String[] associated;
	private long ttd;
	private ChannelSimple pre_fetch;
	private boolean preFetched;
	private String activeParams;
	
	public ChannelLogin(ArrayList<String> data,Channel parent) {
		this.parent=parent;
		this.loggedOn=false;
		type=ChannelLogin.STD;
		mediaOnly=false;
		associated=null;
		ttd=0;
		pre_fetch=null;
		preFetched=false;
		params="";
		activeParams="";
		parse(data);
	}
	
	public void parse(ArrayList<String> data) {
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;
			if(line.contains("pre_fetch {")) {
				ArrayList<String> pf=ChannelUtil.gatherBlock(data,i+1);
				i+=pf.size();
				pre_fetch=new ChannelSimple(pf,parent);
				continue;
			}
			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2)
				continue;
			if(keyval[0].equalsIgnoreCase("user"))
				user=keyval[1];
			if(keyval[0].equalsIgnoreCase("passwd"))
				pwd=keyval[1];
			if(keyval[0].equalsIgnoreCase("url"))
				url=keyval[1];
			if(keyval[0].equalsIgnoreCase("matcher"))
				auth=new ChannelMatcher(keyval[1],null,null);
			if(keyval[0].equalsIgnoreCase("params"))
				params=keyval[1];
			if(keyval[0].equalsIgnoreCase("authstr"))
				authStr=keyval[1];
			if(keyval[0].equalsIgnoreCase("type")) {
				if(keyval[1].equalsIgnoreCase("cookie"))
					type=ChannelLogin.COOKIE;
				if(keyval[1].equalsIgnoreCase("apikey"))
					type=ChannelLogin.APIKEY;
				if(keyval[1].equalsIgnoreCase("simple_cookie"))
					type=ChannelLogin.SIMPLE_COOKIE;
				if(keyval[1].equalsIgnoreCase("auto_cookie"))
					type=ChannelLogin.AUTO_COOKIE;
				if(keyval[1].equalsIgnoreCase("standard")||
				   keyval[1].equalsIgnoreCase("std"))
					type=ChannelLogin.STD;
				if(keyval[1].equalsIgnoreCase("basic"))
					type=ChannelLogin.BASIC;
			}
			if(keyval[0].equalsIgnoreCase("media_only")) {
				if(keyval[1].equalsIgnoreCase("true"))
					mediaOnly=true;
				if(keyval[1].equalsIgnoreCase("false"))
					mediaOnly=false;
			}
			if(keyval[0].equalsIgnoreCase("associate")) {
				associated=keyval[1].split(",");
			}
		}
	}
	
	private String mkQueryString(String usr,String pass) {
		String usr_pwd=user+"="+ChannelUtil.escape(usr)+
		"&"+pwd+"="+ChannelUtil.escape(pass);
		return ChannelUtil.append(activeParams, "&", usr_pwd);
	}
	
	private ChannelAuth mkResult(ChannelAuth a) {
		if(a==null)
			a=new ChannelAuth();
		a.method=(type==SIMPLE_COOKIE?COOKIE:type);
		a.authStr=tokenStr;
		a.ttd=ttd;
		return a;
	}
	
	private ChannelAuth stdLogin(String usr,String pass,ChannelAuth a) throws Exception {
		String query=mkQueryString(usr,pass);
		//Channels.debug("url "+url+" query "+query);
		URL u=new URL(url);
		URLConnection connection;
		if(url.startsWith("https")) {
			connection = (HttpsURLConnection) u.openConnection();
			HttpsURLConnection.setFollowRedirects(true);
			 HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
			((HttpURLConnection) connection).setInstanceFollowRedirects(true);   
			((HttpURLConnection) connection).setRequestMethod("POST");
		}
		else {
			connection = (HttpURLConnection) u.openConnection();
			HttpURLConnection.setFollowRedirects(true);
			((HttpURLConnection) connection).setInstanceFollowRedirects(true);   
			((HttpURLConnection) connection).setRequestMethod("POST");  
		}
		String page=ChannelUtil.postPage(connection, query);
		//PMS.debug("got page after post "+page);
		auth.startMatch(page);
		parent.debug("matching using expr "+auth.getRegexp().pattern());
		while(auth.match()) {
			String token=auth.getMatch("", true);
			if(token!=null&&token.length()>0) {
				loggedOn=true;
				tokenStr=authStr+token;
				return mkResult(a);
			}
		}
		return null;
	}
	
	private ChannelAuth updateCookieDb(String cookie,ChannelAuth a) {
		String u=ChannelUtil.trimURL(url);
		Channels.debug("update cookie db "+u);
		ChannelAuth b=mkResult(null);
		b.authStr=cookie;
		boolean update=Channels.addCookie(u,b);
		if(associated!=null)
			for(int i=0;i<associated.length;i++) {
				u=ChannelUtil.trimURL(associated[i].trim());
				update|=Channels.addCookie(u, b);
			}
		if(update)
			Channels.mkCookieFile();
		return a;
	}
	
	private long parseTTD(String expStr,SimpleDateFormat sdfDate) throws ParseException {
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
	
	private long parseTTD(String expStr) {
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
		return ttd;
	}
	
	
	private ChannelAuth getCookie(URLConnection connection,ChannelAuth a) throws Exception {
		String hName="";
		for (int j=1; (hName = connection.getHeaderFieldKey(j))!=null; j++) {
		 	String cStr=connection.getHeaderField(j);
			Channels.debug("hdr "+hName);
		 	if (!hName.equalsIgnoreCase("Set-Cookie")) 
		 		continue;
		 	String[] fields = cStr.split(";\\s*");
	 		String cookie=fields[0];
	 		int pos;
	 		if((pos=cookie.indexOf(";"))!=-1)
	 			cookie = cookie.substring(0, pos);
	 		if(auth!=null) {
	 			auth.startMatch(cookie);
	 			if(!auth.match())
	 				continue;
	 		}
	        long ttd1=System.currentTimeMillis()+(24*60*60*2*1000);
	 		if(fields.length>1) {
	 			for(int i=1;i<fields.length;i++) {
	 				if(fields[i].contains("expires")) {
	 					String[] exp=fields[i].split(",",2);
	 					if(exp.length>1)
	 						ttd1=parseTTD(exp[1]);
	 				}
	 			}
	 		}
	 		if((ttd==0 || ttd1<ttd) && ttd1!=0)
	 			ttd=ttd1;
	 		updateCookieDb(cookie,a);
	 		tokenStr=ChannelUtil.append(tokenStr,"; ",cookie);
	        loggedOn=true;
		}
		if(!ChannelUtil.empty(tokenStr))
	        return mkResult(a);
		return null;
	}
	
	private ChannelAuth cookieLogin(String usr,String pass,ChannelAuth a) throws Exception {
		ChannelAuth a1=Channels.getCookie(ChannelUtil.trimURL(url));
		if(a1!=null) { // found some in hash
			if(a1.ttd>System.currentTimeMillis()) {
				loggedOn=true;
				tokenStr=a1.authStr;
				ttd=a1.ttd;
				a1.proxy=a.proxy;
				a1.method=type;
				return a1;
			}
		}
		ChannelProxy proxy=a.proxy;
		if(proxy==null)
			proxy=ChannelProxy.NULL_PROXY;
		String query="";
		String method="GET";
		if(!ChannelUtil.empty(usr)) {
			query=mkQueryString(usr,pass);
			method="POST";
		}
		//Channels.debug("okidoki "+query);
		URL u=new URL(url);
		Proxy p=proxy.getProxy();
		//URLConnection connection;
		//Channels.debug("url "+u.toString()+" query "+query);
		if(u.toString().startsWith("https")) {
			HttpsURLConnection connection = (HttpsURLConnection) u.openConnection(p);
			 HttpsURLConnection.setFollowRedirects(true);
			 HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
			((HttpsURLConnection) connection).setInstanceFollowRedirects(true);   
			((HttpsURLConnection) connection).setRequestMethod(method);
			String page=ChannelUtil.postPage(connection, query);
			//Channels.debug("login res page "+page);
			if(ChannelUtil.empty(page))
				return null;
			return getCookie(connection,a);
		}
		else {
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			HttpURLConnection.setFollowRedirects(true);   
			connection.setInstanceFollowRedirects(true);   
			connection.setRequestMethod(method);
			String page=ChannelUtil.postPage(connection, query);
			//Channels.debug("login page "+page);
			if(ChannelUtil.empty(page))
				return null;
			return getCookie(connection,a);
		}
	}
	
	private ChannelAuth basicLogin(String usr,String pass,ChannelAuth a) {
		String tmp=usr+":"+pass;
		tokenStr=Base64.encode(tmp);
		loggedOn=true;
		type=ChannelLogin.STD;
		return mkResult(a);
	}
	
	public ChannelAuth getAuthStr(String usr,String pass,ChannelAuth a) {
		return getAuthStr(usr,pass,false,a);
	}
	
	public ChannelAuth getAuthStr(String usr,String pass,boolean media,ChannelAuth a) {
		Channels.debug("login on channel "+parent.getName()+" type "+type+" on "+loggedOn);
		/*Date d=new Date(ttd);
		Date d1 =new Date();
		Channels.debug("d "+d.toString()+" "+d1.toString());*/
		if(ttd<System.currentTimeMillis()) {
			loggedOn=false;
			preFetched=false;
			ttd=0;
		}
		if(loggedOn)
			return mkResult(a);
		if(!media&&mediaOnly)
			return a;
		try {
			Channels.debug("pre_fetch "+pre_fetch+" prefecth "+preFetched);
			if(pre_fetch!=null&&!preFetched) {
				String page=pre_fetch.fetch();
				Channels.debug("pre_fetch page "+page);
				if(ChannelUtil.empty(page))
					throw new Exception("Bad pre_fetch reply");
				ChannelMatcher m=pre_fetch.getMatcher();
				m.startMatch(page);
				if(!m.match())
					throw new Exception("Bad pre_fetch reply");
				String name=m.getMatch("name",false);
				String url=m.getMatch("url",true);
				Channels.debug("pre_fetch name "+name+" url "+url);
				if(ChannelUtil.empty(name)||ChannelUtil.empty(url))
					throw new Exception("Bad pre_fetch reply");
				activeParams=ChannelUtil.append(params, "&", name+"="+url);
				preFetched=true;
			}
			else
				activeParams=params;
			if(type==ChannelLogin.AUTO_COOKIE)
				return mkResult(a);
			if(type==ChannelLogin.SIMPLE_COOKIE)
				return cookieLogin(null,null,a);
			if(ChannelUtil.empty(usr)||ChannelUtil.empty(pass))
				return a;
			if((type==ChannelLogin.STD)||(type==ChannelLogin.APIKEY))
				return stdLogin(usr,pass,a);
			else if(type==ChannelLogin.COOKIE)
				return cookieLogin(usr,pass,a);
			else if(type==ChannelLogin.BASIC)
				return basicLogin(usr,pass,a);
			return a;
		}
		catch (Exception e) {
			Channels.debug("could not fetch token "+e);
			return a;
		}
	}
	
	public void reset() {
		tokenStr="";
		loggedOn=false;
		ttd=0;
	}
	
	private static class NullHostnameVerifier implements javax.net.ssl.HostnameVerifier {
	    public boolean verify(String hostname, SSLSession session) {
	        return true;
	    }
	}
}

