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
import java.util.Collections;

import net.pms.PMS;
import net.pms.configuration.FormatConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.encoders.Player;
import net.pms.formats.Format;
import net.pms.formats.WEB;

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
	}
	
	public void noSubs() {
		noSubs=true;
	}
	
	public void setRender(RendererConfiguration r) {
		render=r;
	}
	
	
    public InputStream getThumbnailInputStream() throws IOException {
    	if (thumb != null) {
    		try {
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
    	// need to update player as well
    	int i=0;
    	Player pl=null;
        while (pl == null && i < ext.getProfiles().size()) {
                pl = PMS.get().getPlayer(ext.getProfiles().get(i), ext);
                i++;
        }        	
        // if we didn't find a new player leave the old one
        if(pl!=null)
        	player=pl;
    }
    
    public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
    	if(parent instanceof ChannelPMSSaveFolder)
    		if(((ChannelPMSSaveFolder)parent).preventAutoPlay())
    			return null;
    	if(scraper!=null)
    		realUrl=scraper.scrape(ch,url,processor,format,this,noSubs,imdb);
    	else
    		realUrl=ChannelUtil.parseASX(url, ASX);
    	if(ChannelUtil.empty(realUrl))
    		return null;
    	updateStreamDetails();
    	ch.prepareCom();
    	InputStream is=super.getInputStream(low,high,timeseek,mediarenderer);
    	if((saveName!=null)||Channels.cache()) {
    		return startSave(is);
    	}
    	else
    	    return is;
    }
    
    private InputStream getStream() {
    	try {
			URL urlobj = new URL(realUrl);
			Channels.debug("Retrieving " + urlobj.toString());
			URLConnection conn = urlobj.openConnection();
			conn.setRequestProperty("User-Agent",ChannelUtil.defAgentString);
			ChannelAuth auth=ch.prepareCom();
			String cookie="";
			if(auth!=null) {
				if(auth.method==ChannelLogin.STD)
					conn.setRequestProperty("Authorization", auth.authStr);
				else if(auth.method==ChannelLogin.COOKIE) 
					cookie=ChannelUtil.append(cookie,"; ",auth.authStr);
			}
			if(!ChannelUtil.empty(cookie))
				conn.setRequestProperty("Cookie",cookie);
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
    	Channels.debug("cms getinp/0 scrape "+scraper);
    	if(scraper!=null)
    		realUrl=scraper.scrape(ch,url,processor,format,this,noSubs,imdb);
    	else
    		realUrl=ChannelUtil.parseASX(url, ASX);
    	if(ChannelUtil.empty(realUrl))
    		return null;
    	return getStream();
    }
    
    private InputStream startSave(InputStream is) throws IOException {
    	String sName=saveName;
    	boolean cache=ChannelUtil.empty(saveName);
    	if(cache)
    		sName=dispName;
    	String fName=Channels.fileName(sName,cache);
    	fName=ChannelUtil.guessExt(fName,realUrl);
    	if(cache)
    		ChannelUtil.cacheFile(new File(fName),"media");
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
    	return name;
    }

    public boolean isFolder() {
    	return false;
    }

    public long lastModified() {
    	return 0;
    }

    public String getSystemName() {
    	if(ChannelUtil.empty(realUrl))
    		return url;
    	else 
    		return realUrl;
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


}
