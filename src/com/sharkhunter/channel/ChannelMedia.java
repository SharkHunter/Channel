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
	private String[] subtitle;
	private int format;
	private String staticUrl;
	private String proxy;
	private HashMap<String,String> hdrs;
	
	public ChannelMedia(ArrayList<String> data,Channel parent) {
		Ok=false;
		matcher=null;
		this.parent=parent;
		type=ChannelMedia.TYPE_NORMAL;
		script=null;
		subtitle=null;
		scriptType=ChannelMedia.SCRIPT_LOCAL;
		params=new HashMap<String,String>();
		format=-1;
		proxy=null;
		hdrs=new HashMap<String,String>();	
		hdrs.putAll(parent.getHdrs());
		parse(data);
		if(format==-1)
			format=parent.getFormat();
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
				subtitle=keyval[1].split(",");
			}
			if(keyval[0].equalsIgnoreCase("format")) {
				format=ChannelUtil.getFormat(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("url")) {
				staticUrl=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("proxy")) {
				proxy=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("hdr")) {
				String[] k1=keyval[1].split("=");
				if(k1.length<2)
					continue;
				hdrs.put(k1[0], k1[1]);
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
		add(res,nName,url,thumb,autoASX,null);
	}
	
	public void add(DLNAResource res,String nName,String url,String thumb,boolean autoASX,String imdb) {
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
		if(!ChannelUtil.empty(staticUrl)&&(matcher==null)) {// static url
			url=staticUrl;
			nName=name;
		}
		if(ChannelUtil.getProperty(prop, "unescape_url"))
			url=ChannelUtil.unescape(url);
		thumb=ChannelUtil.pendData(thumb, prop, "thumb");
		url=ChannelUtil.pendData(url,prop,"url");
		if(ChannelUtil.empty(url)&&!scriptOnly())
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
					                format,this);
			sf.setImdb(imdb);
			res.addChild(sf);
		}
		else {
			ChannelMediaStream cms=new ChannelMediaStream(parent,nName,url,thumb,script,format,asx,this);
			cms.setImdb(imdb);
			res.addChild(cms);
		}
	}
	
	public String separator(String base) {
		return ChannelUtil.separatorToken(ChannelUtil.getPropertyValue(prop, base+"_separator"));
	}
	
	@Override
	public String scrape(Channel ch, String url, String scriptName,int format,DLNAResource start
			             ,boolean noSub,String imdb) {
		String realUrl;
		ch.debug("scrape sub "+subtitle);
		String subFile="";
		boolean live=ChannelUtil.getProperty(prop, "live");
		if(live)
			params.put("live", "true");
		int asx=ChannelUtil.ASXTYPE_NONE;
		if(ChannelUtil.getProperty(prop, "auto_asx"))
			asx=ChannelUtil.ASXTYPE_AUTO;
		if(type==ChannelMedia.TYPE_ASX)
			asx=ChannelUtil.ASXTYPE_FORCE;
		if(subtitle!=null&&Channels.doSubs()&&!noSub) {
			for(int i=0;i<subtitle.length;i++) {
				ChannelSubs subs=Channels.getSubs(subtitle[i]);
				if(subs==null)
					continue;
				if(!subs.langSupported())
					continue;
				String realName=ChannelUtil.backTrack(start,ChannelUtil.getNameIndex(prop));
				// Maybe we should mangle the name?
				String nameMangle=ChannelUtil.getPropertyValue(prop, "name_mangle");
				realName=ChannelUtil.mangle(nameMangle, realName);
				parent.debug("backtracked name "+realName);
				HashMap<String,String> subName=parent.getSubMap(realName);
				if(!ChannelUtil.empty(imdb))
					subName.put("imdb", imdb);
				subFile=subs.getSubs(subName);
				parent.debug("subs "+subFile);
				params.put("subtitle",subFile);
				if(!ChannelUtil.empty(subFile))
					break;
			}
		}
		if(ChannelUtil.empty(scriptName)) { // no script just return what we got
			params.put("url", ChannelUtil.parseASX(url,asx));
			return ChannelUtil.createMediaUrl(params,format,ch);
		}
		ch.debug("media scrape type "+scriptType+" name "+scriptName);
		if(scriptType==ChannelMedia.SCRIPT_NET) 
			return ChannelNaviXProc.parse(url,scriptName,format,subFile,parent);
		if(scriptType==ChannelMedia.SCRIPT_EXT) {
			String f=ChannelUtil.format2str(format);
			ProcessBuilder pb=new ProcessBuilder(scriptName,url,f);
			String rUrl=ChannelUtil.execute(pb);
			params.put("url",  ChannelUtil.parseASX(rUrl,asx));
			return ChannelUtil.createMediaUrl(params, format,ch);
		}	
		ArrayList<String> sData=Channels.getScript(scriptName);
		if(sData==null) { // weird no script found, log and bail out
			ch.debug("no script "+scriptName+" defined");
			params.put("url", url);
			return ChannelUtil.createMediaUrl(params,format,ch);
		}
		HashMap<String,String> res=ChannelNaviXProc.lite(url,sData,asx,ch);
		if(res==null) {
			ch.debug("Bad script result");
			return null;
		}
		res.put("subtitle", subFile);
		if(live)
			res.put("live", "true");
		return ChannelUtil.createMediaUrl(res,format,ch);
	}
	
	public boolean scriptOnly() {
		return !ChannelUtil.empty(script)&&(matcher==null);
	}
	
	public String rawEntry() {
		StringBuilder sb=new StringBuilder();
		sb.append("media {");
		sb.append("\n");
		if(!ChannelUtil.empty(staticUrl)) {
			sb.append("url=");
			sb.append(staticUrl);
			sb.append("\n");
		}
		if(!ChannelUtil.empty(name)) {
			sb.append("name=");
			sb.append(name);
			sb.append("\n");
		}
		if(matcher!=null) {
			sb.append("matcher=");
			sb.append(matcher.getRegexp().toString());
			sb.append("\n");
			matcher.orderString(sb);
			sb.append("\n");
		}
		if(prop!=null) {
			sb.append("prop=");
			ChannelUtil.list2file(sb,prop);
			sb.append("\n");
		}
		if(subtitle!=null) {
			sb.append("subtitle=");
			ChannelUtil.list2file(sb,subtitle);
			sb.append("\n");
		}
		if(!ChannelUtil.empty(script)) {
			if(scriptType==ChannelMedia.SCRIPT_EXT)
				sb.append("e");
			else if(scriptType==ChannelMedia.SCRIPT_NET)
				sb.append("n");
			sb.append("script=");
			sb.append(script);
			sb.append("\n");
		}
		sb.append("\n}\n");
		return sb.toString();
	}
}
