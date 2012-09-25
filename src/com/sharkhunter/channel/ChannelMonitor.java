package com.sharkhunter.channel;

import java.net.MalformedURLException;
import java.util.ArrayList;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;

public class ChannelMonitor {
	
	private ChannelFolder cf;
	private ArrayList<String> oldEntries;
	private String name;

	ChannelMonitor(ChannelFolder cf,ArrayList<String> oldEntries,String name) {
		this.cf=cf;
		this.oldEntries=oldEntries;
		this.name=name;
	}
	
	public void scan() {
		Channels.debug("scanning "+name);
		VirtualFolder dummy=new VirtualFolder(null,null);
		try {
			cf.match(dummy);
		} catch (MalformedURLException e) {
			return;
		}
		for(DLNAResource r : dummy.getChildren()) {
			if(oldEntries.contains(r.getName().trim())) 
				continue;
			Channels.addNewMonitoredMedia(r,getName().trim());
		}
	}
	
	public String getName() {
		return name;
	}
	
	public boolean addEntry(String newEntry) {
		Channels.debug("add entry "+newEntry+" old "+oldEntries.contains(newEntry));
		if(!oldEntries.contains(newEntry)) {
			oldEntries.add(newEntry);
			return true;
		}
		return false;
	}
}
