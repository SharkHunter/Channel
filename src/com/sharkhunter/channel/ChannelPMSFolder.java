package com.sharkhunter.channel;

import java.io.InputStream;
import java.net.MalformedURLException;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;

public class ChannelPMSFolder extends VirtualFolder implements ChannelFilter{
	
		private ChannelFolder cf;
		private String filter;
		private String url;
		
		public ChannelPMSFolder(ChannelFolder cf,char ch) {
			this(cf,String.valueOf(ch),String.valueOf(ch),"",cf.getThumb());
		}
		
		public ChannelPMSFolder(ChannelFolder cf,String name) {
			this(cf,name,"","",cf.getThumb());
		}
		
		public ChannelPMSFolder(ChannelFolder cf,char ch,String url) {
			this(cf,String.valueOf(ch),String.valueOf(ch),url,cf.getThumb());
		}
		
		public ChannelPMSFolder(ChannelFolder cf,String name,String filter,String url,String thumb) {
			super(name==null?"":name,thumb);
			this.cf=cf;
			this.filter=filter;
			this.url=url;
		}
		
		public void discoverChildren() {
			try {
				cf.match(this,this,url,thumbnailIcon,name);
			} catch (Exception e) {
			}
		}
		
		public boolean refreshChildren() { // Always update
			/*for(DLNAResource f:children) 
				children.remove(f);
			discoverChildren();*/
			return true;
		}
		
		public boolean filter(String str) {
			if(filter==null)
				return true;
			if(filter.equalsIgnoreCase("#")) {
				char first=str.charAt(0);
				if ((first >= 'A' && first <= 'Z') ||
					(first >= 'a' && first <= 'z') )
					return false;
				return true;
			}
			return str.startsWith(filter);
		}
		
		public String getThumb() {
			return (thumbnailIcon);
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
