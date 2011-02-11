package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import net.pms.PMS;

public class ChannelUtil {

	public static String postPage(URLConnection connection,String query) {    
		connection.setDoOutput(true);   
		connection.setDoInput(true);   
		connection.setUseCaches(false);   
		connection.setDefaultUseCaches(false);   
		//connection.setAllowUserInteraction(true);   

		connection.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE; rv:1.9.2.3) Gecko/20100409 Firefox/3.6.3");
		connection.setRequestProperty("Content-Length", "" + query.length());  
		
		try {
			connection.connect();
			// open up the output stream of the connection   
			DataOutputStream output = new DataOutputStream(connection.getOutputStream());   
			output.writeBytes(query);   
			output.flush ();   
			output.close();   

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder page=new StringBuilder();
			String str;
			while ((str = in.readLine()) != null) {
				//PMS.debug("reding login reply "+str);
				//	page.append("\n");
				page.append(str.trim());
				page.append("\n");
			}
			in.close();
			return page.toString();
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public static String fetchPage(URL url) {
		return fetchPage(url,"");
	}
	
	public static String fetchPage(URL url,String auth) {
		try {
			URLConnection connection=url.openConnection();
			connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE; rv:1.9.2.3) Gecko/20100409 Firefox/3.6.3");
			if(auth!=null&&auth.length()>0)
				connection.setRequestProperty("Authorization", auth);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
		    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    StringBuilder page=new StringBuilder();
		    String str;
		    while ((str = in.readLine()) != null) { 
		    //	page.append("\n");
		    	page.append(str.trim());
		    	page.append("\n");
		    }
		    in.close();
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
		String[] ss=s.split("=",2);
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
  	  	if(res==null||res.length()==0)
  	  		return data;
  	  	if(data==null||data.length()==0)
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
	
	public static String pendData(String src,String[] props,String field,String sep) {
		String p=getPropertyValue(props,"prepend_"+field);
		String a=getPropertyValue(props,"append_"+field);
		return append(p,sep,append(src,sep,a));
	}
	
	public static String pendData(String src,String[] props,String field) {
		return pendData(src,props,field,null);
	}
	
	public static String concatField(String conf,String matched,String[] props,String field) {
		String cProp=getPropertyValue(props,"concat_"+field);
		if(cProp==null)
			return matched;
		String sep=getPropertyValue(props,field+"_separator");
		if(cProp.equalsIgnoreCase("front")) {
			return append(conf,sep,matched);
		}
		if(cProp.equalsIgnoreCase("rear"))
			return append(matched,sep,conf);
		return matched;
	}
	
	public static String concatURL(String a,String b) {
		if(a==null||a.length()==0)
			return b;
		if(b==null||b.length()==0)
			return a;
		boolean aLast=a.charAt(a.length()-1)=='/';
		boolean bFirst=b.charAt(0)=='/';
		if(aLast&&bFirst)
			return a+b.substring(1);
		/*if(!(aLast&&bFirst))
			return a+"/"+b;*/
		return a+b;
	}
	
	public static boolean ignoreLine(String line) {
		if(line==null||line.length()==0)
			return true;
		return (line.charAt(0)=='#');
	}
	
}
