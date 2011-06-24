package com.sharkhunter.channel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import net.pms.PMS;

public class ChannelLogin {
	
	public static final int STD=0;
	public static final int COOKIE=1;
	public static final int APIKEY=2;
	public static final int SIMPLE_COOKIE=3;
	public static final int AUTO_COOKIE=4;
	
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
	
	public ChannelLogin(ArrayList<String> data,Channel parent) {
		this.parent=parent;
		this.loggedOn=false;
		type=ChannelLogin.STD;
		mediaOnly=false;
		associated=null;
		ttd=0;
		parse(data);
	}
	
	public void parse(ArrayList<String> data) {
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;
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
	
	private ChannelAuth mkResult(ChannelAuth a) {
		if(a==null)
			a=new ChannelAuth();
		a.method=(type==SIMPLE_COOKIE?COOKIE:type);
		a.authStr=tokenStr;
		a.ttd=ttd;
		return a;
	}
	
	private ChannelAuth stdLogin(String usr,String pass,ChannelAuth a) throws Exception {
		String query=params+"&"+user+"="+URLEncoder.encode(usr,"UTF-8")+
		"&"+pwd+"="+URLEncoder.encode(pass,"UTF-8");
		//Channels.debug("url "+url+" query "+query);
		URL u=new URL(url);
		URLConnection connection;
		if(url.startsWith("https")) {
			connection = (HttpsURLConnection) u.openConnection();
			HttpsURLConnection.setFollowRedirects(true);
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
			//PMS.debug("token "+token);
			if(token!=null&&token.length()>0) {
				loggedOn=true;
				tokenStr=authStr+token;
				return mkResult(a);
			}
		}
		return null;
	}
	
	private String trimUrl(String u) {
		String u1=u.replace("http://", "").replace("https://", "");
		int p=u1.indexOf("/");
		if(p!=-1)
			u1=u1.substring(0,p);
		p=u1.indexOf('.');
		if((p!=-1)&&(u1.startsWith("www"))) // skip wwwxxx.
			u1=u1.substring(p+1);
		return u1;
	}
	
	private ChannelAuth updateCookieDb(String cookie,ChannelAuth a) {
		String u=trimUrl(url);
		Channels.debug("update cookie db "+u);
		a=mkResult(a);
		boolean update=Channels.addCookie(u,a);
		if(associated!=null)
			for(int i=0;i<associated.length;i++) {
				u=trimUrl(associated[i].trim());
				update|=Channels.addCookie(u, a);
			}
		if(update)
			Channels.mkCookieFile();
		return a;
	}
	
	private long parseTTD(String expStr) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
		java.util.Date d;
		try {
			d = sdfDate.parse(expStr);
			return d.getTime();
		} catch (ParseException e) {
			return ttd;
		}
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
	        ttd=System.currentTimeMillis()+(24*60*60*2*1000);
	 		if(fields.length>1)
	 			if(fields[1].contains("expires")) {
	 				String[] exp=fields[1].split(",");
	 				if(exp.length>1)
	 					ttd=parseTTD(exp[1]);
	 			}
	        tokenStr=cookie;
	        loggedOn=true;
	        return updateCookieDb(tokenStr,a);
		}
		return null;
	}
	
	private ChannelAuth cookieLogin(String usr,String pass,ChannelAuth a) throws Exception {
		ChannelAuth a1=Channels.getCookie(trimUrl(url));
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
			query=params+"&"+user+"="+URLEncoder.encode(usr,"UTF-8")+
				  "&"+pwd+"="+URLEncoder.encode(pass,"UTF-8");
			method="POST";
		}
		URL u=new URL(url);
		Proxy p=proxy.getProxy();
		//URLConnection connection;
		Channels.debug("url "+u.toString()+" query "+query);
		if(u.toString().startsWith("https")) {
			HttpsURLConnection connection = (HttpsURLConnection) u.openConnection(p);
			 HttpsURLConnection.setFollowRedirects(true);
			((HttpsURLConnection) connection).setInstanceFollowRedirects(true);   
			((HttpsURLConnection) connection).setRequestMethod(method);
			Channels.debug("post page");
			String page=ChannelUtil.postPage(connection, query);
			if(ChannelUtil.empty(page))
				return null;
			return getCookie(connection,a);
		}
		else {
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			HttpURLConnection.setFollowRedirects(true);   
			connection.setInstanceFollowRedirects(false);   
			connection.setRequestMethod(method);
			String page=ChannelUtil.postPage(connection, query);
			if(ChannelUtil.empty(page))
				return null;
			return getCookie(connection,a);
		}
	}
	
	public ChannelAuth getAuthStr(String usr,String pass,ChannelAuth a) {
		return getAuthStr(usr,pass,false,a);
	}
	
	public ChannelAuth getAuthStr(String usr,String pass,boolean media,ChannelAuth a) {
		Channels.debug("login on channel "+parent.getName()+" type "+type+" on "+loggedOn);
		if(loggedOn)
			return mkResult(a);
		if(!media&&mediaOnly)
			return a;
		try {
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
	}
}

