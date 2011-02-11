package com.sharkhunter.channel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.pms.PMS;

public class ChannelDbg {
	private BufferedWriter os;
	
	public ChannelDbg(File f) {
		try {
			os=new BufferedWriter(new FileWriter(f,false));
		} catch (Exception e) {
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
