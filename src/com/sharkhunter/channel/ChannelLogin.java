package com.sharkhunter.channel;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import net.pms.PMS;

public class ChannelLogin {
	
	private Channel parent;
	private String user;
	private String pwd;
	private String url;
	private ChannelMatcher auth;
	private String params;
	private String authStr;
	private boolean loggedOn;
	private String tokenStr;
	
	public ChannelLogin(ArrayList<String> data,Channel parent) {
		this.parent=parent;
		this.loggedOn=false;
		parse(data);
	}
	
	public void parse(ArrayList<String> data) {
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;
			String[] keyval=line.split("=",2);
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
		}
	}
	 
	
	public String getAuthStr(String usr,String pass) {
		if(loggedOn)
			return tokenStr;
		try {
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
					return tokenStr;
				}
			}
			return "";
		}
		catch (Exception e) {
			PMS.debug("could not fetch token "+e);
			return "";
		}
	}
	
	public void reset() {
		tokenStr="";
		loggedOn=false;
	}
}

