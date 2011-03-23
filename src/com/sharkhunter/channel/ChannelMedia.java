package com.sharkhunter.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;

public class ChannelMedia implements ChannelProps,ChannelScraper {
	public static final int TYPE_NORMAL=0;
	public static final int TYPE_ASX=1;
	
	public static final int SCRIPT_LOCAL=0;
	public static final int SCRIPT_NET=1;
	public static final int SCRIPT_EXT=2;
	
	public boolean Ok;
	private ChannelMatcher matcher;
	private String name;
	private Channel parent;
	private String[] prop;
	private String thumbURL;
	private String script;
	private int scriptType;
	private int type;
	private HashMap<String,String> params;
	private String subtitle;
	
	public ChannelMedia(ArrayList<String> data,Channel parent) {
		Ok=false;
		matcher=null;
		this.parent=parent;
		type=ChannelMedia.TYPE_NORMAL;
		script=null;
		scriptType=ChannelMedia.SCRIPT_LOCAL;
		params=new HashMap<String,String>();
		parse(data);
		Ok=true;
	}
	
	public void parse(ArrayList<String> data) {
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line==null)
				continue;
			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2) // ignore weird lines
				continue;
			if(keyval[0].equalsIgnoreCase("macro")) {
				ChannelMacro m=parent.getMacro(keyval[1]);
				if(m!=null)
					parse(m.getMacro());
				else
					PMS.debug("unknown macro "+keyval[1]);
			}	
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
			if(keyval[0].equalsIgnoreCase("script")) {
				parent.debug("assign script "+keyval[1]);
				script=keyval[1];
				scriptType=ChannelMedia.SCRIPT_LOCAL;
			}
			if(keyval[0].equalsIgnoreCase("nscript")) {
				parent.debug("assign net script "+keyval[1]);
				script=keyval[1];
				scriptType=ChannelMedia.SCRIPT_NET;
			}
			if(keyval[0].equalsIgnoreCase("escript")) {
				parent.debug("assign ext script "+keyval[1]);
				script=keyval[1];
				scriptType=ChannelMedia.SCRIPT_EXT;
			}
			if(keyval[0].equalsIgnoreCase("name"))
				name=keyval[1];
			if(keyval[0].equalsIgnoreCase("prop"))	
				prop=keyval[1].trim().split(",");
			if(keyval[0].equalsIgnoreCase("img"))
				thumbURL=keyval[1];
			if(keyval[0].equalsIgnoreCase("type")) {
				if(keyval[1].trim().equalsIgnoreCase("asx"))
					type=ChannelMedia.TYPE_ASX;
			}
			if(keyval[0].equalsIgnoreCase("put")) {
				String[] stash=keyval[1].split("=",2);
				if(stash.length>1)
					params.put(stash[0],stash[1]);
			}
			if(keyval[0].equalsIgnoreCase("subtitle")) {
				subtitle=keyval[1];
			}
		}
	}
	
	public ChannelMatcher getMatcher() {
		return matcher;
	}
	
	public boolean onlyFirst() {
		return ChannelUtil.getProperty(prop, "only_first");
	}
	
	public void stash(String key,String val) {
		if(ChannelUtil.empty(val))
			return;
		params.put(key, val);
	}
	
	public void add(DLNAResource res,String nName,String url,String thumb,boolean autoASX) {
		if(!ChannelUtil.empty(thumbURL)) {
			if(ChannelUtil.getProperty(prop, "use_conf_thumb"))
				thumb=thumbURL;
		}
		if(!ChannelUtil.empty(name)) {
			nName=ChannelUtil.concatField(name,nName,prop,"name");
			if(nName==null)
				nName=name;
			else if(ChannelUtil.getProperty(prop, "ignore_match"))
				nName=name;
		}
		thumb=ChannelUtil.getThumb(thumb, thumbURL, parent);
		if(ChannelUtil.getProperty(prop, "unescape_url"))
			url=ChannelUtil.unescape(url);
		thumb=ChannelUtil.pendData(thumb, prop, "thumb");
		url=ChannelUtil.pendData(url,prop,"url");
		if(ChannelUtil.empty(url))
			return;
		if(ChannelUtil.empty(nName))
			nName="Unknown";
		nName=StringEscapeUtils.unescapeHtml(nName);
		parent.debug("found media "+nName+" thumb "+thumb+" url "+url);
		// asx is weird and one would expect mencoder to support it no
		int asx=ChannelUtil.ASXTYPE_NONE;
		if(ChannelUtil.getProperty(prop, "auto_asx"))
			asx=ChannelUtil.ASXTYPE_AUTO;
		if(type==ChannelMedia.TYPE_ASX)
			asx=ChannelUtil.ASXTYPE_FORCE;
		
		if(Channels.save())  { // Add save version
			ChannelPMSSaveFolder sf=new ChannelPMSSaveFolder(parent,nName,url,thumb,script,asx,
					                parent.getFormat(),this);
			res.addChild(sf);
		}
		else {
			res.addChild(new ChannelMediaStream(parent,nName,url,thumb,script,parent.getFormat(),
						asx,this));
		}
	}
	
	public String separator(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_separator");
	}
	
	@Override
	public String scrape(Channel ch, String url, String scriptName,int format,DLNAResource start) {
		String realUrl;
		ch.debug("scrape sub "+subtitle);
		String subFile="";
		int asx=ChannelUtil.ASXTYPE_NONE;
		if(ChannelUtil.getProperty(prop, "auto_asx"))
			asx=ChannelUtil.ASXTYPE_AUTO;
		if(type==ChannelMedia.TYPE_ASX)
			asx=ChannelUtil.ASXTYPE_FORCE;
		if(subtitle!=null&&Channels.doSubs()) {
			ChannelSubs subs=Channels.getSubs(subtitle);
			if(subs!=null) {
				String realName=ChannelUtil.backTrack(start,ChannelUtil.getNameIndex(prop));
				// Maybe we should mangle the name?
				String nameMangle=ChannelUtil.getPropertyValue(prop, "name_mangle");
				realName=ChannelUtil.mangle(nameMangle, realName);
				parent.debug("backtracked name "+realName);
				subFile=subs.getSubs(realName);
				parent.debug("subs "+subFile);
				params.put("subtitle",subFile);	
			}
		}
		if(ChannelUtil.empty(scriptName)) { // no script just return what we got
			params.put("url", ChannelUtil.parseASX(url,asx));
			return ChannelUtil.createMediaUrl(params,format);
		}
		ch.debug("media scrape type "+scriptType+" name "+scriptName);
		if(scriptType==ChannelMedia.SCRIPT_NET) 
			return ChannelNaviXProc.parse(url,scriptName,format,subFile,parent);
		if(scriptType==ChannelMedia.SCRIPT_EXT) {
			String f=ChannelUtil.format2str(format);
			ProcessBuilder pb=new ProcessBuilder(scriptName,url,f);
			String rUrl=ChannelUtil.execute(pb);
			params.put("url",  ChannelUtil.parseASX(rUrl,asx));
			return ChannelUtil.createMediaUrl(params, format);
		}	
		ArrayList<String> sData=Channels.getScript(scriptName);
		if(sData==null) { // weird no script found, log and bail out
			ch.debug("no script "+scriptName+" defined");
			params.put("url", url);
			return ChannelUtil.createMediaUrl(params,format);
		}
		realUrl=ChannelNaviXProc.lite(url,sData,format);
		if(ChannelUtil.empty(realUrl)) {
			ch.debug("Bad script result");
			return null;
		}
		params.put("url",  ChannelUtil.parseASX(realUrl,asx));
		return ChannelUtil.createMediaUrl(params,format);
	}
}
