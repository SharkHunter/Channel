package com.sharkhunter.channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.zip.ZipOutputStream;

import net.pms.PMS;

public class ChannelCfg {
	
	private String chPath;
	private String saPath;
	private String rtmpPath;
	private String scriptPath;
	private String sopcastPath;
	private String pplivePath;
	private String perlPath;
	private String pythonPath;
	private String get_flPath;
	private String ytPath;
	private Channels top;
	private boolean subs;
	
	public ChannelCfg(Channels top) {
		chPath=null;
		saPath=null;
		rtmpPath=null;
		scriptPath=null;
		subs=true;
		this.top=top;
	}
	
	///////////////////////////////////
	// Set methods
	///////////////////////////////////
	
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
	
	public void setSopPath(String p) {
		sopcastPath=p;
	}
	
	public void setPPLivePath(String p) {
		pplivePath=p;
	}
	
	public void setPerlPath(String p) {
		perlPath=p;
	}
	
	public void setPythPath(String p) {
		pythonPath=p;
	}
	
	public void setGetFlPath(String p) {
		get_flPath=p;
	}
	
	public void setYouTubePath(String p) {
		ytPath=p;
	}
	
	////////////////////////////////////////
	// Get methods
	////////////////////////////////////////

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
	
	public String getSopPath() {
		return sopcastPath;
	}
	
	public String getPPLivePath() {
		return pplivePath;
	}
	
	public String getPerlPath() {
		return perlPath;
	}
	
	public String getPythonPath() {
		return pythonPath;
	}
	
	public String getFlashPath() {
		if(ChannelUtil.empty(get_flPath))
			return getScriptPath();
		return get_flPath;
	}
	
	public String getYouTubePath() {
		if(ChannelUtil.empty(ytPath))
			return getScriptPath();
		return ytPath;
	}
	
	//////////////////////////////////////
	// Other methods
	//////////////////////////////////////
	
	public void init() {
		// Paths
		chPath=top.getPath();
		saPath=top.getSavePath();
		scriptPath=(String) PMS.getConfiguration().getCustomProperty("pmsencoder.script.directory");
		rtmpPath=(String) PMS.getConfiguration().getCustomProperty("rtmpdump.path");
		sopcastPath=(String) PMS.getConfiguration().getCustomProperty("sopcast.path");
		pplivePath=(String) PMS.getConfiguration().getCustomProperty("pplive.path");
		perlPath=(String) PMS.getConfiguration().getCustomProperty("perl.path");
		pythonPath=(String) PMS.getConfiguration().getCustomProperty("python.path");
		get_flPath=(String) PMS.getConfiguration().getCustomProperty("get-flash-videos.path");
		ytPath=(String) PMS.getConfiguration().getCustomProperty("youtube-dl.path");
		
		// Other
		String dbg=(String)PMS.getConfiguration().getCustomProperty("channels.debug");
		String sub=(String) PMS.getConfiguration().getCustomProperty("channels.subtitle");
		String rtmpMode=(String)PMS.getConfiguration().getCustomProperty("channels.rtmp");
		if(rtmpMode!=null) {
			if(rtmpMode.trim().equalsIgnoreCase("1"))
				Channels.rtmpMethod(Channels.RTMP_MAGIC_TOKEN);
			if(rtmpMode.trim().equalsIgnoreCase("2"))
				Channels.rtmpMethod(Channels.RTMP_DUMP);
		}
		if(!ChannelUtil.empty(dbg))
			if(dbg.equalsIgnoreCase("true"))
				Channels.debug(true);
			else
				Channels.debug(false);
		if(!ChannelUtil.empty(sub))
			if(sub.equalsIgnoreCase("true"))
				Channels.setSubs(true);
			else
				Channels.setSubs(false);
	}
	
	private void configPath(String key,String val) {
		if(!ChannelUtil.empty(val))
			PMS.getConfiguration().setCustomProperty(key,val);
	}
	
	public void commit() {
		top.setSave(saPath);
		top.setPath(chPath);
		ensureCreated(saPath);
		ensureCreated(chPath);
		String dPath=chPath+File.separator+"data";
		ensureCreated(dPath);
		try {
			ChannelNaviXNookie.init(new File(dPath+File.separator+"nookie"));
			validatePMSEncoder();
			//updateRTMPScript();
			PMS.getConfiguration().setCustomProperty("channels.path",chPath);
			PMS.getConfiguration().setCustomProperty("channels.save",saPath);
			configPath("pmsencoder.script.directory",scriptPath);
			configPath("rtmpdump.path",rtmpPath);
			configPath("sopcast.path",sopcastPath);
			configPath("pplive.path",pplivePath);
			configPath("perl.path",perlPath);
			configPath("python.path",pythonPath);
			configPath("get-flash-videos.path",get_flPath);
			configPath("youtube-dl.path",ytPath);
			PMS.getConfiguration().setCustomProperty("channels.debug",String.valueOf(Channels.debugStatus()));
			PMS.getConfiguration().setCustomProperty("channels.subtitles",String.valueOf(Channels.doSubs()));
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
		try {			
			validatePMSEncoder();
			URL u=new URL(chZip);
			URLConnection connection=u.openConnection();
			connection.setRequestProperty("User-Agent",ChannelUtil.defAgentString);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			InputStream in=connection.getInputStream();
			
			//Channels.debug("extract zip ");
	         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
	         ZipEntry entry;
	         while((entry = zis.getNextEntry()) != null) {
	            //Channels.debug("Extracting: " +entry);
	            int count;
	            final int BUFFER = 2048;
	            byte data[] = new byte[BUFFER];
	            // write the files to the disk
	            String fName=chPath+File.separator+entry.getName();
	          /*  if(entry.getName().contains(".groovy")) // script
	            	if(scriptPath!=null)
	            		fName=scriptPath+File.separator+entry.getName();*/
	            FileOutputStream fos1 = new FileOutputStream(fName);
	            BufferedOutputStream dest = new BufferedOutputStream(fos1, BUFFER);
	            while ((count = zis.read(data, 0, BUFFER)) != -1) {
	               dest.write(data, 0, count);
	            }
	            dest.flush();
	            dest.close();
	         }
	         zis.close();
	         in.close();
//	         updateRTMPScript();
	      } catch(Exception e) {
	    	  Channels.debug("error fetching channels "+e);
	      }
	}
	
	//////////////////////////////////////////////////
	// Pack debug info
	//////////////////////////////////////////////////
	
	public void packDbg() {
		String fName=chPath+File.separator+"channel_dbg.zip";
		try {
			ZipOutputStream zos=new ZipOutputStream(new FileOutputStream(fName));
			// 1st the channel.log
			File dbg=Channels.dbgFile();
			FileInputStream in = new FileInputStream(dbg);
			byte[] buf = new byte[1024];
			zos.putNextEntry(new ZipEntry(dbg.getName()));
			
			// Transfer bytes from the file to the ZIP file
			int len;
			while ((len = in.read(buf)) > 0) 
				zos.write(buf, 0, len);
			
			// Complete the entry
			zos.closeEntry();
			in.close();
			
			// 2nd pmsencoder.log
			File pmsenc=new File("pmsencoder.log");
			in = new FileInputStream(pmsenc);
			zos.putNextEntry(new ZipEntry(pmsenc.getName()));
			while ((len = in.read(buf)) > 0) 
				zos.write(buf, 0, len);
			
			// Complete the entry
			zos.closeEntry();
			in.close();
			
			// Finally PMS log
			File pms=new File("debug.log");
			in = new FileInputStream(pms);
			zos.putNextEntry(new ZipEntry(pms.getName()));
			while ((len = in.read(buf)) > 0) 
				zos.write(buf, 0, len);
			
			// Complete the entry
			zos.closeEntry();
			in.close();
			
			zos.close();
			
		} catch (Exception e) {
			PMS.debug("error packing dbg info "+e);
		}
	}
}
