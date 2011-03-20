package com.sharkhunter.channel;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;

public class ChannelUtil {
	
	// Misc constants
	
	public static final String defAgentString="Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE; rv:1.9.2.3) Gecko/20100409 Firefox/3.6.3";
	
	// ASX types
	public static final int ASXTYPE_NONE=0;
	public static final int ASXTYPE_AUTO=1;
	public static final int ASXTYPE_FORCE=2;
	
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
		connection.setRequestProperty("User-Agent",defAgentString);
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
		return fetchPage(connection,null,"",null);
	}
	
	public static String fetchPage(URLConnection connection,ChannelAuth auth,String cookie) {
		return fetchPage(connection,auth,cookie,null);
	}
	
	public static String fetchPage(URLConnection connection,ChannelAuth auth,String cookie,HashMap<String,String> hdr) {
		try {
//			URLConnection connection=url.openConnection();
			connection.setRequestProperty("User-Agent",defAgentString);
			if(auth!=null) {
				if(auth.method==ChannelLogin.STD)
					connection.setRequestProperty("Authorization", auth.authStr);
				else if(auth.method==ChannelLogin.COOKIE) 
					cookie=append(cookie,"; ",auth.authStr);
			}
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
		String[] ss=s.split("\\s*=\\s*",2);
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
  	  	if(empty(res))
  	  		return data;
  	  	if(empty(data))
  	  		return res;
  	  	if(empty(sep))
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
	
	public static String parseASX(String url, int type) {
		if(type==ChannelUtil.ASXTYPE_NONE)
			return url;
		if(type==ChannelUtil.ASXTYPE_AUTO&&!isASX(url))
			return url;
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
	
	public static String unescape(String str) {
		try {
			return URLDecoder.decode(str,"UTF-8");
		} catch (Exception e) {
			//Channels.debug("unesc err "+e);
		}
		return str;
	}
	
	public static String escape(String str) {
		try {
			return URLEncoder.encode(str,"UTF-8");
		} catch (Exception e) {
			//Channels.debug("esc err "+e);
		}
		return str;
	}
	
	public static boolean rtmpStream(String url) {
		return streamType(url,"rtmp");
	}
	
	public static boolean streamType(String url,String type) {
		try {
			URI u=new URI(url);
			return u.getScheme().startsWith(type);
		}
		catch (Exception e) {
			Channels.debug("execp stream type "+e);
			return false;
		}
	}
	
	public static String createMediaUrl(String url,int format) {
		//Channels.debug("create media url entry(str only) "+url);
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("url", url);
		return createMediaUrl(map,format);
	}
	
	public static String createMediaUrl(HashMap<String,String> vars,int format) {
		String rUrl=vars.get("url");
		int rtmpMet=Channels.rtmpMethod();
		Channels.debug("create media url entry "+rUrl+" format "+format);
		if(rUrl.startsWith("http")) {
			if(format!=Format.VIDEO||rtmpMet==Channels.RTMP_MAGIC_TOKEN)
				return rUrl;
			//rUrl="navix://channel?url="+escape(rUrl);
			rUrl="channel?url="+escape(rUrl);
			String agent=vars.get("agent");
			if(empty(agent))
				agent=ChannelUtil.defAgentString;
			rUrl=append(rUrl,"&agent=",escape(agent));
			String sub=vars.get("subtitle");
			if(!empty(sub)) { // we got subtitles
				rUrl="subs://"+rUrl;
				// lot of things to append here
				rUrl=append(rUrl,"&subs=",escape(sub));
				//-spuaa 3 -subcp ISO-8859-10 -subfont C:\Windows\Fonts\Arial.ttf -subfont-text-scale 2 -subfont-outline 1 -subfont-blur 1 -subpos 90 -quiet -quiet -sid 100 -fps 25 -ofps 25 -sub C:\downloads\Kings Speech.srt -lavdopts fast -mc 0 -noskip -af lavcresample=48000 -srate 48000 -o \\.\pipe\mencoder1299956406082
				PmsConfiguration configuration=PMS.getConfiguration();
				//String subtitleQuality = config.getMencoderVobsubSubtitleQuality();
				String subcp=configuration.getMencoderSubCp();
				rUrl=append(rUrl,"&subcp=",escape(subcp));
				rUrl=append(rUrl,"&subtext=",escape(configuration.getMencoderNoAssScale()));
				rUrl=append(rUrl,"&subout=",escape(configuration.getMencoderNoAssOutline()));
				rUrl=append(rUrl,"&subblur=",escape(configuration.getMencoderNoAssBlur()));
				int subpos = 1;
                try {
                        subpos = Integer.parseInt(configuration.getMencoderNoAssSubPos());
                } catch (NumberFormatException n) {
                }
                rUrl=append(rUrl,"&subpos=",String.valueOf(100 - subpos));
              //  rUrl=append(rUrl,"&subdelay=","20000");
			}
			else
				rUrl="navix://"+rUrl;
			Channels.debug("return media url "+rUrl);
			return rUrl;
		}
		
		if(!rtmpStream(rUrl)) // type is sopcast etc.
			return rUrl;
		
		switch(rtmpMet) {
			case Channels.RTMP_MAGIC_TOKEN:
				rUrl=ChannelUtil.append(rUrl, "!!!pms_ch_dash_y!!!", vars.get("playpath"));
				rUrl=ChannelUtil.append(rUrl, "!!!pms_ch_dash_w!!!", vars.get("swfVfy"));
				break;
			
			case Channels.RTMP_DUMP:
				Channels.debug("rtmpdump method");
				rUrl="rtmpdump://channel?url="+escape(rUrl);
				rUrl=ChannelUtil.append(rUrl, "&-y=", escape(vars.get("playpath")));
				rUrl=ChannelUtil.append(rUrl, "&-W=", escape(vars.get("swfVfy")));
				rUrl=ChannelUtil.append(rUrl, "&-s=", escape(vars.get("swfplayer")));
				rUrl=ChannelUtil.append(rUrl, "&-a=", escape(vars.get("app")));
				rUrl=ChannelUtil.append(rUrl, "&-p=", escape(vars.get("pageurl")));
				break;
				
			default:
				rUrl=vars.get("url");
				break;
		}
		Channels.debug("return media url "+rUrl);
		return rUrl;
	}
	
	public static String backTrack(DLNAResource start,int stop) {
		if(start==null)
			return null;
		if(Channels.save()) // compensate for save
			start=start.getParent();
		if(stop==0)
			return start.getName();
		int i=0;
		DLNAResource curr=start;
		while(i<stop) {
			curr=curr.getParent();
			i++;
			if(curr instanceof Channel) {
				curr=null;
				break;
			}
		}
		if(curr!=null)
			return curr.getName();
		return null;
	}
	
	public static String backTrack(DLNAResource start,int[] stops) {
		String res="";
		for(int i=0;i<stops.length;i++) {
			res=append(res," ",backTrack(start,stops[i]));
		}
		return res;
	}
	
	public static int[] getNameIndex(String[] prop) {
		try {
			String x=ChannelUtil.getPropertyValue(prop, "name_index");
			if(!empty(x)) {
				String[] idx=x.split("\\+");
				int[] res=new int[idx.length];
				for(int i=0;i<idx.length;i++) {
					int j= new Integer(idx[i]).intValue();
					if(j>0)
						res[i]=j;
				}
				return res;
			}
		}
		catch (Exception e) {
			Channels.debug("excep "+e);
		}
		return null;
	}
	
	public static String mangle(String re,String str) {
		if(empty(re))
			return str;
		Matcher m=Pattern.compile(re).matcher(str);
		String res="";
		if(!m.find())
			return str;
		for(int i=1;i<=m.groupCount();i++)
			res=res+m.group(i);
		return res;
	}
	
	public static String format2str(int format) {
		switch(format) {
		case Format.AUDIO:
			return "Audio";
		case Format.VIDEO:
			return "Video";
		case Format.IMAGE:
			return "Image";
		default:
			return "Unknown";
		}
	}
	
	public static String execute(ProcessBuilder pb) {
		try {
			Process pid=pb.start();
			InputStream is = pid.getInputStream();
	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(isr);
	        String line;
	        StringBuilder sb=new StringBuilder();
	        while ((line = br.readLine()) != null) 
	        	sb.append(line);
	        pid.waitFor();
	        return sb.toString();
		}
		catch (Exception e) {
			Channels.debug("executing external script failed "+e);
		}
		return null;
	}

	public static void cacheFile(File f,String type) {
		File cache=new File(Channels.cacheFile());
		long ttd=System.currentTimeMillis()+(7*24*60*60); // now + 1week
		String data="\n\r"+f.getAbsolutePath()+","+String.valueOf(ttd)+","+type+"\n\r";
		try {
			FileOutputStream out=new FileOutputStream(cache,true);
			out.write(data.getBytes(), 0, data.length());
			out.flush();
			out.close();
		}
		catch (Exception e) {
		}
	}
	
}
