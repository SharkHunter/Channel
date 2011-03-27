package com.sharkhunter.channel;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ChannelVCR {
	
	private Timer timer;
	
	public ChannelVCR(Date time,final String url,String proc,String name) {
		if(ChannelUtil.empty(name)) {
			name="download";
			int pos=url.lastIndexOf('/');
			if(pos!=-1) {
				name=name.substring(pos);
			}
		}
		final String rName=name;
		TimerTask task = new TimerTask() {
		    @Override
		    public void run() {
		    	Channels.debug("run VCR fetch");
		    	Thread t=ChannelUtil.backgroundDownload(rName,url,false);
				if(t==null)
					return;
				t.start();
				try {
					t.join();
				} catch (InterruptedException e) {
				}
		    }
		};
		timer=new Timer();
		timer.schedule(task, time);
	}
}
