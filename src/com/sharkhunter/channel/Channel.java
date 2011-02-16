package com.sharkhunter.channel;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import net.pms.PMS;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.network.HTTPResource;

public class Channel extends VirtualFolder {
	
	public boolean Ok;
	private String name;
	private int format;
	
	private ArrayList<ChannelFolder> folders;
	private ArrayList<ChannelMacro> macros;
	
	private ChannelCred cred;
	private ChannelLogin logObj;
	
	public Channel(String name) {
		super(name,null);
		Ok=false;
		format=Format.VIDEO;
		folders=new ArrayList<ChannelFolder>();
		Ok=true;
	}
	
	public void parse(ArrayList<String> data,ArrayList<ChannelMacro> macros) {
		folders.clear();
		children.clear();
		childrenNumber=0;
		discovered=false;
		debug("parse channel "+name+" data "+data.toString());
		this.macros=macros;
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line.contains("login {")) {
				// Login data
				ArrayList<String> log=ChannelUtil.gatherBlock(data, i+1);
				i+=log.size();
				logObj=new ChannelLogin(log,this);
			}
			if(line.contains("folder {")) {
				ArrayList<String> folder=ChannelUtil.gatherBlock(data,i+1);
				i+=folder.size();
				ChannelFolder f=new ChannelFolder(folder,this);
				if(f.Ok)
					folders.add(f);
			}
			String[] keyval=line.split("=",2);
			if(keyval.length<2)
				continue;
			if(keyval[0].equalsIgnoreCase("macro")) {
				ChannelMacro m=ChannelUtil.findMacro(macros,keyval[1]);
				if(m!=null)
					parse(m.getMacro(),macros);
				else
					PMS.debug("unknown macro "+keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("format")) {
				if(keyval[1].equalsIgnoreCase("video"))
					format=Format.VIDEO;
				if(keyval[1].equalsIgnoreCase("audio"))
					format=Format.AUDIO;
				if(keyval[1].equalsIgnoreCase("image"))
					format=Format.IMAGE;
				
			}
			if(keyval[0].equalsIgnoreCase("img")) {
				thumbnailIcon=keyval[1];
				if (thumbnailIcon != null && thumbnailIcon.toLowerCase().endsWith(".png"))
					thumbnailContentType = HTTPResource.PNG_TYPEMIME;
				else
					thumbnailContentType = HTTPResource.JPEG_TYPEMIME;
			}
		}
	}
	
	public ChannelMacro getMacro(String macro) {
		return ChannelUtil.findMacro(macros, macro);
	}
	
	public int getFormat() {
		return format;
	}
	
	public String getThumb() {
		return thumbnailIcon;
	}
	
	public boolean refreshChildren() {
		return true; // always re resolve
	}
	
	public void discoverChildren(String s) {
		discoverChildren();
	}
	
	public void discoverChildren() {
		for(int i=0;i<folders.size();i++) {
			ChannelFolder cf=folders.get(i);
			if(cf.isATZ()) 
				addChild(new ChannelATZ(cf));
			else
				try {
					cf.match(this);
				} catch (MalformedURLException e) {
				}
		}
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}
	
	public void debug(String msg) {
		Channels.debug(msg);
	}
	
	public void addCred(ChannelCred c) {
		cred=c;
		if(logObj!=null)
			logObj.reset();
	}
	
	public String user() {
		return cred.user;
	}
	
	public String pwd() {
		return cred.pwd;
	}
	
	public String getAuth() {
		if(logObj==null)
			return "";
		if(cred==null)
			return "";
		if(ChannelUtil.empty(cred.user))
			return "";
		if(ChannelUtil.empty(cred.pwd))
			return "";
		return logObj.getAuthStr(cred.user, cred.pwd);
	}
}
