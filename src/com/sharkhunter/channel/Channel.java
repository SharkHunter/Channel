package com.sharkhunter.channel;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	private ArrayList<ChannelFolder> actions;
	
	private ChannelCred cred;
	private ChannelLogin logObj;
	
	private int searchId;
	private HashMap<String,SearchObj> searchFolders;
	
	private String[] subScript;
	
	private String[] proxies;
	private ChannelProxy activeProxy;
	
	private String[] prop;
	
	private HashMap<String,String> hdrs;
	
	private ChannelFolder favorite;
	private String videoFormat;
	
	private HashMap<String,ChannelVar> vars;
	private HashMap<String,String[]> trashVars;
	
	private ChannelStreamVars streamVars;
	
	public Channel(String name) {
		super(name,null);
		Ok=false;
		favorite=null;
		this.name=name;
		format=Format.VIDEO;
		folders=new ArrayList<ChannelFolder>();
		searchId=0;
		activeProxy=null;
		hdrs=new HashMap<String,String>();
		searchFolders=new HashMap<String,SearchObj>();
		actions=new ArrayList<ChannelFolder>();
		videoFormat=".flv";
		vars=new HashMap<String,ChannelVar>();
		trashVars=new HashMap<String,String[]>();
		streamVars=Channels.defStreamVar();
		streamVars.setInstance("");
		Ok=true;
	}
	
	public void parse(ArrayList<String> data,ArrayList<ChannelMacro> macros) {
		folders.clear();
		vars.clear();
		trashVars.clear();
		getChildren().clear();
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
			if(line.contains("var {")) {
				ArrayList<String> var=ChannelUtil.gatherBlock(data,i+1);
				i+=var.size();
				ChannelVar v=new ChannelVar(var,this);
				vars.put(v.displayName(), v);
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
				format=ChannelUtil.getFormat(keyval[1],format);
			}
			if(keyval[0].equalsIgnoreCase("img")) {
				thumbnailIcon=keyval[1];
				if (thumbnailIcon != null && thumbnailIcon.toLowerCase().endsWith(".png"))
					thumbnailContentType = HTTPResource.PNG_TYPEMIME;
				else
					thumbnailContentType = HTTPResource.JPEG_TYPEMIME;
			}
			if(keyval[0].equalsIgnoreCase("subscript")) {
				subScript=keyval[1].trim().split(",");
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
			if(keyval[0].equalsIgnoreCase("prop"))
				prop=keyval[1].trim().split(",");
			if(keyval[0].equalsIgnoreCase("fallback_video"))
				videoFormat=ChannelUtil.ensureDot(keyval[1].trim());
		}
		mkFavFolder();
	}
	
	private void mkFavFolder() {
		if(noFavorite())
			return;
		ArrayList<String> data=new ArrayList<String>();
		data.add("name=Favorite");
		ChannelFolder f=new ChannelFolder(data,this);
		if(f.Ok) {
			f.setIgnoreFav();
			favorite=f;
		}
	}
	
	public void addFavorite(ArrayList<String> data) {
		if(data.size()<3)
			return;
		if(!data.get(1).contains("folder {")) { // at least one folder must be there
			debug("Illegal favorite block ignore");
			return;
		}
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;
			if(line.contains("folder {")) {
				ArrayList<String> folder=ChannelUtil.gatherBlock(data,i+1);
				i+=folder.size();
				ChannelFolder f=new ChannelFolder(folder,this);
				if(f.Ok) {
					f.setIgnoreFav();
					favorite.addSubFolder(f);
				}
			}
		}
	}
	
	public void addFavorite(ChannelFolder cf) {
		if(cf.Ok) {
			cf.setIgnoreFav();
			favorite.addSubFolder(cf);
		}
	}
	
	public boolean isFavorized(String name) {
		for(ChannelFolder f : favorite.subfolders()) {
			if(name.equals(f.getName()))
				return true;
		}
		return false;
	}
	
	public String nxtSearchId() {
		return String.valueOf(searchId++);
	}
	
	public ChannelMacro getMacro(String macro) {
		return ChannelUtil.findMacro(macros, macro);
	}
	
	public int getMediaFormat() {
		return format;
	}
	
	public String getThumb() {
		return thumbnailIcon;
	}
	
	public HashMap<String,String> getHdrs() {
		return hdrs;
	}
	
	public void resolve() {
//		this.getChildren().clear();
	}
	
	public void discoverChildren(String s) {
		discoverChildren();
	}
	public void discoverChildren() {
		discoverChildren(this);
	}
	
	public void discoverChildren(DLNAResource res) {
		final Channel me=this;
		addChild(new VirtualFolder("Variables",null) {
			public void discoverChildren() {
				for(String var: vars.keySet()) {
					ChannelVar v=vars.get(var);
					addChild(new ChannelPMSVar(var,v));
				}
				streamVars.add(this, me);
			}
		});
		if(favorite!=null)
			try {
				favorite.match(this);
			} catch (MalformedURLException e1) {
			}
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
	
	public boolean isRefreshNeeded() {
		return true;
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
		return logObj.getAuthStr(user(),pwd(),a);
	}
	
	public ChannelAuth prepareCom() {
		Channels.setProxyDNS(ChannelUtil.getProperty(prop, "proxy_dns"));
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
	
	public HashMap<String,String> getSubMap(String realName,int id) {
		HashMap<String,String> res=new HashMap<String,String>();
		res.put("url", realName);
		if(subScript==null)
			return res;
		if(id>(subScript.length-1))
			return null;
		ArrayList<String> s=Channels.getScript(subScript[id]);
		if(s==null)
			if(id==0)
				return res;
			else
				return null;
		return ChannelNaviXProc.lite(realName,s,res);
	}
	
	public boolean noFavorite() {
		return ChannelUtil.getProperty(prop, "no_favorite")||Channels.noFavorite();
	}
	
	public ChannelFolder favorite() {
		return favorite;
	}
	
	///////////////////////////////////////////////
	// Action handling
	///////////////////////////////////////////////
	
	public void addAction(ChannelFolder cf) {
		debug("adding action "+cf.actionName());
		actions.add(cf);
	}
	
	public void action(ChannelSwitch swi,String name,String url,String thumb,DLNAResource res,int form) {
		String action=swi.getAction();
		String rUrl=swi.runScript(url);
		int f=form;
		if(f==-1)
			f=format;
		debug("action "+action+" mangled url "+rUrl+" format "+f);
		for(int i=0;i<actions.size();i++) {
			ChannelFolder cf=actions.get(i);
			if(!action.equals(cf.actionName()))
				continue;
			try {
				cf.action(res,null,rUrl,thumb,name,null,f);
			} catch (MalformedURLException e) {
			}
			return;
		}
	}
	
	private void open(DLNAResource res,String[] names,int pos,DLNAResource child) {
		List<DLNAResource> children=child.getChildren();
		for(int j=0;j<children.size();j++) {
			DLNAResource nxt=children.get(j);
			if(!names[pos].equals(nxt.getDisplayName()))
				continue;
			if((pos+1)==names.length) { // all done
				res.addChild(nxt);
				return;
			}
			open(res,names,pos+1,nxt);
			return;
		}
	}
	
	public void open(DLNAResource res,String[] names) {
		DLNAResource tmp=new VirtualFolder("",null);
		discoverChildren(tmp);
		open(res,names,0,tmp);
	}
	
	public String fallBackVideoFormat() {
		return videoFormat;
	}
	
	public void setVar(String var,String val) {
		setVar("",var,val);
	}
	
	public void setVar(String inst,String var,String val) {
		Channels.debug("set var "+var+" to val "+val+" for inst "+inst);
		ChannelVar v=vars.get(var);
		if(v!=null) {
			v.setValue(val);
			v.setInstance(inst);
		}
		else {
			// might be a stream var, save for later
			String[] tmp={inst,val};
			trashVars.put(var, tmp);
		}
	}
	
	public String resolveVars(String str) {
		for(String key : vars.keySet()) {
			str=vars.get(key).resolve(str);
		}
		return str;
	}
	
	public HashMap<String,ChannelVar> vars() {
		return vars;
	}
	
	public String[] trashVar(String var) {
		return trashVars.get(var);
	}
	
	public ChannelStreamVars defStreamVars() {
		return streamVars;
	}
}
