package com.sharkhunter.channel;
import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.external.AdditionalFolderAtRoot;

import javax.swing.*;

import java.io.*;

public class CH_plugin implements AdditionalFolderAtRoot {

	private static final long DEFAULT_POLL_INTERVAL=20000;
	private Channels chRoot;

	public CH_plugin() {
		try {
			File chFolder=new File(PMS.getConfiguration().getTempFolder(),"channels");
			String confPath=(String)PMS.getConfiguration().getCustomProperty("channels.path");
			String path;
			if(confPath==null) {
				chFolder.mkdir();
				path=chFolder.toString();
			}
			else 
				path=confPath;
			chRoot=new Channels(path,getInterval());
		}
		catch (Exception e) {
			PMS.debug("exp "+e)	;
		}
	}

	private long getInterval() {
		String interval=(String)PMS.getConfiguration().getCustomProperty("channels.poll");
		if(interval!=null) {
			try {
				Long l=Long.parseLong(interval);
				return l.longValue();
			}
			catch (Exception e) {
				PMS.minimal("Illegal interval value "+e.toString());
			}
		}
		return CH_plugin.DEFAULT_POLL_INTERVAL;
	}

	public DLNAResource getChild() {
		return chRoot;
	}

	public void shutdown() {
	}

	public String name() {
		return "Channels";
	}

    //@Override
	public JComponent config() {
		return null;
	}
}
