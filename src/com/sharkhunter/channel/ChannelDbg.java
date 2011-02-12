package com.sharkhunter.channel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.pms.PMS;

public class ChannelDbg {
	private BufferedWriter os;
	private File f;
	
	public ChannelDbg(File f) {
		this.f=f;
		os=null;
	}
	
	public void start() {
		if(os!=null)
			return;
		try {
			os=new BufferedWriter(new FileWriter(f,false));
		} catch (Exception e) {
			os=null;
		}
	}
	
	public void stop() {
		if(os!=null) {
			try {
				os.flush();
				os.close();
			}
			catch (IOException e) {}			
			os=null;
		}
	}
	
	public void debug(String str) {
		if(!Channels.debug)
			return;
		try {
			os.write("\n\r"+str+"\n\r");
			os.flush();
		} catch (IOException e) {
			PMS.debug("[Channel]: "+str);
		}
	}
}
