package com.sharkhunter.channel;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.network.HTTPResource;

public class Channel extends VirtualFolder {
	
	public boolean Ok;
	private String name;
	private int format;
	
	private ArrayList<ChannelFolder> folders;
	private ArrayList<ChannelMacro> macros;
	
	private ChannelCred cred;
	private ChannelLogin logObj;
	
	private int searchId;
	private HashMap<String,SearchObj> searchFolders;
	
	private String subScript;
	
	private String[] proxies;
	private ChannelProxy activeProxy;
	
	private HashMap<String,String> hdrs;
	
	public Channel(String name) {
		super(name,null);
		Ok=false;
		this.name=name;
		format=Format.VIDEO;
		folders=new ArrayList<ChannelFolder>();
		searchId=0;
		activeProxy=null;
		hdrs=new HashMap<String,String>();
		searchFolders=new HashMap<String,SearchObj>();
		Ok=true;
	}
	
	public void parse(ArrayList<String> data,ArrayList<ChannelMacro> macros) {
		folders.clear();
		children.clear();
		childrenNumber=0;
		discovered=false;
		debug("parse channel "+name+" data "+data.toString());
		this.macros=macros;
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line.contains("login {")) {
				// Login data
				ArrayList<String> log=ChannelUtil.gatherBlock(data, i+1);
				i+=log.size();
				logObj=new ChannelLogin(log,this);
			}
			if(line.contains("folder {")) {
				ArrayList<String> folder=ChannelUtil.gatherBlock(data,i+1);
				i+=folder.size();
				ChannelFolder f=new ChannelFolder(folder,this);
				if(f.Ok)
					folders.add(f);
			}
			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2)
				continue;
			if(keyval[0].equalsIgnoreCase("macro")) {
				ChannelMacro m=ChannelUtil.findMacro(macros,keyval[1]);
				if(m!=null)
					parse(m.getMacro(),macros);
				else
					PMS.debug("unknown macro "+keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("format")) {
				if(keyval[1].equalsIgnoreCase("video"))
					format=Format.VIDEO;
				if(keyval[1].equalsIgnoreCase("audio"))
					format=Format.AUDIO;
				if(keyval[1].equalsIgnoreCase("image"))
					format=Format.IMAGE;
				
			}
			if(keyval[0].equalsIgnoreCase("img")) {
				thumbnailIcon=keyval[1];
				if (thumbnailIcon != null && thumbnailIcon.toLowerCase().endsWith(".png"))
					thumbnailContentType = HTTPResource.PNG_TYPEMIME;
				else
					thumbnailContentType = HTTPResource.JPEG_TYPEMIME;
			}
			if(keyval[0].equalsIgnoreCase("subscript")) {
				subScript=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("proxy")) {
				proxies=keyval[1].trim().split(",");
			}
			if(keyval[0].equalsIgnoreCase("hdr")) {
				String[] k1=keyval[1].split("=");
				if(k1.length<2)
					continue;
				hdrs.put(k1[0], k1[1]);
			}
			
		}
	}
	
	public String nxtSearchId() {
		return String.valueOf(searchId++);
	}
	
	public ChannelMacro getMacro(String macro) {
		return ChannelUtil.findMacro(macros, macro);
	}
	
	public int getFormat() {
		return format;
	}
	
	public String getThumb() {
		return thumbnailIcon;
	}
	
	public HashMap<String,String> getHdrs() {
		return hdrs;
	}
	
	public boolean refreshChildren() {
		return true; // always re resolve
	}
	
	public void discoverChildren(String s) {
		discoverChildren();
	}
	
	public void discoverChildren() {
		for(int i=0;i<folders.size();i++) {
			ChannelFolder cf=folders.get(i);
			if(cf.isATZ()) 
				addChild(new ChannelATZ(cf));
			else if(cf.isSearch())
				addChild(new SearchFolder(cf.getName(),cf));
			else
				try {
					cf.match(this);
				} catch (MalformedURLException e) {
				}
		}
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}
	
	public void debug(String msg) {
		Channels.debug(msg);
	}
	
	public String name() {
		return name;
	}
	
	public boolean login() {
		return (logObj!=null);
	}
	
	public void addCred(ChannelCred c) {
		cred=c;
		if(logObj!=null)
			logObj.reset();
	}
	
	public String user() {
		if(cred!=null)
			return cred.user;
		return null;
	}
	
	public String pwd() {
		if(cred!=null)
			return cred.pwd;
		return null;
	}
	
	private ChannelAuth getAuth(ChannelProxy p) {
		ChannelAuth a=new ChannelAuth();
		a.proxy=p;
		a.method=-1;
		if(logObj==null)
			return a;
		if(cred==null)
			return a;
		if(ChannelUtil.empty(cred.user))
			return a;
		if(ChannelUtil.empty(cred.pwd))
			return a;
		return logObj.getAuthStr(cred.user, cred.pwd,a);
	}
	
	public ChannelAuth prepareCom() {
		if(proxies==null) // no proxy, just regular login
			return getAuth(ChannelProxy.NULL_PROXY);
		Channels.debug("activeProxy "+activeProxy);
		if(activeProxy!=null&&activeProxy.isUp()) {
			return getAuth(activeProxy);
		}
		for(int i=0;i<proxies.length;i++) {
			ChannelProxy p=Channels.getProxy(proxies[i]);
			if(p==null)
				continue;
			if(!p.isUp())
				continue;
			Channels.debug("use proxy "+p.getProxy().toString());
			activeProxy=p;
			return getAuth(p);
		}
		return getAuth(ChannelProxy.NULL_PROXY);		
	}
	
	public void addSearcher(String id,SearchObj obj) {
		searchFolders.put(id,obj);
	}
	
	public void research(String str,String id,DLNAResource res) {
		debug("id "+id);
		if(id.startsWith("navix:")) {
			id=id.substring(6);
			ChannelFolder holder=folders.get(0);
			if(holder!=null) {
				if(holder.isNaviX()) {
					ChannelNaviX nx=new ChannelNaviX(this,"",ChannelUtil.getThumb(null,null, this),
							  	id,holder.getPropList(),holder.getSubs());
					ChannelNaviXSearch ns=new ChannelNaviXSearch(nx,id);
					debug("perform navix search");
					ns.search(str, res);
					return;
				}
			}
		}
		SearchObj obj=searchFolders.get(id);
		if(obj==null)
			return;
		obj.search(str, res);
	}
	
	public HashMap<String,String> getSubMap(String realName) {
		HashMap<String,String> res=new HashMap<String,String>();
		res.put("url", realName);
		ArrayList<String> s=Channels.getScript(subScript);
		if(s==null)
			return res;
		return ChannelNaviXProc.lite(realName,s,res);
	}
	
}
