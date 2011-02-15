package com.sharkhunter.channel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.WebStream;

public class ChannelMediaStream extends DLNAResource {

	private String url;
	private String thumb;
	private String name;
	private String processor;
	private int format;
	private Channel ch;
	private String realUrl;
	
	public ChannelMediaStream(Channel ch,String name,String nextUrl,
							  String thumb,String proc,int type) {
		super(type);
		url=nextUrl;
		this.name=name;
		this.thumb=thumb;
		this.processor=proc;
		this.format=type;
		this.ch=ch;
		realUrl=null;
	}
	
    public InputStream getThumbnailInputStream() throws IOException {
    	if (thumb != null)
    		return downloadAndSend(thumb, true);
        else
        	return super.getThumbnailInputStream();
    }
    
    public InputStream getInputStream(long low, long high, double timeseek, RendererConfiguration mediarenderer) throws IOException {
    	realUrl=ChannelNaviXProc.parse(ch,url,processor);
    	return super.getInputStream(low,high,timeseek,mediarenderer);
    }


    public InputStream getInputStream() {
    	realUrl=ChannelNaviXProc.parse(ch,url,processor);
    	if(ChannelUtil.empty(realUrl))
    		return null;
    	try {
			URL urlobj = new URL(realUrl);
			Channels.debug("Retrieving " + urlobj.toString());
			//ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			URLConnection conn = urlobj.openConnection();
			conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE; rv:1.9.2.3) Gecko/20100409 Firefox/3.6.3");
			if(!ChannelUtil.empty(ch.getAuth()))	
				conn.setRequestProperty("Authorization", ch.getAuth());
			InputStream in = conn.getInputStream();
			return in;
		}
		catch (Exception e) {
			Channels.debug("error reading "+e);
			return null;
		}
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
