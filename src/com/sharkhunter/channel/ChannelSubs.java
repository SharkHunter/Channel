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
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.formats.v2.SubtitleType;

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
	private ChannelMatcher select;
	protected String img;
	
	public ChannelSubs() {
		prop=null;
		dPath=new File(Channels.dataPath());
	}

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
			if(keyval[0].equalsIgnoreCase("select")) {
				if(select==null)
					select=new ChannelMatcher(keyval[1],null,this);
				else
					select.setMatcher(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("select_order")) {
				if(select==null)
					select=new ChannelMatcher(null,keyval[1],this);
				else
					select.setOrder(keyval[1]);
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
			if(keyval[0].equalsIgnoreCase("img")) {
				img=keyval[1];
			}
		}
		if(select!=null)
			select.processProps(prop);
		if(matcher!=null)
			matcher.processProps(prop);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}
	
	public String getImg() {
		return img;
	}
	
	private String rarFile(File f) {
		boolean concat=ChannelUtil.getProperty(prop, "rar_concat");
		boolean keep=ChannelUtil.getProperty(prop, "rar_keep");
		boolean rename=!ChannelUtil.getProperty(prop, "rar_norename");
		boolean first=true;
		int id=1;
		com.github.junrar.Archive rarFile = null;
		String firstName=null;
		try {
			rarFile = new com.github.junrar.Archive(f);
			com.github.junrar.rarfile.FileHeader fh =rarFile.nextFileHeader();
			while(fh!=null) {
				if(!fh.getFileNameString().contains(".srt")) {
					fh =rarFile.nextFileHeader();
					continue;
				}
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
					fName=dPath+File.separator+i+entry.getName();		
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
		//Channels.debug("look for "+f.getAbsolutePath());
		if(f.exists())
			return cacheFile(f);
		map.put("lang", lang);
		String subUrl=fetchSubsUrl(map);
		Channels.debug("subUrl "+subUrl);
		if(ChannelUtil.empty(subUrl))
			return null;
		boolean zip=ChannelUtil.getProperty(prop, "zip_force");
		boolean rar=ChannelUtil.getProperty(prop, "rar_force");
		return downloadSubs(subUrl,path,zip,rar);
	}
	
	public static String downloadSubs(String subUrl) {
		int pos=subUrl.lastIndexOf('/');
		String name="sub_"+System.currentTimeMillis();
		if(pos!=-1)
			name=subUrl.substring(pos+1);
		return downloadSubs(subUrl,name);
	}
	
	public static String downloadSubs(String subUrl,String name) {
		name=name.replaceAll("[\\?&=;,]", "").replace('/', '_').replace('\\', '_');
		String path=Channels.dataPath()+File.separator+name+".srt";
		ChannelSubs nullSub=new ChannelSubs();
		return nullSub.downloadSubs(subUrl,path,false,false);
		
	}
	
	
	private String downloadSubs(String subUrl,String path, boolean zip,boolean rar) {
		File f=new File(path);
		if(f.exists())
			return cacheFile(f);
		zip=zip||subUrl.contains("zip");
		rar=rar||subUrl.contains("rar");
		subUrl=subUrl.replace("&amp;", "&");
		if(zip)
			f=new File(path+".zip");
		if(rar)
			f=new File(path+".rar");
		if(!ChannelUtil.downloadBin(subUrl, f,!(zip||rar)))
			return null;
		if(zip)  // zip file
			return zipFile(f);
		if(rar)
			return rarFile(f);
		return cacheFile(f);
	}
	
	private String getMediaName(String mediaName,HashMap<String,String> map) {
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
		return mediaName;
	}
	
	public String fetchSubsUrl(HashMap<String,String> map) {
		String mediaName=map.get("url").trim();
		mediaName=getMediaName(mediaName,map);
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
	
	public HashMap<String,Object> select(HashMap<String,String> map) {
		if(select==null)
			return null;
		String mediaName=map.get("url");
		Channels.debug("sub sel name "+mediaName);
		if(ChannelUtil.empty(mediaName))
			return null;
		mediaName=getMediaName(mediaName.trim(),map);
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
			HashMap<String, Object> res=new HashMap<String,Object>();
			select.startMatch(page);
			while(select.match()) {
				ChannelSubSelected css=new ChannelSubSelected();
				css.owner=this;
				css.url=select.getMatch("url");
				css.script=script;
				css.name=mediaName;
				css.lang=null;
				if(!ChannelUtil.empty(select.getMatch("lang")))
					css.lang=select.getMatch("lang");
				if(ChannelUtil.empty(css.lang)) {
					if(lang!=null&&lang.length==1&&!lang[0].equalsIgnoreCase("all"))
						css.lang=lang[0];
				}
				res.put(select.getMatch("name"), (Object)css);
			}
			return res;
		}
		catch (Exception e) {
			Channels.debug("page exception "+e+" "+realUrl);
		}
		return null;
	}

	public static String icon(Object obj,String icon) {
		if(obj instanceof String)
			return icon;
		ChannelSubSelected css=(ChannelSubSelected)obj;
		if(ChannelUtil.empty(css.lang)) 
			return icon;
		return "/resource/images/codes/"+ChannelISO.iso(css.lang, 3)+".png";
	}
	
	public String resolve(ChannelSubSelected css) {
		return "";
	}
	
	public static String resolve(Object obj) {
		if(obj instanceof String)
			return downloadSubs((String)obj);
		ChannelSubSelected css=(ChannelSubSelected)obj;
		Channels.debug("resolve subtitle url="+css.url+",script="+css.script);
		if(css.owner!=null) {
			String res=css.owner.resolve(css);
			if(!ChannelUtil.empty(res))
				return res;
		}
		if(ChannelUtil.empty(css.script))
			if(ChannelUtil.empty(css.name))
				return downloadSubs(css.url);
			else
				return downloadSubs(css.url,css.name);
		ArrayList<String> s=Channels.getScript(css.script);
		if(s==null) 
			return null;
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("select", "true");
		HashMap<String,String> res=ChannelNaviXProc.lite(css.url, s,map);
		if(res==null)
			return null;
		String subUrl=res.get("url");
		if(ChannelUtil.empty(subUrl))
			return null;
		if(ChannelUtil.empty(css.name))
			return downloadSubs(subUrl);
		else
			return downloadSubs(subUrl,css.name);
	}
	
	public boolean langSupported() {
		return !ChannelUtil.empty(langPrefered());
	}
	
	public String langPrefered() {
		if(lang==null)
			return null;
		String[] langCode=PMS.getConfiguration().getSubtitlesLanguages().split(",");
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
	
	@Override
	public boolean escape(String base) {
		return false;

	}

	@Override
	public boolean unescape(String base) {
		return false;
	}
	
	public String mangle(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_mangle");
	}
	
	public boolean selectable() {
		return (select!=null);
	}
}