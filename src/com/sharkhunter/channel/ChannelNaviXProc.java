package com.sharkhunter.channel;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelNaviXProc {
	
	private static HashMap<String,String> vars=new HashMap<String,String>();
	private static HashMap<String,String> rvars=new HashMap<String,String>();
	public static HashMap<String,String> nookies=new HashMap<String,String>();
	private static long lastExpire=0;
	
	private static String escapeChars(String str) {
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<str.length();i++) {
			char ch;
			switch((ch=str.charAt(i))) {
				case '\"':
					sb.append("\\\"");
					break;
				case '\'':
					sb.append("\\\'");
					break;
				/*case '\\':
					sb.append("\\\\");
					break;*/
				default:
					sb.append(ch);
					break;
			}
		}
		return sb.toString();
	}

	private static String fixVar(String val,String storedVal) {
		if(val.charAt(0)=='\'') { // string literal
			int stop=val.length();
/*			if(val.charAt(stop-1)=='\'')
				return val.substring(1, stop-1);
			else*/
				return val.substring(1);
		}
		else  { // variable
			return storedVal;
		}
	}
	
	private static boolean eqOp(String op) {
		if(op.equals("<=")) // lte
			return true;
		if(op.equals("=")||op.equals("=="))
			return true;
		if(op.equals(">="))
			return true;
		return false;
	}
	
	private static boolean boolOp(String var,String op,String comp) {
		if(var==null)
			var="";
		if(comp==null)
			comp="";
		if(op.equals("<")) // less then
			return (var.compareTo(comp)<0);
		if(op.equals("<=")) // lte
			return (var.compareTo(comp)<=0);
		if(op.equals("=")||op.equals("=="))
			return (var.compareTo(comp)==0);
		if(op.equals("!=")||op.equals("<>"))
			return (var.compareTo(comp)!=0);
		if(op.equals(">"))
			return (var.compareTo(comp)>0);
		if(op.equals(">="))
			return (var.compareTo(comp)>=0);
		return false;
	}
	
	private static boolean parseV2(Channel parent,String[] lines,int start,String url) throws Exception {
		Pattern ifparse=Pattern.compile("^([^<>=!]+)\\s*([!<>=]+)\\s*(.*)");
		boolean if_skip=false;
		boolean if_true=false;
		parent.debug("parse v2 ");
		vars.put("s_url", url);
		for(int i=start;i<lines.length;i++) {
			String line=lines[i];
			if(ChannelUtil.ignoreLine(line))
				continue;
			line=line.trim();
			parent.debug("navix proc line "+line);
			if(if_true)
				if(line.startsWith("else")||line.startsWith("elseif")) {
					if_skip=true;
					continue;
				}
			// this if block was not active skip it
			if(if_skip&&!line.startsWith("else")&&!line.startsWith("elseif")&&!line.startsWith("endif"))
				continue;
			if_skip=false;
			
			if(line.equalsIgnoreCase("scrape")) { // scrape, fetch page...
				String sUrl=vars.get((String)"s_url");
				URLConnection u=null;
				String action=vars.get("s_action");
				if(action!=null&&action.equalsIgnoreCase("geturl")) { 
					// YUCK!! this sucks, we need to get the location out of the redirect...
					HttpURLConnection h=(HttpURLConnection)new URL(sUrl).openConnection();
					h.setInstanceFollowRedirects(false);
					h.connect();
					String hName="";
					for (int j=1; (hName = h.getHeaderFieldKey(j))!=null; j++) {
						if(hName.equalsIgnoreCase("location")) {
							vars.put("v1", h.getHeaderField(j));
							break;
						}
					}
					h.disconnect();
					continue;
				}
				u=new URL(sUrl).openConnection();
				String method=vars.get("s_method");
				String sPage;
				HashMap<String,String> hdr=new HashMap<String,String>();
				for(String key : vars.keySet()) {
					if(!key.startsWith("s_headers."))
						continue;
					hdr.put(key.substring(10), vars.get(key));
				}
				if(method!=null&&method.equalsIgnoreCase("post")) {
					String q=vars.get("s_postdata");					
					sPage=ChannelUtil.postPage(u,(q==null?"":q),vars.get("s_cookie"),hdr);
				}
				else {
					sPage=ChannelUtil.fetchPage(u,null,vars.get("s_cookie"),hdr);
				}
				if(ChannelUtil.empty(sPage)) {
					parent.debug("bad page from proc");
					throw new Exception("empty scrape page");
				}
				vars.put("geturl", u.getURL().toString());
				parent.debug("scrape page "+sPage);
				vars.put("htmRaw", sPage);
				// get headers and cookies
				String hName="";
				for (int j=1; (hName = u.getHeaderFieldKey(j))!=null; j++) {
				 	if (hName.equals("Set-Cookie")) {                  
				 		String[] fields = u.getHeaderField(j).split(";\\s*");
				 		String cookie=fields[0];
				 		String[] cf=cookie.split("=",2);
				 		String cookieName = cf[0];
				        String cookieValue=null;
				        if(cf.length>1)
				        	cookieValue= cf[1];
				        vars.put("cookies."+cookieName, cookieValue);
				 	}
				 	else {
				 		String data=u.getHeaderField(j);
				 		vars.put("headers."+hName, data);
				 	}
				}
				// apply regexp
				Pattern re=Pattern.compile(escapeChars(vars.get("regex")));
				Matcher m=re.matcher(sPage);
				if(m.find()) {			
					for(int j=1;j<=m.groupCount();j++) {
						vars.put("v"+String.valueOf(j), m.group(j));
						rvars.put("v"+String.valueOf(j), m.group(j));
					}
				}
				continue;
			}
			if(line.startsWith("endif")) {
				if_true=false;
				continue;
			}
			
			if(line.startsWith("if ")) { // if block
				String cond=line.substring(3);
				parent.debug("if "+cond+" pattern "+ifparse.pattern());
				Matcher im=ifparse.matcher(cond);
				String var;
				String op=null;
				String comp=null;
				if(!im.find()) {
					var=vars.get(cond);
				}	
				else {
					var=vars.get(im.group(1));
					parent.debug("gc "+im.groupCount()+" "+var);
					if(im.groupCount()>1)
						op=im.group(2);
					if(im.groupCount()>2) {
						String s=im.group(2);
						comp=fixVar(s.trim(),vars.get(s.trim()));
					}
				}
				parent.debug("if var "+var+" op "+op+" comp "+comp);
				if(op==null) { // no operator
					if(var!=null) {
						if_true=true;
						continue;
					}
					else { // skip some lines
						if_skip=true;
						continue;
					}
				}
				if_true=boolOp(var,op,comp);
				if(!if_true)
					if_skip=true;
				continue;
			}
			
			if(line.startsWith("elseif ")) {
				String cond=line.substring(7);
				Matcher im=ifparse.matcher(cond);
				String var;
				String op=null;
				String comp=null;
				if(!im.find()) {
					var=vars.get(cond);
				}	
				else {
					var=vars.get(im.group(1));
					parent.debug("gc "+im.groupCount()+" "+var);
					if(im.groupCount()>1)
						op=im.group(2);
					if(im.groupCount()>2) {
						String s=im.group(2);
						comp=fixVar(s.trim(),vars.get(s.trim()));
					}
				}
				if(op==null) { // no operator
					if(var!=null) {
						if_true=true;
						continue;
					}
					else { // skip some lines
						if_skip=true;
						continue;
					}
				}
				if_true=boolOp(var,op,comp);
				if(!if_true)
					if_skip=true;
				continue;
			}
			if(line.startsWith("else ")) {
				if_true=true;
				continue;
			}
			
			if(line.startsWith("error ")) {
				parent.debug("Error "+line.substring(6));
				throw new Exception("NIPL error");
			}
			
			
			if(line.startsWith("concat ")) {
				String[] ops=line.substring(7).split(" ",2);
				String res=ChannelUtil.append(vars.get(ops[0].trim()),"",
											  fixVar(ops[1],vars.get(ops[1])));
				vars.put(ops[0].trim(), res);
				parent.debug("concat "+ops[0]+" res "+res);
				continue;
			}
			
			if(line.startsWith("match ")) {
				String var=line.substring(6).trim();
				Pattern re=Pattern.compile(escapeChars(vars.get("regex")));
				Matcher m=re.matcher(vars.get(var));
				if(!m.find()) {
					parent.debug("no match "+re.pattern());
					vars.put("nomatch","1");
				}
				else {
					parent.debug("match "+m.groupCount());
					for(int j=1;j<=m.groupCount();j++)	
						vars.put("v"+String.valueOf(j), m.group(j));
				}
				continue;
			}
			
			if(line.startsWith("replace ")) {
				String[] ops=line.substring(8).split(" ",2);
				Pattern re=Pattern.compile(vars.get("regex"));
				Matcher m=re.matcher(vars.get(ops[0]));
				m.replaceAll(ops[1]);
				continue;
			}
			
			if(line.startsWith("unescape ")) {
				String var=line.substring(9).trim();
				String res=ChannelUtil.unescape(vars.get(var));
				vars.put(var, res);
				continue;
			}
			
			if(line.startsWith("escape ")) {
				String var=line.substring(9).trim();
				String res;
				try {
					res = URLEncoder.encode(vars.get(var),"UTF-8");
				} catch (UnsupportedEncodingException e) {
					continue;
				}
				vars.put(var, res);
				continue;
			}
			
			String[] vLine=line.split("=",2);
			if(vLine.length==2) { // variable
				String key=vLine[0].trim();
				String val=vLine[1].trim();
				if(key.startsWith("report_val ")) {
					key=key.substring(11);
					rvars.put(key, fixVar(val,vars.get(val)));
					parent.debug("rvar ass "+key+"="+fixVar(val,vars.get(val)));
				}
				else {
					String realVal=fixVar(val,vars.get(val));
					if(key.startsWith("nookies.")) {						
						nookies.put(key.substring(8), realVal);
						ChannelNaviXNookie.store(key.substring(8),realVal,vars.get("nookie_expires"));
					}
					vars.put(key, realVal);
					parent.debug("var ass "+key+"="+realVal);
				}
				continue;
			}
			
			if(line.startsWith("report")) {
				parent.debug("report found take another spin");
				return true;
			}
			
			if(line.trim().equals("play"))
				return false;
			
		}
		// This is weird no play statement?? throw error
		parent.debug("no play found");
		throw new Exception("NIPL error no play");
	}
	
	private static boolean parseV1(Channel parent,String[] lines,int start,String url) throws Exception {
		String nextUrl=lines[start];
		int modLen=lines.length-start;
		if(nextUrl.contains("error")) {
			parent.debug("navix v1 error");
			throw new Exception("NaviX v1 parsse error");
		}
		if(modLen<2) {
			vars.put("url", nextUrl);
			return false;
		}
		String filt=lines[start+1];
		String ref="";
		String cookie="";
		if(modLen>2)
			ref=lines[start+2];
		if(modLen>3)
			cookie=lines[start+3];
		URLConnection u=new URL(nextUrl).openConnection();
		String sPage=ChannelUtil.fetchPage(u, null, cookie,null);
		if(ChannelUtil.empty(sPage)) 
			throw new Exception("Empty scrap page");
		parent.debug("v1 scrap page "+sPage);
		Pattern re=Pattern.compile(escapeChars(filt));
		Matcher m=re.matcher(sPage);
		if(m.find()) {
			String res=url+"?";
			String sep="";
			String v="";
			for(int j=1;j<=m.groupCount();j++) {
				v=v+sep+"v"+String.valueOf(j)+"="+m.group(j);
				sep="&";
			}
			URLConnection u1=new URL(res+v).openConnection();
			String sPage2=ChannelUtil.fetchPage(u1);
			parent.debug("res "+res+v);
			if(ChannelUtil.empty(sPage2))
				throw new Exception("Empty scrap page");
			String[] l=sPage2.split("\n");
			if(l[0].contains("error"))
				throw new Exception("Empty scrap page");
			vars.put("url", l[0]);
			if(l.length>1) 
				vars.put("swfplayer", l[1]);
			if(l.length>2)
				vars.put("playpath",l[2]);
			if(l.length>3)
				vars.put("pageurl", l[3]);	
		}
		return false;
	}

	public static String parse(Channel parent,String url,String pUrl) {
		if(pUrl==null) // no processor, just return what we got
			return ChannelUtil.createMediaUrl(url);
		URL pu=null;
		try {
			pu = new URL(pUrl+"?url="+url);
		} catch (MalformedURLException e) {
			parent.debug("error fetching page "+e);
			return null;
		}	
		int phase=0;
		boolean loop=true;
		String lastPage="";
		rvars.clear();
		vars.clear();
		// copy nookies to vars
		for(String key : nookies.keySet()) {
			if(ChannelNaviXNookie.expired(key)) {
				nookies.remove(key);
				continue;
			}
			vars.put("nookies."+key, nookies.get(key));
		}
		
		while(loop) {
			if(phase>0) {
				String res="phase="+String.valueOf(phase);
				for(String key : rvars.keySet()) {
					res=res+"&"+key+"="+rvars.get(key);
				}
				parent.debug("rvars "+res);
				res=res.replaceAll("v\\d+=&","&");
				res=res.replace("nomatch=&", "&");
				res=res.replaceAll("&+","&");
                res=res.replaceAll("^&","");
                parent.debug("rvars fixed "+res);
                try {
					//res=URLEncoder.encode(res, "UTF-8");
					parent.debug("res urlified "+res);
					pu=new URL(pUrl+"?"+res);
					parent.debug("pUrl "+pu.toString());
				} catch (Exception e) {
					parent.debug("wierd error "+e);
					return null;
				}
			}
			String procPage;
			try {
				procPage = ChannelUtil.fetchPage(pu.openConnection());
			} catch (Exception e1) {
				procPage="";
			}
			parent.debug("processor page "+procPage);
			if(ChannelUtil.empty(procPage)) 
				return null;
			if(phase>0&&lastPage.equals(procPage)) {
				parent.debug("processor loop");
				return null;
			}
			lastPage=procPage;
			String[] lines=procPage.split("\n");
			int i=0;
			while(ChannelUtil.ignoreLine(lines[i])) 
				i++;
			try {
				if(lines[i].equalsIgnoreCase("v2"))
					loop=parseV2(parent,lines,i+1,url);
				else if(lines[i].equalsIgnoreCase("v1"))
					loop=parseV1(parent,lines,i+1,pUrl);
				else {
					parent.debug("weird version "+lines[i]+" guess "+(phase>0?"v2":"v1"));
					if(phase>0)
						loop=parseV2(parent,lines,i,pUrl);
					else
						loop=parseV1(parent,lines,i,pUrl);
				}
				phase++;
				parent.debug("loop "+loop+" phase "+phase);
			}
			catch (Exception e) {
				parent.debug("error during NIPL parse "+e);
				return null;
			}
		}
		// We made it construct result
		parent.debug("createMediaurl");
		String rUrl=ChannelUtil.createMediaUrl(vars);
		parent.debug("navix return media url "+rUrl);
		return rUrl;
	}
	
	public static String lite(Channel parent,String url,ArrayList<String> lines) {
		String[] arr=lines.toArray(new String[lines.size()]);
		return lite(parent,url,arr);
	}
	public static String lite(Channel parent,String url,String[] lines) {
		try {
			vars.clear();
			if(parseV2(parent,lines,0,url))
				parent.debug("found report statement in NIPL lite script. Hopefully script worked anyway.");
			String rUrl=ChannelUtil.createMediaUrl(vars);
			parent.debug("navix lite return media url "+rUrl);
			return rUrl;
		}
		catch (Exception e) {
			parent.debug("error during NIPL lite parse "+e);
			return null;
		}
	}
}
