package com.sharkhunter.channel;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JComponent;

import org.apache.commons.configuration.ConfigurationException;

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
						, FinalizeTranscoderArgsListener {

	private static final long DEFAULT_POLL_INTERVAL=20000;
	private static boolean initFetchPending=false;
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
			if(initFetchPending) {
				initFetchPending=false;
				cfg.fetchChannels();
			}
			Channels.debug("starting");
			chRoot.setCfg(cfg);
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
	
	public static void postInstall() {
		initFetchPending=true;
		PMS.getConfiguration().setCustomProperty("channels.path", "extras\\channels");
		PMS.getConfiguration().setCustomProperty("pmsencoder.script.directory" ,"extras\\scripts");
		PMS.getConfiguration().setCustomProperty("cookie.path","extras\\cookies");
		PMS.getConfiguration().setCustomProperty("perl.path","extras\\perl\\bin\\perl.exe");
		PMS.getConfiguration().setCustomProperty("python.path","extras\\Python27\\python.exe");
		PMS.getConfiguration().setCustomProperty("rtmpdump.path","extras\\bin\\rtmpdump.exe");
		PMS.getConfiguration().setCustomProperty("youtube-dl.path","extras\\bin\\youtube-dl.exe");
		try {
			PMS.getConfiguration().save();
		} catch (ConfigurationException e) {
		}
		ZipInputStream zis;
		File pepy=new File("extras" + File.separator + "pepy.zip");
		if(!pepy.exists())
			return;
		try {
			zis = new ZipInputStream(new FileInputStream(pepy));
			ZipEntry entry;
			 while((entry = zis.getNextEntry()) != null) {
				 File dst=new File("extras" + File.separator + entry.getName());
				 if(entry.isDirectory()) {
					 dst.mkdirs();
					 continue;
				 }
				 int count;
				 byte data[] = new byte[4096];
				 FileOutputStream fos = new FileOutputStream(dst);
				 BufferedOutputStream dest = new BufferedOutputStream(fos, 4096);
				 while ((count = zis.read(data, 0, 4096)) != -1) {
					 dest.write(data, 0, count);
				 }
				 dest.flush();
				 dest.close();
			 }
			 zis.close();
		 } catch (Exception e) {
			 PMS.info("unzip error "+e);
		 }
		 pepy.delete();
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
		if((!(res instanceof ChannelMediaStream)))
			return cmdList;
		ChannelMediaStream cms=(ChannelMediaStream)res;
		return cms.addStreamvars(cmdList,params);
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
