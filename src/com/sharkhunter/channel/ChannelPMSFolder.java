package com.sharkhunter.channel;

import java.io.InputStream;

import org.apache.commons.lang.StringEscapeUtils;

import net.pms.dlna.virtual.VirtualFolder;

public class ChannelPMSFolder extends VirtualFolder implements ChannelFilter{
	
		private ChannelFolder cf;
		private String filter;
		private String url;
		
		public ChannelPMSFolder(ChannelFolder cf,char ch) {
			this(cf,String.valueOf(ch),String.valueOf(ch),"",cf.getThumb());
		}
		
		public ChannelPMSFolder(ChannelFolder cf,String name) {
			this(cf,name,null,"",cf.getThumb());
		}
		
		public ChannelPMSFolder(ChannelFolder cf,char ch,String url) {
			this(cf,String.valueOf(ch),String.valueOf(ch),url,cf.getThumb());
		}
		
		public ChannelPMSFolder(ChannelFolder cf,String name,String filter,String url,String thumb) {
			super(name==null?"":StringEscapeUtils.unescapeHtml(name),thumb);
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
			return true;
		}
		
		public boolean filter(String str) {
			if(filter==null||filter.length()==0)
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
		
		public ChannelFolder getFolder() {
			return cf;
		}
		
		public InputStream getThumbnailInputStream() {
			try {
				thumbnailIcon=ChannelNaviXProc.simple(thumbnailIcon, cf.thumbScript());
				return downloadAndSend(thumbnailIcon,true);
			}
			catch (Exception e) {
				return super.getThumbnailInputStream();
			}
		}
}
