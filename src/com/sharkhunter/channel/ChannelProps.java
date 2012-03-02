package com.sharkhunter.channel;

public interface ChannelProps {
	public String separator(String base);
	public boolean onlyFirst();
	public String append(String base);
	public String prepend(String base);
	public boolean unescape(String base);
	public boolean escape(String base);
}
