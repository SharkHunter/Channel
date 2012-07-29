package com.sharkhunter.channel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelNaviXUpdate {
	
	private static String baseUrl="http://www.navixtreme.com/";
	private static String myUrl=baseUrl+"playlist/mine.plx";
	private static String loginUrl=baseUrl+"members";
	private static String upUrl=baseUrl+"mylists";
	private static String listId=null;
	private static String naviCookie=null;
	private static boolean local;
	
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
		if(cr==null)
			return;
		URL u=new URL(loginUrl);
		String query="action=takelogin&ajax=1&username="+cr.user+"&password="+cr.pwd;
		URLConnection c=u.openConnection();
		String page=ChannelUtil.postPage(c, query);
		if(ChannelUtil.empty(page)) 
			throw new Exception("Empty NaviX login page");
		String[] lines=page.split("\n");
		if(!lines[0].contains("ok"))
			throw new Exception("NaviX login failed "+page);
		ChannelAuth a=new ChannelAuth();
		ChannelCookie.parseCookie(c, a, baseUrl);
		naviCookie="l_access="+lines[1].trim();
	}
	
	private static void fetchListId(String name) throws Exception {
		Channels.debug("using "+name+" as NaviX upload list");
		if(name.equals("local")) {
			local=true;
			name=Channels.cfg().getNaviXUpload2();
			Channels.debug("remote "+name+" used as NaviX list");
			if(ChannelUtil.empty(name))
				return;
		}
		URL u=new URL(myUrl);
		URLConnection c=u.openConnection();
		String page=ChannelUtil.fetchPage(c);
		Channels.debug("page "+page);
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
		local=false;
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
	
	public static void sync(String name,String url,String proc,int format,
			String thumb,String imdb) throws Exception {
		if(ChannelUtil.empty(proc)) // just to make sure
			proc="";
		if(ChannelUtil.empty(thumb))
			thumb="";
		if(ChannelUtil.empty(imdb))
			imdb="";
		else
			imdb="imdb="+imdb+"!";
		if(url.startsWith("rtmpdump://")) {
			// need to split the rtmpdump stuff back agian
			int pos=url.indexOf("?");
			if(pos!=-1) {
				StringBuffer sb=new StringBuffer();
				String tmp[]=url.substring(pos+1).split("&");
				for(int i=0;i<tmp.length;i++) {
					String[] s=ChannelUtil.unescape(tmp[i]).split("=",2);
					if(s[0].equals("-r"))  { //special stuff
						sb.append(s[1]);
						sb.append(" ");
						continue;
					}
					sb.append(ChannelUtil.rtmpOp(s[0]));
					if(s.length>1) {
						sb.append("="+s[1]);
					}
					else
						sb.append("=true");
					sb.append(" ");
				}
				url=sb.toString();
			}
		}
		String query="action=item_save&id=0&list_id="+listId+"&text_local=0&"+
		"list_pos=top&type="+ChannelUtil.format2str(format).toLowerCase()+
		"&name="+ChannelUtil.escape(name)+
		"&thumb="+ChannelUtil.escape(thumb)+
		"&URL="+ChannelUtil.escape(url)+
		"&processor="+ChannelUtil.escape(proc)+
		"&description="+ChannelUtil.escape(imdb);
		upload(query);
	}
	
	public static void updateMedia(Channel ch,String name,String url,String proc,int format,
			String thumb,String imdb) throws Exception {
		if(local) {
			Channels.addToPLX(ch,name,url,proc,format,thumb,imdb);
		}
		if(netActive())
			sync(name,url,proc,format,thumb,imdb);
	}
	
	public static boolean netActive() {
		return (!ChannelUtil.empty(listId))&&(!ChannelUtil.empty(naviCookie));
	}
	
	public static boolean active() {
		return local||netActive();
	}

}
