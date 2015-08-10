package com.sharkhunter.channel;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;

public class ChannelImageStream extends DLNAResource{
	private String name;
	private String url;
	private String thumb;
	private String auth;

	public ChannelImageStream(String name,String url,String thumb,String auth) {
		super(Format.IMAGE);
		this.name=name;
		this.url=url;
		this.thumb=thumb;
		this.auth=auth;
	}

	public String getName() {
		return name;
	}

	public String getSystemName() {
		return url;
	}

	public boolean isUnderlyingSeekSupported() {
		return true;
	}

	@Override
	public void resolve() {
	}

	@Override
	public boolean isValid() {
		resolveFormat();
		return true;

	}

	@Override
	public long length() {
		return DLNAMediaInfo.TRANS_SIZE;
    }

	public boolean isFolder() {
        return false;
	}

	public InputStream getInputStream() {
		try {
			URL urlobj = new URL(url);
			Channels.debug("Retrieving " + urlobj.toString());
			//ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			URLConnection conn = urlobj.openConnection();
			conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE; rv:1.9.2.3) Gecko/20100409 Firefox/3.6.3");
			if(!ChannelUtil.empty(auth))
				conn.setRequestProperty("Authorization", auth);
			InputStream in = conn.getInputStream();
			return in;
		}
		catch (Exception e) {
			Channels.debug("error reading "+e);
			return null;
		}
	}
}
