package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import net.pms.PMS;

public class ChannelUtil {
	
	public static String postPage(URLConnection connection,String query) {
		return postPage(connection,query,null,null);
	}
	
	
	public static String postPage(URLConnection connection,String query,String cookie,
								  HashMap<String,String> hdr) {    
		connection.setDoOutput(true);   
		connection.setDoInput(true);   
		connection.setUseCaches(false);   
		connection.setDefaultUseCaches(false);   
		//connection.setAllowUserInteraction(true);   

		connection.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE; rv:1.9.2.3) Gecko/20100409 Firefox/3.6.3");
		connection.setRequestProperty("Content-Length", "" + query.length());  
		
		try {
			if(!empty(cookie))
				connection.setRequestProperty("Cookie",cookie);
			
			if(hdr!=null&&hdr.size()!=0) {
				for(String key : hdr.keySet()) 
					connection.setRequestProperty(key,hdr.get(key));
			}
			
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
	
	public static String fetchPage(URLConnection connection) {
		return fetchPage(connection,"","",null);
	}
	
	public static String fetchPage(URLConnection connection,String auth,String cookie) {
		return fetchPage(connection,auth,cookie,null);
	}
	
	public static String fetchPage(URLConnection connection,String auth,String cookie,HashMap<String,String> hdr) {
		try {
//			URLConnection connection=url.openConnection();
			connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE; rv:1.9.2.3) Gecko/20100409 Firefox/3.6.3");
			if(!empty(auth))
				connection.setRequestProperty("Authorization", auth);
			if(!empty(cookie))
				connection.setRequestProperty("Cookie",cookie);
			if(hdr!=null&&hdr.size()!=0) {
				for(String key : hdr.keySet()) 
					connection.setRequestProperty(key,hdr.get(key));
			}
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
	
	public static boolean empty(String s) {
		return (s==null)||(s.length()==0);
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
		if(!empty(thumb))  // if the thumb we found is good use it
			return thumb;
		if(!empty(pThumb)) // otherwise use parents thumb
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
		if(empty(a))
			return b;
		if(empty(b))
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
		if(empty(line))
			return true;
		return (line.charAt(0)=='#');
	}
	
	public static String parseASX(String url) {
		String page;
		try {
			page = ChannelUtil.fetchPage(new URL(url).openConnection());
		} catch (Exception e) {
			Channels.debug("asx fetch failed "+e);
			return url;
		}
		Channels.debug("page "+page);
		int first=page.indexOf("href=");
		if(first==-1)
			return url;
		int last=page.indexOf('\"', first+6);
		if(last==-1)
			return url;
		return page.substring(first+6,last);
	}
	
	public static boolean isASX(String str) {
		return (str!=null&&str.endsWith(".asx"));
	}
	
	public static int calcCont(String[] props) {
		String lim=ChannelUtil.getPropertyValue(props, "continue_limit");
		int ret=Channels.DeafultContLim;
		if(!ChannelUtil.empty(lim)) {
			try {
				ret=Integer.parseInt(lim);
			}
			catch (Exception e) { 
			}
		}
		if(ret<0)
			ret=Channels.DeafultContLim;
		return ret;
	}
	
	public static String extension(String fileName) {
		int pos=fileName.lastIndexOf('.');
		if(pos>0&&pos<fileName.length()-1)
			return fileName.substring(pos);
		return null;
	}
	
	public static String guessExt(String fileName,String url) {
		if(!empty(extension(fileName)))
			return fileName;
		if(!empty(url)) {
			String ext=extension(url);
			if(!empty(url))
				return fileName+ext;
		}
		// No extension, give up
		return fileName;
	}
	
}
