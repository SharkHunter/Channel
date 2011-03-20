package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelCache extends VirtualFolder {
	private File dPath;
	private File sPath;
	private Timer t;
	
	public ChannelCache(String path) {
		super("Cache",null);
		dPath=new File(path+File.separator+"data");
		sPath=null;
		t=new Timer();
		TimerTask task = new TimerTask() {
		    @Override
		    public void run() {
		    	try {
					updateCache(dPath,sPath);
				} catch (Exception e) {
				}
		    }
		};
		GregorianCalendar gc=new GregorianCalendar();
		gc.add(Calendar.DATE, 1);
		t.schedule(task, gc.getTime(),24*60*60*1000);
	}
	
	public void savePath(String path) {
		sPath=new File(path);
	}
	
	public void discoverChildren() {
		try {
			ArrayList<String> keep=updateCache(dPath,sPath);
			for(int i=0;i<keep.size();i++) {
				String[] s=keep.get(i).split(",");
				String type="media";
				if(s.length>2)
					type=s[2];
				if(!type.equals("media"))
					continue;
				File f=new File(s[0]);
				String name=f.getName();
				addChild(new ChannelCacheItem(name,f));
			}
		}
		catch (Exception e) {	
		}
	}
	
	private ArrayList<String> updateCache(File data,File save) throws Exception {
		ArrayList<String> keep=gatherCache(data,save);
		File cache=new File(data+File.separator+"cache");
		FileOutputStream fos=new FileOutputStream(cache);
		for(int i=0;i<keep.size();i++) 
			fos.write(keep.get(i).getBytes(),0,keep.get(i).length());
		fos.flush();
		fos.close();
		return keep;
	}
	
	private ArrayList<String> gatherCache(File data,File save) throws Exception {
		File cache=new File(data+File.separator+"cache");
		FileInputStream fis=new FileInputStream(cache);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis));
		long now=System.currentTimeMillis();
		String line;
		ArrayList<String> keep=new ArrayList<String>();
	    while ((line = in.readLine()) != null) {
	    	if(ChannelUtil.ignoreLine(line))
	    		continue;
	    	line=line.trim();
	    	String[] s=line.split(",");
	    	if(s.length<2)
	    		continue;
	    	File realFile=new File(s[0]);
	       	if(!realFile.exists()) // if the file is gone,no need to bother
	       		continue;
	    	long ttd;
	    	try {
	    		ttd=Long.parseLong(s[1]);
	    	}
	    	catch (Exception e) {
	    		continue;
	    	}
	    	if(now<ttd) {
	    		Channels.debug("file "+s[0]+" has not timed out keep it");
	    		keep.add(line);
	    		continue;
	    	}
	    	if(realFile.exists()) {
	    		Channels.debug("file "+s[0]+" is too old delete it");
	    		realFile.delete();
	    	}
	    }
	    in.close();
	    return keep;
	}
	
	
}
