package com.sharkhunter.channel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.pms.util.FileUtil;
import net.pms.util.OpenSubtitle;

public class ChannelOpenSubs extends ChannelSubs {
	
	public ChannelOpenSubs() {
		super();
		super.setName("OpenSubtitles");
		// Check if OpenSubs is installed, this is
		// an ugly but effective way
		img="http://static.opensubtitles.org/gfx/logo.gif";
		OpenSubtitle.getName("xxx");
	}
	
		
	public boolean selectable() {
		return true;
	}
	
	public boolean langSupported() {
		return true;
	}
	
	public String resolve(ChannelSubSelected css) {
		try {
			String fName=css.name+"_"+ChannelISO.iso(css.lang, 3);
			File f=new File(OpenSubtitle.subFile(fName));
			if(f.exists())
				return f.getAbsolutePath();
			//Channels.debug("fetchin from open subs "+css.url);
			return OpenSubtitle.fetchSubs(css.url,f.getAbsolutePath());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return "";
	}
	
	public HashMap<String,Object> select(HashMap<String,String> map) {
		try {
			if(!ChannelUtil.empty(map.get("imdb")))
				return map(OpenSubtitle.findSubs(map.get("imdb")));
			if(!ChannelUtil.empty("url"))
				return map(OpenSubtitle.querySubs(map.get("url")));
		} catch (Exception e) {
		}
		return null;
	}
	
	private HashMap<String,Object> map(Map<String, Object> data) {
		if(data==null)
			return null;
		HashMap<String,Object> res=new HashMap<String,Object>();
		for(String key : data.keySet()) {
			ChannelSubSelected css=new ChannelSubSelected();
			String[] tmp=key.split(":",2);
			css.name=key;
			if(tmp.length>1) {
				css.name=tmp[1];	
				css.lang=tmp[0];
			}
			css.name=css.name.trim();
			css.owner=this;
			css.url=(String) data.get(key);
			res.put(FileUtil.getFileNameWithoutExtension(css.name), css);			
		}
		return res;
	}
}
