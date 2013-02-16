package com.sharkhunter.channel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pms.PMS;

import org.apache.commons.io.FileUtils;

public class ChannelSMIConv {
	
	private final static String ARROW =" --> ";
	private final static String SUB_REG="<Subtitle .*?TimeIn=\"([^\"]+)\" TimeOut=\"([^\"]+)\"[^>]+>(.*?)</Subtitle>";
	private final static String TEXT_REG="<Text [^>]+>(.*?)</Text>";
	private final static int FLAGS=Pattern.MULTILINE|Pattern.DOTALL;
	
	private static String fixTime(String str) {
		int pos=str.lastIndexOf(':');
		if(pos==-1)
			return str;
		StringBuilder sb=new StringBuilder(str);
		sb.setCharAt(pos, ',');
		return sb.toString();
	}
	
	public static void toSRT(File src,File dst) throws IOException {
		int index=1;
		String fe=PMS.getConfiguration().getMencoderSubCp();
		String data=FileUtils.readFileToString(src,fe);
		Pattern sub=Pattern.compile(SUB_REG,FLAGS);
		Pattern text=Pattern.compile(TEXT_REG);
		Matcher m=sub.matcher(data);
		OutputStreamWriter out=new OutputStreamWriter(new FileOutputStream(dst),fe);
		while(m.find()) {
			StringBuffer sb=new StringBuffer();
			String start=(m.group(1));
			String stop=(m.group(2));
			Matcher m1=text.matcher(m.group(3));
			while(m1.find()) {
				sb.append(m1.group(1));
				sb.append("\n");
			}
			ChannelSubUtil.writeSRT(out, index++, start, stop, sb.toString());
		}
		out.flush();
		out.close();
	}	
}
