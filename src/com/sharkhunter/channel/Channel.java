package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;
import net.pms.formats.Format;
import net.pms.formats.v2.SubtitleType;
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
	
	private SubtitleType embedSubType;
	private ChannelMatcher subConv;
	private boolean subConvTimeMs;
	
	private ArrayList<ChannelSwitch> urlResolve;
	
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
		embedSubType=SubtitleType.SUBRIP;
		subConv=null;
		subConvTimeMs=false;
		urlResolve=new ArrayList<ChannelSwitch>();
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
			if(line.contains("sub_conv {")) {
				ArrayList<String> sc=ChannelUtil.gatherBlock(data,i+1);
				i+=sc.size();
				parseSubConv(sc);
			}
			if(line.contains("resolve {")) {
				ArrayList<String> ur=ChannelUtil.gatherBlock(data,i+1);
				i+=ur.size();
				ChannelSwitch s=new ChannelSwitch(ur,this);
				if(s.Ok)
					urlResolve.add(s);
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
			if(keyval[0].equalsIgnoreCase("sub_type")) {
				if(keyval[1].trim().equalsIgnoreCase("sami"))
					embedSubType=SubtitleType.SAMI;
			}
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
			if(cf.isActionOnly())
				continue;
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
		searchFolders.put(id, obj);
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
	public void searchAll(String str, DLNAResource res) {
		for(String id : searchFolders.keySet()) {
			SearchObj sobj = searchFolders.get(id);
			if(sobj!=null)
				sobj.search(str,res);
		}
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
	
	public ChannelFolder action(ChannelSwitch swi,String name,String url,String thumb,DLNAResource res,int form) {
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
				return cf;
			} catch (MalformedURLException e) {
			}
			return null;
		}
		return null;
	}
	
	public ChannelFolder getAction(String action) {
		for(int i=0;i<actions.size();i++) {
			ChannelFolder cf=actions.get(i);
			if(action.equals(cf.actionName()))
				return cf;
		}
		return null;
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
	
	public String embSubExt() {
		return embedSubType.getExtension();
	}
	
	public SubtitleType getEmbSub() {
		return embedSubType;
	}
	
	public String convSub(String subFile,boolean braviaFix) {
		if(subConv==null) {
			if(braviaFix)
				return braviafySubs(subFile);
			return subFile;
		}
		try {
			return convSub_i(subFile,braviaFix);
		} catch (IOException e) {
			return subFile;
		}
	}
	
	private String convSub_i(String subFile,boolean braviaFix) throws IOException {
		File src=new File(subFile);
		String oFile=Channels.dataEntry(src.getName()+".srt"+(braviaFix?".bravia":""));
		File dst=new File(oFile);
		if(dst.exists())
			return dst.getAbsolutePath();
		int index=1;
		String fe=ChannelUtil.getCodePage();
		String data=FileUtils.readFileToString(src,fe);
		OutputStreamWriter out=new OutputStreamWriter(new FileOutputStream(dst),fe);
		subConv.startMatch(data);
		while(subConv.match()) {
			String start=subConv.getMatch("start");
			String stop=subConv.getMatch("stop");
			String text=subConv.getMatch("text");
			if(ChannelUtil.empty(start)||ChannelUtil.empty(stop)||ChannelUtil.empty(text))
				continue;
			if(braviaFix) {
				// real odd ball trick
				// we add a line to the end of each text to pull up the 
				// text (and only on  BRAVIA)
				text=ChannelUtil.append(text, "\n", DOTS+"\n");
			}
			ChannelSubUtil.writeSRT(out, index++, start, stop, text, subConvTimeMs);
		}
		out.flush();
		out.close();
		if(dst.length()>0)
			return dst.getAbsolutePath();
		else {
			src.delete();
			dst.delete();
			return "";
		}
	}
	
	private void parseSubConv(ArrayList<String> data) {
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2)
				continue;
			if(keyval[0].equalsIgnoreCase("matcher")) {
				if(subConv==null)
					subConv=new ChannelMatcher(keyval[1],null,null);
				else
					subConv.setMatcher(keyval[1]);
				subConv.setChannel(this);
			}
			if(keyval[0].startsWith("emb_matcher_")) {
				if(subConv==null)
					subConv=new ChannelMatcher(null,null,null);
				String name=keyval[0].substring("emb_matcher_".length());
				ChannelMatcher m1=new ChannelMatcher(keyval[1],name+"+",null);
				subConv.addEmbed(name, m1);
				subConv.setChannel(this);
			}
			if(keyval[0].equalsIgnoreCase("order")) {
				if(subConv==null)
					subConv=new ChannelMatcher(null,keyval[1],null);
				else
					subConv.setOrder(keyval[1]);
				subConv.setChannel(this);
			}
			if(keyval[0].equalsIgnoreCase("prop")) {
				String[] tmp=keyval[1].trim().split(",");
				if(subConv==null) {
					subConv=new ChannelMatcher(null,null,null);
				}
				ChannelSimple s=new ChannelSimple(this);
				s.setProp(keyval[1]);
				subConv.setProperties(s);
				subConvTimeMs=ChannelUtil.getProperty(tmp, "time_ms");
				subConv.processProps(tmp);
				subConv.setChannel(this);
			}
		}
	}
	
	private static final String DOTS = "..."; 
	
	private String braviafySubs(String subFile) {
		try {
			File outFile=new File(subFile+".bravia");
			if(outFile.exists())
				return outFile.getAbsolutePath();
			String cp=ChannelUtil.getCodePage();
			BufferedReader in=new BufferedReader(new InputStreamReader(
												 new FileInputStream(subFile),cp));	
			String str;
			StringBuffer sb=new StringBuffer();
			while ((str = in.readLine()) != null) {
				if(ChannelUtil.empty(str)) {
					sb.append(DOTS+"\n\n");
					continue;
				}
				sb.append(str);
				sb.append("\n");
			}
			in.close();
			FileUtils.writeStringToFile(outFile, sb.toString(),cp);
			return outFile.getAbsolutePath();
		} catch (Exception e) {
			Channels.debug("braviafy error "+e);
		}
    	return subFile;
	}
	
	/////////////////////////////////////////////////////////
	
	private String resolved(DLNAResource r) {
		ChannelMediaStream cms=(ChannelMediaStream)r;
		cms.scrape(null);
		return cms.urlResolve();
	}
	
	public String urlResolve(String url,boolean dummyOnly) {
		for(ChannelSwitch resolver : urlResolve) {
			boolean dummy_match=ChannelUtil.getProperty(resolver.getProps(), "dummy_match");
			if(!dummyOnly&&dummy_match)
				continue;
			ChannelMatcher m=resolver.matcher;
			m.startMatch(url);
			if(m.match()) {
				String url1=m.getMatch("url",true);
				VirtualFolder dummy=new VirtualFolder("",null);
				ChannelFolder af=action(resolver,"",url1,null,dummy,-1);
				Channels.debug("urlResolve on channel "+getName()+" for url "+url);
				String mstr ="";
				if(af!=null)
					mstr=af.getProp("crawl_mode");
				// first lets crawl
				ChannelCrawl crawler=new ChannelCrawl();
				DLNAResource res =crawler.startCrawl(dummy, mstr);
				if(res!=null && (res instanceof ChannelMediaStream))
					return resolved(res);
				// pick the first
				for(DLNAResource r : dummy.getChildren()) {
					if(!(r instanceof ChannelMediaStream))
						continue;
					return resolved(r);
				}
			}
		}
		return null;
	}
}
