package com.sharkhunter.channel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;

public class ChannelCacheItem extends VirtualFolder {
	
	private File f;
	
	public ChannelCacheItem(String name,File f) {
		super(name,null);
		this.f=f;
	}
	
	public void discoverChildren() {
		addChild(new VirtualVideoAction("SAVE "+name, true) { //$NON-NLS-1$
            @Override
            public boolean enable() {
            	String sPath=Channels.getSavePath();
            	if(ChannelUtil.empty(sPath))
            		sPath=Channels.getPath();
            	try {
            		Channels.debug("save (from cache) file "+name);
            		FileReader in = new FileReader(f);
            		FileWriter out = new FileWriter(sPath+File.separator+name);
            		int c;

            		while ((c = in.read()) != -1)
            			out.write(c);

            		in.close();
            		out.close();
            		f.delete();
            	}
            	catch (Exception e) {
            	}
            	return true;
            }
		});
		addChild(new VirtualVideoAction("DELETE "+name, true) { //$NON-NLS-1$
            @Override
            public boolean enable() {
            	Channels.debug("delete (from cache) file "+name);
            	f.delete();
            	return true;
            }
		});
	}
	
	public boolean isRefreshNeeded() {
		return true;
	}

}
