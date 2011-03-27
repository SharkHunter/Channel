package com.sharkhunter.channel;

import java.io.InputStream;
import java.io.OutputStream;

public class ChannelSaver implements Runnable {
	
	private OutputStream[] os;
	private InputStream is;
	
	public ChannelSaver(InputStream is,OutputStream[] os) {
		this.os=os;
		this.is=is;
	}
	
	private void closeAll(OutputStream[] oss) {
		for(int i=0;i<oss.length;i++) {
			try {
				os[i].close();
			}
			catch (Exception e) {
				continue;
			}
		}
	}
	
	public void run() {
		int b;
		try {
			while((b=is.read())!=-1) {
				for(int i=0;i<os.length;i++)
					os[i].write(b);
			}
			for(int i=0;i<os.length;i++) {
				os[i].flush();
				os[i].close();
			}
		}
		catch (Exception e) {
			Channels.debug("Saver error "+e);
			// Just to make sure close all
			closeAll(os);
		}
	}
}
