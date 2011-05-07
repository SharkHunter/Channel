package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelSearch extends VirtualFolder {

	private static final int MAX_SIZE=100;
	private ArrayList<ChannelSearchItem> searchList;
	private File file;
	private boolean all;
	ChannelSearchItem last;
	
	public ChannelSearch(File f) {
		super("Recent Searches",null);
		searchList=new ArrayList<ChannelSearchItem>();
		file=f;
		all=true;
		last=null;
	}
	
	public void dump() {
		if(all) {
			try {
				FileOutputStream out = new FileOutputStream(file);
				for(int i=0;i<searchList.size();i++) {
					ChannelSearchItem item=searchList.get(i);
					String str=item.toString()+"\n";
					out.write(str.getBytes());
				}
				out.flush();
				out.close();
			}
			catch (Exception e) {
				Channels.debug("Error dump search "+e);
			}
		}
		else if(last!=null) {
			try {
				FileOutputStream out = new FileOutputStream(file,true);
				String str=last.toString()+"\n";
				out.write(str.getBytes());
				out.flush();
				out.close();
			} catch (Exception e) {
				Channels.debug("Error dump search "+e);
			}
		}
		all=false;
		last=null;
	}
	
	private ChannelSearchItem findItem(Channel ch,String id,String str) {
		ChannelSearchItem old=null;
		for(int i=0;i<searchList.size();i++) {
			ChannelSearchItem res=searchList.get(i);
			if(res.equal(ch,id,str)) {
				// this is a bit wierd but null indicates a hit
				res.touch();
				return null;
			}
			if(old==null)
				old=res;
			else
				old=(res.access()<old.access()?res:old);
		}
		return old;
	}
	
	private void addItem(Channel ch,String id,String str) {
		ChannelSearchItem item=new ChannelSearchItem(id,str,ch);
		last=item;
		searchList.add(item);
	}
	
	public void addSearch(Channel ch,String id,String str) {
		if(searchList.isEmpty()) {
			// need to handle special case of empty list
			addItem(ch,id,str);
			return;
		}
		ChannelSearchItem item=findItem(ch,id,str);
		if(item==null) { // we use null as indicating of hit
			return;
		}
		if(searchList.size()+1>MAX_SIZE) {
			// the oldest is returned from find item
			searchList.remove(item);
			all=true;
		}
		// time to create new item
		addItem(ch,id,str);
	}
	
	public boolean refreshChildren() {
		//children.clear();
		return true; // always re resolve
	}
	
	public void discoverChildren() {
		for(int i=0;i<searchList.size();i++) {
			addChild(searchList.get(i));
		}
	}
}
