package com.sharkhunter.channel;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;

public class ChannelFolder implements ChannelProps{
	public static final int TYPE_NORMAL=0;
	public static final int TYPE_ATZ=1;
	public static final int TYPE_EMPTY=2;
	public static final int TYPE_LOGIN=3;
	public static final int TYPE_ATZ_LINK=4;
	public static final int TYPE_NAVIX=5;
	
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
		parse(data);
		Ok=true;
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
			String[] keyval=line.split("=",2);
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
		return (type==ChannelFolder.TYPE_NAVIX);
	}
	
	public String getProp(String p) {
		return ChannelUtil.getPropertyValue(prop, p);
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
		return ChannelFolder.TYPE_NORMAL;
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
		String realUrl=ChannelUtil.concatURL(url,urlEnd);
		if(isNaviX()) { // i'm navix special handling
			res.addChild(new ChannelNaviX(parent,name,getThumb(),realUrl));
			return;
		}
		if(realUrl!=null&&realUrl.length()!=0) {
			URL urlobj=new URL(realUrl);
			parent.debug("folder match url "+urlobj.toString()+" type "+type);
			page=ChannelUtil.fetchPage(urlobj,parent.getAuth());
			parent.debug("page "+page);
			if(page==null||page.length()==0)
				return;
		}
		parent.debug("subfolders "+subfolders.size());
	    for(int i=0;i<subfolders.size();i++) {
	    	ChannelFolder cf=subfolders.get(i);
	    	ChannelMatcher m=cf.matcher;
	    	if(cf.isATZ()) {
    			res.addChild(new ChannelATZ(cf,urlEnd));
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
	    		if(someName==null||someName.length()==0)
	    			someName=nName;
	    		//PMS.debug("folder type "+cf.type);
	    		//fUrl=ChannelUtil.appendData(fUrl,prop,"url");
	    		if(cf.type==ChannelFolder.TYPE_EMPTY)
	    			cf.match(res,null,fUrl,thumb,someName);
	    		else
	    			res.addChild(new ChannelPMSFolder(cf,someName,null,fUrl,thumb));
	    	}
	    }
	   // PMS.debug("items "+items.size());
	    for(int i=0;i<items.size();i++) {
	    	ChannelItem item=items.get(i);
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
	    		if(someName==null||someName.length()==0)
	    			someName=nName;
	    		//iUrl=ChannelUtil.appendData(iUrl,prop,"url");
	    		if(item.autoMedia())
	    			item.match(res,null,iUrl,someName,thumb);
	    		else
	    			res.addChild(new ChannelPMSItem(item,someName,null,iUrl,thumb));
	    	}
	    } 
	    //PMS.debug("matching media "+medias.size());
	    for(int i=0;i<medias.size();i++) {
	    	ChannelMedia m1=medias.get(i);
	    	ChannelMatcher m=m1.getMatcher();
	    	m.startMatch(page);
	    	while(m.match()) {
	    		String someName=m.getMatch("name",false);
	    		//if(filter!=null&&!filter.filter(someName))
	    			//continue;
	    		String mUrl=m.getMatch("url",true);
	    		String thumb=m.getMatch("thumb",false);
	    		thumb=ChannelUtil.getThumb(thumb, pThumb, parent);
	    		parent.debug("media matching using "+m.getRegexp().pattern());
	    		//PMS.debug("found media "+someName+" url "+mUrl);
	    		if(someName==null||someName.length()==0)
	    			someName=nName;
	    		//mUrl=ChannelUtil.appendData(mUrl,prop,"url");
	    		m1.add(res, someName, mUrl, thumb);
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
			
}
