package com.sharkhunter.channel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.LoggerFactory;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;

public class ChannelDbg {
	private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ChannelDbg.class);
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

	private void ensureDbgPack() throws ConfigurationException {
		PmsConfiguration conf=PMS.getConfiguration();
		if(conf==null)
			return;
		String str = (String) conf.getCustomProperty("dbgpack");
		if (str == null) {
			str=f.getPath();
		}
		else {
			if(str.contains(f.getPath()))
				return;
			str=str+","+f.getPath();
		}
		conf.setCustomProperty("dbgpack", str);
		conf.save();
	}

	public void start() {
		if(os!=null)
			return;
		try {
			ensureDbgPack();
		} catch (ConfigurationException e1) {
		}
		try {
			os=new BufferedWriter(new FileWriter(f,false));
			debug("Started "+Channels.VERSION);
		} catch (Exception e) {
			os=null;
		}
	}

	public void stop() {
		if(os!=null) {
			try {
				os.flush();
				os.close();
				debug("Stopped");
			}
			catch (IOException e) {}
			os=null;
		}
	}

	public boolean status() {
		return (os!=null);
	}

	public File logFile() {
		return f;
	}

	public void debug(String str) {
		if(os==null)
			return;
		try {
			String s=sdfHour.format(new Date(System.currentTimeMillis()))+" "+str;
			os.write("\n\r"+s+"\n\r");
			os.flush();
		} catch (IOException e) {
			LOGGER.debug("{Channel} {}", str);
		}
	}
}
