package com.sharkhunter.channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.pms.PMS;

public class ChannelCfg {
	
	private String chPath;
	private String saPath;
	private String rtmpPath;
	private String scriptPath;
	private Channels top;
	
	public ChannelCfg(Channels top) {
		chPath=null;
		saPath=null;
		rtmpPath=null;
		scriptPath=null;
		this.top=top;
	}
	
	public void setPath(String p) {
		chPath=p;
	}
	
	public void setSavePath(String p) {
		saPath=p;
	}
	
	public void setRtmpPath(String p) {
		rtmpPath=p;
	}
	
	public void setScriptPath(String p) {
		scriptPath=p;
	}

	public String getPath() {
		return chPath;
	}
	
	public String getSavePath() {
		return saPath;
	}
	
	public String getRtmpPath() {
		return rtmpPath;
	}
	
	public String getScriptPath() {
		return scriptPath;
	}
	
	public void init() {
		chPath=top.getPath();
		saPath=top.getSavePath();
		scriptPath=(String) PMS.getConfiguration().getCustomProperty("pmsencoder.script.directory");			
	}
	
	public void commit() {
		top.setSave(saPath);
		top.setPath(chPath);
		ensureCreated(saPath);
		ensureCreated(chPath);
		try {
			validatePMSEncoder();
			updateRTMPScript();
			PMS.getConfiguration().setCustomProperty("channels.path",chPath);
			PMS.getConfiguration().setCustomProperty("channels.save",saPath);
			PMS.getConfiguration().setCustomProperty("pmsencoder.script.directory",scriptPath);
			PMS.getConfiguration().save();
		} catch (Exception e) {
		}
	}
	
	private void updateRTMPScript() throws Exception {
		if(ChannelUtil.empty(rtmpPath))
			return;
		String fName=scriptPath+File.separator+"rtmp.groovy";
		StringBuilder sb=new StringBuilder();
		FileInputStream fis=new FileInputStream(fName);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis));
		String str;
	    while ((str = in.readLine()) != null) {
	    	sb.append(str);
	    	sb.append("\n");
	    }
	    in.close();
	    str=sb.toString();
	    str=str.replace("RTMPDUMP = \'C:\\\\rtmpdump.exe\'", "RTMPDUMP = \'"+rtmpPath+"\'");
	    FileOutputStream fos=new FileOutputStream(fName);
	    fos.write(str.getBytes());
	    fos.flush();
	    fos.close();
	}
	
	public void ensureCreated(String p) {
		File f=new File(p);
		if(!(f.exists()&&f.isDirectory()))
			f.mkdir();
	}
	
	private void validatePMSEncoder() throws IOException {
		setEngines();
		if(ChannelUtil.empty(scriptPath)) { 
			ensureCreated("scripts");
			scriptPath=new File("scripts").getCanonicalPath().toString();
		}
		else
			ensureCreated(scriptPath);
	}
	
	private void setEngines() {
		List<String> eng=PMS.getConfiguration().getEnginesAsList(PMS.get().getRegistry());
		for(int i=0;i<eng.size();i++) {
			if(eng.get(i).equalsIgnoreCase("pmsencoder")) // pmsencoder is there  
				return;
		}
		eng.add("pmsencoder");
		PMS.getConfiguration().setEnginesAsList((ArrayList<String>) eng);		
	}
	
	////////////////////////////////////////////
	// Fetch files
	///////////////////////////////////////////
	
	private static final String chZip="http://cloud.github.com/downloads/SharkHunter/Channel/channels.zip"; 
	
	public void fetchChannels() {
		String zName=chPath+File.separator+"tmp.zip";
		try {			
			validatePMSEncoder();
			URL u=new URL(chZip);
			URLConnection connection=u.openConnection();
			connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE; rv:1.9.2.3) Gecko/20100409 Firefox/3.6.3");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			InputStream in=connection.getInputStream();
			BufferedOutputStream fos=new BufferedOutputStream(new FileOutputStream(zName));
			int b;
			while((b=in.read())!=-1) 
				fos.write(b);

			fos.flush();
			fos.close();
			in.close();
			
			//Channels.debug("extract zip ");
			FileInputStream fis = new FileInputStream(zName);
	         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
	         ZipEntry entry;
	         while((entry = zis.getNextEntry()) != null) {
	            //Channels.debug("Extracting: " +entry);
	            int count;
	            final int BUFFER = 2048;
	            byte data[] = new byte[BUFFER];
	            // write the files to the disk
	            String fName=chPath+File.separator+entry.getName();
	            if(entry.getName().contains(".groovy")) // script
	            	if(scriptPath!=null)
	            		fName=scriptPath+File.separator+entry.getName();
	            FileOutputStream fos1 = new FileOutputStream(fName);
	            BufferedOutputStream dest = new BufferedOutputStream(fos1, BUFFER);
	            while ((count = zis.read(data, 0, BUFFER)) != -1) {
	               dest.write(data, 0, count);
	            }
	            dest.flush();
	            dest.close();
	         }
	         zis.close();
	         updateRTMPScript();
	      } catch(Exception e) {
	    	  Channels.debug("error fetching channels "+e);
	      }
	      new File(zName).delete();
	}
}
