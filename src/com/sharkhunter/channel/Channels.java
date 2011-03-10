package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import no.geosoft.cc.io.FileListener;
import no.geosoft.cc.io.FileMonitor;

public class Channels extends VirtualFolder implements FileListener {

	// Constants for RTMP string constructions
	public static final int RTMP_MAGIC_TOKEN=1;
	public static final int RTMP_DUMP=2;
	public int rtmp;
	
	public static final int DeafultContLim=5;
	public static final int ContSafetyVal=-100;
	
    private File file;
    private FileMonitor fileMonitor;
    private ArrayList<File> chFiles;
    private ArrayList<ChannelMacro> macros;
    private ArrayList<ChannelCred> cred;
    private HashMap<String,ChannelMacro> scripts;
    private ChannelDbg dbg;
    private static Channels inst=null;
    private String savePath;
    private boolean appendTS;
    
    public Channels(String path,long poll) {
    	super("Channels",null);
    	this.file=new File(path);
    	inst=this;
    	chFiles=new ArrayList<File>();
    	cred=new ArrayList<ChannelCred>();
    	scripts=new HashMap<String,ChannelMacro>();
    	savePath="";
    	appendTS=false;
    	//rtmp=Channels.RTMP_MAGIC_TOKEN;
    	rtmp=Channels.RTMP_DUMP;
    	PMS.minimal("Start channel 0.58");
    	dbg=new ChannelDbg(new File(path+File.separator+"channel.log"));
    	fileMonitor=null;
    	if(poll>0)
    		fileMonitor=new FileMonitor(poll);
    	fileChanged(file);
    	if(poll>0) {
    		fileMonitor.addFile(file);
    		fileMonitor.addListener(this);
    	}
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
    	for(DLNAResource f:children)
    		if((f instanceof Channel)&&(f.getDisplayName().equals(name)))
    				return (Channel) f;
    	return null;
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
    			}
    			else {
    				Channel ch=new Channel(chName);
    				if(ch.Ok) {
    					ch.parse(chData,macros);
    					addChild(ch);
    				}	
    				else {
    					PMS.minimal("channel "+chName+" was not parsed ok");
    				}
    			}
    		}
    	}
    }
    
    private void parseMacroScript(String data) {
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
    	}
    }
    
    public void parseChannels(File f)  throws Exception {
    	BufferedReader in=new BufferedReader(new FileReader(f));
    	String str;
    	boolean macro=false;
    	boolean script=false;
    	StringBuilder sb=new StringBuilder();
    	String ver="unknown";    	
    	while ((str = in.readLine()) != null) {
    		str=str.trim();
    		if(ChannelUtil.ignoreLine(str))
				continue;
    	    if(str.trim().startsWith("macrodef"))
    	    	macro=true;
    	    if(str.trim().startsWith("scriptdef"))
    	    	script=true;
    	    if(str.trim().startsWith("version")) {
    	    	String[] v=str.split("\\s*=\\s*");
    	    	if(v.length<2)
    	    		continue;
    	    	ver=v[1];
    	    	continue; // don't append these
    	    }	
    	    sb.append(str);
    	    sb.append("\n");
    	}
    	in.close();
    	PMS.minimal("parsing channel file "+f.toString()+" version "+ver);
    	debug("parsing channel file "+f.toString()+" version "+ver);
    	if(macro||script)
    		parseMacroScript(sb.toString());
    	readChannel(sb.toString());
    	addCred();
    }
    
    
    private void addCred() {
    	for(int i=0;i<cred.size();i++) {
    		ChannelCred cr=cred.get(i);
    		Channel ch=find(cr.channelName);
    		if(ch==null)
    			continue;
    		cr.ch=ch;
    		ch.addCred(cr);
    	}
    		
    }
    
    private void handleCred(File f)  {
    	BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(f));
			String str;
			while ((str = in.readLine()) != null) {
				str=str.trim();
				if(ChannelUtil.ignoreLine(str))
					continue;
				String[] s=str.split("\\s*=\\s*",2);
				if(s.length<2)
					continue;
				String[] s1=s[0].split("\\.");
				if(s1.length<2)
					continue;
				if(!s1[0].equalsIgnoreCase("channel"))
					continue;
				String[] s2=s[1].split(",");
				if(s2.length<2)
					continue;
				String chName=s1[1];
				ChannelCred ch=null;
				for(int i=0;i<cred.size();i++)
					if(cred.get(i).channelName.equals(chName)) {
						ch=cred.get(i);
						break;
					}
				if(ch==null) {
					ch=new ChannelCred(s2[0],s2[1],chName);
					cred.add(ch);
				}
				ch.user=s2[0];
				ch.pwd=s2[1];
			}
			addCred();
		}
    	catch (Exception e) {} 
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
						PMS.minimal("Error parsing file "+f.toString()+" ("+e.toString()+")");
						//e.printStackTrace();
					}	
				}
			}
			else if(f.getAbsolutePath().endsWith(".cred"))
				handleCred(f);
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
				else
					if(f.exists())
						parseChannels(f);
			} catch (Exception e) {
				PMS.minimal("Error parsing file "+f.toString()+" ("+e.toString()+")");
				//e.printStackTrace();
			}	
		}
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
		PMS.debug("[Channel]: using save path "+sPath);
		debug("using save path "+sPath);
	}
	
	public static boolean save() {
		return !ChannelUtil.empty(inst.savePath);
	}
	
	public static String fileName(String name) {
		String ts="";
		String ext=ChannelUtil.extension(name);
		if(inst.appendTS) 
			ts="_"+String.valueOf(System.currentTimeMillis());
		// if we got an extension we move it to the end of the filename
		return inst.savePath+File.separator+name+ts+(ChannelUtil.empty(ext)?"":ext);
	}
	
	///////////////////////////////////////////
	// Path handling
	///////////////////////////////////////////
	
	public String getSavePath() {
		return inst.savePath;
	}
	
	public String getPath() {
		return inst.file.getAbsolutePath();
	}
	
	public void setPath(String path) {
		debug("Set chanelpath to "+path);
		file=new File(path);
	}
	
	////////////////////////////////////////////
	// Script functions
	////////////////////////////////////////////
	
	public static ArrayList<String> getScript(String name) {
		ChannelMacro m=inst.scripts.get(name);
		if(m!=null) { // found a script return data
			return m.getMacro();
		}
		return null;
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
}
