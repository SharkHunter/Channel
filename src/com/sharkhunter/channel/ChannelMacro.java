package com.sharkhunter.channel;

import java.util.ArrayList;

public class ChannelMacro {
	private String name;
	private ArrayList<String> block;
	
	public ChannelMacro(String name,ArrayList<String> data) {
		this.name=name;
		block=data;
	}
	
	public ArrayList<String> getMacro() {
		return block;
	}
	
	public String getName() {
		return name;
	}
}
