package com.sharkhunter.channel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import net.pms.dlna.DLNAResource;

public class ChannelMonitorMgr {
	
	private ArrayList<ChannelMonitor> monitors;
	
	private static final int PERIOD = 24*60*60*1000;
	private static final int DEFAULT_DELAY_SCAN = 10*1000;
	
	public ChannelMonitorMgr() {
		monitors=new ArrayList<ChannelMonitor>();
		Timer timer=new Timer();
		GregorianCalendar launchTime=new GregorianCalendar();
		launchTime.set(Calendar.HOUR_OF_DAY, 0);
		launchTime.set(Calendar.MINUTE,15);
		Date now=new Date(System.currentTimeMillis());
		if(now.after(launchTime.getTime()))
			launchTime.add(Calendar.DATE, 1);
		timer.schedule(new TimerTask() {
		    public void run() {
		    	scanAll();
		    }}, launchTime.getTime(),PERIOD);
	}
	
	public void add(ChannelMonitor m) {
		monitors.add(m);
	}
	
	public void scanAll() {
		for(ChannelMonitor m : monitors)
    		m.scan();
	}
	
	public boolean monitored(String name) {
		for(ChannelMonitor m :monitors) {
			if(m.getName().equals(name))
				return true;
		}
		return false;
	}
	
	public ChannelMonitor getMonitor(String name) {
		for(ChannelMonitor m :monitors) {
			if(name.equals(m.getName()))
				return m;
		}
		return null;
	}
	
	public boolean update(String name,String newEntry) {
		ChannelMonitor m=getMonitor(name);
		if(m!=null)
			return m.addEntry(newEntry);
		return false;
	}
	
	public void delayedScan() {
		delayedScan(DEFAULT_DELAY_SCAN);
	}
	
	public void delayedScan(final int delay) {
		if(!Channels.cfg().crawl())
			return;
		Runnable r=new Runnable() {
    		public void run() {
    			ChannelUtil.sleep(delay);
    			scanAll();
    		}
    	};
    	new Thread(r).start();
	}
}
