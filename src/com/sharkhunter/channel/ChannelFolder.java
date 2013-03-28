package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;
import net.pms.movieinfo.MovieInfoVirtualFolder;

public class ChannelFolder implements ChannelProps, SearchObj{
	public static final int TYPE_NORMAL=0;
	public static final int TYPE_ATZ=1;
	public static final int TYPE_EMPTY=2;
	public static final int TYPE_LOGIN=3;
	public static final int TYPE_ATZ_LINK=4;
	public static final int TYPE_NAVIX=5;
	public static final int TYPE_RECURSE=6;
	public static final int TYPE_SEARCH=7;
	public static final int TYPE_NAVIX_SEARCH=8;
	public static final int TYPE_ACTION=9;	
	public static final int TYPE_REMATCH=10;
	public static final int TYPE_EXEC=11;
	
	public boolean Ok;
	
	private String name;
	private String url;
	private int format;
	private int type;
	private String[] prop;
	
	private ChannelMatcher matcher;
	
	private ArrayList<ChannelFolder> subfolders;
	private Channel parent;
	private ChannelFolder parentFolder;
	private ArrayList<ChannelItem> items;
	private ArrayList<ChannelMedia> medias; 
	private ArrayList<ChannelSwitch> switches;
	
	private int continues;
	private boolean contAll;
	
	private String[] sub;
	
	private String searchId;
	private String pre_script;
	private String post_script;
	private String thumb_script;
	
	private String hook;
	private String tag;
	
	private String proxy;
	
	private HashMap<String,String> hdrs;
	
	private String group;
	private String imdbId;
	private String staticThumb;
	
	private boolean ignoreFav;
	
	private String actionName;
	private String[] action_prop;
	private String videoFormat;
	
	public ChannelFolder(ArrayList<String> data,Channel parent) {
		this(data,parent,null);
	}
	
	public ChannelFolder(ChannelFolder cf) {
		Ok=true;
		type=ChannelFolder.TYPE_NORMAL;
		this.parent=cf.parent;
		parentFolder=cf.parentFolder;
		matcher=cf.matcher;
		subfolders=cf.subfolders;
		items=cf.items;
		format=cf.format;
		url=cf.url;
		prop=cf.prop;
		medias=cf.medias;
		continues=ChannelUtil.calcCont(prop);
		contAll=cf.contAll;
		pre_script=cf.pre_script;
		post_script=cf.post_script;
		thumb_script=cf.thumb_script;
		proxy=cf.proxy;
		hdrs=cf.hdrs;
		group=cf.group;
		imdbId=cf.imdbId;
		ignoreFav=cf.ignoreFav;
		switches=cf.switches;
		actionName=cf.actionName;
		action_prop=cf.action_prop;
		videoFormat=cf.videoFormat;
	}
	
	public ChannelFolder(ArrayList<String> data,Channel parent,ChannelFolder pf) {
		Ok=false;
		type=ChannelFolder.TYPE_NORMAL;
		this.parent=parent;
		parentFolder=pf;
		matcher=null;
		subfolders=new ArrayList<ChannelFolder>();
		items=new ArrayList<ChannelItem>();
		medias=new ArrayList<ChannelMedia>();
		switches=new ArrayList<ChannelSwitch>();
		contAll=false;
		continues=Channels.DeafultContLim;
		pre_script=null;
		post_script=null;
		proxy=null;
		ignoreFav=false;
		actionName="";
		action_prop=null;
		format=-1;
		hdrs=new HashMap<String,String>();
		videoFormat=null;
		if(pf!=null)
			hdrs.putAll(pf.hdrs);
		else
			hdrs.putAll(parent.getHdrs());
		parse(data);
		continues=ChannelUtil.calcCont(prop);
		if(continues<0)
			contAll=true;
		if(isSearch())
			setSearchId();
		if(!ChannelUtil.empty(actionName))
			parent.addAction(this);
		if(ChannelUtil.empty(videoFormat)) {
			if(parentFolder==null)
				videoFormat=parent.fallBackVideoFormat();
			else
				videoFormat=parentFolder.fallBackVideoFormat();
		}
		Ok=true;
	}
	
	private void setSearchId() {
		searchId=ChannelUtil.getPropertyValue(prop, "search_id");
		if(ChannelUtil.empty(searchId))
			searchId=parent.nxtSearchId();
		parent.addSearcher(searchId, this);
	}
	
	public void parse(ArrayList<String> data) {
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;
			if(line.contains("folder {")) {
				ArrayList<String> folder=ChannelUtil.gatherBlock(data,i+1);
				i+=folder.size();
				ChannelFolder f=new ChannelFolder(folder,parent,this);
				if(f.Ok)
					subfolders.add(f);
				continue;
			}
			if(line.contains("item {")) {
				ArrayList<String> it=ChannelUtil.gatherBlock(data,i+1);
				i+=it.size();
				ChannelItem item=new ChannelItem(it,parent,this);
				if(item.Ok)
					items.add(item);
				continue;
			}
			if(line.contains("media {")) {
				ArrayList<String> m=ChannelUtil.gatherBlock(data,i+1);
				i+=m.size();
				ChannelMedia med=new ChannelMedia(m,parent);
				if(med.Ok)
					medias.add(med);
				continue;
			}
			if(line.contains("switch {")) {
				ArrayList<String> m=ChannelUtil.gatherBlock(data,i+1);
				i+=m.size();
				ChannelSwitch sw=new ChannelSwitch(m,parent);
				sw.setParentFolder(this);
				if(sw.Ok)
					switches.add(sw);
				continue;
			}
			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2)
				continue;
			if(keyval[0].equalsIgnoreCase("macro")) {
				ChannelMacro m=parent.getMacro(keyval[1]);
				if(m!=null)
					parse(m.getMacro());
				else
					PMS.debug("unknown macro "+keyval[1]);
			}	
			if(keyval[0].equalsIgnoreCase("name"))
				name=keyval[1];
			if(keyval[0].equalsIgnoreCase("type")) 	
				type=parseType(keyval[1]);
			if(keyval[0].equalsIgnoreCase("url"))
				url=keyval[1];
			if(keyval[0].equalsIgnoreCase("format")) {
				format=ChannelUtil.getFormat(keyval[1],format);
			}				
			if(keyval[0].equalsIgnoreCase("prop"))	
				prop=keyval[1].trim().split(",");
			if(keyval[0].equalsIgnoreCase("matcher")) {
				if(matcher==null)
					matcher=new ChannelMatcher(keyval[1],null,this);
				else
					matcher.setMatcher(keyval[1]);
				matcher.setChannel(parent);
			}
			if(keyval[0].equalsIgnoreCase("order")) {
				if(matcher==null)
					matcher=new ChannelMatcher(null,keyval[1],this);
				else
					matcher.setOrder(keyval[1]);
				matcher.setChannel(parent);
			}
			if(keyval[0].equalsIgnoreCase("subtitle")) {
				sub=keyval[1].split(",");
			}
			if(keyval[0].equalsIgnoreCase("pre_script")) {
				pre_script=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("post_script")) {
				post_script=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("thumb_script")) {
				thumb_script=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("tag")) {
				tag=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("hook")) {
				hook=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("proxy")) {
				proxy=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("hdr")) {
				String[] k1=keyval[1].split("=",2);
				if(k1.length<2)
					continue;
				hdrs.put(k1[0], k1[1]);
			}
			if(keyval[0].equalsIgnoreCase("thumb"))
				staticThumb=keyval[1];
			if(keyval[0].equalsIgnoreCase("imdb"))
				imdbId=keyval[1];
			if(keyval[0].equalsIgnoreCase("action_name"))
				actionName=keyval[1];
			if(keyval[0].equalsIgnoreCase("action_prop"))	
				action_prop=keyval[1].trim().split(",");
			if(keyval[0].equalsIgnoreCase("fallback_video"))
				videoFormat=ChannelUtil.ensureDot(keyval[1].trim());
		}
		if(matcher!=null)
			matcher.processProps(prop);
	}
	
	public void setType(int t) {
		type=t;
	}
	
	public int getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getURL() {
		return url;
	}
	
	public boolean isATZ() {
		return ((type==ChannelFolder.TYPE_ATZ)||(type==ChannelFolder.TYPE_ATZ_LINK));
	}
	
	public boolean isNaviX() {
		return (type==ChannelFolder.TYPE_NAVIX)||(type==ChannelFolder.TYPE_NAVIX_SEARCH);
	}
	
	public boolean isSearch() {
		return (type==ChannelFolder.TYPE_SEARCH);
	}
	
	public boolean isActionOnly() {
		return (type==ChannelFolder.TYPE_ACTION);
	}
	
	public boolean isFavorized(String name) {
		return (isFavItem()||parent.isFavorized(name));
	}
	
	public String getProp(String p) {
		return ChannelUtil.getPropertyValue(prop, p);
	}
	
	public boolean getProperty(String p) {
		return ChannelUtil.getProperty(prop, p);
	}
	
	public String[] getPropList() {
		return prop;
	}
	
	public String[] getSubs() {
		return sub;
	}
	
	public String actionName() {
		return actionName;
	}
	
	public void addSubFolder(ChannelFolder f) {
		subfolders.add(f);
		f.parentFolder = this;
	}
	
	public void setIgnoreFav() {
		ignoreFav=true;
	}
	
	public boolean ignoreFav() {
		return ignoreFav||parent.noFavorite();
	}
	
	public int getFormat() {
		return format;
	}
	
	private int parseType(String t) {
		if(t.compareToIgnoreCase("atz")==0)
			return ChannelFolder.TYPE_ATZ;
		if(t.compareToIgnoreCase("empty")==0)
			return ChannelFolder.TYPE_EMPTY;
		if(t.compareToIgnoreCase("atzlink")==0)
			return ChannelFolder.TYPE_ATZ_LINK;
		if(t.compareToIgnoreCase("navix")==0)
			return ChannelFolder.TYPE_NAVIX;
		if(t.compareToIgnoreCase("recurse")==0)
			return ChannelFolder.TYPE_RECURSE;
		if(t.compareToIgnoreCase("search")==0)
			return ChannelFolder.TYPE_SEARCH;
		if(t.compareToIgnoreCase("navix_search")==0)
			return ChannelFolder.TYPE_NAVIX_SEARCH;
		if(t.compareToIgnoreCase("action")==0)
			return ChannelFolder.TYPE_ACTION;
		if(t.compareToIgnoreCase("rematch")==0)
			return ChannelFolder.TYPE_REMATCH;
		if(t.compareToIgnoreCase("exec")==0)
			return ChannelFolder.TYPE_EXEC;
		return ChannelFolder.TYPE_NORMAL;
	}
	
	private boolean doContinue(String name,String realUrl) {
		String cn=ChannelUtil.getPropertyValue(prop, "continue_name");
		String cu=ChannelUtil.getPropertyValue(prop, "continue_url");
		parent.debug("cont "+continues+" name "+name);
		if(!ChannelUtil.empty(cn)) { // continue
			if(name.matches(cn)) {
				continues--;
				if((contAll||continues>0)&&(continues>Channels.ContSafetyVal)) {
					return true;
				}
			}
		}
		if(!ChannelUtil.empty(cu)) {
			if(realUrl.matches(cu)) {
				continues--;
				if((contAll||continues>0)&&(continues>Channels.ContSafetyVal)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private class PeekRes {
		boolean res;
		String thumbUrl;
	}
	
	private PeekRes mkPeekRes(boolean b) {
		return mkPeekRes(b,"");
	}
	
	private PeekRes mkPeekRes(boolean b,String url) {
		PeekRes r=new PeekRes();
		r.res=b;
		r.thumbUrl=url;
		return r;
	}
	
	private PeekRes peek(String urlEnd,String[] props) {
		if((type!=ChannelFolder.TYPE_NORMAL)&&
		   (type!=ChannelFolder.TYPE_EMPTY)) // non normal folders are not peekable
			return mkPeekRes(true);
		if(!ChannelUtil.getProperty(props, "peek")) // no peek prop
			return mkPeekRes(true);
		if(matcher==null) // static folders are not peekable
			return mkPeekRes(true);
		String realUrl=ChannelUtil.concatURL(url,urlEnd);
		String page="";
		if(!ChannelUtil.empty(realUrl)) {
			URL urlobj;
			try {
				urlobj=new URL(realUrl);
				page=ChannelUtil.fetchPage(urlobj.openConnection(),parent.prepareCom(),null);
			} catch (Exception e) {
				page="";
			}
			parent.debug("page "+page);
			if(ChannelUtil.empty(page)) // no page found
				return mkPeekRes(true);
		}
		for(int i=0;i<medias.size();i++) {
	    	ChannelMedia m1=medias.get(i);
	    	ChannelMatcher m=m1.getMatcher();
	    	m.startMatch(page);
	    	if(m.match()) { // match found
	    		String thumb=m.getMatch("thumb",false);
	    		return mkPeekRes(true,thumb);
	    	}
		}
		for(int i=0;i<items.size();i++) {
	    	ChannelItem item=items.get(i);
	    	ChannelMatcher m=item.getMatcher();
	    	m.startMatch(page);
	    	if(m.match()) 
	    	{ // match found
	    		String thumb=m.getMatch("thumb",false);
	    		return mkPeekRes(true,thumb);
	    	}
		}
		for(int i=0;i<subfolders.size();i++) {
	    	ChannelFolder cf=subfolders.get(i);
	    	ChannelMatcher m=cf.matcher;
	    	if(cf.isATZ()) 
	    		return mkPeekRes(true);
	    	if(m==null) 
	    		return mkPeekRes(true);
	    	m.startMatch(page);
	    	if(m.match()) { // match found
	    		String thumb=m.getMatch("thumb",false);
	    		return mkPeekRes(true,thumb);
	    	}
		}
		// if we made it here there are no matches so we say peek failed
		return mkPeekRes(false);
	}
	
	public void match(DLNAResource res) throws MalformedURLException {
		match(res,null,"",null,null,null,null);
	}
	
	public void match(DLNAResource res,ChannelFilter filter,String urlEnd,
			String pThumb,String nName) throws MalformedURLException {
		match(res,filter,urlEnd,pThumb,nName,null,null);
	}
	
	public void match(DLNAResource res,ChannelFilter filter,String urlEnd,
			String pThumb,String nName,String imdb) throws MalformedURLException {
		match(res,filter,urlEnd,pThumb,nName,imdb,null);
	}
	
	public void match(DLNAResource res,ChannelFilter filter,String urlEnd,
			String pThumb,String nName,String imdb,String embSubs) throws MalformedURLException {
		String page="";
		boolean cache=ChannelUtil.getProperty(prop, "cache");
		File cFile=cacheFile();
		if(cache&&!isATZ())
			addCacheClear(res,cFile);
//		parent.debug("folder match "+name+" nName "+nName);
		if(ChannelUtil.getProperty(prop, "test_login")) {
			parent.prepareCom();
			return;
		}
		if(filter==null&&matcher==null&&type==ChannelFolder.TYPE_NORMAL) { // static folder
			// static folders are not subject to filter
			parent.debug("static folder "+type);
			ChannelPMSFolder cpf=new ChannelPMSFolder(this,name);
			cpf.setImdb(ChannelUtil.empty(imdb)?imdbId:imdb);
			res.addChild(cpf);
			return;
		}
		boolean post=false;
		String methodProp=ChannelUtil.getPropertyValue(prop, "http_method");
		if(methodProp!=null&&methodProp.equalsIgnoreCase("post"))
			post=true;
		String realUrl=url;		
		if(isNaviX()) { // i'm navix special handling
			realUrl=ChannelUtil.concatURL(url,urlEnd);	
			ChannelNaviX nx=new ChannelNaviX(parent,name,ChannelUtil.getThumb(null,pThumb, parent),
					  realUrl,prop,sub);
			if(ignoreFav)
				nx.setIgnoreFav();
			res.addChild(nx);
			return;
		}
		if(isSearch()) {
			post=true;
			if(methodProp!=null&&!methodProp.equalsIgnoreCase("post"))
				post=false;
		}
		boolean dummy=false;
		boolean oldCache=false;
		if(url!=null)
			dummy=url.equals("dummy_url");
		if(!post)
			realUrl=ChannelUtil.concatURL(url,StringEscapeUtils.unescapeHtml(urlEnd));
		if(dummy)
			realUrl=urlEnd;
		if(cache&&cFile.exists()) {
			String ageTime=ChannelUtil.getPropertyValue(prop, "cache_age");
			if(ChannelUtil.empty(ageTime))
				ageTime="7";
			oldCache=oldCache(ageTime,cFile);
			if(!oldCache)
				realUrl=cFile.toURI().toURL().toString();
		}
		//realUrl=ChannelNaviXProc.simple(realUrl, pre_script);
		realUrl=ChannelScriptMgr.runScript(pre_script, realUrl, parent);
		if(!ChannelUtil.empty(realUrl)&&!dummy) {
			URL urlobj=new URL(realUrl.replaceAll(" ", "%20"));
			parent.debug("folder match url "+urlobj.toString()+" type "+type+" post "+post+" "+urlEnd+" emb "+embSubs);
			try {
				ChannelAuth a=parent.prepareCom();
				if(!ChannelUtil.empty(proxy))  {// override channel proxy
					ChannelProxy p0=Channels.getProxy(proxy);
					if(p0!=null) // update if we found a proxy leave it otherwise
						a.proxy=p0;
				}
				Proxy p=ChannelUtil.proxy(a);
				URLConnection conn=urlobj.openConnection(p);
				if(post) {
					String q=ChannelUtil.append(urlEnd, "&", 
							ChannelUtil.getPropertyValue(prop, "post_data"));
					page=ChannelUtil.postPage(conn, q,"",hdrs);
				}
				else
					page=ChannelUtil.fetchPage(conn,a,"",hdrs);
				ChannelCookie.parseCookie(conn, a, realUrl);
			} catch (Exception e) {
				Channels.debug("fetch exception "+e);
				page="";
			}
			parent.debug("page "+page);
			if(type==ChannelFolder.TYPE_EXEC) // execfolders are done
				return;
			if(ChannelUtil.empty(page)) {
				if(cache&&oldCache) { // use the old cache no matter what
					urlobj=new URL(cFile.toURI().toURL().toString().replaceAll(" ", "%20"));
					try {
						page=ChannelUtil.fetchPage(urlobj.openConnection());
					} catch (IOException e) {
					}
					if(ChannelUtil.empty(page)) // still empty
						return;
				}
				else
					return;
			}
			if(cache&&!cFile.exists()) {
				cFile.delete();
				try {
					ChannelUtil.downloadText(new ByteArrayInputStream(page.getBytes()), cFile);
				} catch (Exception e) {
				}
			}
			else if(oldCache) 
				cFile.delete();
		}
		int form=format;
		//Channels.debug("format before flipp "+format);
		if(format==-1) // we don't know our format better get it
			if(parentFolder!=null)
				format=parentFolder.getFormat();
			else
				format=parent.getMediaFormat();
		//Channels.debug("format after flipp "+format);
		ArrayList<ChannelMedia> med=medias;
		ArrayList<ChannelItem> ite=items;
		ArrayList<ChannelFolder> fol=subfolders;
		ArrayList<ChannelSwitch> swi=switches;
		if(type==ChannelFolder.TYPE_RECURSE) {
			med=parentFolder.medias;
			ite=parentFolder.items;
			fol=parentFolder.subfolders;
			swi=parentFolder.switches;
		}
		// 1st Media
		// Channels.debug("matching media "+medias.size());
		DLNAResource allPlay=findAllPlay(res,"PLAY");
		DLNAResource allSave=findAllPlay(res,"SAVE&PLAY");
		int medCnt=0;
		HashMap<String,String> uniqueMedia = new HashMap<String,String>();   // from matched name to matched URL
    	boolean discardDuplicates = ChannelUtil.getProperty(prop, "discard_duplicates");
	    for(int i=0;i<med.size();i++) {
	    	ChannelMedia m1=med.get(i);
	    	ChannelMatcher m=m1.getMatcher();
	    	Channels.debug("media "+m+" sc "+m1.scriptOnly());
	    	if(m==null) { // no matcher => static media
	    		if(Channels.isIllegal(nName, ChannelIllegal.TYPE_NAME))
	    				continue;
	    		String thumb=ChannelUtil.getThumb(null, pThumb, parent);
	    		if(allPlay==null&&Channels.cfg().allPlay()) {
	    			allPlay=new ChannelPMSAllPlay("PLAY",pThumb);
	    			res.addChild(allPlay);
	    			if(Channels.save()) {
	    				allSave=new ChannelPMSAllPlay("SAVE&PLAY",pThumb);
	    				res.addChild(allSave);
	    			}
	    		}
	    		String ru=(m1.scriptOnly()?realUrl:null);
	    		if(Channels.isIllegal(ru, ChannelIllegal.TYPE_URL))
	    			continue;
	    		boolean asx=ChannelUtil.getProperty(prop, "auto_asx");
	    		if(ChannelUtil.empty(imdb))
	    			imdb=imdbId;
	    		m1.add(res, nName, ru, thumb, asx,imdb);
	    		medCnt++;
	    		if(allPlay!=null) {
	    			m1.add(allPlay, nName,ru, thumb, asx,ChannelMedia.SAVE_OPT_PLAY);
	    			if(Channels.save())
	    				m1.add(allSave, nName,ru, thumb, asx,ChannelMedia.SAVE_OPT_SAVE);
	    		}
	    		continue;
	    	}
	    	// we could match the video format here
	    	m.startMatch(page);
	    	parent.debug("media matching using "+m.getRegexp().pattern()+" emb sub "+embSubs);
	    	while(m.match()) {
	    		Channels.debug("allplay "+allPlay+" cfg "+Channels.cfg().allPlay());
	    		if(allPlay==null&&Channels.cfg().allPlay()) {
	    			allPlay=new ChannelPMSAllPlay("PLAY",pThumb);
	    			res.addChild(allPlay);
	    			if(Channels.save()) {
	    				allSave=new ChannelPMSAllPlay("SAVE&PLAY",pThumb);
	    				res.addChild(allSave);
	    			}
	    		}
	    		String someName=m.getMatch("name",false);
	    		if(Channels.isIllegal(someName, ChannelIllegal.TYPE_NAME))
	    			continue;
	    		if(filter!=null&&!filter.filter(someName))
	    			continue;
	    		String mUrl=m.getMatch("url",true);
	    		mUrl=ChannelUtil.relativeURL(mUrl,realUrl,m1.relativeURL());
	    		if(Channels.isIllegal(mUrl, ChannelIllegal.TYPE_URL))
	    			continue;
	    		if (discardDuplicates) {
	    			if (uniqueMedia.containsKey(someName)) {
	    				/*String storedUrl=uniqueMedia.get(someName);
	    				if(storedUrl.equals(mUrl))*/
	    					continue;
	    			}
	    			uniqueMedia.put(someName, mUrl);
	    		}
	    		String thumb=m.getMatch("thumb",false);
	    		String imdbId=m.getMatch("imdb",false);
	    		String subs=m.getMatch("subs", false);
	    		String playpath=m.getMatch("playpath",false);
	    		String swfplayer=m.getMatch("swfVfy",false);
	    		String swfUrl=m.getMatch("swfUrl",false);
	    		String pageUrl=m.getMatch("pageUrl",false);
	    		String app=m.getMatch("app",false);
	    		thumb=ChannelUtil.getThumb(thumb, pThumb, parent);
	    		if(ChannelUtil.empty(someName))
	    			someName=nName;
	    		if(ChannelUtil.empty(imdbId))
	    			imdbId=imdb;
	    		Channels.debug("set fb format "+videoFormat+" url "+mUrl);
	    		m1.setFallBackFormat(videoFormat);
    			boolean asx=ChannelUtil.getProperty(prop, "auto_asx");
    			HashMap<String,String> map=new HashMap<String,String>();
    			if(!ChannelUtil.empty(swfUrl))
    				map.put("swfurl", swfUrl);
    			if(!ChannelUtil.empty(playpath))
    				map.put("playpath", playpath);
    			if(!ChannelUtil.empty(swfplayer))
    				map.put("swfVfy", swfplayer);
    			if(!ChannelUtil.empty(pageUrl))
    				map.put("pageurl", pageUrl);
    			if(!ChannelUtil.empty(app))
    				map.put("app", app);
    			if(ChannelUtil.empty(subs))
    				subs=embSubs;
    			if(Channels.isCode(someName, ChannelIllegal.TYPE_NAME)||
    			   Channels.isCode(mUrl, ChannelIllegal.TYPE_URL)) {
    				ChannelPMSCode addOn=new ChannelPMSCode(someName,thumb);
    				m1.add(addOn, someName, mUrl, thumb,
    	    				asx,imdbId,format,ChannelMedia.SAVE_OPT_NONE,subs,map);
    				m1.stash("playpath",playpath);
    	    		m1.stash("swfVfy",swfplayer);
    				res.addChild(addOn);
    				continue;
    			}
	    		m1.add(res, someName, mUrl, thumb,
	    				asx,imdbId,format,ChannelMedia.SAVE_OPT_NONE,subs,map);
	    		m1.stash("playpath",playpath);
	    		m1.stash("swfVfy",swfplayer);
	    		medCnt++;
	    		if(allPlay!=null) {
	    			m1.add(allPlay, someName, mUrl, thumb,
		    				asx,imdbId,format,ChannelMedia.SAVE_OPT_PLAY,subs, null);
	    			if(Channels.save())
	    				m1.add(allSave, someName, mUrl, thumb,
	    	    				asx,imdbId,format,ChannelMedia.SAVE_OPT_SAVE,subs, null);
	    		}
	    		if(m1.onlyFirst())
	    			break;
	    	}
	    } 
	    // 2nd items
		// PMS.debug("items "+items.size());
	    for(int i=0;i<ite.size();i++) {
	    	ChannelItem item=ite.get(i);
	    	ChannelMatcher m=item.getMatcher();
	    	m.startMatch(page);
	    	parent.debug("item matching using expr "+m.getRegexp().pattern());
	    	while(m.match()) {
	    		String someName=m.getMatch("name",false);
	    		//if(filter!=null&&!filter.filter(someName))
	    			//continue;
	    		String iUrl=m.getMatch("url",true);
	    		if(Channels.isIllegal(someName, ChannelIllegal.TYPE_NAME)||
	    		   Channels.isIllegal(iUrl, ChannelIllegal.TYPE_URL))
	    		   continue;
	    		String thumb=m.getMatch("thumb",false);
	    		thumb=ChannelUtil.getThumb(thumb, pThumb, parent);
	    		PMS.debug("found item "+someName+" url "+iUrl);
	    		if(ChannelUtil.empty(someName))
	    			someName=nName;
	    		//iUrl=ChannelUtil.appendData(iUrl,prop,"url");
	    		if(item.autoMedia())
	    			item.match(res,null,iUrl,someName,thumb);
	    		else
	    			res.addChild(new ChannelPMSItem(item,someName,null,iUrl,thumb));
	    		if(item.onlyFirst())
	    			break;
	    	}
	    } 
	    // 3rd channel switches
	    for(int i=0;i<swi.size();i++) {
	    	ChannelSwitch sw=swi.get(i);
	    	Channel ch=Channels.findChannel(sw.getName());
	    	if(ch==null)
	    		continue;
	    	ChannelMatcher m=sw.matcher;
	    	m.startMatch(page);
	    	parent.debug("switch matching using expr "+m.getRegexp().pattern());
	    	while(m.match()) {
	    		String someName=m.getMatch("name",false);
	    		if(Channels.isIllegal(someName, ChannelIllegal.TYPE_NAME))
	 	    		   continue;
	    		if(filter!=null&&!filter.filter(someName))
	    			continue;
	    		String fUrl=m.getMatch("url",true);
	    		if(Channels.isIllegal(fUrl, ChannelIllegal.TYPE_URL))
	    			continue;
	    		String thumb=m.getMatch("thumb",false);
	    		parent.debug("matched "+someName+" url "+fUrl);
	    		ChannelPMSSwitch csp=new ChannelPMSSwitch(ch,sw,someName,null,fUrl,thumb);
	    		csp.setFormat(format);
	    		res.addChild(csp);
	    	}
	    }
	    // last but not least folders
		parent.debug("subfolders "+fol.size());
	    for(int i=0;i<fol.size();i++) {
	    	ChannelFolder cf=fol.get(i);
	    	ChannelMatcher m=cf.matcher;
	    	if(cf.isActionOnly()) // pure action folders are skipped
	    		continue;
	    	if(cf.isATZ()) {
    			res.addChild(new ChannelATZ(cf,urlEnd));
    			continue;
    		}
	    	if(cf.isSearch()) { // search folder
	    		Channels.debug("search folder");
	    		res.addChild(new SearchFolder(cf.name,cf));
	    		continue;
	    	}
	    	if(cf.isNaviX()) { // i'm navix special handling
				realUrl=ChannelUtil.concatURL(cf.url,urlEnd);	
				ChannelNaviX nx=new ChannelNaviX(parent,cf.name,ChannelUtil.getThumb(null,pThumb, parent),
						  realUrl,cf.prop,cf.sub);
				if(ignoreFav)
					nx.setIgnoreFav();
				res.addChild(nx);
				continue;
			}
	    	if(m==null) {
	    		parent.debug("nested static folder");
	    		ChannelPMSFolder cpf=new ChannelPMSFolder(cf,cf.name);
				cpf.setImdb(ChannelUtil.empty(imdb)?cf.imdbId:imdb);
				res.addChild(cpf);
	    		continue;
	    	}	
	    	m.startMatch(page);
	    	parent.debug("folder matching using expr "+m.getRegexp().pattern());
	    	HashMap<String,ArrayList<ChannelPMSFolder>> groups=new HashMap<String,ArrayList<ChannelPMSFolder>>();
	    	HashMap<String,String> uniqueNames = new HashMap<String,String>();   // from matched name to matched URL
	    	//boolean discardDuplicates = ChannelUtil.getProperty(prop, "discard_duplicates");
	    	boolean ignoreMatch = ChannelUtil.getProperty(prop, "ignore_match");
	    	while(m.match()) {
	    		String someName=m.getMatch("name",false);
	    		if(filter!=null&&!filter.filter(someName))
	    			continue;
	    		String fUrl=m.getMatch("url",true);
	    		if(Channels.isIllegal(fUrl, ChannelIllegal.TYPE_URL))
	 	    		   continue;
	    		String thumb=m.getMatch("thumb",false);
	    		String group=m.getMatch("group",false);
	    		String imdbId=m.getMatch("imdb",false);
	    		String subs=m.getMatch("subs",false);
	    		if(ChannelUtil.empty(subs))
	    			subs=embSubs;
	    		if(ChannelUtil.empty(imdbId)) {
	    			imdbId=imdb;
	    			if(ChannelUtil.empty(thumb))
	    				thumb=pThumb; // we know that will use this thumb so save a scrape here
	    		}
	    		if(ChannelUtil.empty(thumb)&&
	    		   !ChannelUtil.empty(imdbId)) {
	    			thumb=imdbId;
	    			cf.thumb_script="imdbThumb";
	    		}
	    		else {
	    			thumb=ChannelUtil.getThumb(thumb, pThumb, parent);
	    			
	    		}
	    		parent.debug("matching "+someName+" url "+fUrl+" thumb "+thumb+" group "+group+" imdb "+imdbId);
	    		if(ignoreMatch)
    				someName=cf.name;
	    		if(ChannelUtil.empty(someName)) {
	    			if(ChannelUtil.empty(cf.name))
	    				someName=nName;
	    			else
	    				someName=cf.name;
	    			someName=m.pend(someName, "name");
	    		}
	    		if(ChannelUtil.getProperty(cf.prop, "bad")) {
	    			res.addChild(ChannelResource.redX(someName));
	    			return;
	    		}
	    		if(Channels.isIllegal(someName, ChannelIllegal.TYPE_NAME))
	 	    		   continue;
	    		//parent.debug("cf.name "+cf.name+" ignore "+ignoreMatch);
	    		if(ChannelUtil.getProperty(cf.prop, "prepend_parenturl"))
	    			fUrl=ChannelUtil.concatURL(realUrl,fUrl);
	    		fUrl=ChannelUtil.relativeURL(fUrl,realUrl,
						 					 ChannelUtil.getPropertyValue(prop, "relative_url"));
	    		fUrl=ChannelScriptMgr.runScript(post_script, fUrl, parent,page);
	    		//Channels.debug("xxxx "+fUrl+" disc "+discardDuplicates);
	    		if (discardDuplicates && !ignoreMatch) {
	    			if (uniqueNames.containsKey(someName)) {
	    				String uniqueURL = uniqueNames.get(someName);
	    				if (uniqueURL.equalsIgnoreCase(fUrl)) 
	    					continue;
	    			}
	    			uniqueNames.put(someName, fUrl);
	    		}
	    		PeekRes pr=cf.peek(fUrl,prop);
	    		//Channels.debug("peek re "+pr.res);
	    		if(!pr.res)
	    			continue;
	    		cf.ignoreFav=ignoreFav; // forward this along the path
	    		if(!ChannelUtil.empty(pr.thumbUrl))
	    			thumb=pr.thumbUrl;
	    		if(!ChannelUtil.empty(cf.group)&&Channels.useGroupFolder()) {
	    			ChannelPMSFolder cpf=new ChannelPMSFolder(cf,someName,null,fUrl,thumb);
	    			ArrayList<ChannelPMSFolder> groupData=groups.get(cf.group);
	    			if(groupData==null) 
	    				groupData=new ArrayList<ChannelPMSFolder>();
	    			groupData.add(cpf);
	    			groups.put(group, groupData);
	    			continue;
	    		}
	    		if(doContinue(someName,fUrl)) {
	    			cf.match(res,null,fUrl,thumb,someName);
	    			return;
	    		}
	    		if(cf.type==ChannelFolder.TYPE_EMPTY) {
	    			cf.match(res,null,fUrl,thumb,someName,imdbId,subs);
	    		}
	    		else if(cf.type==ChannelFolder.TYPE_EXEC) {
	    			final String vvaUrl=fUrl;
	    			final ChannelFolder fcf=cf;
	    			final DLNAResource myRes=res;
	    			res.addChild(new VirtualVideoAction(someName,true) {
	    				public boolean enable() {
	    					try {
								fcf.match(myRes,null,vvaUrl,null,null,null);
							} catch (MalformedURLException e) {
								Channels.debug("bad match for exec folder "+e);
							}
							return true;
	    				}
	    			});
	    		}
	    		else {
	    			Channels.debug("add "+someName+" furl "+fUrl);
	    			ChannelPMSFolder cpf=new ChannelPMSFolder(cf,someName,null,fUrl,thumb);
	    			cpf.setImdb(imdbId);
	    			cpf.setEmbSubs(subs);
	    			res.addChild(cpf);
	    		}
	    		if(cf.onlyFirst())
	    			break;
	    	}
	    	// Only do this if we got more than one media
	    	// 1st find allPlay and allSave folders
	    	allPlay=findAllPlay(res,"PLAY");
	    	allSave=findAllPlay(res,"SAVE&PLAY");
	    	// Remove them
	    	if(allPlay!=null) {
	    		ChannelPMSAllPlay a=(ChannelPMSAllPlay)allPlay;
	    		a.clearID();
	    		res.getChildren().remove(allPlay);
	    	}
	    	if(allSave!=null) {
	    		ChannelPMSAllPlay a=(ChannelPMSAllPlay)allSave;
	    		a.clearID();
	    		res.getChildren().remove(allSave);
	    	}
	    	// If we got more than one left...
	    	if(numberOfMedia(res)>1) {
	    		if(allPlay!=null)
	    			res.addChild(allPlay);
	    		if(allSave!=null)
	    			res.addChild(allSave);
	    	}
	    	if(!groups.isEmpty()) { // we got groups
	    		Object[] keySet=groups.keySet().toArray();
	    		Arrays.sort(keySet);  
	            for(int j=0;j<keySet.length;j++)   {  
	            	String key=(String) keySet[j];
	    			ArrayList<ChannelPMSFolder> groupData=groups.get(key);
	    			ChannelPMSFolder pfold=groupData.get(0);
	    			String thumb=pfold.getThumb();
	    			if(groupData.size()==1) { // only one, thats not a group
	    				ChannelFolder cf1=pfold.getFolder();
	    				cf1.group=null; // clear group
	    				res.addChild(pfold);
	    			}
	    			else 
	    				res.addChild(new ChannelGroupFolder(nName+" #"+key,groupData,thumb));
	    		}
	    	}
	    }
	    format=form;
	}
	
	public String getThumb() { // relic method just return parents thumb
		if(!ChannelUtil.empty(staticThumb))
			return staticThumb;
		if(parent!=null)
			return parent.getThumb();
		return null;
	}
	
	public String separator(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_separator");
	}
	
	public boolean onlyFirst() {
		return ChannelUtil.getProperty(prop, "only_first");
	}
	
	public String append(String base) {
		return ChannelUtil.getPropertyValue(prop,"append_"+base);
	}
	public String prepend(String base) {
		return ChannelUtil.getPropertyValue(prop,"prepend_"+base);
	}

	@Override
	public void search(String searchString, DLNAResource searcher) {
		try {
			Channels.debug("do search "+searchString+" ignorefav "+ignoreFav());
			Channels.addSearch(parent, searchId, searchString);
			searchString=ChannelUtil.escape(searchString);
			searchString=ChannelUtil.append(ChannelUtil.getPropertyValue(prop, "prepend_url"),null,
							   ChannelUtil.append(searchString, null, ChannelUtil.getPropertyValue(prop, "append_url")));
			match(searcher,null,searchString,"","");
		} catch (MalformedURLException e) {
		}
	}
	
	 
	
	public String thumbScript() {
		return thumb_script;
	}
	
	public Channel getChannel() {
		return parent;
	}
	
	public boolean otherChar(char c) {
		String p=getProp("locals");
		if(!ChannelUtil.empty(p)) {
			for(int j=0;j<p.length();j++)
				if(p.charAt(j)==c)
					return true;
		}
		return !((c >= 'A' && c <= 'Z')||(c >= 'a' && c <= 'z'));
	}
	
	public void addMovieInfo(DLNAResource res,String imdb,String thumb) {
		if(Channels.useMovieInfo()&&
		   !ChannelUtil.empty(imdb)&&
		   ChannelUtil.getProperty(prop, "movieinfo")) {
			res.addChild(new ChannelMovieInfoFolder(imdb,thumb));
		}
	}
	
	public String rawEntry() {
		StringBuilder sb=new StringBuilder();
		sb.append("folder {\n");
		if(!ChannelUtil.empty(url)) {
			sb.append("url=");
			sb.append(url);
			sb.append("\n");
		}
		if(!ChannelUtil.empty(name)) {
			sb.append("name=");
			sb.append(name);
			sb.append("\n");
		}
		sb.append("type=");
		sb.append(ChannelUtil.type2str(type));
		sb.append("\n");
		
		if(matcher!=null) {
			sb.append("matcher=");
			sb.append(matcher.regString());
			sb.append("\n");
			matcher.orderString(sb);
			sb.append("\n");
		}
		if(prop!=null) {
			sb.append("prop=");
			ChannelUtil.list2file(sb,prop);
			sb.append("\n");
		}
		if(!ChannelUtil.empty(post_script)) {
			sb.append("post_script=");
			sb.append(post_script);
			sb.append("\n");
		}
		if(!ChannelUtil.empty(pre_script)) {
			sb.append("pre_script=");
			sb.append(pre_script);
			sb.append("\n");
		}
		if(sub!=null) {
			sb.append("subtitle=");
			ChannelUtil.list2file(sb,sub);
			sb.append("\n");
		}
		for(int i=0;i<medias.size();i++) {
			ChannelMedia m=medias.get(i);
			sb.append(m.rawEntry());
			sb.append("\n");
		}
		for(int i=0;i<items.size();i++) {
			ChannelItem m=items.get(i);
			sb.append(m.rawEntry());
			sb.append("\n");
		}
		for(int i=0;i<subfolders.size();i++) {
			ChannelFolder cf=subfolders.get(i);
			sb.append(cf.rawEntry());
			sb.append("\n}\n");
		}
		return sb.toString();
	}
	
	private String mkFavEntry(String urlEnd,String name,String thumb,String imdb) {
		StringBuilder sb=new StringBuilder();
		String realUrl=ChannelUtil.concatURL(url,urlEnd);
		realUrl=ChannelScriptMgr.runScript(pre_script, realUrl, parent);
		sb.append("favorite {\n");
		sb.append("owner=");
		sb.append(parent.name());
		sb.append("\n");
		sb.append("folder {\n");
		if(!ChannelUtil.empty(realUrl)) {
			sb.append("url=");
			sb.append(realUrl);
			sb.append("\n");
		}
		if(!ChannelUtil.empty(name)) {
			sb.append("name=");
			sb.append(name);
			sb.append("\n");
		}
		if(!ChannelUtil.empty(thumb)) {
			sb.append("thumb=");
			sb.append(thumb);
			sb.append("\n");
		}
		if(!ChannelUtil.empty(imdb)) {
			sb.append("imdb=");
			sb.append(imdb);
			sb.append("\n");
		}
		if(prop!=null) {
			sb.append("prop=");
			ChannelUtil.list2file(sb,prop);
			sb.append("\n");
		}
		if(sub!=null) {
			sb.append("subtitle=");
			ChannelUtil.list2file(sb,sub);
			sb.append("\n");
		}
		for(int i=0;i<medias.size();i++) {
			ChannelMedia m=medias.get(i);
			sb.append(m.rawEntry());
			sb.append("\n");
		}
		for(int i=0;i<items.size();i++) {
			ChannelItem m=items.get(i);
			sb.append(m.rawEntry());
			sb.append("\n");
		}
		for(int i=0;i<switches.size();i++) {
			ChannelSwitch m=switches.get(i);
			sb.append(m.rawEntry());
			sb.append("\n");
		}
		for(int i=0;i<subfolders.size();i++) {
			ChannelFolder cf=subfolders.get(i);
			sb.append(cf.rawEntry());
			sb.append("\n}\n");
		}
		sb.append("\n}\n");
		sb.append("\n}\r\n");
		return sb.toString();	
	}
	
	public String mkFav(String urlEnd,String name,String thumb,String imdb) {
		ChannelFolder copy=new ChannelFolder(this);
		copy.matcher=null;
		copy.staticThumb=thumb;
		copy.imdbId=imdb;
		copy.name=name;
		copy.url=ChannelUtil.concatURL(url,urlEnd);
		parent.addFavorite(copy);
		return mkFavEntry(urlEnd,name,thumb,imdb);
	}
	
	public Boolean isFavItem() {
		return parentFolder != null && parentFolder == parent.favorite();
	}

	public ArrayList<ChannelFolder> subfolders() {
		return subfolders;
	}

	public void remove() {
		if(parentFolder != null)
			parentFolder.subfolders().remove(this);
	}
	
	public void action(DLNAResource res,ChannelFilter filter,String urlEnd,
			String pThumb,String nName,String imdb,int f) throws MalformedURLException {
		String[] old_prop=prop;
		int old_format=format;
		if(f!=-1)
			format=f;
		Channels.debug("action called "+format);
		if(action_prop!=null)
			prop=action_prop;
		match(res,filter,urlEnd,pThumb,nName,imdb);
		prop=old_prop;
		format=old_format;
	}
	
	public String fallBackVideoFormat() {
		return videoFormat;
	}
	
	@Override
	public boolean escape(String base) {
		return ChannelUtil.getProperty(prop, base+"_escape");

	}

	@Override
	public boolean unescape(String base) {
		return ChannelUtil.getProperty(prop, base+"_unescape");
	}
	
	public String mangle(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_mangle");
	}
	
	////////////////////////////////////////////////////////////////
	// Cahceing folders
	////////////////////////////////////////////////////////////////
	
	private File cacheFile() {
		String n=name;
		if(url!=null)
			n=""+url.hashCode();
		return new File(Channels.dataPath()+File.separator+parent.name()+"_"+n);
	}
	
	public void addCacheClear(DLNAResource res) {
		addCacheClear(res,cacheFile());
	}
	
	private void addCacheClear(DLNAResource res,final File f) {
		res.addChild(new VirtualVideoAction("Force update", true) {
			public boolean enable() {
				f.delete();
				return true;
			}
		});
	}
	
	private boolean oldCache(String time,File f) {
		if(time.equals("0")) // never age this entry
			return false;
		int t;
		try {
			t=Integer.parseInt(time);
			
		} catch (Exception e) {
			t=7;
		}
		Calendar c=Calendar.getInstance();
		Calendar d=Calendar.getInstance();
		d.setTimeInMillis(f.lastModified());
		d.add(Calendar.DATE, t);
		return c.after(d);
	}
	
	private ChannelPMSAllPlay findAllPlay(DLNAResource res,String name) {
		for(DLNAResource tmp : res.getChildren()) {
			if(tmp instanceof ChannelPMSAllPlay) {
				if(tmp.getName().startsWith(name))
					return (ChannelPMSAllPlay) tmp;
			}				
		}
		return null;
	}
	
	private int numberOfMedia(DLNAResource res) {
		int cnt=0;
		for(DLNAResource tmp : res.getChildren()) {
			if(tmp instanceof ChannelMediaStream)
				cnt++;
			if(tmp instanceof ChannelPMSSaveFolder)
				cnt++;
		}
		return cnt;
	}
}
