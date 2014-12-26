package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.PlaylistFolder;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;
import no.geosoft.cc.io.FileListener;
import no.geosoft.cc.io.FileMonitor;

public class Channels extends VirtualFolder implements FileListener {

	// Version string
	public static final String VERSION="2.25";
	public static final String ZIP_VER="211";
	
	// Constants for RTMP string constructions
	public static final int RTMP_MAGIC_TOKEN=1;
	public static final int RTMP_DUMP=2;
	public int rtmp;
	
	public static final int DeafultContLim=5;
	public static final int ContSafetyVal=-100;
	
	// Define keywords
	private static final String[] defWords={"macrodef","scriptdef","subdef","proxydef"};
	
    private File file;
    private FileMonitor fileMonitor;
    private ArrayList<File> chFiles;
    private ArrayList<ChannelMacro> macros;
    private ArrayList<ChannelCred> cred;
    private HashMap<String,ChannelMacro> scripts;
    private HashMap<String,ChannelSubs> subtitles;
    private ChannelDbg dbg;
    private static Channels inst=null;
    private String savePath;
    private boolean appendTS;
    private boolean subs;
    private ChannelCache cache;
    private boolean doCache;
    private ChannelOffHour oh;
    private ChannelCfg cfg;
    private HashMap<String,ArrayList<ChannelAuth>> cookies;
    private ChannelSearch searchDb;
    private HashMap<String,ChannelProxy> proxies;
    private HashMap<String,HashMap<String,String>> stash;
    private boolean groupFolder;
    private ChannelMovieInfo movieInfo;
    private ArrayList<ArrayList<String>> favorite;
    private ChannelStreamVars defStreamVar;
    private ArrayList<ChannelIllegal> illegals;
    private ArrayList<ChannelIllegal> codes;
    private boolean allUnlocked;
    private ChannelSubs openSubs;
    private VirtualFolder monitor;
    private ChannelMonitorMgr monMgr;
    private PropertiesConfiguration credConf;
    
    public Channels(String path,String name,String img) {
    	super(name,img);
    	// First the simple fields
    	this.file=new File(path);
    	inst=this;
    	subs=true;
    	doCache=false;
    	savePath="";
    	oh=null;
    	appendTS=false;
    	groupFolder=false;
    	movieInfo=null;
    	openSubs=null;
    	// Setup list and tables
    	favorite=new ArrayList<ArrayList<String>>();
    	chFiles=new ArrayList<File>();
    	cred=new ArrayList<ChannelCred>();
    	scripts=new HashMap<String,ChannelMacro>();
    	subtitles=new HashMap<String,ChannelSubs>();
    	cookies=new HashMap<String,ArrayList<ChannelAuth>>();
    	proxies=new HashMap<String,ChannelProxy>();
    	stash=new HashMap<String,HashMap<String,String>>();
    	illegals=new ArrayList<ChannelIllegal>();
    	codes=new ArrayList<ChannelIllegal>();
    	cache=new ChannelCache(path);
    	defStreamVar=new ChannelStreamVars();
    	searchDb=new ChannelSearch(new File(dataPath()+File.separator+"search.txt"));
    	//rtmp=Channels.RTMP_MAGIC_TOKEN;
    	rtmp=Channels.RTMP_DUMP;
    	PMS.minimal("Start channel "+VERSION);
    	dbg=new ChannelDbg(new File(path+File.separator+"channel.log"));
    	dbg.start();
    	stash.put("default", new HashMap<String,String>());
    	credConf = new PropertiesConfiguration();
    	credConf.setListDelimiter((char) 0);
    	// Add std folders
    	try {
			ChannelNaviX local=new ChannelNaviX(null,"Local Playlist",null,
												localPLX().toURI().toURL().toString(),
												null,null);
			local.setIgnoreSave();
			local.setIgnoreFav();
			addChild(local);
		} catch (MalformedURLException e) {
		}
    	
    	addChild(cache);
    	addChild(searchDb);
    	addChild(new ChannelPMSCode("Unlock All",null,true));
    	fileMonitor=null;
    	allUnlocked=false;
    	setOpenSubs(true);
    }
    
    private void addMonitor() {
    	if(!cfg.monitor())
    		return;
    	monMgr=new ChannelMonitorMgr();
    	monitor=new ChannelPlainFolder("New Monitored Media");
    	addChild(monitor);
    	clearMonitor();
    }
    
    public void start(long poll) {
    	addMonitor();
    	// Move any work favorites first
    	if(workFavFile().exists()) {
    		try {
    			favFile().delete();
    			FileUtils.copyFile(workFavFile(), favFile());
    		} catch (IOException e) {
    		}
    	}
    	try {
    		FileUtils.copyFile(favFile(), workFavFile());
    	} catch (IOException e1) {
    	}
    	// Start filemonitoring
    	if(poll>0)
    		fileMonitor=new FileMonitor(poll);
    	// Parse the files for the first time
    	fileChanged(file);
    	if(poll>0) {
    		fileMonitor.addFile(file);
    		fileMonitor.addListener(this);
    	}
    	try {
    		initSearch(new File(dataPath()+File.separator+"search.txt"));
    	}
    	catch (Exception e) {
    		debug("Error reading search db "+e);
    	}
    	initNaviXUploader();
    	parseMonitorFile();
    	if(cfg.monitor())
    		monMgr.delayedScan();
    	try {
			dumpEmptyCreds();
		} catch (IOException e) {
		}
    }
    
    private void dumpEmptyCreds() throws IOException {
    	for(Channel ch : getChannels()) {
    		if(!ch.login())
    			continue;
    		String key = "channel."+ch.name();
    		if(credConf.getProperty(key) == null) {
    			// add this one
    			credConf.setProperty(key, "");
    		}
    	}
    	try {
			credConf.save();
		} catch (ConfigurationException e) {
			debug("error saving creds");
		}
    }
    
    private void initNaviXUploader() {
    	try { // must do this last we need some credentials first
    		if(ChannelUtil.empty(cfg.getNaviXUpload())) {
    			debug("No navix upload playlist configured");
    			return;
    		}
    		boolean added=false;
    		for(int i=0;i<cred.size();i++) {
        		ChannelCred cr=cred.get(i);
        		if(ChannelUtil.empty(cr.channelName))
        			continue;
        		if(cr.channelName.equalsIgnoreCase("navix")) {
        			ChannelNaviXUpdate.init(cfg.getNaviXUpload(),cr);
        			added=true;
        			break;
        		}
    		}
    		if(!added)
    			ChannelNaviXUpdate.init(cfg.getNaviXUpload(),null);
		} catch (Exception e) {
			debug("error navix up startup "+e);
		}
    }
    
    public static void initNaviX() {
    	inst.initNaviXUploader();
    }
    
    public static void debug(String msg) {
    	inst.dbg.debug("[Channel] "+msg);
    }
    
    public static void debug(boolean start) {
    	if(start)
    		inst.dbg.start();
    	else
    		inst.dbg.stop();
    }
    
    public static boolean debugStatus() {
    	return inst.dbg.status();
    }
    
    public static File dbgFile() {
    	return inst.dbg.logFile();
    }
    
	private Channel find(String name) {
    	for(DLNAResource f:getChildren()) {
    		if((f instanceof Channel)&&(f.getDisplayName().trim().equals(name.trim())))
    				return (Channel) f;
    	}
    	return null;
    }
    
    public static Channel findChannel(String name) {
    	if(inst==null)
    		new CH_plugin();
    	return inst.find(name);
    }
    
    public static ArrayList<Channel> getChannels() {
    	ArrayList<Channel> res=new ArrayList<Channel>();
    	for(DLNAResource f:inst.getChildren())
    		if(f instanceof Channel) {
    			res.add((Channel) f);
    		}
    	return res;
    }
    
    private void addFavorites() {
    	if(noFavorite())
    		return;
    	for(int i=0;i<favorite.size();i++) {
    		ArrayList<String> block=favorite.get(i);
    		String[] o=block.get(0).split("\\s*=\\s*");
	    	if(o.length<2&&!o[0].equalsIgnoreCase("owner"))
	    		continue;
    		Channel ch=find(o[1]);
    		if(ch!=null) {
    			ch.addFavorite(block);
    			favorite.remove(i);
    			i--; // stay in sync, otherwise we leapfrog
    		}
    	}
    }
    
    private void readChannel(String data)  throws Exception {
    	String str;
    	String[] lines=data.split("\n");
    	for(int i=0;i<lines.length;i++) {
    	    str=lines[i].trim();
    	    if(str.startsWith("macrodef ")) {
    	    	ArrayList<String> mData=ChannelUtil.gatherBlock(lines, i+1);
    	    	i+=mData.size();
    	    	continue;
    	    }
    	    if(str.startsWith("channel ")) {
    			String chName=str.substring(8,str.lastIndexOf('{')).trim();
    			ArrayList<String> chData=ChannelUtil.gatherBlock(lines, i+1);
    			i+=chData.size();
    			Channel old=find(chName);
    			if(old!=null) {
    				old.parse(chData,macros);
    				initVar(chName,old);
    			}
    			else {
    				Channel ch=new Channel(chName);
    				if(ch.Ok) {
    					ch.parse(chData,macros);
    					initVar(chName,ch);
    					addChild(ch);
    				}	
    				else {
    					PMS.minimal("channel "+chName+" was not parsed ok");
    				}
    			}
    		}
    	}
    }
    
    private void parseDefines(String data) {
    	String str;
    	String[] lines=data.split("\n");
    	macros=new ArrayList<ChannelMacro>();
    	for(int i=0;i<lines.length;i++) {
    	    str=lines[i].trim();
    	    if(str.startsWith("macrodef ")) {
    	    	String mName=str.substring(9,str.lastIndexOf('{')).trim();
    	    	ArrayList<String> mData=ChannelUtil.gatherBlock(lines, i+1);
    	    	i+=mData.size();
    	    	macros.add(new ChannelMacro(mName,mData));
    	    	continue;
    	    }
    	    if(str.startsWith("scriptdef ")) {
    	    	String sName=str.substring(10,str.lastIndexOf('{')).trim();
    	    	ArrayList<String> sData=ChannelUtil.gatherBlock(lines, i+1);
    	    	i+=sData.size();
    	    	if(scripts.get(sName)!=null) {
    	    		debug("Duplicate definition of script "+sName+" found. Ignore this one.");
    	    		continue;
    	    	}
    	    	scripts.put(sName, new ChannelMacro(sName,sData));
    	    	continue;
    	    }
    	    if(str.startsWith("favorite ")) {
    	    	ArrayList<String> fData=ChannelUtil.gatherBlock(lines, i+1);
    	    	i+=fData.size();
    	    	if(noFavorite())
    	    		continue;
    	    	favorite.add(fData);
    	    	continue;
    	    }
    	    if(str.startsWith("subdef ")) {
    	    	String sName=str.substring(7,str.lastIndexOf('{')).trim();
    	    	ArrayList<String> sData=ChannelUtil.gatherBlock(lines, i+1);
    	    	i+=sData.size();
    	    	if(subtitles.get(sName)!=null)
    	    		continue;
    	    	subtitles.put(sName, new ChannelSubs(sName,sData,file));
    	    	continue;
    	    }
    	    if(str.startsWith("proxydef ")) {
    	    	String sName=str.substring(9,str.lastIndexOf('{')).trim();
    	    	ArrayList<String> sData=ChannelUtil.gatherBlock(lines, i+1);
    	    	i+=sData.size();
    	    	if(proxies.get(sName)!=null)
    	    		continue;
    	    	try {
					proxies.put(sName, new ChannelProxy(sName,sData,file));
				} catch (UnknownHostException e) {
					debug("bad proxydef for "+sName);
				}
    	    	continue;
    	    }
    	    if(str.startsWith("stash ")) {
    	    	String sName=str.substring(6,str.lastIndexOf('{')).trim();
    	    	ArrayList<String> sData=ChannelUtil.gatherBlock(lines, i+1);
    	    	i+=sData.size();
    	    	HashMap<String,String> newStash=new HashMap<String,String>();
    	    	for(int j=0;j<sData.size();j++) {
    	    		String[] s=sData.get(j).split(",",2);
    	    		String v="";
    	    		if(s.length>1)
    	    			v=s[1];
    	    		newStash.put(s[0], v);
    	    	}
    	    	stash.put(sName, newStash);
    	    }
    	    if(str.startsWith("illegal")) {
    	    	ArrayList<String> iData=ChannelUtil.gatherBlock(lines, i+1);
    	    	i+=iData.size();
    	    	illegals.add(new ChannelIllegal(iData));
    	    }
    	    if(str.startsWith("code")) {
    	    	ArrayList<String> iData=ChannelUtil.gatherBlock(lines, i+1);
    	    	i+=iData.size();
    	    	codes.add(new ChannelIllegal(iData));
    	    }
    	}
    }
    
    public void parseChannels(File f)  throws Exception {
    	BufferedReader in=new BufferedReader(new FileReader(f));
    	String str;
    	boolean defines=false;
    	StringBuilder sb=new StringBuilder();
    	String ver="unknown";    	
    	while ((str = in.readLine()) != null) {
    		str=str.trim();
    		if(ChannelUtil.ignoreLine(str))
				continue;
    	    if(str.trim().startsWith("macrodef"))
    	    	defines=true;
    	    if(str.trim().startsWith("scriptdef"))
    	    	defines=true;
    	    if(str.trim().startsWith("subdef"))
    	    	defines=true;
    	    if(str.trim().startsWith("proxydef"))
    	    	defines=true;
    	    if(str.trim().startsWith("stash"))
    	    	defines=true;
    	    if(str.trim().startsWith("version")) {
    	    	String[] v=str.split("\\s*=\\s*");
    	    	if(v.length<2)
    	    		continue;
    	    	ver=v[1];
    	    	continue; // don't append these
    	    }	
    	    if(str.trim().startsWith("favorite")) 
    	    	defines=true;
    	    if(str.trim().startsWith("illegal")) 
    	    	defines=true;
    	    if(str.trim().startsWith("code")) 
    	    	defines=true;
    	    sb.append(str);
    	    sb.append("\n");
    	}
    	in.close();
    	PMS.minimal("parsing channel file "+f.toString()+" version "+ver);
    	debug("parsing channel file "+f.toString()+" version "+ver);
    	String s=sb.toString();
    	if(defines)
    		parseDefines(s);
    	readChannel(s);
    	addCred();
    	addFavorites();
    }
    
    
    private void addCred() {
    	for(int i=0;i<cred.size();i++) {
    		ChannelCred cr=cred.get(i);
    		Channel ch=find(cr.channelName);
    		if(ch==null)
    			continue;
    		cr.ch=ch;
    		debug("adding cred to channel "+cr.channelName);
    		ch.addCred(cr);
    	}
    		
    }
    
    @SuppressWarnings("unchecked")
	private void handleCred(File f)  {
    	if (!f.isFile()) 	
    		return;
		try {
	    	credConf.load(f);
	    	credConf.setFile(f);
			Iterator itr = credConf.getKeys();
			while(itr.hasNext()) {
				String key = (String) itr.next();
				String[] ownerTag = key.split("\\.", 2);
				if(!ownerTag[0].equalsIgnoreCase("channel"))
					continue;
				Object val = credConf.getProperty(key);
				ArrayList<String> usrPwd = null;
				if (val instanceof String) {
					usrPwd = new ArrayList<String>();
					usrPwd.add((String) val);
				}
				else if (val instanceof List<?>) {
					usrPwd = (ArrayList<String>) val;
				}
				if (usrPwd == null) {
					continue;
				}
				for (String val1 : usrPwd) {
					String[] s2 = val1.split(",", 2);
					if(s2.length<2)
						continue;
					String chName=ownerTag[1];
					ChannelCred ch=null;
					for(int i=0;i<cred.size();i++) {
						if(cred.get(i).channelName.equals(chName)) {
							ch=cred.get(i);
							break;
						}
					}
					if(ch==null) {
						ch=new ChannelCred(s2[0],s2[1],chName);
						cred.add(ch);
					}
					ch.user=s2[0];
					ch.pwd=s2[1];
				}
			}
			addCred();
		}
    	catch (Exception e) {
    		debug("handle cred exe "+e);
    	} 
    }
    
    private void handleVCR(File f) throws IOException {
    		BufferedReader in = new BufferedReader(new FileReader(f));
    		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			String str;
			GregorianCalendar now=new GregorianCalendar();
			while ((str = in.readLine()) != null) {
				str=str.trim();
				if(ChannelUtil.ignoreLine(str))
					continue;
				String[] data=str.split(",");
				if(data.length<2)
					continue;
				Date d;
				String name="";
				try {
					d=sdfDate.parse(data[0]);
				} catch (ParseException e) {
					debug("bad date format "+str);
					continue;
				}
				if(d.before(now.getTime())) {
					debug("Time already past "+str);
					continue;
				}
				String proc="";
				if(data.length>2)
					proc=data[2];
				if(data.length>3)
					name=data[3];
				ChannelVCR.start(d,data[1],proc,name);
			}
    }
    
    private void handleDirChange(File dir) throws Exception {
    	if(!dir.exists()) // file (or dir is gone) ignore
			return;
    	File[] files=dir.listFiles();
		for(int i=0;i<files.length;i++) {
			File f=files[i];
			if(f.getAbsolutePath().endsWith(".ch")) { // only bother about ch-files
				if(!chFiles.contains(f)) { // new file
					try {
						parseChannels(f);
						chFiles.add(f);
						if(fileMonitor!=null)
							fileMonitor.addFile(f);
					} catch (Exception e) {
						debug("Error parsing file "+f.toString()+" ("+e.toString()+")");
					}	
				}
			}
			else if(f.getAbsolutePath().endsWith(".cred"))
				handleCred(f);
			else if(f.getAbsolutePath().endsWith(".vcr"))
				handleVCR(f);
			else if(f.getAbsolutePath().contains("stream.var")) 
				defStreamVar.parse(f);
		}	
    }

	@Override
	public void fileChanged(File f) {
		if(f.isDirectory()) { // directory modified, new file or file gone?
			try {
				handleDirChange(f);
			} catch (Exception e) {
			}
		}
		else { // file change
			try {
				if(f.getAbsolutePath().endsWith(".cred"))
					handleCred(f);
				else if(f.getAbsolutePath().endsWith(".vcr"))
					handleVCR(f);
				else if(f.getAbsolutePath().contains("stream.var")) 
					defStreamVar.parse(f);
				else
					if(f.exists())
						parseChannels(f);
			} catch (Exception e) {
				PMS.minimal("Error parsing file "+f.toString()+" ("+e.toString()+")");
			}	
		}
	}
	
	//////////////////////////////////////

	private void startOffHour() {
		if(Channels.cfg().stdAlone())
			return;
		String cfg=(String) PMS.getConfiguration().getCustomProperty("channels.offhour");
		if(ChannelUtil.empty(cfg)) // no config, nothing to do
			return;
		String ohDb=file.getAbsolutePath()+File.separator+"data"+File.separator+"offhour";
		String[] s=cfg.split(",");
		int max=ChannelOffHour.DEFAULT_MAX_THREAD;
		int dur=ChannelOffHour.DEFAULT_MAX_DURATION;
		boolean cache=false;
		if(s.length>1)
			dur=ChannelUtil.convInt(s[1],dur);
		if(s.length>2)
			max=ChannelUtil.convInt(s[2],max);
		if(s.length>3&&s[3].equalsIgnoreCase("cache"))
			cache=true;
		oh=new ChannelOffHour(max,dur,s[0],new File(ohDb),cache);
		oh.init();
	}
	
	////////////////////////////////////
	// Save handling
	////////////////////////////////////
	
	public void setSave(String sPath) {
		setSave(sPath,null);
		
	}
	
	public void setSave(String sPath,String ts) {
		savePath=sPath;
		appendTS=(ChannelUtil.empty(ts)?false:true);
		cache.savePath(sPath);
		if(oh==null) 
			startOffHour();
		PMS.debug("[Channel]: using save path "+sPath);
		debug("using save path "+sPath);
	}
	
	public static boolean save() {
		return !ChannelUtil.empty(inst.savePath);
	}
	
	public static String fileName(String name,boolean cache) {
		return fileName(name,cache,null);
	}
	
	public static String fileName(String name,boolean cache,String imdb) {
		name=name.trim();
		name=ChannelPMSSaveFolder.washName(name);
		String ext=ChannelUtil.extension(name);			
		String fName=name;
		if(inst.appendTS) { 
			// if we got an extension we move it to the end of the filename
			String ts="_"+String.valueOf(System.currentTimeMillis());
			fName=ChannelUtil.append(name, null, ts);
			fName=ChannelUtil.append(fName,null, ext);
		}
		if(!ChannelUtil.empty(imdb)) {
			int pos=fName.lastIndexOf('.');
			if(pos!=-1)
				fName=fName.substring(0, pos-1)+"_imdb"+imdb+"_"+fName.substring(pos);
			else
				fName=ChannelUtil.append(fName, "_", "imdb"+imdb+"_");
		}
		debug("fname is "+fName);
		// remove some odd chars
		fName=fName.trim().replaceAll(" ", "_").replaceAll("<", "").replaceAll(">", "")
		.replaceAll(":", "").replaceAll("\"", "");
		if(!cache&&save())
			return cfg().getSavePath()+File.separator+fName;
		else
			return getPath()+File.separator+"data"+File.separator+fName;
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}
	
	///////////////////////////////////////////
	// Path handling
	///////////////////////////////////////////
	
	public static String getSavePath() {
		return inst.savePath;
	}
	
	public static String getPath() {
		return inst.file.getAbsolutePath();
	}
	
	public void setPath(String path) {
		debug("Set chanelpath to "+path);
		file=new File(path);
	}
	
	public static String dataPath() {
		return getPath()+File.separator+"data";
	}
	
	public static String dataEntry(String str) {
		return dataPath()+File.separator+str;
	}
	
	////////////////////////////////////////////
	// Script functions
	////////////////////////////////////////////
	
	public static ArrayList<String> getScript(String name) {
		if(ChannelUtil.empty(name))
			return null;
		ChannelMacro m=inst.scripts.get(name);
		if(m!=null) { // found a script return data
			return m.getMacro();
		}
		return null;
	}
	
	public static ChannelSubs getSubs(String name) {
		if(inst.openSubs!=null&&name.equals(inst.openSubs.getName()))
			return inst.openSubs;
		return inst.subtitles.get(name);
	}
	
	/////////////////////////////////
	// RtmpMethod change
	/////////////////////////////////
	
	public static void rtmpMethod(int newVal) {
		inst.rtmp=newVal;
	}
	
	public static int rtmpMethod() {
		return inst.rtmp;
	}
	
	public static void setSubs(boolean b) {
		inst.subs=b;
	}
	
	public static boolean doSubs() {
		return inst.subs;
	}
	
	public static boolean cache() {
		return inst.doCache;
	}
	
	public static void setCache(boolean b) {
		inst.doCache=true;
	}
	
	public static String cacheFile() {
		return inst.file.getAbsolutePath()+File.separator+"data"+File.separator+"cache";
	}
	
	public static ChannelOffHour getOffHour() {
		return inst.oh;
	}
	
	public static ChannelCfg cfg() {
		return inst.cfg;
	}
	
	public void setCfg(ChannelCfg c) {
		cfg=c;
		if(!ChannelUtil.empty(cfg.getCredPath())&&!cfg.getCredPath().equals(file)) {
			File f=new File(cfg.getCredPath());
			if(fileMonitor!=null)
				fileMonitor.addFile(f);
			handleCred(f);

		}
		if(cfg.clearCookies()) {
			// delete cookie file and creat an empty placeholder
			new File(cfg.getCookiePath()).delete();
			writeCookieFile(cfg.getCookiePath());
		}
		else {
			if(readCookieFile(cfg.getCookiePath())) // only need to write back if we removed some
				writeCookieFile(cfg.getCookiePath());
		}
	}
	
	//////////////////////////////////////////
	// Cookie mgmt
	//////////////////////////////////////////
	
	public static boolean readCookieFile(String file) {
		return readCookieFile(file,inst.cookies);
	}
	
	public static boolean readCookieFile(String file,HashMap<String,ArrayList<ChannelAuth>> map) {
		boolean skipped=false;
		try {
			BufferedReader in=new BufferedReader(new FileReader(file));
			String str;
	    	while ((str = in.readLine()) != null) {
	    		if(ChannelUtil.ignoreLine(str)) 
	    			continue;
	    		str=str.trim();
	    		String[] line=str.split("\t");
	    		if(line.length<7) // bad line skip it
	    			continue;
	    		String url=line[0];
	    		String ttd0=line[4];
	    		String key=line[5];
	    		String val=line[6];
	    		long ttd=0;
	    		try {
	    			ttd=Long.parseLong(ttd0);
	    		}
	    		catch (NumberFormatException e1) {
	    		}
	    		if(ttd<System.currentTimeMillis()) { // ignore old cookies
	    			skipped=true;
	    			continue;
	    		}
	    		ChannelAuth a=new ChannelAuth();
	    		a.authStr=key+"="+val;
	    		a.method=ChannelLogin.COOKIE;
	    		a.ttd=ttd;
	    		a.proxy=null;
	    		ArrayList<ChannelAuth> old=map.get(url);
	    		if(old==null) {
	    			old=new ArrayList<ChannelAuth>();
	    		}
	    		old.add(a);
	    		map.put(url, old);
	    			
	    	}	
		} catch (Exception e) {
		}	
		return skipped;
	}
	
	private static void writeCookieFile(String file) {
		try {
			FileOutputStream out=new FileOutputStream(file);
			// write a dummy line to make sure the file exists
			Date now=new Date();
			String data="# Cookie file generated "+ now.toString() +"\n"; 
			out.write(data.getBytes(), 0, data.length());
			for(String key : inst.cookies.keySet()) {
				ArrayList<ChannelAuth> list= inst.cookies.get(key);
				for(int i=0;i<list.size();i++) {
					ChannelAuth a=list.get(i);
					data=key+"\tTRUE\t/\tFALSE\t"+String.valueOf(a.ttd)+"\t"+a.authStr.replace('=', '\t')+
					"\n";
					out.write(data.getBytes(), 0, data.length());
				}
			}	
			out.flush();
			out.close();
		}
		catch (Exception e) {
			debug("Error writing cookie file "+e);
		}
	}
	
	public static void mkCookieFile() {
		String file=cfg().getCookiePath();
		if(ChannelUtil.empty(file))
			return;
		File f=new File(file);
		// The logic here:
		// Create a new HashMap (=map) as a sandbox
		// If a cookie file exists read that and store the cookies in the sandbox
		// Move all cookie from the internal jar to the sandbox UNLESS
		// the sandbox is newer.
		// Finally rewrite the sandbox.
		HashMap<String,ArrayList<ChannelAuth>> map=null;
		if(f.exists()) { // life is full of troubles, first we must fix stuff here
			map=new HashMap<String,ArrayList<ChannelAuth>>();
			readCookieFile(file,map);
		}
		if(map!=null) { // we got a new map (we reread the cookie file)
			for(String key : inst.cookies.keySet()) {
				ArrayList<ChannelAuth> a=inst.cookies.get(key);
				ArrayList<ChannelAuth> b=map.get(key);
				if(b==null)
					map.put(key,a);
				else { // cookie file contain this key
					// if it's newer or lives longer we'll use it
					for(int i=0;i<a.size();i++) {
						mergeChannelAuths(a.get(i),b);
					}
					map.put(key, a);
				}
			}
			// map now contains all keys from file + our internal
			// NOTE!! Cookies in the sandbox that are "new"
			// don't need to be checked for age since no old
			// cookies makes it in to the jar
			inst.cookies=map;
		}
		f.delete(); // clear the file and the rewrite it
		writeCookieFile(file);
	}
	
	private static void mergeChannelAuths(ChannelAuth old,ArrayList<ChannelAuth> list) {
		String cookie=old.authStr.split("=")[0];
		ChannelAuth tmp=null;
		for(int j=0;j<list.size();j++) {
			ChannelAuth a=list.get(j);
			String c1=a.authStr.split("=")[0];
			if(!c1.equals(cookie))
				continue;
			// cookie in list
			if(!a.authStr.equals(old.authStr)&&old.ttd>a.ttd)
				return;
			else if(old.ttd>a.ttd)
				return;
			tmp=a;
			break;
		}
		if(tmp!=null) {
			list.remove(old);
			list.add(tmp);
		}
	}
	 
	private static boolean findCookie(ArrayList<ChannelAuth> l,ChannelAuth a) {
		String cookie=a.authStr.split("=")[0];
		for(int i=0;i<l.size();i++) {
			ChannelAuth a1=l.get(i);
			String c1=a1.authStr.split("=")[0];
			if(c1.equals(cookie)) {
				if(a.ttd>a1.ttd)  // new cookie is newer
					a1=a;
				return true;
			}
		}
		return false;
	}
	
	public static boolean addCookie(String url,ChannelAuth a) {
		ArrayList<ChannelAuth> l=inst.cookies.get(url);
		if(l==null) 
			l=new ArrayList<ChannelAuth>();
		if(!findCookie(l,a))
			l.add(a);
		inst.cookies.put(url, l);
		return true;
	}
	
	public static ChannelAuth getCookie(String url) {
		ArrayList<ChannelAuth> l=inst.cookies.get(url);
		if(l==null)
			return null;
		ChannelAuth a=new ChannelAuth(l.get(0));
		for(int i=1;i<l.size();i++) {
			a.authStr=ChannelUtil.append(a.authStr,"; ",l.get(i).authStr);
		}
		return a;
	}
	
	////////////////////////////////////////////////
	// Handle search db
	/////////////////////////////////////////////////
	
	private void initSearch(File f) throws Exception {
		if(!f.exists())
			return;
		BufferedReader in=new BufferedReader(new FileReader(f));
    	String str;
    	while ((str = in.readLine()) != null) {
    		if(ChannelUtil.ignoreLine(str)) 
    			continue;
    		str=str.trim();
    		String[] line=str.split(",");
    		if(line.length<3) // bad line skip it
    			continue;
    		String chStr=line[0];
    		String id=line[1];
    		String search=line[2];
    		Channel ch=find(chStr);
    		if(ch==null) // no channel?!
    			continue;
    		searchDb.addSearch(ch,id,search);
    	}
    	in.close();
    	searchDb.dump();
	}
	
	public static void addSearch(Channel ch,String id,String str) {
		debug("add search entry "+ch.name()+" "+id+" "+str);
		inst.searchDb.addSearch(ch, id, str);
		inst.searchDb.dump();
	}
	
	////////////////////////////////////////////////////
	// Proxy handling
	////////////////////////////////////////////////////
	
	public static ChannelProxy getProxy(String name) {
		return inst.proxies.get(name);
	}
	
	//////////////////////////////////////////////
	// Find stuff in the stash
	/////////////////////////////////////////////
	
	public static HashMap<String,String> getStash(String key) {
		return inst.stash.get(key);
	}
	
	public static String getStashData(String key,String stashKey) {
		return getStashData(key,stashKey,null);
	}	
	
	public static String getStashData(String key,String stashKey,String def) {
		HashMap<String,String> s=getStash(key);
		if(s==null)
			return def;
		String res=s.get(stashKey);
		if(res==null)
			return def;
		return res;
	}
	
	public static void putStash(String stash,String key,String val) {
		putStash(stash,key,val,false);
	}
	
	public static void putStash(String stash,String key,String val,boolean create) {
		HashMap<String,String> s=getStash(stash);
		if(s!=null)
			s.put(key, val);
		else if(create){
			s=new HashMap<String,String>();
			s.put(key, val);
			inst.stash.put(stash, s);
		}		
	}
	
	/////////////////////////////////
	// Groupfolder
	/////////////////////////////////
	
	public static void setGroup(boolean b) {
		inst.groupFolder=b;
	}
	
	public static boolean useGroupFolder() {
		return inst.groupFolder;
	}
	
	///////////////////////////////////
	// MovieInfo
	///////////////////////////////////
	
	public static void setMovieInfo(boolean b) {
		if(b) {
			inst.movieInfo=null;
			try {
				String plugin=PMS.getConfiguration().getPluginDirectory();
				File mi=new File(plugin+File.separator+"movieinfo.jar");
				if(!mi.exists()) {
					inst.movieInfo=null;
					return;
				}
				URLClassLoader cl=(URLClassLoader) inst.getClass().getClassLoader();
				URL u=mi.toURI().toURL();
				Class clClass = URLClassLoader.class;
				Class[] parameters = new Class[]{URL.class};
				Method method = clClass.getDeclaredMethod("addURL", parameters);
				method.setAccessible(true);
				method.invoke(cl, new Object[]{u});
				inst.movieInfo=new ChannelMovieInfo();
			}
			catch ( Throwable t) {
				Channels.debug("MovieInfo inegration failed");
				inst.movieInfo=null;
			}
		}
		else
			inst.movieInfo=null;
	}
	
	public static boolean useMovieInfo() {
		return (inst.movieInfo!=null);
	}
	
	public static ChannelMovieInfo movieInfo() {
		return inst.movieInfo;
	}
	
	/////////////////////////////////////////////////
	// Favorite handling methods
	/////////////////////////////////////////////////
	
	public static File workFavFile() {
		return new File(getPath()+File.separator+"000favorite.work");
	}
	
	public static File favFile() {
		return new File(getPath()+File.separator+"000favorite.ch");
	}
	
	public static boolean noFavorite() {
		return !cfg().favorite();
	}
	
	public static boolean noPlay() {
		return cfg().noPlay();
	}
	
	//////////////////////////////////////////////////////////////
	// ChannelVars handling
	//////////////////////////////////////////////////////////////
	
	public void initVar(String channel,Channel ch) {
		cfg().chVars(channel,ch);
	}
	
	public static void setChVar(String ch,String inst,String var,String val) {
		cfg().putChVars(ch,inst,var,val);
	}
	
	////////////////////////////////////////////////////////////////
	// ProxyDNS
	////////////////////////////////////////////////////////////////
	
	private static boolean useProxyDNS(boolean channel) {
		int mode=cfg().proxyDNSMode();
		return (mode==ChannelCfg.PROXY_DNS_CHANNEL)&&channel;
	}
	
	public static void setProxyDNS(boolean channel) {
		if(!useProxyDNS(channel))
			return;
		String p=cfg().proxyDNS();
		if(ChannelUtil.empty(p)) // no proxyDNS
			return;
		setProxyDNS(p);
	}
	
	public static void setProxyDNS(String server) {
		debug("set DNS server to "+server);
		System.setProperty("sun.net.spi.nameservice.nameservers", server);
		System.setProperty("sun.net.spi.nameservice.provider.1", "dns,dnsjava");
	}
	
	public static void restoreProxyDNS() {
		// trick here, if mode!=channel this is always false and we dont swap
		// the DNS back and forth
		if(!useProxyDNS(true)) 
			return;
		System.clearProperty("sun.net.spi.nameservice.nameservers");
		System.clearProperty("sun.net.spi.nameservice.provider.1");
	}
	
	//////////////////////////////////////////////////////////////////////
	
	public static ChannelStreamVars defStreamVar() {
		return inst.defStreamVar;
	}
	
	///////////////////////////////////////////////////////////////////////
	
	private static boolean isIllegal(ArrayList<ChannelIllegal> list,String str,int type) {
		for(ChannelIllegal ill : list) {
			if(ill.isIllegal(str, type)) 
				return true;
		}
		return false;
	}
	
	public static boolean isIllegal(String str,int type) {
		return isIllegal(inst.illegals,str,type);
	}
	
	public static boolean isCode(String str,int type) {
		if(allUnlocked())
			return false;
		if(ChannelUtil.empty(getCode()))
			return false;
		return isIllegal(inst.codes,str,type);
	}
	
	public static String getCode() {
		if(cfg().stdAlone())
			return "";
		return (String) PMS.getConfiguration().getCustomProperty("channels.code");
	}
	
	public static void unlockAll() {
		inst.allUnlocked=true;
	}
	
	public static boolean allUnlocked() {
		return inst.allUnlocked;
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	public static File localPLX() {
		return new File(dataPath()+File.separator+"navix_local.plx");
	}
	
	public static void addToPLX(Channel ch,String name,String url,String proc,int format,
			String thumb,String imdb) throws IOException {
		FileWriter out=new FileWriter(localPLX(),true);
		StringBuffer sb=new StringBuffer();
		sb.append("###################\n");
		sb.append("name=");
		sb.append(name.trim());
		sb.append("\n");
		sb.append("type=");
		sb.append(ChannelUtil.format2str(format));
		sb.append("\n");
		sb.append("channel=");
		sb.append(ch.name());
		sb.append("\n");
		if(!ChannelUtil.empty(thumb)) {
			sb.append("thumb=");
			sb.append(thumb);
			sb.append("\n");
		}
		if(!ChannelUtil.empty(proc)) {
			sb.append("processor=");
			sb.append(proc);
			sb.append("\n");
		}
		sb.append("URL=");
		sb.append(url);
		sb.append("\n");
		sb.append("###################\n");
		out.write(sb.toString());
		out.close();
	}
	
	//////////////////////////////////////////////////////
	
	public static ChannelSubs openSubs() {
		return inst.openSubs;
	}
	
	public static void setOpenSubs(boolean b) {
		if(b) {
			try {
				inst.openSubs=new ChannelOpenSubs();
			}
			catch ( java.lang.NoClassDefFoundError e) {
				Channels.debug("OpenSubs not loaded ignore integration");
				inst.openSubs=null;
			}
		}
		else
			inst.openSubs=null;
	}
	
	////////////////////////////////////////////////////////////////
	// Monitor stuff
	////////////////////////////////////////////////////////////////
	
	private class ChannelPlainFolder extends VirtualFolder {
		public ChannelPlainFolder(String name) {
			super(name,null);
		}
	}
	
	private static File monitorFile() {
		return new File(dataPath()+File.separator+"monitor");
	}
	
	private void parseMonitorFile() {
		File f=monitorFile();
		if(!cfg.monitor()||!f.exists())
			return;
		try {
			BufferedReader in=new BufferedReader(new FileReader(f));
			String str;    	
			ArrayList<String> entries=null;
			String name="";
			Channel owner=null;
			StringBuilder sb=null;
			String templ=null;
			String search=null;
			boolean try_search = false;
			while ((str = in.readLine()) != null) {
				if(ChannelUtil.empty(str))
					continue;
				str=str.trim();
				if(str.equals(ChannelUtil.FAV_BAR.trim())) {
					if(entries==null)
						entries=new ArrayList<String>();
					else {
						if(sb==null)
							continue;
						String[] lines=sb.toString().split("\n");
						for(int i=0;i<lines.length;i++) {
							String str1=lines[i].trim();
							if(str1.startsWith("folder ")) {
								ArrayList<String> fData=ChannelUtil.gatherBlock(lines, i+1);
								i+=fData.size();
								ChannelFolder mf=new ChannelFolder(fData,owner);
								// need to make first folder empty
								mf.setType(ChannelFolder.TYPE_EMPTY);
								Channels.debug("add new monitor "+name);
								ChannelMonitor m=new ChannelMonitor(mf,entries,name);
								m.setTemplate(templ);
								m.setSearch(search);
								m.setTrySearch(try_search);
								monMgr.add(m);
								// Clear all vars
								name=null;
								templ=null;
								entries=null;
								owner=null;
								search=null;
								try_search = false;
							}
						}
					}
					continue;
				}
				if(ChannelUtil.ignoreLine(str))
					continue;
				if(str.startsWith("entry=")) {
					if(entries==null)
						continue;
					String entry=str.substring(6);
					if(!entries.contains(entry.trim()))
						entries.add(entry.trim());
					continue;
				}
				if(str.startsWith("owner=")) {
					owner=find(str.substring(6).trim());
					continue;
				}
				if(str.startsWith("templ=")) {
					templ=str.substring(6).trim();
					continue;
				}
				if(str.startsWith("search=")) {
					search=str.substring(7);
					continue;
				}
				if(str.startsWith("monitor")) {
					sb=new StringBuilder();
					continue;
				}
				if(str.startsWith("try_search=")) {
					try_search = str.substring(11).equalsIgnoreCase("true");
					continue;
				}
				if(str.startsWith("name=")) {
					if(ChannelUtil.empty(name)) { // only pick 1st name, just to simplify parsing
						name=str.substring(5);
						continue;
					}
					// subsequence name belongs to some struct leave it
					// and it will be added normally
				}
				sb.append(str);
				sb.append("\n");
			}
			in.close();
		} catch (Exception e) {
			debug("mon file parse error "+e);
		}				
	}
	
	private static String templWash(String name,String templ) {
		if(ChannelUtil.empty(templ))
			return name;
		HashMap<String,String> vars=new HashMap<String,String>();
		vars.put("__wash__", "1");
		return ChannelNaviXProc.simple(name, templ, vars);
	}
	
	
	private static String monNameWash(String name,String templ,String searched,
									  String nameProp) {
		if(!ChannelUtil.empty(nameProp)&&
		   nameProp.equalsIgnoreCase("search")&&
		   !ChannelUtil.empty(searched)) {
				return searched;
		}
		return templWash(name,templ);
	}
	
	
	public static void monitor(DLNAResource res,ChannelFolder cf,
			   String data) throws IOException {
		monitor(res,cf,data,null);
	}
		
	public static void monitor(DLNAResource res,ChannelFolder cf,
							   String data,String templ) throws IOException {
		if(!inst.cfg.monitor())
			return;
		if(inst.monMgr.monitored(res.getName())) 
			return;
		File f=monitorFile();
		boolean newFile=!f.exists();
		String searched=ChannelUtil.searchInPath(res);
		FileWriter out=new FileWriter(f,true);
		String nameProp=cf.getProp("monitor_name");
		String washedName=monNameWash(res.getName(),templ,searched,nameProp);
		StringBuffer sb=new StringBuffer();
		boolean try_search = cf.getProperty("monitor_try_search");
		if(newFile) {
			sb.append("######\n");
			sb.append("## NOTE!!!!!\n");
			sb.append("## This file is auto generated\n");
			sb.append("## Edit with EXTREME care\n");
			sb.append("## If you remove things make sure to remove\n");
			sb.append("## EVERYTHING between the # bars\n\n");
		}
		sb.append(ChannelUtil.FAV_BAR);
		ChannelUtil.appendVarLine(sb, "name", washedName);
		if(!ChannelUtil.empty(templ))
			ChannelUtil.appendVarLine(sb, "templ", templ);
		if(!ChannelUtil.empty(searched))
			ChannelUtil.appendVarLine(sb, "search", searched);
		if(try_search)
			ChannelUtil.appendVarLine(sb, "try_search", "true");
		ArrayList<String> entries=new ArrayList<String>();
		for(DLNAResource r : res.getChildren()) {
			if(ChannelUtil.filterInternals(r))
				continue;
			ChannelUtil.appendVarLine(sb, "entry", r.getName());
			entries.add(r.getName());
		}
		sb.append(data);
		sb.append("\n");
		sb.append(ChannelUtil.FAV_BAR);
		out.write(sb.toString());
		out.close();
		String[] lines=data.split("\n");
    	for(int i=0;i<lines.length;i++) {
    		String str=lines[i].trim();
    	    if(str.startsWith("monitor")||
    	       str.startsWith("owner")||
			   str.startsWith("try_search") ||
    	       str.startsWith("templ")) {
    	    	// skip the start lines
    	    	continue;
    	    }
    	    if(str.startsWith("folder ")) { // 1st folder
    	    	ArrayList<String> fData=ChannelUtil.gatherBlock(lines, i+1);
    			i+=fData.size();
    			ChannelFolder mf=new ChannelFolder(fData,cf.getChannel());
    			// need to make first folder empty
    			mf.setType(ChannelFolder.TYPE_EMPTY);
    			ChannelMonitor m=new ChannelMonitor(mf,entries,washedName);
    			m.setTemplate(templ);
    			m.setSearch(searched);
				m.setTrySearch(try_search);
    			inst.monMgr.add(m);
    			continue;
    	    }
    	}
	}
	
	public static void updateMonitor(String monName,String newEntry) {
		try {
			if(!inst.monMgr.update(monName, newEntry)) // weird?
				return;
			File f=monitorFile();
			String str = FileUtils.readFileToString(f);
			String restr="name="+monName;
			int pos = str.indexOf(restr);
			if(pos > -1) {
				str=str.replaceFirst(restr, restr+"\nentry="+newEntry.trim()+"\n");
				str=str.replaceAll("(?m)^[ \t]*\r?\n", "");
				FileOutputStream out=new FileOutputStream(f,false);
				out.write(str.getBytes());
				out.flush();
				out.close();
			}
		}
		catch (Exception e) {
			debug("update mon file error "+e);
		}
	}
	
	private void allPlayed(DLNAResource res,String name) {
		for(DLNAResource r : res.getChildren()) {
			if(r instanceof VirtualVideoAction)
				continue;
			updateMonitor(name,r.getName());
		}
	}
	
	private static DLNAResource findMonitorFolder(DLNAResource start,String name) {
		for(DLNAResource r : start.getChildren()) {
			if(name.equals(r.getName()))
				return r;
		}
		return null;
	}
	
	private void addNewMonitoredMedia_i(final DLNAResource r,final String folder) {
		DLNAResource f=findMonitorFolder(monitor,folder);
		if(f==null) {
			f=new ChannelPlainFolder(folder);
			monitor.addChild(f);
			final DLNAResource f1=f;
			f.addChild(new VirtualVideoAction("Clear",true) {
				public boolean enable() {
					allPlayed(f1,folder.trim());
					monitor.getChildren().remove(f1);
					return true;
				}
			});
		}
		f.addChild(r);	
	}

	public static void addNewMonitoredMedia(DLNAResource r,String folder) {
		inst.addNewMonitoredMedia_i(r, folder);
	}
	
	public static void clearNewMediaFolder(String folder) {
		DLNAResource f=findMonitorFolder(inst.monitor,folder);
		if(f!=null)
			inst.monitor.getChildren().remove(f);
	}
	
	public static boolean monitoredPlay(DLNAResource res) {
		return (inst.monitor==res);
	}	
	
	public static void clearMonitor() {
		inst.monitor.getChildren().clear();
		inst.monitor.addChild(new VirtualVideoAction("Rescan",true) {
			@Override
			public boolean enable() {
				clearMonitor();
				inst.monMgr.scanAll();
				return true;
			}
    	});
	}
	
	///////////////////////////////////////////////////////
	// URL Resolving
	///////////////////////////////////////////////////////
	
	public String urlResolve(String url,boolean dummyOnly) {
		for(Channel ch : getChannels()) {
			String u = ch.urlResolve(url,dummyOnly);
			if(!ChannelUtil.empty(u))
				return u;
		}
		return null;
	}
}
