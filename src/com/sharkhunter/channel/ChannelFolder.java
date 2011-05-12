package com.sharkhunter.channel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;

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
	
	public boolean Ok;
	
	private String name;
	private String url;
	private String format;
	private int type;
	private String[] prop;
	
	private ChannelMatcher matcher;
	
	private ArrayList<ChannelFolder> subfolders;
	private Channel parent;
	private ChannelFolder parentFolder;
	private ArrayList<ChannelItem> items;
	private ArrayList<ChannelMedia> medias; 
	
	private int continues;
	private boolean contAll;
	
	private String[] sub;
	
	private String searchId;
	private String urlScript;
	
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
		contAll=false;
		continues=Channels.DeafultContLim;
		parse(data);
		continues=ChannelUtil.calcCont(prop);
		if(continues<0)
			contAll=true;
		if(isSearch())
			setSearchId();
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
			if(keyval[0].equalsIgnoreCase("format"))
				format=keyval[1];
			if(keyval[0].equalsIgnoreCase("prop"))	
				prop=keyval[1].trim().split(",");
			if(keyval[0].equalsIgnoreCase("matcher")) {
				if(matcher==null)
					matcher=new ChannelMatcher(keyval[1],null,this);
				else
					matcher.setMatcher(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("order")) {
				if(matcher==null)
					matcher=new ChannelMatcher(null,keyval[1],this);
				else
					matcher.setOrder(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("subtitle")) {
				sub=keyval[1].split(",");
			}
			if(keyval[0].equalsIgnoreCase("url_script")) {
				urlScript=keyval[1];
			}
		}
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
	
	public String getProp(String p) {
		return ChannelUtil.getPropertyValue(prop, p);
	}
	
	public String[] getPropList() {
		return prop;
	}
	
	public String[] getSubs() {
		return sub;
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
		Channels.debug("peek3");
		String realUrl=ChannelUtil.concatURL(url,urlEnd);
		String page="";
		if(!ChannelUtil.empty(realUrl)) {
			URL urlobj;
			try {
				urlobj=new URL(realUrl);
				page=ChannelUtil.fetchPage(urlobj.openConnection(),parent.getAuth(),null);
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
		match(res,null,"",null,null);
	}
	
	public void match(DLNAResource res,ChannelFilter filter,String urlEnd,
			String pThumb,String nName) throws MalformedURLException {
		String page="";
		if(filter==null&&matcher==null&&type==ChannelFolder.TYPE_NORMAL) { // static folder
			// static folders are not subject to filter
			parent.debug("static folder");
			res.addChild(new ChannelPMSFolder(this,name));
			return;
		}
		boolean post=false;
		String methodProp=ChannelUtil.getPropertyValue(prop, "http_method");
		if(methodProp!=null&&methodProp.equalsIgnoreCase("post"))
			post=true;
		String realUrl=url;		
		if(isNaviX()) { // i'm navix special handling
			realUrl=ChannelUtil.concatURL(url,urlEnd);	
			res.addChild(new ChannelNaviX(parent,name,ChannelUtil.getThumb(null,pThumb, parent),
										  realUrl,prop,sub));
			return;
		}
		if(isSearch()) {
			post=true;
			if(methodProp!=null&&!methodProp.equalsIgnoreCase("post"))
				post=false;
		}
		if(!post)
			realUrl=ChannelUtil.concatURL(url,urlEnd);	
		if(!ChannelUtil.empty(realUrl)) {
			URL urlobj=new URL(realUrl);
			parent.debug("folder match url "+urlobj.toString()+" type "+type+" post "+post+" "+urlEnd);
			try {
				if(post) 
					page=ChannelUtil.postPage(urlobj.openConnection(), urlEnd);
				else
					page=ChannelUtil.fetchPage(urlobj.openConnection(),parent.getAuth(),null);
			} catch (Exception e) {
				page="";
			}
			parent.debug("page "+page);
			if(ChannelUtil.empty(page))
				return;
		}
		ArrayList<ChannelMedia> med=medias;
		ArrayList<ChannelItem> ite=items;
		ArrayList<ChannelFolder> fol=subfolders;
		if(type==ChannelFolder.TYPE_RECURSE) {
			med=parentFolder.medias;
			ite=parentFolder.items;
			fol=parentFolder.subfolders;
		}
		// 1st Media
		 //PMS.debug("matching media "+medias.size());
	    for(int i=0;i<med.size();i++) {
	    	ChannelMedia m1=med.get(i);
	    	ChannelMatcher m=m1.getMatcher();
	    	if(m==null) { // no matcher => static media
	    		String thumb=ChannelUtil.getThumb(null, pThumb, parent);
	    		m1.add(res,nName,null,thumb,ChannelUtil.getProperty(prop, "auto_asx"));
	    		continue;
	    	}
	    	m.startMatch(page);
	    	while(m.match()) {
	    		String someName=m.getMatch("name",false);
	    		//if(filter!=null&&!filter.filter(someName))
	    			//continue;
	    		String mUrl=m.getMatch("url",true);
	    		String thumb=m.getMatch("thumb",false);
	    		String playpath=m.getMatch("playpath",false);
	    		String swfplayer=m.getMatch("swfVfy",false);
	    		thumb=ChannelUtil.getThumb(thumb, pThumb, parent);
	    		parent.debug("media matching using "+m.getRegexp().pattern());
	    		if(ChannelUtil.empty(someName))
	    			someName=nName;
	    		m1.add(res, someName, mUrl, thumb,ChannelUtil.getProperty(prop, "auto_asx"));
	    		m1.stash("playpath",playpath);
	    		m1.stash("swfVfy",swfplayer);
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
	    // last but not least folders
		parent.debug("subfolders "+fol.size());
	    for(int i=0;i<fol.size();i++) {
	    	ChannelFolder cf=fol.get(i);
	    	ChannelMatcher m=cf.matcher;
	    	if(cf.isATZ()) {
    			res.addChild(new ChannelATZ(cf,urlEnd));
    			continue;
    		}
	    	if(cf.isSearch()) { // search folder
	    		Channels.debug("search folder");
	    		res.addChild(new SearchFolder(cf.name,cf));
	    		continue;
	    	}
	    	if(m==null) {
	    		parent.debug("nested static folder");
	    		res.addChild(new ChannelPMSFolder(cf,cf.name));
	    		continue;
	    	}	
	    	m.startMatch(page);
	    	parent.debug("folder matching using expr "+m.getRegexp().pattern());
	    	while(m.match()) {
	    		String someName=m.getMatch("name",false);
	    		if(filter!=null&&!filter.filter(someName))
	    			continue;
	    		String fUrl=m.getMatch("url",true);
	    		String thumb=m.getMatch("thumb",false);
	    		thumb=ChannelUtil.getThumb(thumb, pThumb, parent);
	    		parent.debug("matching "+someName+" url "+fUrl+" thumb "+thumb);
	    		if(ChannelUtil.empty(someName))
	    				someName=nName;//(!ChannelUtil.empty(cf.name)?cf.name:nName);
	    		parent.debug("cf.name "+cf.name+" ignore "+ChannelUtil.getProperty(cf.prop, "ignore_match"));
	    		if(ChannelUtil.getProperty(cf.prop, "ignore_match"))
	    				someName=cf.name;
	    		if(ChannelUtil.getProperty(cf.prop, "prepend_parenturl"))
	    			fUrl=ChannelUtil.concatURL(realUrl,fUrl);
	    		fUrl=ChannelNaviXProc.simple(fUrl, urlScript);
	    		PeekRes pr=cf.peek(fUrl,prop);
	    		if(!pr.res)
	    			continue;
	    		if(!ChannelUtil.empty(pr.thumbUrl))
	    			thumb=pr.thumbUrl;
	    		if(doContinue(someName,fUrl)) {
	    			cf.match(res,null,fUrl,thumb,someName);
	    			return;
	    		}
	    		if(cf.type==ChannelFolder.TYPE_EMPTY)
	    			cf.match(res,null,fUrl,thumb,someName);
	    		else
	    			res.addChild(new ChannelPMSFolder(cf,someName,null,fUrl,thumb));
	    		if(cf.onlyFirst())
	    			break;
	    	}
	    }
	}
	
	public String getThumb() { // relic method just return parents thumb
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

	@Override
	public void search(String searchString, DLNAResource searcher) {
		try {
			Channels.debug("do search "+searchString);
			Channels.addSearch(parent, searchId, searchString);
			match(searcher,null,searchString,"","");
		} catch (MalformedURLException e) {
		}
	}
			
}
