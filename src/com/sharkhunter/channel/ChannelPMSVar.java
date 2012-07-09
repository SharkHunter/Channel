package com.sharkhunter.channel;

import net.pms.dlna.virtual.VirtualFolder;
import net.pms.dlna.virtual.VirtualVideoAction;

public class ChannelPMSVar extends VirtualFolder {
	
	private ChannelVar var;
	private long changed;
	
	private static final long AUTO_PLAY_FACTOR=(1000*5);

	public ChannelPMSVar(String name,ChannelVar v) {
		super("Set "+name+" variable",null);
		var=v;
		changed=0;
	}
	
	private boolean preventAutoPlay() {
		// Normally changed is 0 and 0+5000 is never larger
		// then now.
		return (changed+AUTO_PLAY_FACTOR)>System.currentTimeMillis();
	}
	
	private void addAction(final String value,boolean curr) {
		String disp=var.niceValue(value);
		addChild(new VirtualVideoAction(disp,curr) {
			public boolean enable() {
				if(preventAutoPlay())
					return false;
				var.setValue(value);
				changed=System.currentTimeMillis();
				return true;
			}
		});
	}
	
	public void discoverChildren() {
		String[] vals=var.values();
		for(int i=0;i<vals.length;i++) 
			addAction(vals[i],vals[i].equals(var.value()));
	}
	
	public boolean isTranscodeFolderAvailable() {
		return false;
	}

}
