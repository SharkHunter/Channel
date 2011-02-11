package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import no.geosoft.cc.io.FileListener;
import no.geosoft.cc.io.FileMonitor;

public class Channels extends VirtualFolder implements FileListener {
//public class Channels {
    private File file;
    private FileMonitor fileMonitor;
    private ArrayList<File> chFiles;
    private ArrayList<ChannelMacro> macros;
    private ArrayList<ChannelCred> cred;
    public static boolean debug=false;
    
    public static ChannelDbg dbg;

    public Channels(String path,long poll) {
    	super("Channels",null);
    	this.file=new File(path);
    	chFiles=new ArrayList<File>();
    	cred=new ArrayList<ChannelCred>();
    	PMS.minimal("Start channel 0.30");
    //	PMS.get().getExtensions().set(0, new WEB());
    	fileMonitor=null;
    	if(poll>0)
    		fileMonitor=new FileMonitor(poll);
    	fileChanged(file);
    	if(poll>0) {
    		fileMonitor.addFile(file);
    		fileMonitor.addListener(this);
    	}
    	//Channels.dbg=new ChannelDbg(new File(path+File.separator+"channel.log"));
    	Channels.debug=true;
    	//Channels.dbg.debug("Started");
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
    			//	old.setDbg(this.dbg);
    				old.parse(chData,macros);
    			}
    			else {
    				Channel ch=new Channel(chName);
    				if(ch.Ok) {
//    					ch.setDbg(this.dbg);
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
    
    private void parseMacros(String data) {
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
    	    }
    	}
    }
    
    public void parseChannels(File f)  throws Exception {
    	BufferedReader in=new BufferedReader(new FileReader(f));
    	String str;
    	boolean macro=false;
    	StringBuilder sb=new StringBuilder();
    	String ver="unknown";    	
    	while ((str = in.readLine()) != null) {
    		str=str.trim();
    	    if(str.startsWith("#"))
    	    	continue;
    	    if(str.length()==0)
    	    	continue;
    	    if(str.trim().startsWith("macrodef"))
    	    	macro=true;
    	    if(str.trim().startsWith("debug")) {
    	    	String[] v=str.split("=");
    	    	if(v.length<2)
    	    		continue;
    	    	if(v[1].equalsIgnoreCase("true"))
    	    		Channels.debug=true;
    	    	continue;
    	    }
    	    if(str.trim().startsWith("version")) {
    	    	String[] v=str.split("=");
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
    	if(macro)
    		parseMacros(sb.toString());
    	readChannel(sb.toString());
    	addCred();
    }
    
    private void handleFormat(File f) throws Exception {
    /*	BufferedReader in=new BufferedReader(new FileReader(f));
    	String str;
    	WEB w=new WEB();
    	while ((str = in.readLine()) != null) {
    		str=str.trim();
    	    if(str.startsWith("#"))
    	    	continue;
    	    if(str.length()==0)
    	    	continue;
    	    w.addExtra(str);
    	}
    	PMS.get().getExtensions().set(0, w);*/
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
				if(str.startsWith("#"))
					continue;
				if(str.length()==0)
					continue;
				String[] s=str.split("=",2);
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
			else if(f.getAbsolutePath().endsWith(".form")) {
				handleFormat(f);
			}
			else if(f.getAbsolutePath().endsWith(".cred"))
				handleCred(f);
			else if(f.getName().startsWith("ch_debug")) {
				PMS.minimal("Channel debug file found "+f.exists());
				if(fileMonitor!=null)
					fileMonitor.addFile(f);
				Channels.debug=f.exists();
			}
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
				if(f.getAbsolutePath().endsWith(".form")) 
					handleFormat(f);
				else if(f.getAbsolutePath().endsWith(".cred"))
					handleCred(f);
				else if(f.getName().startsWith("ch_debug")) {
					PMS.minimal("Channel debug file found "+f.exists());
					if(fileMonitor!=null)
						fileMonitor.addFile(f);
					Channels.debug=f.exists();
				}
				else
					if(f.exists())
						parseChannels(f);
			} catch (Exception e) {
				PMS.minimal("Error parsing file "+f.toString()+" ("+e.toString()+")");
				//e.printStackTrace();
			}	
		}
	}
}
