package com.sharkhunter.channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;

import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;

public class ChannelMediaStream extends DLNAResource {

	private String url;
	private String thumb;
	private String name;
	private String processor;
	private int format;
	private Channel ch;
	private String realUrl;
	private boolean autoASX;
	private ChannelScraper scraper;
	private String saveName; 
	
	public ChannelMediaStream(Channel ch,String name,String nextUrl,
							  String thumb,String proc,int type,boolean asx,
							  ChannelScraper scraper) {
		this(ch,name,nextUrl,thumb,proc,type,asx,scraper,null);
		
	}
	public ChannelMediaStream(Channel ch,String name,String nextUrl,
							  String thumb,String proc,int type,boolean asx,
							  ChannelScraper scraper,String saveName) {
		super(type);
		url=nextUrl;
		this.name=name;
		this.thumb=thumb;
		this.processor=proc;
		this.format=type;
		this.ch=ch;
		realUrl=null;
		autoASX=asx;
		this.scraper=scraper;
		this.saveName=saveName;
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
    
    public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
    	if(scraper!=null)
    		realUrl=scraper.scrape(ch,url,processor,format);
    	else
    		realUrl=url;
    	if(autoASX&&ChannelUtil.isASX(realUrl))
    		realUrl=ChannelUtil.parseASX(realUrl);
    	if(ChannelUtil.empty(realUrl))
    		return null;
    	InputStream is=super.getInputStream(low,high,timeseek,mediarenderer);
    	if(saveName!=null) {
    		return startSave(is);
    	}
    	else
    	    return is;
    }


    public InputStream getInputStream() {
    	if(scraper!=null)
    		realUrl=scraper.scrape(ch,url,processor,format);
    	else
    		realUrl=url;
    	if(ChannelUtil.empty(realUrl))
    		return null;
    	try {
			URL urlobj = new URL(realUrl);
			Channels.debug("Retrieving " + urlobj.toString());
			URLConnection conn = urlobj.openConnection();
			conn.setRequestProperty("User-Agent",ChannelUtil.defAgentString);
			ChannelAuth auth=ch.getAuth();
			String cookie="";
			if(auth!=null) {
				if(auth.method==ChannelLogin.STD)
					conn.setRequestProperty("Authorization", auth.authStr);
				else if(auth.method==ChannelLogin.COOKIE) 
					cookie=ChannelUtil.append(cookie,"; ",auth.authStr);
			}
			if(!ChannelUtil.empty(cookie))
				conn.setRequestProperty("Cookie",cookie);
			InputStream is = conn.getInputStream();
			if(saveName!=null) {
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
    
    private InputStream startSave(InputStream is) throws IOException {
    	String fName=Channels.fileName(saveName);
    	fName=ChannelUtil.guessExt(fName,realUrl);
		BufferedOutputStream fos=new BufferedOutputStream(new FileOutputStream(fName));
 	   	PipedOutputStream pos=(new PipedOutputStream());
 	   	PipedInputStream pis=new PipedInputStream(pos);
		OutputStream[] oss=new OutputStream[2];
		oss[0]=fos;
		oss[1]=pos;
		Thread cs=new Thread(new ChannelSaver(is,oss));
		cs.start(); 
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
		checktype();
		return true;
	}


}
