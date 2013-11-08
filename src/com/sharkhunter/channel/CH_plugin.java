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

import com.sun.jna.Platform;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.WebStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.encoders.Player;
import net.pms.external.AdditionalFolderAtRoot;
import net.pms.external.FinalizeTranscoderArgsListener;
import net.pms.external.URLResolver;
//import net.pms.external.LastPlayedParent;
import net.pms.external.StartStopListener;
import net.pms.io.OutputParams;

public class CH_plugin implements AdditionalFolderAtRoot, StartStopListener, 
								  FinalizeTranscoderArgsListener, URLResolver {

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
	
	private static String linuxPath(String program) {
		ProcessBuilder pb=new ProcessBuilder("which",program);
		return ChannelUtil.execute(pb);
	}
	
	public static void unzip(String path,File f) {
		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
			ZipEntry entry;
			 while((entry = zis.getNextEntry()) != null) {
				 File dst=new File(path + File.separator + entry.getName());
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
	}
	
	public static void postInstall() {
		initFetchPending=true;
		PMS.getConfiguration().setCustomProperty("channels.path", "extras\\channels");
		PMS.getConfiguration().setCustomProperty("pmsencoder.script.directory" ,"extras\\scripts");
		PMS.getConfiguration().setCustomProperty("cookie.path","extras\\cookies");
		if(Platform.isWindows()) {
			PMS.getConfiguration().setCustomProperty("perl.path","extras\\perl\\bin\\perl.exe");
			PMS.getConfiguration().setCustomProperty("python.path","extras\\Python27\\python.exe");
			PMS.getConfiguration().setCustomProperty("youtube-dl.path","extras\\bin\\youtube-dl.exe");
			PMS.getConfiguration().setCustomProperty("rtmpdump.path","extras\\bin\\rtmpdump.exe");
			PMS.getConfiguration().setCustomProperty("curl.path","extras\\bin\\curl.exe");
		}
		else if(Platform.isLinux()) {
			PMS.getConfiguration().setCustomProperty("perl.path",linuxPath("perl"));
			PMS.getConfiguration().setCustomProperty("python.path",linuxPath("python"));
			PMS.getConfiguration().setCustomProperty("rtmpdump.path",linuxPath("rtmpdump"));
			PMS.getConfiguration().setCustomProperty("curl.path",linuxPath("curl"));
		}
		File pepy=new File("extras" + File.separator + "pepy.zip");
		if(pepy.exists()) {
			unzip("extras", pepy);
			pepy.delete();
		}
		if(Platform.isWindows()) {
			File pywin=new File("extras" + File.separator + "pywin.zip");
			if(pywin.exists()) {
				String pylib="extras\\Python27\\Lib\\site-packages";
				if(new File(pylib).exists()) {
					unzip(pylib,pywin);
					PMS.getConfiguration().setCustomProperty("python.pywin_extra","true");
				}
				pywin.delete();
			}
		}
		try {
			PMS.getConfiguration().save();
		} catch (ConfigurationException e) {
		}
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
		if(!Channels.cfg().useStreamVar())
			return cmdList;
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
	
	public static void main(String[] args) {
		try {
			PMS.setConfiguration(new PmsConfiguration());
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.exit(0);
		}
		PMS.getConfiguration().setFfmpegAlternativePath("ffmpeg.exe");
		Channels chRoot=new Channels(".","","");
		ChannelCfg cfg=new ChannelCfg(chRoot);
		cfg.setStdAlone(true);
		cfg.setRtmpPath("rtmpdump.exe");
		chRoot.setCfg(cfg);
		chRoot.setSave(".");
		cfg.setSavePath(".");
		chRoot.start(0);
		if(args.length>3)
			cfg.setCrawlFL(args[3]);
		if(args.length>4)
			cfg.setCrawlHL(args[4]);
		boolean allCrawl=(cfg.getCrawlFLMode()==ChannelCrawl.CRAWL_ALL);
		ArrayList<Thread> threads=new ArrayList<Thread>();
		Channel ch=Channels.findChannel(args[0]);
		if(ch==null) {
			System.out.println("No channel "+args[0]+" found");
			System.exit(0);
		}
		String action=args[1];
		ChannelSwitch swi=new ChannelSwitch(action);
		VirtualFolder dummy=new VirtualFolder(null,null);
		ChannelFolder cf=ch.getAction(action);
		ch.action(swi, "", args[2], "", dummy, -1);
		ChannelCrawl crawler=new ChannelCrawl();
		String modeStr="FLA";
		if(cf!=null) {
			modeStr=cf.getProp("crawl_mode");
			if(ChannelUtil.empty(modeStr))
				modeStr="FLA";
		}
		DLNAResource r=crawler.startCrawl(dummy,modeStr);
		if(r==null && !(r instanceof ChannelMediaStream)) { // nothing found give up
			System.out.println("Nothing found give up");
			System.exit(0);
		}
		ChannelMediaStream cms=(ChannelMediaStream)r;
		cms.scrape(null);
		String url=cms.getSystemName();
		String outFile;
		if(args.length>6&&!allCrawl)
			outFile=args[6];
		else {
			outFile=crawler.goodName();
			if(ChannelUtil.empty(ChannelUtil.extension(outFile)))
				outFile=outFile+Channels.cfg().getCrawlFormat();
		}
		
		Thread t=ChannelUtil.newBackgroundDownload(outFile,url);
		t.start();
		threads.add(t);
		System.out.println("Downloading "+crawler.goodName()+" to "+outFile);
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("Done");
		System.exit(0);
	}
	
	private static final String DUMMY_URL = "http://dummy_url.dummy.dummy/";

	public DLNAResource create(String arg0) {
		String[] tmp=arg0.split(">");
		Channel ch = Channels.findChannel(tmp[1]);
		if(ch==null) // channel is gone?
			return null;
		Channels.debug("create lp cms "+tmp[2]+" ru "+tmp[0]+" channel "+ch);
		int format=-1;
		if(tmp.length>3)
			format=ChannelUtil.getFormat(tmp[3]);
		String thumb="";
		if(tmp.length>4)
			thumb=tmp[4];
		Channels.debug("format is "+format+" thumb "+thumb);
		if(tmp[0].startsWith("resolve@")) {
			String url=tmp[0].substring(8);
			if(!url.contains("://")) {
				url=DUMMY_URL+url;
			}
			return new ChannelResolve(tmp[2],url,thumb,ch,format);
		}
		return new ChannelMediaStream(tmp[2],tmp[0],ch,format,thumb);
	}

	
	public URLResult urlResolve(String url) {
		URLResult res = new URLResult();
		boolean dummyOnly=url.contains(DUMMY_URL);
		url=url.replace(DUMMY_URL, "");
		res.url=chRoot.urlResolve(url,dummyOnly);
		if(!ChannelUtil.empty(res.url)) {
			if(res.url.startsWith("precoder://")) {
				res.url=res.url.substring(11);
				String[] tmp=res.url.split("####");
				res.precoder=new ArrayList<String>();
				if(ChannelUtil.extension(tmp[0]).equals(".py"))
					res.precoder.add(Channels.cfg().getPythonPath());
				if(ChannelUtil.extension(tmp[0]).equals(".pl"))
					res.precoder.add(Channels.cfg().getPerlPath());
				res.precoder.add(Channels.cfg().getScriptPath()+File.separator+tmp[0]);
				res.precoder.add(tmp[1]);
				res.url=null;
			}
		}
		return res;
	}
	
}
