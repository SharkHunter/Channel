package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import net.pms.PMS;

public class ChannelUtil {

	public static String fetchPage(URL url) {
		try {
		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		    StringBuilder page=new StringBuilder();
		    String str;
		    while ((str = in.readLine()) != null) { 
		    	page.append("\n");
		    	page.append(str.trim());
		    	page.append("\n");
		    }
		    in.close();
		//	debug("page "+page.toString());
		    return page.toString();
		}
		catch (Exception e) {
			PMS.debug("fetch exception "+e.toString());
		    return "";
		}
	}
	
	private static String findProperty(String[] props,String prop) {
		if(props==null)
			return null;
		for(int i=0;i<props.length;i++) {
			if(props[i].startsWith(prop))
				return props[i];
		}
		return null;
	}
	
	public static String getPropertyValue(String[] props,String prop) {
		String s=findProperty(props,prop);
		if(s==null)
			return null;
		String[] ss=s.split("=");
		if(ss.length<2)
			return null;
		return ss[1];
	}
	
	public static boolean getProperty(String[] props,String prop) {
		return (findProperty(props,prop)!=null);
	}
	
	public static ArrayList<String> gatherBlock(String[] lines,int start) {
		ArrayList<String> res=new ArrayList<String>();
    	int curls=1;
    	for(int i=start;i<lines.length;i++) {
    		String str=lines[i].trim();
    		if(str.startsWith("}")) {
    			res.add("}");
    			curls--;
    			if(curls==0)
    				break;
    			continue;
    		}
    		if(str.contains("{"))
    			curls++;
    		res.add(str+"\n");
    	}
    	return res; 
	}
	
	public static ArrayList<String> gatherBlock(ArrayList<String> data,int start) {
		ArrayList<String> res=new ArrayList<String>();
    	int curls=1;
    	for(int i=start;i<data.size();i++) {
    		String str=data.get(i).trim();
    		if(str.startsWith("}")) {
    			res.add("}");
    			curls--;
    			if(curls==0)
    				break;
    			continue;
    		}
    		if(str.contains("{"))
    			curls++;
    		res.add(str+"\n");
    	}
    	return res; 
	}
	
	public static String append(String res,String sep,String data) {
  	  	if(res.length()==0)
  	  		return data;
  	  	if(data.length()==0)
  	  		return res;
  	  	if(sep==null)
  	  		return res+data;
  	  	return res+sep+data;
    }
	
	public static ChannelMacro findMacro(ArrayList<ChannelMacro> macros,String macro) {
		if(macros==null)
			return null;
		for(int i=0;i<macros.size();i++) { 	
			ChannelMacro m=macros.get(i);
			if(m!=null&&macro.equals(m.getName()))
				return m;
		}
		return null;
	}
	
	public static String getThumb(String thumb,String pThumb,Channel ch) {
		if(thumb!=null&&thumb.length()!=0)  // if the thumb we found is goo use it
			return thumb;
		if(pThumb!=null&&pThumb.length()!=0) // otherwise use parents thumb
			return pThumb;
		if(ch!=null) // last resort
			return ch.getThumb();
		return null;
	}
	
}
