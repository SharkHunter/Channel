package com.sharkhunter.channel;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import net.pms.PMS;

public class ChannelLogin {
	
	public static final int STD=0;
	public static final int COOKIE=0;
	
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
	
	public ChannelLogin(ArrayList<String> data,Channel parent) {
		this.parent=parent;
		this.loggedOn=false;
		type=ChannelLogin.STD;
		mediaOnly=false;
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
				
		}
	}
	
	private ChannelAuth mkResult() {
		ChannelAuth a=new ChannelAuth();
		a.method=type;
		a.authStr=tokenStr;
		return a;
	}
	
	private ChannelAuth stdLogin(String usr,String pass) throws Exception {
		String query=params+"&"+user+"="+URLEncoder.encode(usr,"UTF-8")+
		"&"+pwd+"="+URLEncoder.encode(pass,"UTF-8");
		URL u=new URL(url);
		//PMS.debug("url "+url+" query "+query);
		HttpsURLConnection connection = (HttpsURLConnection) u.openConnection();
		HttpsURLConnection.setFollowRedirects(true);   
		connection.setInstanceFollowRedirects(true);   
		connection.setRequestMethod("POST");  
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
				return mkResult();
			}
		}
		return null;
	}
	
	private ChannelAuth cookieLogin(String usr,String pass) throws Exception {
		String query=params+"&"+user+"="+URLEncoder.encode(usr,"UTF-8")+
		"&"+pwd+"="+URLEncoder.encode(pass,"UTF-8");
		URL u=new URL(url);
		HttpURLConnection connection = (HttpURLConnection) u.openConnection();
		HttpURLConnection.setFollowRedirects(true);   
		connection.setInstanceFollowRedirects(true);   
		connection.setRequestMethod("POST");  
		String page=ChannelUtil.postPage(connection, query);
		String hName="";
		for (int j=1; (hName = connection.getHeaderFieldKey(j))!=null; j++) {
		 	if (!hName.equals("Set-Cookie")) 
		 		continue;
		 	String[] fields = connection.getHeaderField(j).split(";\\s*");
	 		String cookie=fields[0];
		 	cookie = cookie.substring(0, cookie.indexOf(";"));
	        /*tokenStr=cookie;
	        loggedOn=true;
	        return mkResult();*/
		 	return null;
		}
		return null;
	}
	
	public ChannelAuth getAuthStr(String usr,String pass) {
		return getAuthStr(usr,pass,false);
	}
	
	public ChannelAuth getAuthStr(String usr,String pass,boolean media) {
		if(loggedOn)
			return mkResult();
		if(!media&&mediaOnly)
			return null;
		try {
			if(type==ChannelLogin.STD)
				return stdLogin(usr,pass);
			else if(type==ChannelLogin.COOKIE)
				return cookieLogin(usr,pass);
			return null;
		}
		catch (Exception e) {
			PMS.debug("could not fetch token "+e);
			return null;
		}
	}
	
	public void reset() {
		tokenStr="";
		loggedOn=false;
	}
}

