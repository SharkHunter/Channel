package com.sharkhunter.channel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import net.pms.PMS;

public class ChannelDbg {
	private BufferedWriter os;
	private File f;
	private SimpleDateFormat sdfHour;
	private SimpleDateFormat sdfDate;
	
	public ChannelDbg(File f) {
		this.f=f;
		os=null;
		sdfHour = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US); //$NON-NLS-1$
        sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); //$NON-NLS-1$
	}
	
	public void start() {
		if(os!=null)
			return;
		try {
			os=new BufferedWriter(new FileWriter(f,false));
			debug("Started");
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
	
	public boolean status() {
		return (os!=null);
	}
	
	public void debug(String str) {
		if(os==null)
			return;
		try {
			String s=sdfHour.format(new Date(System.currentTimeMillis()))+" "+str;
			os.write("\n\r"+s+"\n\r");
			os.flush();
		} catch (IOException e) {
			PMS.debug("[Channel]: "+str);
		}
	}
}
