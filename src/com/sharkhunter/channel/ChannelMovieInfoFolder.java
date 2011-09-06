package com.sharkhunter.channel;

import java.io.InputStream;
import net.pms.dlna.virtual.VirtualFolder;

public class ChannelMovieInfoFolder extends VirtualFolder {
	
	private String imdb;
	
	public ChannelMovieInfoFolder(String imdbId,String thumb) {
		super("#--MOVIE INFO--#",thumb);
		imdb=(imdbId.startsWith("tt")?imdbId:"tt"+imdbId);
	}
	
	public void discoverChildren() {
		Channels.movieInfo().addFolders(this, imdb, thumbnailIcon);
	}
	
	public InputStream getThumbnailInputStream() {
		try {
			//thumbnailIcon=ChannelNaviXProc.simple(thumbnailIcon, cf.thumbScript());
			return downloadAndSend(thumbnailIcon,true);
		}
		catch (Exception e) {
			return super.getThumbnailInputStream();
		}
	}

}
