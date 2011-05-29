package com.sharkhunter.channel;
import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.external.AdditionalFolderAtRoot;

import javax.swing.*;
import java.io.*;

public class CH_plugin implements AdditionalFolderAtRoot {

	private static final long DEFAULT_POLL_INTERVAL=20000;
	private Channels chRoot;
	private ChannelCfg cfg;
	private ChannelGUI gui;

	public CH_plugin() {
		try {
			File chFolder=new File("channels");
			String pluginName=(String)PMS.getConfiguration().getCustomProperty("channels.name");
			String img=(String)PMS.getConfiguration().getCustomProperty("channels.img");
			if(ChannelUtil.empty(pluginName))
				pluginName="Channels";
			if(ChannelUtil.empty(img))
				img="http://findicons.com/icon/download/226723/tv/128/png";
			String confPath=(String)PMS.getConfiguration().getCustomProperty("channels.path");
			String path;
			if(confPath==null) {
				chFolder.mkdir();
				path=chFolder.toString();
			}
			else 
				path=confPath;
			String save=(String)PMS.getConfiguration().getCustomProperty("channels.save");
			chRoot=new Channels(path,getInterval(),pluginName,img);
			cfg=new ChannelCfg(chRoot);
			cfg.init();
			chRoot.setCfg(cfg);
			if(save!=null) {
				String ts=(String)PMS.getConfiguration().getCustomProperty("channels.save_ts");
				String savePath=save;
				if(ChannelUtil.empty(save.trim()))
					savePath=path+File.separator+"saved";
				chRoot.setSave(savePath,ts);
				cfg.ensureCreated(savePath);
				cfg.setSavePath(savePath);
			}
			gui=new ChannelGUI(cfg,chRoot);
			String dPath=confPath+File.separator+"data";
			cfg.ensureCreated(dPath);
			cfg.commit();
			ChannelNaviXNookie.init(new File(dPath+File.separator+"nookie"));
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
		return chRoot.getDisplayName();
	}

    //@Override
	public JComponent config() {
		//JFrame frame=new JFrame("Configure Channels");
		cfg.init();
		return gui.draw();
	}
	
}
