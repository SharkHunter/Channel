package com.sharkhunter.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualVideoAction;
import net.pms.formats.Format;

public class ChannelMedia implements ChannelProps,ChannelScraper {
	public static final int TYPE_NORMAL=0;
	public static final int TYPE_ASX=1;
	
	public static final int SAVE_OPT_NONE=0;
	public static final int SAVE_OPT_SAVE=1;
	public static final int SAVE_OPT_PLAY=2;
	
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
	private String videoFormat;
	private ChannelMatcher formatMatcher;
	
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
		videoFormat=null;
		formatMatcher=null;
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
					matcher.setChannel(parent);
			}
			if(keyval[0].equalsIgnoreCase("order")) {
				if(matcher==null)
					matcher=new ChannelMatcher(null,keyval[1],this);
				else
					matcher.setOrder(keyval[1]);
				matcher.setChannel(parent);
			}
			if(keyval[0].equalsIgnoreCase("format_matcher")) {
				if(formatMatcher==null)
					formatMatcher=new ChannelMatcher(keyval[1],"format",this);
				else
					formatMatcher.setMatcher(keyval[1]);
				formatMatcher.setChannel(parent);
			}
			if(keyval[0].equalsIgnoreCase("script")) {
				//parent.debug("assign script "+keyval[1]);
				script=keyval[1];
				scriptType=ChannelMedia.SCRIPT_LOCAL;
			}
			if(keyval[0].equalsIgnoreCase("nscript")) {
				//parent.debug("assign net script "+keyval[1]);
				script=keyval[1];
				scriptType=ChannelMedia.SCRIPT_NET;
			}
			if(keyval[0].equalsIgnoreCase("escript")) {
				parent.debug("assign ext script "+keyval[1]);
				if(!ChannelUtil.empty(keyval[1])) {
					script=keyval[1];
					scriptType=ChannelMedia.SCRIPT_EXT;
				}
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
				format=ChannelUtil.getFormat(keyval[1],format);
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
			if(keyval[0].equalsIgnoreCase("fallback_video"))
				videoFormat=ChannelUtil.ensureDot(keyval[1].trim());
		}
		if(matcher!=null)
			matcher.processProps(prop);
	}
	
	public void setFallBackFormat(String s) {
		videoFormat=s;
	}
	
	public ChannelMatcher getMatcher() {
		return matcher;
	}
	
	public ChannelMatcher getFormatMatcher() {
		return formatMatcher;
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
		add(res,nName,url,thumb,autoASX,null,-1,ChannelMedia.SAVE_OPT_NONE,null, null);
	}

	public void add(DLNAResource res,String nName,String url,String thumb,boolean autoASX,int sOpt) {
		add(res,nName,url,thumb,autoASX,null,-1,sOpt,null, null);
	}
	
	public void add(DLNAResource res,String nName,String url,String thumb,boolean autoASX,String imdb) {
		add(res,nName,url,thumb,autoASX,imdb,-1,ChannelMedia.SAVE_OPT_NONE,null, null);
	}
	
	public void add(DLNAResource res,String nName,String url,String thumb,
			boolean autoASX,String imdb,int f,String subs) {
		add(res,nName,url,thumb,autoASX,imdb,f,ChannelMedia.SAVE_OPT_NONE,subs, null);
	}
	
	public void add(final DLNAResource res,String nName,String url,String thumb,
			boolean autoASX,String imdb,int f,int sOpt,String subs, HashMap<String, String> rtmpStash) {
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
	//	url=ChannelUtil.pendData(url,prop,"url");
		if(ChannelUtil.empty(url)&&!scriptOnly())
			return;
		if(ChannelUtil.empty(nName))
			nName="Unknown";
		nName=StringEscapeUtils.unescapeHtml(nName);
		parent.debug("found media "+nName+" thumb "+thumb+" url "+url+" format "+format+
				" script "+script+" embedsubs "+subs+" imdb "+imdb+" stash "+rtmpStash);
		// asx is weird and one would expect mencoder to support it no
		int asx=ChannelUtil.ASXTYPE_NONE;
		if(ChannelUtil.getProperty(prop, "auto_asx"))
			asx=ChannelUtil.ASXTYPE_AUTO;
		if(type==ChannelMedia.TYPE_ASX)
			asx=ChannelUtil.ASXTYPE_FORCE;
		final int resF=(format==-1?(f==-1?parent.getMediaFormat():f):format);
		if(ChannelUtil.getProperty(prop, "bad")) {
			res.addChild(ChannelResource.redX(nName));
			return;
		}
		if(ChannelUtil.getProperty(prop, "ignore_save")) {
			ChannelMediaStream cms=new ChannelMediaStream(parent,nName,url,thumb,script,resF,asx,this);
			cms.setImdb(imdb);
			cms.setSaveMode(ChannelUtil.getProperty(prop, "raw_save"));
			cms.setFallbackFormat(videoFormat);
			cms.setEmbedSub(subs);
			cms.setStash(rtmpStash);
			res.addChild(cms);
			return;
		}
		if((Channels.save()&&sOpt==ChannelMedia.SAVE_OPT_NONE)||
			(subtitle!=null||!ChannelUtil.empty(subs))) { // Add save version
			ChannelPMSSaveFolder sf=new ChannelPMSSaveFolder(parent,nName,url,thumb,script,asx,
					                resF,this);
			sf.setImdb(imdb);
			sf.setDoSubs(subtitle!=null||!ChannelUtil.empty(subs));
			sf.setEmbedSub(subs);
			sf.setSaveMode(ChannelUtil.getProperty(prop, "raw_save"));
			sf.setFallbackFormat(videoFormat);
			sf.setStash(rtmpStash);
			res.addChild(sf);
		}
		else {
			ChannelMediaStream cms;
			if(sOpt==ChannelMedia.SAVE_OPT_PLAY)
				cms=new ChannelMediaStream(parent,nName,url,thumb,script,resF,asx,this,name,null);
			else if(sOpt==ChannelMedia.SAVE_OPT_SAVE)
				cms=new ChannelMediaStream(parent,nName,url,thumb,script,resF,asx,this,name,name);
			else
				cms=new ChannelMediaStream(parent,nName,url,thumb,script,resF,asx,this);
			cms.setImdb(imdb);
			cms.setSaveMode(ChannelUtil.getProperty(prop, "raw_save"));
			cms.setFallbackFormat(videoFormat);
			cms.setEmbedSub(subs);
			cms.setStash(rtmpStash);
			res.addChild(cms);
		}
	}
	
	public String separator(String base) {
		return ChannelUtil.separatorToken(ChannelUtil.getPropertyValue(prop, base+"_separator"));
	}
	
	@Override
	public String scrape(Channel ch, String url, String scriptName,int format,DLNAResource start
			             ,boolean noSub,String imdb,Object embedSubs,
			             HashMap<String,String> extraMap,RendererConfiguration render) {
		ch.debug("scrape sub "+subtitle+" format "+format);
		String subFile="";
		boolean live=ChannelUtil.getProperty(prop, "live");
		HashMap<String,String> vars=new HashMap<String,String>(params);
		if(live)
			vars.put("live", "true");
		int asx=ChannelUtil.ASXTYPE_NONE;
		if(ChannelUtil.getProperty(prop, "auto_asx"))
			asx=ChannelUtil.ASXTYPE_AUTO;
		if(type==ChannelMedia.TYPE_ASX)
			asx=ChannelUtil.ASXTYPE_FORCE;
		Channels.debug("embedsubs "+embedSubs+" nosub "+noSub);
		if((subtitle!=null||embedSubs!=null)&&Channels.doSubs()&&!noSub) {
			if(embedSubs!=null) {
				subFile=ChannelSubs.resolve(embedSubs);
			}
			if(ChannelUtil.empty(subFile)) {
				subFile=subScrape(start,ChannelUtil.ensureImdbtt(imdb));
			}
			if(!ChannelUtil.empty(subFile)) {
				vars.put("subtitle", subFile);
			}
			Channels.debug("subFile "+subFile);
		}	
		Channels.debug("scrape script name "+scriptName);
		if(ChannelUtil.empty(scriptName)) { // no script just return what we got
			vars.put("url", ChannelUtil.parseASX(url,asx));
			if(extraMap!=null)
				vars.putAll(extraMap);
			if(live)
				vars.put("live", "true");	
			return ChannelUtil.createMediaUrl(vars,format,ch,render);
		}
		ch.debug("media scrape type "+scriptType+" name "+scriptName);
		if(scriptType==ChannelMedia.SCRIPT_NET) 
			return ChannelNaviXProc.parse(url,scriptName,format,subFile,parent);
		if(scriptType==ChannelMedia.SCRIPT_EXT) {
			/*String f=ChannelUtil.format2str(format);
			ProcessBuilder pb=new ProcessBuilder(scriptName,url,f);
			String rUrl=ChannelUtil.execute(pb);*/
			boolean no_format=ChannelUtil.getProperty(prop, "script.no_format");
			String rUrl=ChannelScriptMgr.runScript(scriptName,url,parent,no_format);
			if(rUrl.startsWith("RTMPDUMP")) { // rtmpdump magic
				vars.put("__type__", "RTMPDUMP");
				String tmp=rUrl.replace("RTMPDUMP", "");
				asx=ChannelUtil.ASXTYPE_NONE; // force none
				int first=tmp.indexOf("###");
				int last=tmp.lastIndexOf("###");
				rUrl=tmp;
				if(first!=-1&&last!=-1) {
					rUrl=tmp.substring(first+3, last-3);
				}
			}
			vars.put("url",  ChannelUtil.parseASX(rUrl,asx));
			return ChannelUtil.createMediaUrl(vars, format,ch,render);
		}	
		ArrayList<String> sData=Channels.getScript(scriptName);
		if(sData==null) { // weird no script found, log and bail out
			ch.debug("no script "+scriptName+" defined");
			vars.put("url", url);
			return ChannelUtil.createMediaUrl(vars,format,ch,render);
		}
		HashMap<String,String> res=ChannelNaviXProc.lite(url,sData,asx,ch);
		if(res==null) {
			ch.debug("Bad script result");
			return null;
		}
		res.put("subtitle", subFile);
		if(live)
			res.put("live", "true");
		res.put("__type__", "normal");
		return ChannelUtil.createMediaUrl(res,format,ch,render);
	}
	
	public boolean scriptOnly() {
		return !ChannelUtil.empty(script)&&(matcher==null);
	}
	
	public String append(String base) {
		return ChannelUtil.getPropertyValue(prop,"append_"+base);
	}
	public String prepend(String base) {
		return ChannelUtil.getPropertyValue(prop,"prepend_"+base);
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
		for(String key: params.keySet()) {
			sb.append("put=");
			sb.append(key);
			sb.append("=");
			sb.append(params.get(key));
			sb.append("\n");
		}
		sb.append("\n}\n");
		return sb.toString();
	}
	
	public long delay() {
		String x=ChannelUtil.getPropertyValue(prop, "delay");
		if(ChannelUtil.empty(x))
			return 0;
		if(x.equals("dynamic"))
			return -1;
		try {
			return 1000*Long.parseLong(x);
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	public String backtrackedName(DLNAResource start) {
		return ChannelSubUtil.backtrackedName(start, prop);
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
	
	public String subScrape(DLNAResource start,String imdb) {
		return subScrape(start,imdb,false);
	}
	
	public String subScrape(DLNAResource start,String imdb,boolean select) {
		if(subtitle==null) 
			return null;
		String subFile=null;
		for(int i=0;i<subtitle.length;i++) {
			ChannelSubs subs=Channels.getSubs(subtitle[i]);
			if(subs==null)
				continue;
			if(!subs.langSupported())
				continue;
			String realName=backtrackedName(start);
			parent.debug("backtracked name "+realName);
			HashMap<String,String> subName;
			int subScript=0;
			while((subName=parent.getSubMap(realName,subScript))!=null) {
				subScript++;
				if(!ChannelUtil.empty(imdb))
					subName.put("imdb", ChannelUtil.ensureImdbtt(imdb));
				subFile=subs.getSubs(subName);
				parent.debug("subs "+subFile);
				if(!ChannelUtil.empty(subFile))
					return subFile;
			}
		}
		return null;
	}
	
	public ArrayList<String> subSites() {
		return ChannelSubUtil.subSites(subtitle);
	}
	
	public HashMap<String,Object> subSelect(DLNAResource start,String imdb) {
		return ChannelSubUtil.subSelect(start, imdb, subtitle, parent, backtrackedName(start));
	}
	
	public HashMap<String,Object> subSelect(DLNAResource start,String imdb,String subSite) {
		return ChannelSubUtil.subSelect(start, imdb, subSite, subtitle, parent, backtrackedName(start));
	}
	
	public String relativeURL() {
		return ChannelUtil.getPropertyValue(prop, "relative_url");
	}

	public boolean getBoolProp(String p) {
		return ChannelUtil.getProperty(prop, p);
	}

	@Override
	public String lastPlayResolveURL(DLNAResource start) {
		String resolver=ChannelUtil.getPropertyValue(prop, "last_play_action");
		if(ChannelUtil.empty(resolver))
			return null;
		DLNAResource tmp=start;
		while(tmp!=null) {
			if(tmp instanceof Channels)
				return null;
			if(!(tmp instanceof ChannelPMSFolder)) {
				tmp=tmp.getParent();
				continue;
			}
			ChannelPMSFolder f=(ChannelPMSFolder)tmp;
			ChannelFolder cf=f.getFolder();
			if(resolver.equals(cf.actionName())) {
				return f.getURL();
			}
			tmp=tmp.getParent();
		}
		return null;
	}
}
