package com.sharkhunter.channel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.encoders.Player;
import net.pms.external.AdditionalFolderAtRoot;
//import net.pms.external.ExternalPlaylist;
import net.pms.external.FinalizeTranscoderArgsListener;
import net.pms.external.StartStopListener;
import net.pms.io.OutputParams;

public class CH_plugin implements AdditionalFolderAtRoot, StartStopListener
						, FinalizeTranscoderArgsListener/*,
						ExternalPlaylist*/ {

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
			chRoot=new Channels(path,pluginName,img);
			cfg=new ChannelCfg(chRoot);
			cfg.init();
			chRoot.setCfg(cfg);
			Channels.debug("starting");
			chRoot.start(getInterval());
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
			chRoot.debug("init exp "+e);
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
		cfg.init();
		return gui.draw();
	}

	@Override
	public void donePlaying(DLNAMediaInfo arg0, DLNAResource arg1) {
		if(arg1 instanceof ChannelMediaStream)
			((ChannelMediaStream)arg1).donePlaying();
	}

	@Override
	public void nowPlaying(DLNAMediaInfo arg0, DLNAResource arg1) {
		if(arg1 instanceof ChannelMediaStream)
			((ChannelMediaStream)arg1).nowPlaying();
	}
	
	private void removeArg(List<String> list,String arg) {
		removeArg(list,arg,false);
	}
	
	private void removeArg(List<String> list,String arg,boolean boolOp) {
		int pos;
		if((pos=list.indexOf(arg))!=-1) {
			/*list.set(pos,"-vcodec");
			list.set(pos+1, "copy");
			list.add(pos+2,"-acodec");
			list.add(pos+3,"copy");*/
			list.set(pos, "-f");
			list.set(pos+1,"copy");
		}
	}
	
	private void dbgArg(List<String> cmdList) {
		for(int i=0;i<cmdList.size();i++)
			Channels.debug("arg "+i+":"+cmdList.get(i));
		Channels.debug("############");
	}

	@Override
	public List<String> finalizeTranscoderArgs(Player player, String name,
			DLNAResource res, DLNAMediaInfo media, OutputParams params,
			List<String> cmdList) {
		Channels.debug("finalize args:");
		Channels.debug("name "+name+" params "+params.toString());
		Channels.debug("player "+player.name());
		dbgArg(cmdList);
		if((!(res instanceof ChannelMediaStream)))
			return cmdList;		
		ChannelMediaStream cms=(ChannelMediaStream)res;
		String f=cms.realFormat();
		/*if(ChannelUtil.empty(f))
			return cmdList;*/
		
		boolean pipeSeen=false;
		String pipeName=null;
		ArrayList<String> out=new ArrayList<String>();
		for(int i=0;i<cmdList.size();i++) {
			String arg=cmdList.get(i);
			if(arg.equals("|")) {
				pipeSeen=true;
				continue;
			}
			if(arg.startsWith("\\\\.\\pipe\\")) {
				pipeName=arg;
				continue;
			}
		}
		String curlPath=(String)PMS.getConfiguration().getCustomProperty("curl.path");
		String cookiePath=Channels.cfg().getCookiePath();
		out.add(curlPath);
		out.add("-s");
		out.add("-S");
		out.add("-b");
		out.add(cookiePath);
		out.add("--location-trusted");
		out.add("--output");
		out.add(pipeName);
		Channels.debug("full "+cms.fullUrl());
		out.add(cms.fullUrl());
		return out;
		
/*		if(res.getSystemName().startsWith("rtmp")&&player.name().equals("PMSEncoder")) {
			RendererConfiguration r=params.mediaRenderer;				
			//if(r.isPS3()||r.isXBOX())
		/*	if(!r.isBRAVIA()) {
				boolean pipeSeen=false;
				String pipeName=null;
				ArrayList<String> out=new ArrayList<String>();
				for(int i=0;i<cmdList.size();i++) {
					String arg=cmdList.get(i);
					if(arg.equals("|")) {
						pipeSeen=true;
						continue;
					}
					if(arg.startsWith("\\\\.\\pipe\\")) {
						pipeName=arg;
						continue;
					}
					if(arg.equals("-o")||arg.equals("-"))
						continue;
					if(!pipeSeen)
						out.add(arg);
				}
				if(ChannelUtil.empty(pipeName))
					return cmdList;
				out.add("-o");
				out.add(pipeName);
				dbgArg(out);
				return out;
			}
			dbgArg(cmdList);
			return cmdList;
		}*/
	}
	
	/*public DLNAResource fromPlaylist(String name,String uri,String thumb,
									 String extra,String className) {
		Channels.debug("call from pl "+name+" "+uri+" "+extra);
		String[] es=extra.split(",");
		Channel ch=Channels.findChannel(es[0]);
		Channels.debug("ch is "+ch+" es "+es[0]);
		if(ch==null)
			return null;
		String proc=null;
		int type=ch.getFormat();
		int asx=ChannelUtil.ASXTYPE_AUTO;
		if(es.length>1) {
			//proc=es[1];
		}
		if(es.length>2)
			type=ChannelUtil.getFormat(es[1]);
		if(ChannelUtil.empty(name))
			name="Unknown";
		return (new ChannelMediaStream(ch,name,uri,thumb,proc,type,asx,(ChannelScraper)null)); 	
	}*/
	
}
