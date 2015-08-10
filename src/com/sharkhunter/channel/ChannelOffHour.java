package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import net.pms.encoders.Player;
import net.pms.encoders.PlayerFactory;

public class ChannelOffHour {

	private HashMap<String,String> urls2fetch;
	private ArrayList<Thread> threads;
	private long startedAt;

	// Values of when to fetch etc.
	private int maxParralell;
	private int duration;
	private GregorianCalendar nextTime;
	private File file;
	private boolean cache;

	// Constants
	public static final int DEFAULT_MAX_THREAD=2;
	public static final int DEFAULT_MAX_DURATION=60;

	public ChannelOffHour(int max,int dur,String start,File f,boolean cache) {
		maxParralell=max;
		duration=dur;
		file=f;
		urls2fetch=new HashMap<String,String>();
		threads=new ArrayList<Thread>();
		nextTime=new GregorianCalendar();
		this.cache=cache;
		setStartTime(start);
		schedule(true);
	}

	public void schedule(boolean first) {
		final ChannelOffHour inst=this;
		TimerTask task = new TimerTask() {
		    @Override
		    public void run() {
		    	Channels.debug("run offhour fetch");
		    	inst.startedAt=System.currentTimeMillis();
		    	inst.startFetch();
		    	inst.monitorThread();
		    	Channels.debug("resched");
		    	inst.schedule(false);
		    	Channels.debug("reschedule done");
		    }
		};
		if(!first)
			getNextTime();
		Channels.debug("next offhour time "+nextTime.getTime().toString());
		Timer time=new Timer();
		time.schedule(task, nextTime.getTime());
	}

	private void setStartTime(String start) {
		SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
		try {
			int y=nextTime.get(Calendar.YEAR);
			int m=nextTime.get(Calendar.MONTH);
			int d=nextTime.get(Calendar.DATE);
			Date startTime=sdfHour.parse(start);
			nextTime.setTime(startTime);
			nextTime.set(Calendar.YEAR, y);
			nextTime.set(Calendar.MONTH, m);
			nextTime.set(Calendar.DATE, d);
		} catch (ParseException e) {
			Channels.debug("Error during offhour time parsing "+e);
			nextTime.set(Calendar.HOUR_OF_DAY,12);
			nextTime.set(Calendar.MINUTE, 0);
		}
		Date now=new Date(System.currentTimeMillis());
		if(now.after(nextTime.getTime())) // already missed first trigger, schedule tomorrow
			nextTime.add(Calendar.DATE, 1);
	}

	private Date getNextTime() {
		nextTime.add(Calendar.DATE, 1);
		return nextTime.getTime();
	}

	public void update(String url,String name,boolean add) {
		if(add)
			urls2fetch.put(name,url);
		else
			urls2fetch.remove(name);
		Channels.debug((add?"Added":"Removed")+" "+name+"("+url+") to off hour schedule");
		storeDb();
	}

	public boolean scheduled(String name) {
		return urls2fetch.containsKey(name);
	}

	public void init() {
		if(!file.exists()) // no file, bail out early
			return;
		try {
			BufferedReader in=new BufferedReader(new FileReader(file));
			String str;
			while ((str = in.readLine()) != null) {
				if(ChannelUtil.ignoreLine(str))
					continue;
				String[] s=str.trim().split(",",2);
				if(s.length<2)
					continue;
				urls2fetch.put(s[0],s[1]);
			}
			in.close();
		}
		catch (Exception e) {
			Channels.debug("Error reading offhour file "+e);
		}
	}

	public void monitorThread() {
		if(threads.size()==0) // no threads, no idea to monitor
			return;
		final long stop=startedAt+(60*duration);
		final ChannelOffHour inst=this;
		TimerTask task = new TimerTask() {
			public void run() {
				long now=System.currentTimeMillis();
				// time to quit, leave all threads running
				// until they're done
				if(now>stop)
					return;
				ArrayList<Thread> tmp=new ArrayList<Thread>(threads);
				for(int i=0;i<tmp.size();i++) {
					Thread t1=tmp.get(i);
					if(t1.isAlive()) // thread is alive let it be
						continue;
					// thread is dead, maybe start a new one
					threads.remove(t1);
					Channels.debug("thread died");
					if(urls2fetch.size()==0) { // no urls left
						return;
					}
					String[] s=getKeyVal();
					Channels.debug("launch new fetch "+s[0]+" "+s[1]);
					inst.startThread(s[0], s[1]);
				}
				if(threads.size()>0) {
					// reschedule
					inst.monitorThread();
				}

		    }
		};
		GregorianCalendar gc=new GregorianCalendar();
		gc.add(Calendar.SECOND,5);
		Timer t=new Timer();
		t.schedule(task, gc.getTime());
	}

	public void startThread(String name,String url) {
		Thread t=ChannelUtil.backgroundDownload(name,url,cache);
		if(t==null)
			return;
		threads.add(t);
		t.start();
	}

	public void startFetch() {
		if(urls2fetch.size()==0) // nothing to do
			return;
		int max=maxParralell;
		ArrayList<String> starts=new ArrayList<String>();
		for(String name : urls2fetch.keySet()) { // start threads
			if(max==0) // no more threads should start
				break;
			starts.add(name);
			String url=urls2fetch.get(name);
			Channels.debug("about to fetch "+url);
			startThread(name,url);
			max--;
		}
		for(int i=0;i<starts.size();i++)
			urls2fetch.remove(starts.get(i));
		storeDb();
	}

	private String[] getKeyVal() {
		for(String name : urls2fetch.keySet()) {
			String v=urls2fetch.get(name);
			String[] res=new String[2];
			res[0]=name;
			res[1]=v;
			urls2fetch.remove(name);
			return res;
		}
		return null;
	}

	private Player getPMSEnc() {
		ArrayList<Player> pls=PlayerFactory.getPlayers();
		for(int i=0;i<pls.size();i++) {
			if(pls.get(i).name().equalsIgnoreCase("pmsencoder"))
				return pls.get(i);
		}
		return null;
	}

	private void storeDb() {
		try {
			FileOutputStream out=new FileOutputStream(file);
			for(String name:urls2fetch.keySet() ) {
				out.write((name+","+urls2fetch.get(name)+"\n\r").getBytes());
			}
			out.flush();
			out.close();
		}
		catch (Exception e) {
		}
	}
}
