package com.sharkhunter.channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualVideoAction;
import net.pms.formats.Format;
import net.pms.io.OutputParams;

public class ChannelUtil {
	
	// Misc constants
	
	public static final String defAgentString="Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE; rv:1.9.2.3) Gecko/20100409 Firefox/3.7.3";
	
	// ASX types
	public static final int ASXTYPE_NONE=0;
	public static final int ASXTYPE_AUTO=1;
	public static final int ASXTYPE_FORCE=2;

	
	public static String postPage(URLConnection connection,String query) {
		return postPage(connection,query,"",null);
	}
	
	public static String postPage(URLConnection connection,String query,String cookie,
								  HashMap<String,String> hdr) {   
		URL url=connection.getURL();
		connection.setDoOutput(true);   
		connection.setDoInput(true);   
		connection.setUseCaches(false);   
		connection.setDefaultUseCaches(false);   
		//connection.setAllowUserInteraction(true);   

		connection.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("User-Agent",defAgentString);
		if(query==null)
			query="";
		connection.setRequestProperty("Content-Length", "" + query.length());  
		
		try {
			String c1=ChannelCookie.getCookie(url.toString());
			if(!empty(c1)) {
				if(!cookieContains(c1,cookie))
					cookie=append(cookie,"; ",c1);
			}
			if(!empty(cookie))
				connection.setRequestProperty("Cookie",cookie);
			if(hdr!=null&&hdr.size()!=0) {
				for(String key : hdr.keySet()) 
					connection.setRequestProperty(key,hdr.get(key));
			}
			connection.setConnectTimeout(10000);

			connection.connect();
			// open up the output stream of the connection
			if(!empty(query)) {
				DataOutputStream output = new DataOutputStream(connection.getOutputStream());
				output.writeBytes(query);   
				output.flush ();   
				output.close();
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder page=new StringBuilder();
			String str;
			while ((str = in.readLine()) != null) {
				//	page.append("\n");
				page.append(str.trim());
				page.append("\n");
			}
			in.close();
			Channels.restoreProxyDNS();
			return page.toString();
		}
		catch (Exception e) {
			Channels.debug("post error "+e);
			Channels.restoreProxyDNS();
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
			URL url=connection.getURL();
			connection.setConnectTimeout(10000);
			connection.setRequestProperty("User-Agent",defAgentString);
			if(auth!=null) {
				Channels.debug("auth "+auth.method+" authstr "+auth.authStr);
				if(auth.method==ChannelLogin.STD)
					connection.setRequestProperty("Authorization", auth.authStr);
				else if(auth.method==ChannelLogin.COOKIE) 
					cookie=append(cookie,"; ",auth.authStr);
				else if(auth.method==ChannelLogin.APIKEY) {
					url=new URL(url.toString()+auth.authStr);
					connection=url.openConnection();
				}
			}
			Channels.debug("fpage cookie "+cookie);
			String c1=ChannelCookie.getCookie(url.toString());
			if(!empty(c1)) {
				if(!cookieContains(c1,cookie)) {
					cookie=append(cookie,"; ",c1);
				}
			}
			Channels.debug("fpage2 cookie "+cookie+" "+url);
			if(!empty(cookie))
				connection.setRequestProperty("Cookie",cookie);
			if(hdr!=null&&hdr.size()!=0) {
				for(String key : hdr.keySet()) 
					connection.setRequestProperty(key,hdr.get(key));
			}
		//	connection.setRequestProperty("Content-Length", "0"); 
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
			Channels.restoreProxyDNS();
			return page.toString();
		}
		catch (Exception e) {
			Channels.debug("fetch exception "+e.toString());
			Channels.restoreProxyDNS();
			return "";
		}
	}
	
	public static boolean cookieContains(String cookie1,String cookie0) {
		if(empty(cookie0))
			return false;
		String[] c1=cookie1.split("; ");
		String[] c0=cookie0.split("; ");
		for(int i=0;i<c1.length;i++) {
			String[] cookie=c1[i].split("=");
			for(int j=0;j<c0.length;j++) {
				String[] c2=c0[j].split("=");
				if(c2[0].equals(cookie[0]))
					return true;
			}
		}
		return false;
	}
	
	public static boolean downloadText(InputStream in,File f) throws Exception {
		String subcp=getCodePage();
		OutputStreamWriter out=new OutputStreamWriter(new FileOutputStream(f),subcp);
		InputStreamReader inn=new InputStreamReader(in);
		char[] buf=new char[4096];
		int len;
		Thread me=Thread.currentThread();
		while((len=inn.read(buf))!=-1) {
			out.write(buf, 0, len);
			if(me.isInterrupted())
				break;
		}
		out.flush();
		out.close();
		in.close();
		return true;
	}
	
	public static boolean downloadBin(String url,File f) {
		return downloadBin(url,f,false);
	}
	
	public static boolean downloadBin(String url,File f,boolean text) {
		try {
			URL u=new URL(url);
			URLConnection connection=u.openConnection();
			connection.setRequestProperty("User-Agent",ChannelUtil.defAgentString);
			String cookie=ChannelCookie.getCookie(url);
			if(!empty(cookie))
				connection.setRequestProperty("Cookie",cookie);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			InputStream in=connection.getInputStream();
			if(text)
				return downloadText(in,f);
			FileOutputStream out=new FileOutputStream(f);
			byte[] buf = new byte[4096];
			int len;
			Thread me=Thread.currentThread();
			while((len=in.read(buf))!=-1)  {
				out.write(buf, 0, len);
				if(me.isInterrupted())
					break;
			}
			out.flush();
			out.close();
			in.close();
			return true;
		}
		catch (Exception e) {
			Channels.debug("Error fetching bin file "+e);
		}
		return false;
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
		res=ChannelUtil.separatorToken(res);
		data=ChannelUtil.separatorToken(data);
		sep=ChannelUtil.separatorToken(sep);
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
		Channels.debug("prepend "+p+" append "+a+" src "+src+" f "+field);
		return append(p,sep,append(src,sep,a));
	}
	
	public static String pendData(String src,String[] props,String field) {
		return pendData(src,props,field,null);
	}
	
	public static String concatField(String conf,String matched,String[] props,String field) {
		String cProp=getPropertyValue(props,"concat_"+field);
		if(cProp==null)
			return matched;
		String sep=separatorToken(getPropertyValue(props,field+"_separator"));
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
		return extension(fileName,false);
	}
	
	public static String extension(String fileName,boolean stripDot) {
		if(ChannelUtil.empty(fileName))
			return null;
		int pos=fileName.lastIndexOf('.');
		if((pos>0)&&(pos<fileName.length()-1))
			return fileName.substring(pos+(stripDot?1:0));
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
	
	private static String addSubs(String rUrl,String sub,Channel ch,
								  RendererConfiguration render) {
		boolean braviaFix = false;
		if(render != null)
			braviaFix = render.isBRAVIA() && Channels.cfg().subBravia();
		sub=ch.convSub(sub,braviaFix);
		Channels.debug("sub fiel "+sub);
		rUrl=append(rUrl,"&subs=",escape(sub));
		//-spuaa 3 -subcp ISO-8859-10 -subfont C:\Windows\Fonts\Arial.ttf -subfont-text-scale 2 -subfont-outline 1 -subfont-blur 1 -subpos 90 -quiet -quiet -sid 100 -fps 25 -ofps 25 -sub C:\downloads\Kings Speech.srt -lavdopts fast -mc 0 -noskip -af lavcresample=48000 -srate 48000 -o \\.\pipe\mencoder1299956406082
		PmsConfiguration configuration=PMS.getConfiguration();
		//String subtitleQuality = config.getMencoderVobsubSubtitleQuality();
		String subcp=getCodePage();
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
		rUrl=append(rUrl,"&subtype=",ch.embSubExt());
	  //  rUrl=append(rUrl,"&subdelay=","20000");
		return rUrl;
	}
	
	public static String badResource() {
		return "resource://"+ChannelResource.redXUrl();
	}
	
	public static String createMediaUrl(String url,int format,Channel ch,
			RendererConfiguration render) {
		//Channels.debug("create media url entry(str only) "+url);
		HashMap<String,String> map=new HashMap<String,String>();
		map.put("url", url);
		return createMediaUrl(map,format,ch,render);
	}
	
	public static String createMediaUrl(HashMap<String,String> vars,int format,
										Channel ch,RendererConfiguration render) {
		if(!empty(vars.get("bad"))) {
			// bad link, return error url
			return badResource();
		}
		String rUrl=vars.get("url");
		if(empty(rUrl)||Channels.noPlay()) // what do we do?
			return null;
		rUrl=rUrl.replace("HTTP://", "http://"); // ffmpeg has problems with ucase HTTP
		int rtmpMet=Channels.rtmpMethod();
		String type=vars.get("__type__");			
		Channels.debug("create media url entry "+rUrl+" format "+format+" type "+type);
		if(rUrl.startsWith("http")) {
			if((format!=Format.VIDEO)||
			   (rtmpMet==Channels.RTMP_MAGIC_TOKEN))    
				return rUrl;
			//rUrl="navix://channel?url="+escape(rUrl);
			rUrl="channel?url="+escape(rUrl);
			String agent=vars.get("agent");
			if(empty(agent))
				agent=ChannelUtil.defAgentString;
			rUrl=append(rUrl,"&agent=",escape(agent));	
			if(!empty(vars.get("referer")))
				rUrl=append(rUrl,"&referer=",escape(vars.get("referer")));
			String sub=vars.get("subtitle");
			if(!empty(sub)) { // we got subtitles
				rUrl="subs://"+rUrl;
				// lot of things to append here
				rUrl=addSubs(rUrl,sub,ch,render);
			}
			else
				if(!empty(type)&&type.equals("navix"))
					rUrl="navix://"+rUrl;
				else
					rUrl=vars.get("url");
			rUrl=rUrl.replace("HTTP://", "http://"); // ffmpeg has problems with ucase HTTP
			Channels.debug("return media url "+rUrl);
			return rUrl;
		}
		
		if(!empty(type)&&type.equals("RTMPDUMP")) {
			Channels.debug("rmtpdump spec "+rUrl);
			String[] args=rUrl.split("!!!");
			String res="";
			for(int i=0;i<args.length;i++) {
				if(empty(args[i]))
					continue;
				String[] kv=args[i].split("=",2);
				if(kv.length<2)
					continue;
				if(kv[0].equals("flv"))
					continue;
				res=append(res,"&",kv[0]+"="+escape(kv[1]));
			}
			rUrl="rtmpdump://channel?"+res;
			String sub=vars.get("subtitle");
			if(!empty(sub)) { // we got subtitles
				// lot of things to append here
				rUrl=addSubs(rUrl,sub,ch,render);
			}
			Channels.debug("return media url rtmpdump spec "+rUrl);
			return rUrl;
		}
		
		String[] s=rUrl.split(" ");
		if(s.length>1) {
			// This is likely rtmp from navix
			rUrl=s[0]; // first one is the url
			for(int i=1;i<s.length;i++) {
				String[] ss=s[i].split("=",2);
				if(ss.length<2)
					vars.put(ss[0].toLowerCase(), "");
				else
					vars.put(ss[0].toLowerCase(), ss[1]);
			}
		}
		
		if(!rtmpStream(rUrl)) // type is sopcast etc.
			return rUrl;
		if(rUrl.startsWith("rtmpdump://"))
			return rUrl;
		
		switch(rtmpMet) {
			case Channels.RTMP_MAGIC_TOKEN:
				rUrl=ChannelUtil.append(rUrl, "!!!pms_ch_dash_y!!!", vars.get("playpath"));
				rUrl=ChannelUtil.append(rUrl, "!!!pms_ch_dash_w!!!", vars.get("swfVfy"));
				break;
			
			case Channels.RTMP_DUMP:
				Channels.debug("rtmpdump method");
				rUrl="rtmpdump://channel?-r="+escape(rUrl);
				if(!empty(vars.get("live")))
					rUrl=append(rUrl,"","&-v");
				rUrl=ChannelUtil.append(rUrl, "&-y=", escape(vars.get("playpath")));
				rUrl=ChannelUtil.append(rUrl, "&--swfVfy=", escape(vars.get("swfVfy")));
				rUrl=ChannelUtil.append(rUrl, "&--swfVfy=", escape(vars.get("swfvfy")));
				rUrl=ChannelUtil.append(rUrl, "&-s=", escape(vars.get("swfplayer")));
				rUrl=ChannelUtil.append(rUrl, "&-a=", escape(vars.get("app")));
				rUrl=ChannelUtil.append(rUrl, "&-p=", escape(vars.get("pageurl")));
				rUrl=ChannelUtil.append(rUrl, "&-s=", escape(vars.get("swfurl")));
				String sub=vars.get("subtitle");
				if(!empty(sub)) { // we got subtitles
					// lot of things to append here
					rUrl=addSubs(rUrl,sub,ch,render);
				}
				break;
				
			default:
				rUrl=vars.get("url");
				break;
		}
		Channels.debug("return media url "+rUrl);
		return rUrl;
	}
	
	public static String rtmpOp(String op) {
		if(op.equals("-r"))
			return "";
		if(op.equals("-y"))
			return "playpath";
		if(op.equals("-s"))
			return "swfUrl";
		if(op.equals("-p"))
			return "pageUrl";
		if(op.equals("-v"))
			return "live";
		if(op.equals("--swfVfy"))
			return "swfVfy";
		return op;
			
	}
	
	private static boolean backTrackCompensate(DLNAResource start) {
		// if we are in a subslector skip that
		if(start instanceof ChannelPMSSubSelector) 
			return true;
		// Sub sites should be skipped to
		if(start instanceof ChannelPMSSubSiteSelector)
			return true;
		return false;
	}
	
	public static String backTrack(DLNAResource start,int stop) {
		if(start==null)
			return null;
		int i=0;
		DLNAResource curr=start;
		if(Channels.save())
			curr=start.getParent();
		while(backTrackCompensate(curr)) // inital compensation
			curr=curr.getParent();
		if(stop==0)
			return curr.getName();
		while(i<stop) {
			curr=curr.getParent();
			if(backTrackCompensate(curr))
				continue; // don't count these
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
	
	public static String backTrack(DLNAResource start,int[] stops,String sep) {
		if(empty(sep))
			sep=" ";
		String res="";
		if(stops==null)
			return backTrack(start,0);
		for(int i=0;i<stops.length;i++) {
			res=append(res,sep,backTrack(start,stops[i]));
		}
		return res;
	}
	
	
	public static String backTrack(DLNAResource start,int[] stops) {
		return backTrack(start,stops, " ");
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
	
	public static String execute(String script,String url,int format) {
		return execute(script,url,format2str(format));
	}
	
	public static String execute(String script,String url,String format) {
		ProcessBuilder pb=new ProcessBuilder(script,"\""+url+"\"",format);
		return execute(pb);
	}
	
	public static String execute(ProcessBuilder pb) {
		return execute(pb,false);
	}
	
	public static String execute(ProcessBuilder pb,boolean verbose) {
		try {
			Channels.debug("about to execute "+pb.command());
			pb.redirectErrorStream(true);
			Process pid=pb.start();
			InputStream is = pid.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			StringBuilder sb=new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
				if(verbose)
					Channels.debug("execute read line "+line);
			}
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
	
	public static Thread backgroundDownload(String name,String url,boolean cache) {
		String fName=ChannelUtil.guessExt(Channels.fileName(name, cache),
				url);
		if(ChannelUtil.rtmpStream(url)) {
			try {
				//		URL u=new URL(url); 
				// rtmp stream special fix
				if(empty(extension(fName))) // no extension. Rtmp is normally mp4
					fName=fName+".mp4";
				final ProcessBuilder pb=buildPid(fName,url);
				if(pb==null)
					return null;
				Channels.debug("start process ");
				Runnable r = new Runnable() {
					public void run() {
						ChannelUtil.execute(pb);
					}
				};
				return new Thread(r);
			}
			catch (Exception e) {
				return null;
			}
		}
		
		String subFile="";
		if(url.startsWith("http")||
		   url.startsWith("navix")||
		   url.startsWith("subs")) {
			if(url.startsWith("navix")||
			   url.startsWith("subs")) {
				int pos=url.indexOf('?');
				if(pos==-1)
					return null;
				String[] data=url.substring(pos+1).split("&");
				for(int i=0;i<data.length;i++) {
					String[] kv=data[i].split("=");
					if(kv[0].equals("url"))
						url=ChannelUtil.unescape(kv[1]);
					if(kv[0].equals("subs"))
						subFile=unescape(kv[1]);
				}
			}
			if(empty(url))
				return null;
			final String rUrl=url;
			final String sFile=subFile;
			if(empty(extension(fName))) // no ext guess mpg as ext
				fName=fName+".mpg";
			final String fName1=fName;
			Runnable r = new Runnable() {
				public void run() {
					File f=new File(fName1);
					if(!empty(sFile)) {
						File s=new File(sFile);
						byte[] buf=new byte[4096];
						try {
							FileInputStream in = new FileInputStream(s);
							FileOutputStream out = new FileOutputStream(
									new File(f.getParent()+File.separator+s.getName()));
							while ((in.read(buf)) != -1)
								out.write(buf);

							in.close();
							out.close();
						}
						catch (Exception e) {
							// ignore this
							Channels.debug("Error moving subtitle file "+sFile);
						}
					}					
					// download the actaul movie, subtitles are done
					downloadBin(rUrl,f);
				}
			};
			return new Thread(r);
		}
		return null;
	}
	
	private static ProcessBuilder buildPid(String fName,String url) {
			int rtmpMet=Channels.rtmpMethod();
			if(rtmpMet==Channels.RTMP_MAGIC_TOKEN) {
				url=url.replace("!!!pms_ch_dash_y!!!", " -y ");
				url=url.replace("!!!pms_ch_dash_w!!!", " -W ");
				return null;
			}
			ArrayList<String> args=new ArrayList<String>();
			args.add(Channels.cfg().getRtmpPath());
			int pos=url.indexOf('?');
			if(pos==-1)
				return null;
			String[] data=url.substring(pos+1).split("&");
			for(int i=0;i<data.length;i++) {
				String[] kv=data[i].split("=");
				args.add(kv[0]);
				if(kv.length>1)
					args.add("\""+ChannelUtil.unescape(kv[1])+"\"");
			}
			args.add("-o");
			args.add("\""+fName+"\"");
			return new ProcessBuilder(args);
	}
	
	public static int getFormat(String type) {
		return getFormat(type,-1);
	}
	
	public static int getFormat(String type,int def) {
		if(type.equalsIgnoreCase("image"))
			return Format.IMAGE;
		if(type.equalsIgnoreCase("video"))
			return Format.VIDEO;
		if(type.equalsIgnoreCase("audio"))
			return Format.AUDIO;
		return def;
	}
 
	public static Proxy proxy(ChannelAuth a) {
		if(a==null)
			return Proxy.NO_PROXY;
		if(a.proxy==null)
			return Proxy.NO_PROXY;
		return a.proxy.getProxy();
	}
	
	public static String separatorToken(String str) {
		if(str==null)
			return null;
		if(str.equals("###0")) // this is space :)
			return " ";
		if(str.equals("###n")) // this is newline
			return "\r\n";
		return str;
	}
	
	public static boolean cookieMethod(int method) {
		return (method==ChannelLogin.COOKIE)||(method==ChannelLogin.SIMPLE_COOKIE);
	}
	
	public static void list2file(StringBuilder sb,String[] list) {
		for(int i=0;i<list.length;i++) {
			sb.append(list[i]);
			sb.append(",");
		}
	}
	
	public final static String FAV_BAR="\n############################\n"; 
	
	public static void addToFavFile(String data,String name,String owner) {
		File f=Channels.workFavFile();
		try {
			String str;
			if(!f.exists()) {
				str=FAV_BAR;
				str=str+"## Auto generated favorite file,Edit with care\n\n\n";
			}
			else
				str = FileUtils.readFileToString(f);	
			Channels.debug("adding to fav file "+name);
			String n="## Name: "+name+",Owner: "+owner;
			int pos = str.indexOf(n);
			if(pos > -1) {
				Channels.debug("already in fav file");
				return;
			}
			str=str+FAV_BAR+n+"\r\n\n"+data;
			FileOutputStream out=new FileOutputStream(f,false);
			out.write(str.getBytes());
			out.flush();
			out.close();
		}
		catch (Exception e) {
		}
	}
	
	public static void RemoveFromFavFile(String name, String url) {
		File f=Channels.workFavFile();
		try {
			String str = FileUtils.readFileToString(f);
			Channels.debug("removing from fav file "+name);
			int pos = str.indexOf(url);
			if(pos > -1) {
				FileOutputStream out=new FileOutputStream(f,false);
				pos = str.lastIndexOf(FAV_BAR,pos); // head
				out.write(str.substring(0,pos).getBytes());
				pos = str.indexOf(FAV_BAR,pos+30);  // tail
				if(pos > -1)
					out.write(str.substring(pos).getBytes());
				out.flush();
				out.close();
			}
		}
		catch (Exception e) {
		}
	}
			
	public static String type2str(int type) {
		switch(type) {
		case ChannelFolder.TYPE_ATZ:
			return "atz";
		case ChannelFolder.TYPE_ATZ_LINK:
			return "atzlink";
		case ChannelFolder.TYPE_EMPTY:
			return "empty";
		case ChannelFolder.TYPE_LOGIN:
			return "login";
		case ChannelFolder.TYPE_NAVIX:
			return "navix";
		case ChannelFolder.TYPE_RECURSE:
			return "recurse";
		case ChannelFolder.TYPE_SEARCH:
			return "search";
		case ChannelFolder.TYPE_EXEC:
			return "exec";
		default:
			return "normal";
		}
	}
	
	public static String stripExt(String str,int cnt,String strip) {
		if(cnt==0)
			cnt=-1;
		while(cnt!=0) {
			int pos=str.lastIndexOf(strip);
			if(pos==-1) // no more dots return what we got so far
				return str;
			str=str.substring(0,pos);
			cnt--;
		}
		return str;
	} 
	
	public static void sleep(String time) {
		try {
			sleep(Long.parseLong(time));
		}
		catch (Exception e) { 
		}
	}
	
	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		}
		catch (Exception e){}
	}
	
	public static String ensureDot(String str) {
		if(str.charAt(0)!='.')
			return "."+str;
		return str;
	}
	
	public static List<String> addStreamVars(List<String> args,ChannelStreamVars streamVars,
				OutputParams params) {
		ArrayList<String> res=new ArrayList<String>();
		for(int i=0;i<args.size();i++) {
			String arg=args.get(i);
			if(arg==null)
				continue;
			if(arg.contains("ffmpeg")) {
				res.add(arg);
				streamVars.resolve("ffmpeg",res,params);
				continue;
			}
			if(arg.contains("mencoder")) {
				res.add(arg);
				streamVars.resolve("mencoder",res,params);
				continue;
			}
			res.add(arg);
		}
		return res;
	}
	
	public static String ensureImdbtt(String imdb) {
		/*if(empty(imdb))
			return imdb;
		return (imdb.startsWith("tt")?imdb:"tt"+imdb);*/
		return imdb;
	}
	
	public static String trimURL(String url) {
		String u1=url.replace("http://", "").replace("https://", "");
		int p=u1.indexOf("/");
		if(p!=-1)
			u1=u1.substring(0,p);
		p=u1.indexOf('.');
		if((p!=-1)&&(u1.startsWith("www"))) // skip wwwxxx.
			u1=u1.substring(p+1);
		return u1;
	}
	
	public static void killThread(Thread t) {
		if(t!=null) {
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {
			}
		}
	}
	
	public static final String REL_HOST = "host";
	public static final String REL_PATH = "path";
	
	public static String relativeURL(String rUrl,String pUrl,String relType) {
		if(empty(relType)||
		   empty(rUrl)||
		   empty(pUrl)||
		   rUrl.startsWith("http"))
			return rUrl;
		if(relType.equalsIgnoreCase(ChannelUtil.REL_HOST)) {
			int pos=pUrl.indexOf("//");
			if(pos==-1)
				return rUrl;
			pos=pUrl.indexOf("/", pos+1);
			if(pos==-1)
				return rUrl;
			String host=pUrl.substring(0,pos+1);
			return concatURL(host,rUrl);
		}
		if(relType.equalsIgnoreCase(ChannelUtil.REL_PATH)) {
			int pos=pUrl.lastIndexOf("/");
			if(pos==-1)
				return rUrl;
			String path=pUrl.substring(0, pos+1);
			return concatURL(path,rUrl);
		}
		return rUrl;
	}
	
	public static void appendVarLine(StringBuffer sb,String var,String val) {
		sb.append(var);
		sb.append("=");
		sb.append(val.trim());
		sb.append("\n");
	}
		
	public static Thread newBackgroundDownload(final String name,String url) {
		if(ChannelUtil.rtmpStream(url)) {
			try {
				//		URL u=new URL(url); 
				// rtmp stream special fix
				final ProcessBuilder pb=buildPid(name,url);
				if(pb==null)
					return null;
				Channels.debug("start rtmp process ");
				Runnable r = new Runnable() {
					public void run() {
						ChannelUtil.execute(pb);
					}
				};
				return new Thread(r);
			}
			catch (Exception e) {
				return null;
			}
		}
		String subFile="";
		if(url.startsWith("http")||
		   url.startsWith("navix")||
		   url.startsWith("subs")) {
			if(url.startsWith("navix")||
			   url.startsWith("subs")) {
				int pos=url.indexOf('?');
				if(pos==-1)
					return null;
				String[] data=url.substring(pos+1).split("&");
				for(int i=0;i<data.length;i++) {
					String[] kv=data[i].split("=");
					if(kv[0].equals("url"))
						url=ChannelUtil.unescape(kv[1]);
					if(kv[0].equals("subs"))
						subFile=unescape(kv[1]);
				}
			}
			if(empty(url))
				return null;
			final String rUrl=url;
			final String sFile=subFile;
			Runnable r = new Runnable() {
				public void run() {
					File f=new File(name);
					if(!empty(sFile)) {
						File s=new File(sFile);
						byte[] buf=new byte[4096];
						try {
							FileInputStream in = new FileInputStream(s);
							FileOutputStream out = new FileOutputStream(
									new File(f.getParent()+File.separator+s.getName()));
							while ((in.read(buf)) != -1)
								out.write(buf);

							in.close();
							out.close();
						}
						catch (Exception e) {
							// ignore this
							Channels.debug("Error moving subtitle file "+sFile);
						}
					}					
					// download the actaul movie, subtitles are done
					Channels.debug("rUrl "+rUrl);
					if(rUrl.contains(".m3u8")) {
						// we need to use ffmpeg here
						String ext=extension(name);
						if(empty(ext))
							f=new File(name+".mp4");
						ArrayList<String> args=new ArrayList<String>();
						String ffmpeg=PMS.getConfiguration().getFfmpegAlternativePath();
						if(empty(ffmpeg))
							ffmpeg=PMS.getConfiguration().getFfmpegPath();
						args.add(ffmpeg);
						args.add("-i");
						args.add(rUrl);
						args.add("-threads");
						args.add(""+PMS.getConfiguration().getNumberOfCpuCores());
						args.add("-v");
						args.add("0");
						args.add("-y");
						args.add(f.getAbsolutePath());
						ProcessBuilder pb=new ProcessBuilder(args);
						execute(pb);
					}
					else
						downloadBin(rUrl,f);
				}
			};
			return new Thread(r);
		}
		return null;
	}
	
	public static boolean filterInternals(DLNAResource r) {
		return ((r instanceof VirtualVideoAction)||(r instanceof ChannelPMSAllPlay));
	}
	
	public static String searchInPath(DLNAResource res) {
		while(res!=null) {
			if((res instanceof Search)) {
				return ((Search)res).result();
			}
			if(res instanceof SearchFolder)
				return ((SearchFolder)res).result();
			res=res.getParent();
		}
		return "";
	}
	
	public static int convInt(String str,int def) {
		try {
			Integer i=Integer.valueOf(str);
			return i.intValue();
		}
		catch (Exception e) {
		}
		return def;
	}

	public static String getCodePage() {
		String cp = PMS.getConfiguration().getSubtitlesCodepage();
		if(empty(cp))
			return "utf-8";
		return cp;
	}
	
}
