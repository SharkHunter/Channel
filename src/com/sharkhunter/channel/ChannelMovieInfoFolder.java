package com.sharkhunter.channel;

import java.io.InputStream;

import net.pms.movieinfo.MovieInfoVirtualFolder;



public class ChannelMovieInfoFolder extends MovieInfoVirtualFolder{
	
	private String imdb;
	
	public ChannelMovieInfoFolder(String imdbId,String thumb) {
		super(thumb);
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
