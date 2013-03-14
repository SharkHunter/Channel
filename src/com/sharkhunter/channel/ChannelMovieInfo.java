package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.movieinfo.FileMovieInfoVirtualFolder;

public class ChannelMovieInfo {

	private String plugins;
	private int numberOfActors;
	private int lineLength;
	
	public ChannelMovieInfo() {
		numberOfActors=0;
		lineLength=0;
		parse();
	}
	
	public void addFolders(DLNAResource res,String imdbId,String thumb) {
		if (plugins != null) {
			String[] plgn = plugins.split(",");
			for(int i=0;i < plgn.length;i++)
				if (!plgn[i].equals(","))
					try {
						FileMovieInfoVirtualFolder fi = new FileMovieInfoVirtualFolder(plgn[i] +" INFO", thumb,numberOfActors,lineLength,imdbId);
						res.addChild(fi);
					}
			catch (Exception e) {
			}
		}
	}
	
	private void parse() 
	{
		File miConf = new File("MOVIEINFO.conf"); 
		if (!miConf.exists())
			miConf = new File("plugins/MOVIEINFO.conf"); 
		if (miConf.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(miConf), "UTF-8")); 
				String line = null;
				while ((line=br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0 && !line.startsWith("#") && line.indexOf("=") > -1) { 
						if(line.startsWith("Plugins="))plugins = line.substring(line.indexOf("=")+1,line.length()).toUpperCase();
						if(line.startsWith("NumberOfActors="))numberOfActors = Integer.parseInt(line.substring(line.indexOf("=")+1,line.length()));
						if(line.startsWith("Linelength="))lineLength = Integer.parseInt(line.substring(line.indexOf("=")+1,line.length()));
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			PMS.minimal("MOVIEINFO.conf file not found!");
	}
}
