package com.sharkhunter.channel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.WebAudioStream;
import net.pms.dlna.WebVideoStream;

public class ChannelItem implements ChannelProps{
	
	public boolean Ok;
	
	private Channel parent;
	private ChannelFolder parentFolder;
	
	private ChannelMatcher matcher;
	
	private String url;
	private String name;
	private String[] prop;
	
	private String thumbURL;
	
	private ArrayList<ChannelMedia> mediaURL;
	
	public ChannelItem(ArrayList<String> data,Channel parent,ChannelFolder pf) {
		Ok=false;
		this.parent=parent;
		parentFolder=pf;
		matcher=null;
		mediaURL=new ArrayList<ChannelMedia>();
		thumbURL=null;
		parse(data);
		Ok=true;
	}
	
	public void parse(ArrayList<String> data) {
		for(int i=0;i<data.size();i++) {
			String line=data.get(i).trim();
			if(line.contains("media {")) {
				ArrayList<String> m=ChannelUtil.gatherBlock(data,i+1);
				i+=m.size();
				ChannelMedia m1=new ChannelMedia(m,parent);
				if(m1.Ok)
					mediaURL.add(m1);
				continue;
			}
			String[] keyval=line.split("=",2);
			if(keyval.length<2) // ignore weird lines
				continue;
			if(keyval[0].equalsIgnoreCase("macro")) {
				ChannelMacro m=parent.getMacro(keyval[1]);
				if(m!=null)
					parse(m.getMacro());
				else
					PMS.debug("unknown macro "+keyval[1]);
			}	
			if(keyval[0].equalsIgnoreCase("name"))
				name=keyval[1];
			if(keyval[0].equalsIgnoreCase("url"))	
				url=keyval[1];
			if(keyval[0].equalsIgnoreCase("prop"))	
				prop=keyval[1].trim().split(",");
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
		}
	}
	
	public boolean autoMedia() {
		return ChannelUtil.getProperty(prop, "auto_media");
	}
	
	public ChannelMatcher getMatcher() {
		return matcher;
	}
	
	public void match(DLNAResource res) throws MalformedURLException {
		match(res,"","","",null);
	}
	
	public void match(DLNAResource res,String filter,String urlEnd,String backupName,
			String pThumb) throws MalformedURLException {
		String u=ChannelUtil.concatURL(url,urlEnd);
		URL urlobj=new URL(ChannelUtil.pendData(u,prop,"url"));
		parent.debug("item match url "+urlobj.toString());
		String page=ChannelUtil.fetchPage(urlobj,parent.getAuth());
	    parent.debug("page "+page);
	    if(page==null||page.length()==0)
	    	return;
	    for(int i=0;i<mediaURL.size();i++) {
	    	ChannelMedia m1=mediaURL.get(i);
	    	ChannelMatcher m=m1.getMatcher();
	    	m.startMatch(page);
	    	parent.debug("matching using expr "+m.getRegexp().pattern());
	    	while(m.match()) {
	    		String mURL=m.getMatch("url",true);
	    		String newName=m.getMatch("name",false,backupName);
	    		String thumb=m.getMatch("thumb", false);
	    		if(thumb==null||thumb.length()==0)
	    			if(pThumb!=null&&pThumb.length()!=0)
	    				thumb=pThumb;
	    			else
	    				thumb=parent.getThumb();
	    		
	    		m1.add(res, newName, mURL, thumb);
	    	}
	    }
	}
	
	public String separator(String base) {
		return ChannelUtil.getPropertyValue(prop, base+"_separator");
	}
	
}
