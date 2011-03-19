package com.sharkhunter.channel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private String nameScript;

	public ChannelSubs(String name,ArrayList<String> data,File dPath) {
		best=1;
		script=null;
		nameScript=null;
		this.dPath=new File(dPath.getAbsolutePath()+File.separator+"data");
		this.name=name;
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
				nameScript=keyval[1];
			}
			if(keyval[0].equalsIgnoreCase("script")) {
				script=keyval[1];
			}
				
		}
	}
	
	public String getSubs(String mediaName) {
		Channels.debug("get subs "+mediaName);
		if(ChannelUtil.empty(mediaName))
			return null;
		mediaName=mediaName.trim();
		File f=new File(dPath.getAbsolutePath()+File.separator+mediaName);
		if(f.exists())
			return f.getAbsolutePath();
		String subUrl=fetchSubsUrl(mediaName);
		Channels.debug("subUrl "+subUrl);
		if(ChannelUtil.empty(subUrl))
			return null;
		subUrl=subUrl.replace("&amp;", "&");
		try {
			URL u=new URL(subUrl);
			URLConnection connection=u.openConnection();
			connection.setRequestProperty("User-Agent",ChannelUtil.defAgentString);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			InputStream in=connection.getInputStream();
			FileOutputStream out=new FileOutputStream(f);
			byte[] buf = new byte[4096];
			int len;
			while((len=in.read(buf))!=-1)
				out.write(buf, 0, len);
			out.flush();
			out.close();
			in.close();
			return f.getAbsolutePath();
		}
		catch (Exception e) {
			Channels.debug("Error fetching subfile "+e);
		}
		return null;
	}
	
	public String fetchSubsUrl(String mediaName) {
		if(!ChannelUtil.empty(nameScript)) {
			ArrayList<String> s=Channels.getScript(nameScript);
			if(s!=null) {
				mediaName=ChannelNaviXProc.lite(mediaName, s, Format.AUDIO);
			}
			else
				mediaName=ChannelUtil.escape(mediaName);
		}	
		else
			mediaName=ChannelUtil.escape(mediaName);
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
				if(s!=null) 
					return ChannelNaviXProc.lite(page, s, Format.AUDIO);
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
	
	
	@Override
	public boolean onlyFirst() {
		return false;
	}

	@Override
	public String separator(String base) {
		return null;
	}
	
}