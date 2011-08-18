package com.sharkhunter.channel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelNaviXUpdate {
	
	private static String myUrl="http://navix.turner3d.net/playlist/mine.plx";
	private static String loginUrl="http://navix.turner3d.net/members";
	private static String upUrl="http://navix.turner3d.net/mylists";
	private static String listId=null;
	private static String naviCookie=null;
	
	private static boolean setId(String url,String name,String name1,String type) {
		Pattern re=Pattern.compile("/(\\d+)/");
		if(ChannelUtil.empty(url)) {
			name1=null;
			type=null;
			url=null;
			return false;
		}
		if(ChannelUtil.empty(type)) {
			name1=null;
			type=null;
			url=null;
			return false;
		}
		if(ChannelUtil.empty(name1)) {
			name1=null;
			type=null;
			url=null;
			return false;
		}
		// All is well take a look
		if(type.equals("playlist")&&
		   name.equalsIgnoreCase(name1)) {
			Matcher m=re.matcher(url);
			if(m.find()) {
				listId=m.group(1);
				Channels.debug("set list id "+listId);
				return true;
			}
		}
		return false;
	}
	
	private static void fetchCookie(ChannelCred cr) throws Exception{
		URL u=new URL(loginUrl);
		String query="action=takelogin&ajax=1&username="+cr.user+"&password="+cr.pwd;
		String page=ChannelUtil.postPage(u.openConnection(), query);
		if(ChannelUtil.empty(page))
			return;
		String[] lines=page.split("\n");
		if(!lines[0].contains("ok"))
			return;
		naviCookie="l_access="+lines[1].trim();
	}
	
	private static void fetchListId(String name) throws Exception {
		Channels.debug("using "+name+" as NaviX upload list");
		URL u=new URL(myUrl);
		URLConnection c=u.openConnection();
		String page=ChannelUtil.fetchPage(c);
		if(ChannelUtil.empty(page))
			return;
		String[] lines=page.split("\n");
		String name1=null;
		String url=null;
		String type=null;
		for(int i=0;i<lines.length;i++) {
			String line=lines[i].trim();
			if(ChannelUtil.ignoreLine(line)) { // new block
				if(setId(url,name,name1,type))
					return;
			}
			if(line.startsWith("URL="))
				url=line.substring(4);
			else if(line.startsWith("name="))
				name1=line.substring(5);
			else if(line.startsWith("type="))
				type=line.substring(5);
		}
		setId(url,name,name1,type);
	}
	
	public static void init(String listName,ChannelCred cred) throws Exception {
		fetchCookie(cred);
		fetchListId(listName);		
	}
	
	private static void upload(String query) throws Exception {
		if(ChannelUtil.empty(listId)||ChannelUtil.empty(naviCookie))
			throw new Exception("NaviXupdater missing listId or cookie");
		Channels.debug("about to upload "+query);
		URL u=new URL(upUrl);
		String s=ChannelUtil.postPage(u.openConnection(), query,naviCookie,null);
		Channels.debug("navixup res "+s);
	}
	
	public static void updatePlx(String name,String url) throws Exception {
		String query="action=item_save&id=0&list_id="+listId+"&text_local=0&"+
			"list_pos=top&type=plx&name="+ChannelUtil.escape(name)+"&URL="+ChannelUtil.escape(url);
		upload(query);
	}
	
	public static void updateMedia(String name,String url,String proc,int format,
			String thumb,String imdb) throws Exception {
		if(ChannelUtil.empty(proc)) // just to make sure
			proc="";
		if(ChannelUtil.empty(thumb))
			thumb="";
		if(ChannelUtil.empty(imdb))
			imdb="";
		else
			imdb="imdb="+imdb+"!";
		String query="action=item_save&id=0&list_id="+listId+"&text_local=0&"+
		"list_pos=top&type="+ChannelUtil.format2str(format).toLowerCase()+
		"&name="+ChannelUtil.escape(name)+
		"&thumb="+ChannelUtil.escape(thumb)+
		"&URL="+ChannelUtil.escape(url)+
		"&processor="+ChannelUtil.escape(proc)+
		"&description="+ChannelUtil.escape(imdb);
		upload(query);
	}
	
	public static boolean active() {
		return (!ChannelUtil.empty(listId))&&(!ChannelUtil.empty(naviCookie));
	}

}
