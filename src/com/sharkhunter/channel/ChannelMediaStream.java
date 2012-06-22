package com.sharkhunter.channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
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
import net.pms.formats.Format;
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
    
    
    private void updateStreamDetails() {
    	Format old_ext=ext;
    	// update format, use checktype to be future proof
    	ext=null;
    	checktype();
    	if(ext==null) { // no ext found restore what we got and bail out
    		ext=old_ext;
    		return;
    	}	
    	if(ext.getProfiles()==null) // no profiles, what do we do? give up
    		return;
    	// need to update player as well
    	int i=0;
    	Player pl=null;
        while (pl == null && i < ext.getProfiles().size()) {
                pl = PMS.get().getPlayer(ext.getProfiles().get(i), ext);
                i++;
        }
        String name = getName();
		
		for (Class<? extends Player> clazz : ext.getProfiles()) {
			for (Player p : PMS.get().getPlayers()) {
				if (p.getClass().equals(clazz)) {
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
			}
		}
		// if we didn't find a new player leave the old one
      //  if(pl!=null)
		Channels.debug("set player to "+pl);
        	player=pl;
    }
    
    public void scrape() {
    	if(!scraped) {
    		if(scraper!=null)
    			realUrl=scraper.scrape(ch,url,processor,format,this,noSubs,imdb,
    								   embedSub,stash);
    		else
    			realUrl=ChannelUtil.parseASX(url, ASX);
    		scrapeTime=System.currentTimeMillis();
    	}
    	scraped=true;
    }
    
    private void scrape_i() {
    	Channels.debug("scrape "+name+" nosubs "+noSubs);
    	fool=Channels.cfg().netDiscStyle();
    	if(scraped)
    		return;
    	scrape();
    	if(ChannelUtil.empty(realUrl))
    		return ;
    	Channels.debug("real "+realUrl+" nd "+fool+" noSubs "+noSubs);
    	if(fool) {
    		if(realUrl.startsWith("subs://"))
    			fixStuff(realUrl.substring(7),true);
    		else if(realUrl.startsWith("navix://")) {
    			fixStuff(realUrl.substring(8),false);
    		}
    	}
    	if(media==null) {
    		media=new DLNAMediaInfo();
    		media.audioCodes=new ArrayList<DLNAMediaAudio>();
    	}
    	if(noSubs) { // make sure subs are off here
			media_subtitle=new DLNAMediaSubtitle();
			media_subtitle.id=-1;
		}
    	Channels.debug("call update");
    	updateStreamDetails();
    	fool=false;
    	ch.prepareCom();
    }
    
    
    public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
    	if(parent instanceof ChannelPMSSaveFolder)
    		if(((ChannelPMSSaveFolder)parent).preventAutoPlay())
    			return null;
    	scrape_i();
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
    	Channels.debug("cms getinp/2... scrape "+scraper+" url "+realUrl);
    	scrape_i();
    	if(delayed())
    		return null;
    	InputStream is=super.getInputStream(range,mediarenderer);
    	if((saveName!=null)||Channels.cache()) {
    		return startSave(is);
    	}
    	else
    	    return is;
    }
    
    private InputStream getStream() {
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
    	scrape_i();
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
		if(u.startsWith("http")&&fool)
			return ensureExt(u.substring(u.lastIndexOf("/")+1));
		return (u); // need this to
    }
    
    private boolean legalExt(String ext) {
    	if(ChannelUtil.empty(ext))
    		return false;
    	ext=ext.substring(1); // remove the dot
    	ArrayList<Format> formats=PMS.get().getExtensions();
    	for(Format f : formats) {
    		String[] supported=f.getId();
    		for(int i=0;i<supported.length;i++)
    			if(ext.equals(supported[i]))
    				return true;
    	}
    	return false;
    }
    
    private String ensureExt(String str) {
    	if(legalExt(ChannelUtil.extension(str))) {
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
			ext = PMS.get().getAssociatedExtension("dummy.avi");
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
	}
	
	public void nowPlaying() {
		startTime=System.currentTimeMillis();
	}
	
	public void setImdb(String i) {
		imdb=i;
	}
	
	private void fixStuff(String str,boolean subs) {
		String[] splits=str.split("&");
		for(int i=0;i<splits.length;i++) {
			if(splits[i].contains("url=")) {
				String tmp=splits[i].substring(splits[i].indexOf("url=")+4);
				realUrl=ChannelUtil.unescape(tmp);
				Channels.debug("set sub url "+realUrl);
				continue;
			}
			if(splits[i].contains("subs=")&&!noSubs&&subs) {
				DLNAMediaSubtitle sub=new DLNAMediaSubtitle();
				String tmp=splits[i].substring(splits[i].indexOf("subs=")+5);
				sub.file=new File(ChannelUtil.unescape(tmp));
				sub.type=DLNAMediaSubtitle.SUBRIP;
				sub.id=1;
				sub.lang="und";
				media.container="unknown"; // avoid bug in mencvid
				media_subtitle=sub;
				Channels.debug("set sub file "+sub.file.getAbsolutePath());
				continue;
			}
		}
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
			return ChannelUtil.backTrack(this, 0);
	}
	
	public String playlistURI() {
		if(scraper==null)
			ChannelUtil.parseASX(url, ASX);
		if(ChannelUtil.empty(processor))
			return scraper.scrape(ch,url,processor,format,this,noSubs,imdb,
								  embedSub,stash);
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
		if(streamVars==null)
			return cmdList;
		List<String> cmdList1=ChannelUtil.addStreamVars(cmdList,streamVars,params);
		if(cmdList1==null)
			cmdList1=cmdList;
		return cmdList1;
	}
	
	public void setStreamVars(ChannelStreamVars vars) {
	//	streamVars=vars;
	}
	
	public String toString() {
		return super.toString()+" url "+url+" proc "+processor+" "+" real "+realUrl;
	}
	
	
	
}
