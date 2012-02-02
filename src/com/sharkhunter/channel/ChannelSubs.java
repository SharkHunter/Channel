package com.sharkhunter.channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;

public class ChannelSubs implements ChannelProps {
	
	private String name;
	private String url;
	private ChannelMatcher matcher;
	private int best;
	private int pathCnt;
	private String[] prop;
	private File dPath;
	private String script;
	private String[] nameScript;
	private String[] lang;

	public ChannelSubs(String name,ArrayList<String> data,File dPath) {
		best=1;
		script=null;
		nameScript=null;
		this.dPath=new File(dPath.getAbsolutePath()+File.separator+"data");
		this.name=name;
		lang=null;
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			String[] keyval=line.split("\\s*=\\s*",2);
			if(keyval.length<2) // ignore weird lines
				continue;
			if(keyval[0].equalsIgnoreCase("url"))
				url=keyval[1];
			if(keyval[0].equalsIgnoreCase("matcher")) {
				if(matcher==null)
					matcher=new ChannelMatcher(keyval[1],null,this);
				else
					matcher.setMatcher(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("order")) {
				if(matcher==null)
					matcher=new ChannelMatcher(null,keyval[1],this);
				else
					matcher.setOrder(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("best_match")) {
				try {
					Integer j=new Integer(keyval[1]);
					best=j.intValue();
				}
				catch (Exception e) {
				}
				if(best<1)
					best=1;
			}
			if(keyval[0].equalsIgnoreCase("prop"))	
				prop=keyval[1].trim().split(",");	
			if(keyval[0].equalsIgnoreCase("name_script")) {
				nameScript=keyval[1].split(",");
			}
			if(keyval[0].equalsIgnoreCase("script")) {
				script=keyval[1];
			}	
			if(keyval[0].equalsIgnoreCase("lang")) {
				lang=keyval[1].trim().split(",");
			}
		}
	}
	
	private String rarFile(File f) {
		boolean concat=ChannelUtil.getProperty(prop, "rar_concat");
		boolean keep=ChannelUtil.getProperty(prop, "rar_keep");
		boolean rename=!ChannelUtil.getProperty(prop, "rar_norename");
		boolean first=true;
		int id=1;
		Archive rarFile = null;
		String firstName=null;
		try {
			rarFile = new Archive(f);
			FileHeader fh =rarFile.nextFileHeader();
			while(fh!=null) {
				if(!fh.getFileNameString().contains(".srt"))
					continue;
				String fName=dPath+File.separator+fh.getFileNameString();
				FileOutputStream fos=null;
				if(rename) {
					String i=first?"":"_"+String.valueOf(id++);
					
				}
				if(concat) {
					if(first) {
						fName=(f.getAbsolutePath()+".srt").replace(".rar", "");
						fos = new FileOutputStream(fName);		
					}
				}
				else {
					fos = new FileOutputStream(fName);
				}
				if(first) {
					first=false;
					firstName=fName;
				}
				rarFile.extractFile(fh,fos);
				fos.flush();
				fos.close();
				fh =rarFile.nextFileHeader();
			}
			if(!keep)
				f.delete();
		} catch (Exception e) {
			Channels.debug("rar file extract error "+e);
		} finally {
			try {
				rarFile.close();
			} catch (IOException e) {
			}
		}
		if(ChannelUtil.empty(firstName))
			return null;
		return cacheFile(new File(firstName));
	}
	
	private String zipFile(File f) {
		boolean concat=ChannelUtil.getProperty(prop, "zip_concat");
		boolean keep=ChannelUtil.getProperty(prop, "zip_keep");
		boolean rename=!ChannelUtil.getProperty(prop, "zip_norename");
		boolean first=true;
		int id=1;
		ZipInputStream zis;
		try {
			zis = new ZipInputStream(new FileInputStream(f));
		} catch (Exception e) {
			Channels.debug("error reading zipped subtile");
			return null;
		}
		ZipEntry entry;
		try {
			String firstName=null;
			OutputStream dest=null;
			while((entry = zis.getNextEntry()) != null) {
				Channels.debug("Extracting: " +entry);
				int count;
				final int BUFFER = 2048;
				byte data[] = new byte[BUFFER];
				// write the files to the disk
				String fName=dPath+File.separator+entry.getName();
				if(!entry.getName().contains(".srt")) // no .srt ignore
					continue;
				if(rename) {
					String i=first?"":"_"+String.valueOf(id++);
					fName=(f.getAbsolutePath()+i+".srt").replace(".zip", "");
				}
				if(concat) {
					if(first) {
						fName=(f.getAbsolutePath()+".srt").replace(".zip", "");
						FileOutputStream fos1 = new FileOutputStream(fName);		
						dest = new BufferedOutputStream(fos1, BUFFER);
					}
				}
				else {
					FileOutputStream fos1 = new FileOutputStream(fName);
					dest=new BufferedOutputStream(fos1, BUFFER);
				}
				if(first) {
					first=false;
					firstName=fName;
				}
				while ((count = zis.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				if(!concat) {
					dest.flush();
					dest.close();
				}	
			}
			if(concat) {
				dest.flush();
				dest.close();
			}
			zis.close();
			if(!keep)
				f.delete();
			return cacheFile(new File(firstName)); // return the first name no matter what
		} 
		catch (Exception e) {
			Channels.debug("error "+e+" reading zipped subtile");
			return null;
		}
	}
	
	private String cacheFile(File f) {
		ChannelUtil.cacheFile(f,"sub");
		return f.getAbsolutePath();
	}
	
	public String getSubs(String mediaName) {
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("url", mediaName);
		return getSubs(map);
	}
	
	public String getSubs(HashMap<String,String> map) {
		String mediaName=map.get("url");
		Channels.debug("get subs "+mediaName);
		if(ChannelUtil.empty(mediaName))
			return null;
		String lang=langPrefered();
		int iso=3;
		if(ChannelUtil.getProperty(prop, "iso2"))
			iso=2;
		lang=ChannelISO.iso(lang, iso);
		String stash=ChannelUtil.getPropertyValue(prop, "lang_stash");
		if(!ChannelUtil.empty(stash))
			lang=Channels.getStashData(stash, lang, lang);
		mediaName=mediaName.trim();
		String path=dPath.getAbsolutePath()+File.separator+mediaName;
		path=ChannelUtil.append(path, "_", map.get("season"));
		path=ChannelUtil.append(path, "", map.get("episode"));
		path=ChannelUtil.append(path, "_", lang);
		File f=new File(path+".srt");
		if(f.exists())
			return cacheFile(f);
		map.put("lang", lang);
		String subUrl=fetchSubsUrl(map);
		Channels.debug("subUrl "+subUrl);
		if(ChannelUtil.empty(subUrl))
			return null;
		boolean zip=ChannelUtil.getProperty(prop, "zip_force")||subUrl.contains("zip");
		boolean rar=ChannelUtil.getProperty(prop, "rar_force")||subUrl.contains("rar");
		subUrl=subUrl.replace("&amp;", "&");
		if(zip)
			f=new File(path+".zip");
		if(rar)
			f=new File(path+".rar");
		if(!ChannelUtil.downloadBin(subUrl, f))
			return null;
		if(zip)  // zip file
			return zipFile(f);
		if(rar)
			return rarFile(f);
		return cacheFile(f);
	}
	
	public String fetchSubsUrl(HashMap<String,String> map) {
		String mediaName=map.get("url").trim();
		if(nameScript!=null) {
			String nScript=nameScript[0];
			ArrayList<String> s=Channels.getScript(nScript);
			if(s!=null) {
				HashMap<String,String> res=ChannelNaviXProc.lite(mediaName, s,map);
				if(res==null) // weird stuff, this didn't work anyhow, try another one
					return null;
				mediaName=res.get("url");
				if(nameScript.length>1)
					if(nameScript[1].equalsIgnoreCase("full")) // full script
						return mediaName;
			}
			else
				mediaName=ChannelUtil.escape(mediaName);
		}	
		else
			mediaName=ChannelUtil.escape(mediaName);
		if(ChannelUtil.empty(mediaName))
			return null;
		String realUrl=ChannelUtil.concatURL(url,mediaName);
		Channels.debug("try fecth "+realUrl);
		try {
			URL u=new URL(realUrl);
			String page=ChannelUtil.fetchPage(u.openConnection());
			Channels.debug("subs page "+page);
			if(ChannelUtil.empty(page))
				return null;
			if(!ChannelUtil.empty(script)) { // we got a script, we'll use it 
				ArrayList<String> s=Channels.getScript(script);
				if(s!=null) {
					HashMap<String,String> res=ChannelNaviXProc.lite(page, s,map);
					return res.get("url");
				}
			}
			matcher.startMatch(page);
			int cnt=1;
			String first=null;
			while(matcher.match()) {
				if(cnt==best)
					return matcher.getMatch("url",true);
				if(cnt==1) // we might never find a better one...
					first=matcher.getMatch("url",true);
				cnt++;
			}
			return first;
		}
		catch (Exception e) {
			Channels.debug("page exception "+e+" "+realUrl);
		}
		return null;
	}
	
	public boolean langSupported() {
		return !ChannelUtil.empty(langPrefered());
	}
	
	public String langPrefered() {
		if(lang==null)
			return null;
		String[] langCode=PMS.getConfiguration().getMencoderSubLanguages().split(",");
		if(lang[0].equals("all"))
			return langCode[0];
		for(int j=0;j<langCode.length;j++) 
			for(int i=0;i<lang.length;i++) 
				if(ChannelISO.equal(langCode[j],lang[i]))
					return lang[i];
		return null;
	}
	
	
	@Override
	public boolean onlyFirst() {
		return false;
	}

	@Override
	public String separator(String base) {
		return null;
	}

	@Override
	public String append(String base) {
		return null;
	}

	@Override
	public String prepend(String base) {
		return null;
	}
}