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

public class ChannelSubs implements ChannelProps {
	
	private String name;
	private String url;
	private ChannelMatcher matcher;
	private int best;
	private int pathCnt;
	private Pattern nameMangle;
	private String[] prop;
	private File dPath;

	public ChannelSubs(String name,ArrayList<String> data,File dPath) {
		best=1;
		pathCnt=0;
		this.dPath=new File(dPath.getAbsolutePath()+File.separator+"data");
		this.name=name;
		nameMangle=null;
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
			if(keyval[0].equalsIgnoreCase("mangle_name")) {
				nameMangle=Pattern.compile(keyval[1]);
			}
			if(keyval[0].equalsIgnoreCase("prop"))	
				prop=keyval[1].trim().split(",");				
		}
	}
	
	public String getSubs(ArrayList<DLNAResource> path) {
		DLNAResource r;
		if(pathCnt==0) // pick last
			r=path.get(path.size()-1);
		else
			r=path.get(pathCnt-1);
		return getSubs(r.getName());
	}
	
	public String getSubs(String mediaName) {
		Channels.debug("get subs "+mediaName);
		if(ChannelUtil.empty(mediaName))
			return null;
		// Maybe we should mangle the name?
		if(nameMangle!=null) {
			Matcher m=nameMangle.matcher(mediaName);
			if(m.find())
				mediaName=m.group(1);
		}
		mediaName=mediaName.trim();
		Channels.debug("mangled name "+mediaName);
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
			File f=new File(dPath.getAbsolutePath()+File.separator+mediaName);
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
		String realUrl=ChannelUtil.concatURL(url,ChannelUtil.escape(mediaName));
		Channels.debug("try fecth "+realUrl);
		try {
			URL u=new URL(realUrl);
			String page=ChannelUtil.fetchPage(u.openConnection());
			Channels.debug("subs page "+page);
			if(ChannelUtil.empty(page))
				return null;
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