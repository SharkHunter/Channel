package com.sharkhunter.channel;

import java.io.InputStream;
import java.util.ArrayList;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;

public class ChannelPMSCode extends VirtualFolder {
	
	private ArrayList<DLNAResource> res;
	private String code;
	private long changed;
	private String thumb;
	private boolean codeOnly;
	
	public ChannelPMSCode(String name,String thumb) {
		this(name,thumb,false);
	}
	
	public ChannelPMSCode(String name,String thumb,boolean codeOnly) {
		super(name,thumb);
		this.thumb=thumb;
		res=new ArrayList<DLNAResource>();
		code="";
		changed=0;
		this.codeOnly=codeOnly;
	}
	
	private boolean preventAutoPlay() {
		// Normally changed is 0 and 0+15000 is never larger
		// then now.
		return (changed+15000)>System.currentTimeMillis();
	}
	
	// Major trick here.
	// We override the addChild, which makes the ChannelMedia
	// (which calls this in it's "owner") ends up here and just 
	// adds it to our internal list.
	// We use the explicit super.addChild to add the code VVA objs
	public void addChild(DLNAResource r) {
		res.add(r);
	}
	
	public void setCode(String str) {
		if(str!=null)
			code=str;
	}
	
	public void discoverChildren(String str) {
		String goodCode=Channels.getCode();
		if(goodCode==null||!goodCode.equals(str)) {
			discoverChildren();
			return;
		}
		if(codeOnly) {
			Channels.unlockAll();
			return;
		}
		for(DLNAResource newRes : res) 
			super.addChild(newRes);
	}

	public void discoverChildren() {
		super.addChild(new ChannelCommitCode(this,thumb));
		for(int i=0;i<10;i++) {
			final int j=i;
			super.addChild(new VirtualVideoAction(String.valueOf(i),true) {
				public boolean enable() {
					if(preventAutoPlay()) {
						changed=System.currentTimeMillis();
						return false;
					}
					code+=String.valueOf(j);
					changed=System.currentTimeMillis();
					return true;
				}
			});
		}
		super.addChild(new VirtualVideoAction("Clear",true) {
				public boolean enable() {
					if(preventAutoPlay()) {
						changed=System.currentTimeMillis();
						return false;
					}
					code="";
					changed=System.currentTimeMillis();
					return true;
				}
			});
	}
	
	public void add(DLNAResource r) {
		if(code.equals(Channels.getCode())) {
			if(codeOnly) {
				Channels.unlockAll();
				return;
			}
			for(DLNAResource newRes : res) 
				r.addChild(newRes);
		}
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}
}
