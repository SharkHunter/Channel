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
	private String cookiePath;
	private Channels top;
	private boolean subs;
	private boolean cache;
	private String credPath;
	
	public ChannelCfg(Channels top) {
		chPath=null;
		saPath=null;
		rtmpPath=null;
		scriptPath=null;
		cookiePath=null;
		subs=true;
		credPath=null;
		this.top=top;
		cache=false;
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
	
	public void setCookiePath(String p) {
		cookiePath=p;
	}
	
	public void setCredPath(String p) {
		credPath=p;
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
	
	public boolean getCache() {
		return cache;
	}
	
	public String getCookiePath() {
		return cookiePath;
	}
	
	public String getCredPath() {
		return credPath;
	}
	
	//////////////////////////////////////
	// Other methods
	//////////////////////////////////////
	
	public void init() {
		// Paths
		chPath=Channels.getPath();
		saPath=Channels.getSavePath();
		scriptPath=(String) PMS.getConfiguration().getCustomProperty("pmsencoder.script.directory");
		rtmpPath=(String) PMS.getConfiguration().getCustomProperty("rtmpdump.path");
		sopcastPath=(String) PMS.getConfiguration().getCustomProperty("sopcast.path");
		pplivePath=(String) PMS.getConfiguration().getCustomProperty("pplive.path");
		perlPath=(String) PMS.getConfiguration().getCustomProperty("perl.path");
		pythonPath=(String) PMS.getConfiguration().getCustomProperty("python.path");
		get_flPath=(String) PMS.getConfiguration().getCustomProperty("get-flash-videos.path");
		ytPath=(String) PMS.getConfiguration().getCustomProperty("youtube-dl.path");
		cookiePath=(String) PMS.getConfiguration().getCustomProperty("cookie.path");
		credPath=(String) PMS.getConfiguration().getCustomProperty("cred.path");
		
		// Other
		String dbg=(String)PMS.getConfiguration().getCustomProperty("channels.debug");
		String sub=(String) PMS.getConfiguration().getCustomProperty("channels.subtitles");
		String cacheStr=(String) PMS.getConfiguration().getCustomProperty("channels.cache");
		String rtmpMode=(String)PMS.getConfiguration().getCustomProperty("channels.rtmp");
		String group=(String)PMS.getConfiguration().getCustomProperty("channels.group_folder");
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
		if(!ChannelUtil.empty(cacheStr))
			if(cacheStr.equalsIgnoreCase("true"))
				Channels.setCache(true);
			else
				Channels.setCache(false);
		
		// Defaults
		if(ChannelUtil.empty(rtmpPath)) {
			File plugPath=new File(PMS.getConfiguration().getMplayerPath());
			String ext=(PMS.get().isWindows()?".exe":"");
			File f=new File(plugPath.getParent()+File.separator+"rtmpdump"+ext);
			if(f.exists()&&f.canExecute())
				rtmpPath=f.getAbsolutePath();
		}
		if(ChannelUtil.empty(scriptPath)) {
			File f=new File(chPath);
			scriptPath=f.getParent()+File.separator+"scripts";
		}
		if(ChannelUtil.empty(cookiePath)) {
			cookiePath=Channels.dataPath()+File.separator+"cookies";
		}
		if(ChannelUtil.empty(credPath)) // defaults to channel path
			credPath=chPath;
		if(!ChannelUtil.empty(group)&&group.equalsIgnoreCase("true"))
			Channels.setGroup(true);
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
			configPath("channels.path",chPath);
			configPath("channels.save",saPath);
			configPath("pmsencoder.script.directory",scriptPath);
			configPath("rtmpdump.path",rtmpPath);
			configPath("sopcast.path",sopcastPath);
			configPath("pplive.path",pplivePath);
			configPath("perl.path",perlPath);
			configPath("python.path",pythonPath);
			configPath("get-flash-videos.path",get_flPath);
			configPath("youtube-dl.path",ytPath);
			configPath("cookie.path",cookiePath);
			if(!chPath.equals(credPath))
				configPath("cred.path",credPath);
			PMS.getConfiguration().setCustomProperty("channels.debug",String.valueOf(Channels.debugStatus()));
			PMS.getConfiguration().setCustomProperty("channels.subtitles",String.valueOf(Channels.doSubs()));
			PMS.getConfiguration().setCustomProperty("channels.group_folder",String.valueOf(Channels.useGroupFolder()));
			PMS.getConfiguration().save();
		} catch (Exception e) {
		}
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
	private static final String extFile="http://cloud.github.com/downloads/SharkHunter/Channel/ext.txt";
	
	public void fetchChannels() {
		try {			
			validatePMSEncoder();
			URL u=new URL(chZip);
			URLConnection connection=u.openConnection();
			connection.setRequestProperty("User-Agent",ChannelUtil.defAgentString);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			InputStream in=connection.getInputStream();
			
			//Channels.debug("extract zip "+scriptPath);
	         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
	         ZipEntry entry;
	         while((entry = zis.getNextEntry()) != null) {
	          //  Channels.debug("Extracting: " +entry);
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
	         in.close();
	      } catch(Exception e) {
	    	  Channels.debug("error fetching channels "+e);
	      }
	}
	
	private void readFile(String url,String outFile) {
		try {
			URL u=new URL(url);
			URLConnection connection=u.openConnection();
			connection.setRequestProperty("User-Agent",ChannelUtil.defAgentString);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			InputStream in=connection.getInputStream();
			File f=new File(outFile);
			FileOutputStream out=new FileOutputStream(f);
			byte[] buf = new byte[4096];
			int len;
			while((len=in.read(buf))!=-1)
				out.write(buf, 0, len);
			out.flush();
			out.close();
			in.close();
		}
		catch (Exception e) {
			Channels.debug("error fetching externals(read) "+e);
		}
	}
	
	private void fetchExternals() {
		try {
			URL u=new URL(extFile);
			URLConnection connection=u.openConnection();
			connection.setRequestProperty("User-Agent",ChannelUtil.defAgentString);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			InputStream in=connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				if(ChannelUtil.ignoreLine(line))
					continue;
				String[] str=line.split(",");
				if(str.length<3)
					continue;
				String eUrl=str[0];
				String type=str[1];
				String cfgVar=str[2];
				ensureCreated(scriptPath);
				if(type.equalsIgnoreCase("raw")) {
					String outFile=scriptPath+File.separator+cfgVar.replace(".path", "");
					readFile(eUrl,outFile);
					configPath(cfgVar,outFile);
				}					
			}
			PMS.getConfiguration().save();	
		}
		catch (Exception e) {
			Channels.debug("error fetching externals "+e);
		}
	}
}
