package com.sharkhunter.channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.pms.PMS;
import net.pms.configuration.FormatConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaAudio;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.Range;
import net.pms.encoders.Player;
import net.pms.encoders.PlayerFactory;
import net.pms.formats.Format;
import net.pms.formats.FormatFactory;
import net.pms.formats.v2.SubtitleType;
import net.pms.formats.WEB;
import net.pms.io.BufferedOutputFile;
import net.pms.io.OutputParams;
import net.pms.io.WindowsNamedPipe;

public class ChannelMediaStream extends DLNAResource {

	private String url;
	private String thumb;
	private String name;
	private String processor;
	private int format;
	private Channel ch;
	private String realUrl;
	private int ASX;
	private ChannelScraper scraper;
	private String saveName; 
	private String dispName;
	private Thread saver;
	private boolean scraped;
	private long startTime;
	private boolean noSubs;
	private String imdb;
	private RendererConfiguration render;
	private boolean rawSave;
	private boolean fool;
	private String videoFormat;
	private long scrapeTime;
	private long delay;
	private Object embedSub;
	private ChannelStreamVars streamVars;
	private HashMap<String,String> stash;
	private Thread bgThread;
	private int bgCnt;
	private String hdr;
	
	public ChannelMediaStream(Channel ch,String name,String nextUrl,
			  String thumb,String proc,int type,int asx,
			  ChannelScraper scraper) {	
		this(ch,name,nextUrl,thumb,proc,type,asx,scraper,name,null);
	}
	public ChannelMediaStream(Channel ch,String name,String nextUrl,
							  String thumb,String proc,int type,int asx,
							  ChannelScraper scraper,String dispName) {
		this(ch,name,nextUrl,thumb,proc,type,asx,scraper,dispName,null);
		
	}
	public ChannelMediaStream(Channel ch,String name,String nextUrl,
							  String thumb,String proc,int type,int asx,
							  ChannelScraper scraper,String dispName,String saveName) {
		super(type);
		url=nextUrl;
		this.name=name;
		this.thumb=thumb;
		this.processor=proc;
		this.format=type;
		this.ch=ch;
		realUrl=null;
		ASX=asx;
		this.scraper=scraper;
		this.saveName=saveName;
		this.dispName=dispName;
		saver=null;
		scraped=false;
		startTime=0;
		noSubs=false;
		imdb=null;
		render=null;
		rawSave=false;
		fool=Channels.cfg().netDiscStyle();
		videoFormat=null;
		scrapeTime=0;
		delay=scraper.delay();
		embedSub=null;
		streamVars=null;
		bgThread=null;
		bgCnt=0;
		hdr="";
		setMedia(new DLNAMediaInfo());
		getMedia().setAudioTracksList(new ArrayList<DLNAMediaAudio>());
		getMedia().setMediaparsed(true);
	}
	
	public ChannelMediaStream(ChannelMediaStream cms) {
		this(cms,null);
	}
	
	public ChannelMediaStream(ChannelMediaStream cms,String name) {
		super(cms.getType());
		url=cms.url;
		this.name=(ChannelUtil.empty(name)?cms.name:name);
		this.thumb=cms.thumb;
		this.processor=cms.processor;
		this.format=cms.getType();
		this.ch=cms.ch;
		realUrl=null;
		ASX=cms.ASX;
		this.scraper=cms.scraper;
		this.saveName=cms.saveName;
		this.dispName=cms.dispName;
		saver=cms.saver;
		scraped=false;
		startTime=0;
		noSubs=cms.noSubs;
		imdb=cms.imdb;
		render=cms.render;
		rawSave=cms.rawSave;
		fool=Channels.cfg().netDiscStyle();
		videoFormat=cms.videoFormat;
		scrapeTime=0;
		delay=cms.delay;
		embedSub=cms.embedSub;
		streamVars=cms.streamVars;
		stash=cms.stash;
		bgThread=cms.bgThread;
		bgCnt=cms.bgCnt;
		hdr=cms.hdr;
		setMedia(cms.getMedia());
		getMedia().setAudioTracksList(new ArrayList<DLNAMediaAudio>());
		getMedia().setMediaparsed(true);
	}
	
	public ChannelMediaStream(String name,String realUrl,Channel parent,int format,
			String thumb) {
		super(format);
		url=realUrl;
		this.name=name;
		this.thumb=thumb;
		this.processor=null;
		this.format=format;
		this.ch=parent;
		this.realUrl=realUrl;
		ASX=ChannelUtil.ASXTYPE_AUTO;
		this.scraper=null;
		this.saveName=null;
		this.dispName=null;
		saver=null;
		scraped=false;
		startTime=0;
		noSubs=false;
		imdb=null;
		render=null;
		rawSave=false;
		fool=Channels.cfg().netDiscStyle();
		videoFormat=null;
		scrapeTime=0;
		delay=0;
		embedSub=null;
		streamVars=null;
		bgThread=null;
		bgCnt=0;
		hdr="";
		setMedia(new DLNAMediaInfo());
		getMedia().setAudioTracksList(new ArrayList<DLNAMediaAudio>());
		getMedia().setMediaparsed(true);
	}
	
	public String saveName() {
		return saveName;
	}
	
	public void noSubs() {
		noSubs=true;
	}
	
	public void subs() {
		noSubs=false;
	}
	
	public void setRender(RendererConfiguration r) {
		render=r;
	}
	
	public void setSaveMode(boolean raw) {
		rawSave=raw;
	}
	
	public void setFallbackFormat(String s) {
		videoFormat=s;
	}
	
	public void setEmbedSub(Object str) {
		embedSub=str;
	}
	
	public ChannelScraper scraper() {
		return scraper;
	}
	
	public String imdb() {
		return imdb;
	}
	
	public void setName(String n) {
		name=n;
	}
	
	public void setStash(HashMap<String,String> map) {
		stash=map;
	}
	
    public InputStream getThumbnailInputStream() throws IOException {
    	if (thumb != null) {
    		try {
    			if(thumb.startsWith("/resource/"))
    				return getResourceInputStream(thumb.substring(10));
    			return downloadAndSend(thumb, true);
    		}
    		catch (Exception e) {
    		}
    	}
		return super.getThumbnailInputStream();
    }
    
    
    private void updateStreamDetails(boolean loop) {
    	Format old_ext=getExt();
    	// update format, use checktype to be future proof
    	setExt(null);
    	checktype();
    	if(getExt()==null) { // no ext found restore what we got and bail out
    		setExt(old_ext);
    		return;
    	}	


    	// need to update player as well
    	int i=0;
    	Player pl=null;
        String name = getName();

        for (Player p : PlayerFactory.getPlayers()) {
            String end = "[" + p.id() + "]";

            if (name.endsWith(end)) {
                //nametruncate = name.lastIndexOf(end);
                pl = p;
                break;
            } else if (getParent() != null && getParent().getName().endsWith(end)) {
                //getParent().nametruncate = getParent().getName().lastIndexOf(end);
                pl = p;
                break;
            }
		}

        if (pl == null) {
            pl = PlayerFactory.getPlayer(this);
        }

		// if we didn't find a new player leave the old one
      //  if(pl!=null)
		if(Channels.cfg().usePMSEncoder()) {
			boolean forceTranscode = false;
			if (getExt() != null) {
				forceTranscode = getExt().skip(PMS.getConfiguration().getForceTranscodeForExtensions(), getDefaultRenderer() != null ? getDefaultRenderer().getTranscodedExtensions() : null);
			}

			boolean isIncompatible = false;

			if (!getExt().isCompatible(getMedia(),getDefaultRenderer())) {
				isIncompatible = true;
			}
			boolean mp2=false;
			if(streamVars!=null) {
				ArrayList<String> dummyArgs=new ArrayList<String>();
				streamVars.resolve("null",dummyArgs,null);
				for(String val : dummyArgs) {
					if(val.startsWith("mp2Force")) {
						mp2=true;
						break;
					}
				}
			}
			Channels.debug("set player to nullplayer "+isIncompatible+" force "+forceTranscode+" fool "+fool+" force mp2 "+mp2);
			if(!fool)
				setPlayer(pl);
			else
				setPlayer(new ChannelNullPlayer(isIncompatible||forceTranscode||mp2));
		}
		else {
			setPlayer(pl);
		}
    }
    
    public DLNAMediaSubtitle getSubs() {
    	DLNAMediaSubtitle sub=this.getMediaSubtitle();
    	if(sub!=null&&sub.id!=-1)
    		return null;
    	return sub;
    }
    
    public void scrape(RendererConfiguration render) {
    	if(!scraped) {
    		if(scraper!=null)
    			realUrl=scraper.scrape(ch,url,processor,format,this,noSubs,imdb,
    								   embedSub,stash,render);
    		else
    			realUrl=ChannelUtil.parseASX(url, ASX);
    		scrapeTime=System.currentTimeMillis();
    	}
    	scraped=true;
    }
    
    private void scrape_i(RendererConfiguration render) {
    	Channels.debug("scrape "+name+" nosubs "+noSubs);
    	fool=Channels.cfg().netDiscStyle();
    	if(scraped)
    		return;
    	scrape(render);
    	realUrl=ChannelResource.NullURL(realUrl);
    	if(ChannelUtil.empty(realUrl))
    		return;
    	Channels.debug("real "+realUrl+" nd "+fool+" noSubs "+noSubs+" "+Channels.cfg().usePMSEncoder());
    	if(Channels.cfg().usePMSEncoder()) {
    		if(fool) {
    			if(realUrl.startsWith("subs://"))
    				fixStuff(realUrl.substring(7),true);
    			else if(realUrl.startsWith("navix://")) {
    				fixStuff(realUrl.substring(8+8),false);
    			}
    		}
    	}
    	else {
    		if(realUrl.startsWith("subs://"))
				fixStuff(realUrl.substring(7),true);
    		else if(realUrl.startsWith("navix://")) 
    			fixStuff(realUrl.substring(8+8),false);
    		else if(realUrl.startsWith("rtmpdump://")) {
    			rtmpUrl(realUrl.substring(11+8));
    		}
    		else if(realUrl.startsWith("rtmp://")) {
    			rtmpUrl(realUrl.substring(7));
    		}
    	}
    	if(noSubs) { // make sure subs are off here
			media_subtitle=new DLNAMediaSubtitle();
			media_subtitle.setId(-1);
		}
    	Channels.debug("call update "+realUrl);
    	updateStreamDetails(true);
    	fool=false;
    	ch.prepareCom();
    }
    
    
    public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
    	if(parent instanceof ChannelPMSSaveFolder)
    		if(((ChannelPMSSaveFolder)parent).preventAutoPlay())
    			return null;
    	scrape_i(mediarenderer);
    	if(delayed())
    		return null;
    	InputStream is=null;//super.getInputStream(low,high,timeseek,mediarenderer);
    	if((saveName!=null)||Channels.cache()) {
    		return startSave(is);
    	}
    	else
    	    return is;
    }
    
    //public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
    public InputStream getInputStream(Range range, RendererConfiguration mediarenderer) throws IOException {
    	PMS.debug("cms getinp/2 scrape "+scraper+" url "+realUrl);
    	if(parent instanceof ChannelPMSSaveFolder)
    		if(((ChannelPMSSaveFolder)parent).preventAutoPlay())
    			return null;
    	Channels.debug("cms getinp/2... scrape "+scraper+" url "+realUrl+" "+scraped);
    	scrape_i(mediarenderer);
    	if(realUrl.startsWith("resource://")) {
    		return getResourceInputStream(realUrl.substring("resource://".length()));
    	}
    	if(delayed())
    		return null;
    	Channels.debug("using player "+getPlayer());
    	InputStream is=super.getInputStream(range,mediarenderer);
    	if(Channels.cfg().fileBuffer()&&!ChannelUtil.empty(realUrl)&&
    	   !realUrl.startsWith("rtmpdump://channel?"))
    		return is;
    	if((saveName!=null)||Channels.cache()) {
    		return startSave(is);
    	}
    	else
    	    return is;
    }
    
    private InputStream getStream() {
    	if(realUrl.startsWith("resource://")) {
    		return getResourceInputStream(realUrl.substring("resource://".length()));
    	}
    	try {
			URL urlobj = new URL(realUrl.replaceAll(" ", "%20"));
			Channels.debug("Retrieving " + urlobj.toString());
			URLConnection conn = urlobj.openConnection();
			conn.setRequestProperty("User-Agent",ChannelUtil.defAgentString);
			ChannelAuth auth=ch.prepareCom();
			String cookie="";
			if(auth!=null) {
				if(auth.method==ChannelLogin.STD)
					conn.setRequestProperty("Authorization", auth.authStr);
				else if(ChannelUtil.cookieMethod(auth.method)) 
					cookie=ChannelUtil.append(cookie,"; ",auth.authStr);
			}
			if(!ChannelUtil.empty(cookie))
				conn.setRequestProperty("Cookie",cookie);
			conn.connect();
			InputStream is=conn.getInputStream();
			if(Channels.cfg().fileBuffer()&&!realUrl.startsWith("rtmpdump://channel?"))
	    		return is;
			if((saveName!=null)||Channels.cache()) {
				return startSave(is);
			}
			else
				return is;
    	}
    	catch (Exception e) {
			Channels.debug("error reading "+e);
			return null;
		}
    }


    public InputStream getInputStream() {
    	Channels.debug("cms getinp/0 scrape "+scraper+" url "+realUrl);
    	scrape_i(getDefaultRenderer());
    	if(delayed())
    		return null;
    	if(ChannelUtil.empty(realUrl))
    		return null;
    	return getStream();
    }
    
    private InputStream startSave(InputStream is) throws IOException {
    	String sName=saveName;
    	boolean cache=ChannelUtil.empty(saveName);
    	if(cache)
    		sName=dispName;
    	String fName=Channels.fileName(sName,cache,imdb);
    	fName=ChannelUtil.guessExt(fName,realUrl);
    	if(cache)
    		ChannelUtil.cacheFile(new File(fName),"media");
    	if(rawSave&&Channels.cfg().rawSave()) {
    		final String fName1=fName;
    		Runnable r=new Runnable() {
    			public void run() {
    				ChannelUtil.downloadBin(realUrl, new File(fName1));
    			}
    		};
    		new Thread(r).start();
    		return is;
    	}
    	BufferedOutputStream fos=new BufferedOutputStream(new FileOutputStream(fName));
 	   	PipedOutputStream pos=(new PipedOutputStream());
 	   	PipedInputStream pis=new PipedInputStream(pos);
		OutputStream[] oss=new OutputStream[2];
		oss[0]=fos;
		oss[1]=pos;
		saver=new Thread(new ChannelSaver(is,oss));
		saver.start(); 
		return new BufferedInputStream(pis);
    }

    public long length() {
    	return DLNAMediaInfo.TRANS_SIZE;
    }

    public String getName() {
    	if(delay==0)
    		return name;
    	long d=(scrapeTime+delay)-System.currentTimeMillis();
		if(d<=0&&scrapeTime!=0)
			return name;
		this.setDiscovered(false);
		return "Delay "+(delay==-1?"dynamic":d/1000)+" "+name;
    }

    public boolean isFolder() {
    	return false;
    }

    public long lastModified() {
    	return 0;
    }
    
    public boolean directStream() {
    	if(format==Format.AUDIO)
    		return true;
    	String u;
    	if(ChannelUtil.empty(realUrl))
    		u=url;
    	else {
    		u= realUrl;
    	}
    	return u.startsWith("http")&&(media_subtitle!=null);
    }

    public String getSystemName() {
    	String u;
    	if(ChannelUtil.empty(realUrl))
    		u=url;
    	else {
    		u= realUrl;
    	}
		if(format==Format.AUDIO)
			return u.substring(u.lastIndexOf("/")+1);
		if((u.startsWith("http")||u.startsWith("rtmp"))&&fool)
			return ensureExt(u.substring(u.lastIndexOf("/")+1));
		return (u); // need this to
    }
    
    private boolean legalExt(String ext) {
    	if(ChannelUtil.empty(ext))
    		return false;
    	ArrayList<Format> formats= (ArrayList<Format>) FormatFactory.getSupportedFormats();
    	for(Format f : formats) {
    		String[] supported=f.getId();
    		for(int i=0;i<supported.length;i++)
    			if(ext.equalsIgnoreCase(supported[i]))
    				return true;
    	}
    	return false;
    }
    
    private String ensureExt(String str) {
    	if(legalExt(ChannelUtil.extension(str,true))) {
    		return str;
    	}
    	if(ChannelUtil.empty(videoFormat))
    		return str+ch.fallBackVideoFormat();
    	return str+(videoFormat);
    }
    
    public String realFormat() {
    	String u;
    	if(ChannelUtil.empty(realUrl))
    		u=url;
    	else {
    		u= realUrl;
    	}
    	if(!u.startsWith("http"))
    		return null;
    	return ensureExt(u);
    }
    
    public String fullUrl() {
    	if(ChannelUtil.empty(realUrl))
    		return url;
    	else {
    		return realUrl;
    	}
    }

	public boolean isValid() {
		if(render!=null&&render.isXBOX()&&(format==Format.VIDEO)) {
			ext = FormatFactory.getAssociatedFormat("dummy.avi");
			if(media==null)
				media=new DLNAMediaInfo();
			media.mimeType=FormatConfiguration.MIMETYPE_AUTO;
			media.mediaparsed=true;
			return true;
		}
		checktype();
		return true;
	}
	
	public void donePlaying() {
		if(parent instanceof ChannelPMSSaveFolder)
			((ChannelPMSSaveFolder)parent).childDone();
		DLNAResource tmp=this.getParent();
		ArrayList<DLNAResource> ancestors=new ArrayList<DLNAResource>();
		while(tmp!=null) {
			if(tmp instanceof Channels) // no need to continue here
				break;
			if(Channels.monitoredPlay(tmp)) {
				if(ancestors.size()==0) { // odd case1
					tmp.getChildren().remove(this);					
					break;
				}
				DLNAResource oldest=ancestors.get(ancestors.size()-1);
				DLNAResource entry=this;
				if(ancestors.size()-2>0) {
					// remove everything from the second folder down
					entry=ancestors.get(ancestors.size()-2);
				}
				oldest.getChildren().remove(entry);
				if(oldest.getChildren().size()==0) {
					// tmp is monitor folder
					// and there is nothing left remove this
					tmp.getChildren().remove(oldest);
				}
				// update monitor data and file
				Channels.updateMonitor(oldest.getName(),entry.getName());
				break;
			}
			ancestors.add(tmp);
			tmp=tmp.getParent();
		}
		if(bgThread!=null) 
			if(--bgCnt!=0) // more people using this thread don't kill the dl
				return;
		// killThread can handle bgThread==null, so it's safe to call it
		// for all threads
		ChannelUtil.killThread(bgThread);
		bgThread=null;
	}
	
	public void nowPlaying() {
		startTime=System.currentTimeMillis();
	}
	
	public void setImdb(String i) {
		imdb=i;
	}
	
	public void bgThread(Thread t) {			
		bgThread=t;
		bgCnt=1;
	}
	
	public void moreBg() {
		bgCnt++;
	}
	
	public boolean isBgDownload() {
		return (bgThread!=null);
	}
	
	private void fixStuff(String str,boolean subs) {
		String[] splits=str.split("&");
		for(int i=0;i<splits.length;i++) {
			if(splits[i].contains("url=")) {
				String tmp=splits[i].substring(splits[i].indexOf("url=")+4);
				realUrl=ChannelUtil.unescape(tmp);
				continue;
			}
			if(splits[i].contains("agent=")) {
				String tmp=splits[i].substring(splits[i].indexOf("agent=")+6);
				hdr=hdr+"-user-agent \""+ChannelUtil.unescape(tmp)+"\" ";
				continue;
			}
			if(splits[i].contains("subs=")&&!noSubs&&subs) {
				DLNAMediaSubtitle sub=new DLNAMediaSubtitle();
				String tmp=splits[i].substring(splits[i].indexOf("subs=")+5);
				try {
					sub.setExternalFile(new File(ChannelUtil.unescape(tmp)));
					Channels.debug("set sub file "+sub.getExternalFile().getAbsolutePath());
				} catch (FileNotFoundException e) {
					return;
				} 
				sub.setId(101);
				sub.setLang("und");
				sub.setType(SubtitleType.SUBRIP);
				media.container="unknown"; // avoid bug in mencvid
				media_subtitle=sub;
				continue;
			}
		}
	}
	
	private void rtmpUrl(String str) {
		String[] tmp=str.split("&");
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<tmp.length;i++) {
			String[] s=ChannelUtil.unescape(tmp[i]).split("=",2);
			if(s[0].equals("-r"))  { //special stuff
				sb.append(s[1]);
				sb.append(" ");
				continue;
			}
			if(s[0].equals("--swfVfy")) {
				sb.append("swfVfy=1");
				sb.append(" swfUrl");
				sb.append("="+s[1]);
				sb.append(" ");
				continue;
			}
			if(s[0].equals("subs")) {
				// subtitle
				DLNAMediaSubtitle sub=new DLNAMediaSubtitle();
				try {
					sub.setExternalFile(new File(ChannelUtil.unescape(s[1])));
					Channels.debug("set sub file "+sub.getExternalFile().getAbsolutePath());
				} catch (FileNotFoundException e) {
				}
				sub.setId(101);
				sub.setLang("und");
				sub.setType(SubtitleType.SUBRIP);
				media.container="unknown"; // avoid bug in mencvid
				media_subtitle=sub;
				continue;
			}
			if(s[0].equals("subtype")) {
				if(getSubs()!=null) {
					DLNAMediaSubtitle sub=getSubs();
					SubtitleType t=SubtitleType.valueOfFileExtension(s[1]);
					sub.setType(t);
				}
			}
			String op=ChannelUtil.rtmpOp(s[0]);
			if(op.equals(s[0]))
				continue;
			sb.append(op);
			if(s.length>1) {
				sb.append("="+s[1]);
			}
			else
				sb.append("=true");
			sb.append(" ");
		}
		realUrl=sb.toString();
	}
	
	private long getDynDelay() {
		try {
			return Long.parseLong(Channels.getStashData("default", "sleep","0"));
		} catch (Exception e) {
			return 0;
		}
	}
	
	private boolean delayed() {
		if(delay==-1) { // dynamic
			delay=1000*getDynDelay();
		}
		return ((scrapeTime+delay)>System.currentTimeMillis());
	}
	
	////////////////////////////////////////
	// Playlist
	////////////////////////////////////////
	
	public String playlistName() {
		if(scraper!=null)
			return scraper.backtrackedName(this);
		else
			return name;
	}
	
	public String playlistURI() {
		if(scraper==null)
			ChannelUtil.parseASX(url, ASX);
		if(ChannelUtil.empty(processor))
			return scraper.scrape(ch,url,processor,format,this,noSubs,imdb,
								  embedSub,stash,null);
		return url;
	}
	
	public String playlistExtra() {
		String res=ch.getName();
		if(scraper==null)
			return res;
		if(ChannelUtil.empty(processor))
			return res;
		return res+","+processor+","+ChannelUtil.format2str(format);
	}
	
	public String playlistThumb() {
		return thumb;
	}
	
	public List<String> addStreamvars(List<String> cmdList,OutputParams params) {
		Channels.debug("streamvars adding "+getName()+" "+streamVars);
		if(streamVars==null)
			return cmdList;
		List<String> cmdList1=ChannelUtil.addStreamVars(cmdList,streamVars,params);
		if(cmdList1==null)
			cmdList1=cmdList;
		return cmdList1;
	}
	
	public void setStreamVars(ChannelStreamVars vars) {
		streamVars=vars;
	}
	
	public String toString() {
		return super.toString()+" url "+url+" proc "+processor+" "+" real "+realUrl;
	}
	
	public String write() {
		//ch,url,processor,format,this,noSubs,imdb,
		   //embedSub,stash
		String lpr =url;
		if(scraper!=null) {
			lpr=scraper.lastPlayResolveURL(this.getParent());
			if(ChannelUtil.empty(lpr))
				lpr=url;
			else
				lpr="resolve@"+lpr;
		}
		String res= lpr+">"+ch.getName()+">"+playlistName()+">"+ChannelUtil.format2str(format)+">"+thumb+
		">"+processor+">"+realUrl;
		res=res.replaceAll("[\n\r]", "");
		return res;
	}
	
	public byte[] getHeaders() {
		return hdr.trim().getBytes();
	}
	
	public boolean isURLResolved() {
		if(scraper==null)
			return false;
		return !scraper.getBoolProp("do_resolve");
	}
	
	private boolean live() {
		if(ChannelUtil.empty(realUrl)) {
			if(scraper==null)
				return false;
			return scraper.getBoolProp("live");
		}
		return scraper.getBoolProp("live")||
			   realUrl.contains("live=")||
			   realUrl.contains("-v");
	}
	
	public boolean isResumeable() {
		boolean b=true;
		if (getFormat() != null) {
			// Only resume videos
			b=getFormat().isVideo();
		}
		if(scraper!=null) {
			b=b&&!live();
		}
		return b;
	}
	
	public String urlResolve() {
		if(Channels.cfg().usePMSEncoder()) {
			if(fool) {
				if(realUrl.startsWith("subs://"))
					fixStuff(realUrl.substring(7),true);
				else if(realUrl.startsWith("navix://")) {
					fixStuff(realUrl.substring(8+8),false);
				}
			}
		}
		else {
			if(realUrl.startsWith("subs://"))
				fixStuff(realUrl.substring(7),true);
			else if(realUrl.startsWith("navix://")) 
				fixStuff(realUrl.substring(8+8),false);
			else if(realUrl.startsWith("rtmpdump://")) {
				rtmpUrl(realUrl.substring(11+8));
			}
			else if(realUrl.startsWith("rtmp://")) {
				rtmpUrl(realUrl.substring(7));
			}
		}
		return getSystemName();
	}
}
