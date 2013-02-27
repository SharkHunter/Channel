package com.sharkhunter.channel;

import java.io.IOException;
import java.io.InputStream;

import net.pms.dlna.DLNAResource;

public class ChannelResource extends DLNAResource {
	
	private String res;
	private String thumb;
	private String name;
	
	private static final String redXMov="videos/button_cancel-512.mpg";
	private static final String redXThumb="images/icon-videothumbnail-cancel.png";
	
	public static String redXUrl() {
		if(ChannelUtil.empty(Channels.cfg().badURL()))
			return redXMov;
		return Channels.cfg().badURL();
	}
	
	private static String tURL() {
		return redXThumb;
	}
	
	public static ChannelResource redX() {
		return redX("Error");
	}
	
	public static ChannelResource redX(String name) {
		return new ChannelResource(name,redXUrl(),tURL());
	}
	
	public static InputStream redXStream() throws IOException {
		return redX("").getInputStream();
	}
	
	public static String NullURL(String url) {
		if(!ChannelUtil.empty(url))
			return url;
		if(!ChannelUtil.empty(Channels.cfg().nullURL()))
			return Channels.cfg().nullURL();
		return url;
	}
	
	public ChannelResource(String name,String res) {
		this(name,res,null);
	}
	
	public ChannelResource(String name,String res,String thumb) {
		this.name=name;
		this.res=res;
		this.thumb=thumb;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return getResourceInputStream(res);
	}
	
	public InputStream getThumbnailInputStream() throws IOException {
    	if (thumb != null) {
    		try {
    			return getResourceInputStream(thumb);
    		}
    		catch (Exception e) {
    		}
    	}
		return super.getThumbnailInputStream();
    }

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSystemName() {
		return getName();
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public long length() {
		return 0;
	}
	

}
