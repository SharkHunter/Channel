package com.sharkhunter.channel;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAResource;


public class ChannelNaviXProc {
	
	private static HashMap<String,String> vars=new HashMap<String,String>();
	private static HashMap<String,String> rvars=new HashMap<String,String>();
	public static HashMap<String,String> nookies=new HashMap<String,String>();
	private static long lastExpire=0;
	
	private static void debug(String msg) {
		if(ChannelUtil.empty(vars.get("nodebug")))
			Channels.debug(msg);
	}
	
	private static String escapeChars(String str) {
		StringBuilder sb=new StringBuilder();
	//	str=str.replaceAll("\\(", "\\\\\\(");
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
				return ChannelUtil.separatorToken(val.substring(1));
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
	
	private static String getVar(String key) {
		if(key.startsWith("pms_stash.")) {
			// special PMS stash
			String[] kSplit=key.split("\\.",3);
			if(kSplit.length>1) {
				String stash="default";
				String sKey=kSplit[1];
				if(kSplit.length>2) {
					stash=kSplit[1];
					sKey=kSplit[2];
				}
				return Channels.getStashData(stash, sKey);
			}
		}
		return vars.get(key);
	}
	
	private static void putVar(String key,String val) {
		if(key.startsWith("pms_stash.")) {
			// special PMS stash
			String[] kSplit=key.split("\\.",3);
			if(kSplit.length>1) {
				String stash="default";
				String sKey=kSplit[1];
				if(kSplit.length>2) {
					stash=kSplit[1];
					sKey=kSplit[2];
				}
				Channels.putStash(stash, sKey,val);
			}
		}
		vars.put(key, val);
	}
	
	private static void clearVs(int maxV) {
		for(int j=1;j<=maxV;j++) {
			vars.remove("v"+String.valueOf(j));
			rvars.remove("v"+String.valueOf(j));
		}
	}
	
	private static boolean parseV2(String[] lines,int start,String url) throws Exception {
		return parseV2(lines,start,url,null);
	}
	
	private static boolean parseV2(String[] lines,int start,String url,ChannelAuth a) throws Exception {
		Pattern ifparse=Pattern.compile("^([^<>=!]+)\\s*([!<>=]+)\\s*(.*)");
		boolean if_skip=false;
		boolean if_true=false;
		int maxV=0;
		vars.put("s_url", url);
		for(int i=start;i<lines.length;i++) {
			String line=lines[i];
			if(ChannelUtil.ignoreLine(line))
				continue;
			line=line.trim();
			if(line.startsWith("nodebug=\'")) {
				// take this first to disable debugging early
				vars.put("nodebug", line.substring(9));
			}
			debug("navix proc line "+line);
			if(if_true)
				if(line.startsWith("else")||line.startsWith("elseif")) {
					if_skip=true;
					continue;
				}
			// this if block was not active skip it
			if(if_skip&&!line.startsWith("else")&&!line.startsWith("elseif")&&!line.startsWith("endif"))
				continue;
			if_skip=false;
			
			if(line.startsWith("print")) {
				String var=line.substring(6).trim();
				String msg;
				if(var.startsWith("\'"))
					msg = var.substring(1);
				else
					msg = var + "=" + getVar(var);
				PMS.debug(msg);
				Channels.debug(msg);
				continue;
			}
			
			if(line.equalsIgnoreCase("scrape")) { // scrape, fetch page...
				String sUrl=vars.get((String)"s_url");
				URLConnection u=null;
				String action=vars.get("s_action");
				Proxy p=ChannelUtil.proxy(a);
				if(action!=null&&action.equalsIgnoreCase("geturl")) { 
					// YUCK!! this sucks, we need to get the location out of the redirect...
					debug("geturl called "+sUrl);
					HttpURLConnection h=(HttpURLConnection)new URL(sUrl).openConnection(p);
					h.setInstanceFollowRedirects(false);
					h.connect();
					String hName="";
					vars.put("geturl", h.getURL().toString());
					Channels.debug("put "+h.getURL().toString());
					for (int j=1; (hName = h.getHeaderFieldKey(j))!=null; j++) {
						Channels.debug("hdr "+hName+" val "+h.getHeaderField(j));
						if(hName.equalsIgnoreCase("location")) {
							vars.put("v1", h.getHeaderField(j));
							maxV=1;
							break;
						}
					}
					h.disconnect();
					continue;
				}
				u=new URL(sUrl).openConnection(p);
				String method=vars.get("s_method");
				String sPage;
				HashMap<String,String> hdr=new HashMap<String,String>();
				for(String key : vars.keySet()) {
					if(!key.startsWith("s_headers."))
						continue;
					hdr.put(key.substring(10), vars.get(key));
				}
				if(!ChannelUtil.empty(vars.get("s_referer"))) {
					hdr.put("Referer", vars.get("s_referer"));
				}
				if(method!=null&&method.equalsIgnoreCase("post")) {
					String q=vars.get("s_postdata");					
					sPage=ChannelUtil.postPage(u,(q==null?"":q),vars.get("s_cookie"),hdr);
				}
				else {
					sPage=ChannelUtil.fetchPage(u,null,vars.get("s_cookie"),hdr);
				}
				if(ChannelUtil.empty(sPage)) {
					Channels.debug("bad page from proc");
					throw new Exception("empty scrape page");
				}
				vars.put("geturl", u.getURL().toString());
				debug("scrape page "+sPage);
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
				clearVs(maxV);
				maxV=0;
				if(m.find()) {			
					for(int j=1;j<=m.groupCount();j++) {
						vars.put("v"+String.valueOf(j), m.group(j));
						rvars.put("v"+String.valueOf(j), m.group(j));
					}
					maxV=m.groupCount();
				}
				continue;
			}
			if(line.startsWith("endif")) {
				if_true=false;
				continue;
			}
			
			if(line.startsWith("if ")) { // if block
				String cond=line.substring(3);
				debug("if "+cond+" pattern "+ifparse.pattern());	
				Matcher im=ifparse.matcher(cond);
				String var;
				String op=null;
				String comp=null;
				if(!im.find()) {
					var=getVar(cond);
				}	
				else {
					var=getVar(im.group(1));
					debug("gc "+im.groupCount()+" "+var);
					if(im.groupCount()>1)
						op=im.group(2);
					if(im.groupCount()>2) {
						String s=im.group(3);
						comp=fixVar(s.trim(),getVar(s.trim()));
					}
				}
				debug("if var "+var+" op "+op+" comp "+comp);
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
					var=getVar(cond);
				}	
				else {
					var=getVar(im.group(1));
					Channels.debug("gc "+im.groupCount()+" "+var);
					if(im.groupCount()>1)
						op=im.group(2);
					if(im.groupCount()>2) {
						String s=im.group(3);
						comp=fixVar(s.trim(),getVar(s.trim()));
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
				Channels.debug("Error "+line.substring(6));
				throw new Exception("NIPL error");
			}
			
			
			if(line.startsWith("concat ")) {
				String[] ops=line.substring(7).split(" ",2);
				String res=ChannelUtil.append(getVar(ops[0].trim()),"",
											  fixVar(ops[1],getVar(ops[1])));
				putVar(ops[0].trim(), res);
				debug("concat "+ops[0]+" res "+res);
				continue;
			}
			
			if(line.startsWith("match ")) {
				String var=line.substring(6).trim();
				Pattern re=Pattern.compile(escapeChars(vars.get("regex")));
				Matcher m=re.matcher(getVar(var));
				clearVs(maxV);
				maxV=0;
				vars.remove("nomatch");
				if(!m.find()) {
					debug("no match "+re.pattern());
					vars.put("nomatch","1");
				}
				else {
					debug("match "+m.groupCount());
					for(int j=1;j<=m.groupCount();j++)	{
						debug("match v"+j+" = "+m.group(j));
						vars.put("v"+String.valueOf(j), m.group(j));
					}
					maxV=m.groupCount();
				}
				continue;
			}
			
			if(line.startsWith("replace ")) {
				String[] ops=line.substring(8).split(" ",2);
				Pattern re=Pattern.compile(vars.get("regex"));
				Matcher m=re.matcher(getVar(ops[0]));
				String res=m.replaceAll(fixVar(ops[1],getVar(ops[1])));
				putVar(ops[0], res);
				continue;
			}
			
			if(line.startsWith("unescape ")) {
				String var=line.substring(9).trim();
				String res=ChannelUtil.unescape(getVar(var));
				putVar(var, res);
				continue;
			}
			
			if(line.startsWith("escape ")) {
				String var=line.substring(7).trim();
				String res=ChannelUtil.escape(getVar(var));
				putVar(var, res);
				continue;
			}
			
			String[] vLine=line.split("=",2);
			if(vLine.length==2) { // variable
				String key=vLine[0].trim();
				String val=vLine[1].trim();
				if(key.startsWith("report_val ")) {
					key=key.substring(11);
					rvars.put(key, fixVar(val,vars.get(val)));
					debug("rvar ass "+key+"="+fixVar(val,vars.get(val)));
				}
				else {
					String realVal=fixVar(val,vars.get(val));
					if(key.startsWith("nookies.")) {						
						nookies.put(key.substring(8), realVal);
						ChannelNaviXNookie.store(key.substring(8),realVal,vars.get("nookie_expires"));
					}
					else if(key.startsWith("pms_stash.")) {
						// special PMS stash
						String[] kSplit=key.split("\\.",3);
						if(kSplit.length>1) {
							String stash="default";
							String sKey=kSplit[1];
							if(kSplit.length>2) {
								stash=kSplit[1];
								sKey=kSplit[2];
							}
							Channels.putStash(stash, sKey, realVal);
						}
					}
					vars.put(key, realVal);
					debug("var ass "+key+"="+realVal);
				}
				continue;
			}
			
			//////////////////////////////////////
			// These ones are channel specific
			//////////////////////////////////////
			
			if(line.startsWith("prepend ")) {
				String[] ops=line.substring(8).split(" ",2);
				String res=ChannelUtil.append(fixVar(ops[1],getVar(ops[1])),"",
											  getVar(ops[0].trim()));
				putVar(ops[0].trim(), res);
				debug("prepend "+ops[0]+" res "+res);
				continue;
			}
			
			if(line.startsWith("call ")) {
				String nScript=line.substring(5).trim();
				nScript=fixVar(nScript,getVar(nScript));
				if(ChannelUtil.empty(nScript)) {
					Channels.debug("Calling unknown script "+nScript);
					continue;
				}
				String arg=vars.get("url");
				if(ChannelUtil.empty(arg))
					arg=url;
				debug("call script "+nScript+" arg "+arg);
				String r=ChannelScriptMgr.runScript(nScript, arg, null);
				debug("script returned "+r);
				// extract the url
				String[] splits=r.split("&");
				putVar("v1",r); // fallback data
				for(int z=0;z<splits.length;z++) {
					if(splits[z].contains("url=")) {
						String tmp=splits[z].substring(splits[z].indexOf("url=")+4);
						putVar("v1",ChannelUtil.unescape(tmp));
						break;
					}
				}					
				continue;
			}
			
			if(line.startsWith("stripExt ")) {
				String[] ops=line.substring(9).split(" ");
				String var=ops[0].trim();
				String strip=".";
				int stripCnt=1;
				if(ops.length>1) {
					String num=ops[1].trim();
					try {
						stripCnt=Integer.parseInt(num);
					}
					catch (Exception e) {}
				}
				if(ops.length>2) {
					strip=ops[2];
				}
				String res=ChannelUtil.stripExt(getVar(var),stripCnt,strip);
				putVar(var, res);
				continue;
			}
			
			if(line.startsWith("sleep ")) {
				String time=line.substring(6).trim();
				time=fixVar(time,getVar(time));
				ChannelUtil.sleep(time);
				continue;
			}
			
			//////////////////////
			// Exit form here
			//////////////////////
			
			if(line.startsWith("report")) {
				Channels.debug("report found take another spin");
				return true;
			}
			
			if(line.trim().equals("play"))
				return false;
			
		}
		// This is weird no play statement?? throw error
		Channels.debug("no play found");
		throw new Exception("NIPL error no play");
	}
	
	private static boolean parseV1(String[] lines,int start,String url) throws Exception {
		String nextUrl=lines[start];
		int modLen=lines.length-start;
		if(nextUrl.contains("error")) {
			Channels.debug("navix v1 error");
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
		Channels.debug("v1 scrap page "+sPage);
		Pattern re=Pattern.compile(escapeChars(filt));
		Matcher m=re.matcher(sPage);
		if(m.find()) {
			String res=url+"?";
			String sep="";
			String v="";
			for(int j=1;j<=m.groupCount();j++) {
				v=v+sep+"v"+String.valueOf(j)+"="+ChannelUtil.escape(m.group(j));
				sep="&";
			}
			URLConnection u1=new URL(res+v).openConnection();
			String sPage2=ChannelUtil.fetchPage(u1);
			Channels.debug("res "+res+v+" spage2 "+sPage2);
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
	
	private static void addSpecials(Channel ch) {
		if(ch!=null) {
			vars.put("user", ch.user());
			vars.put("pwd", ch.pwd());
			HashMap <String,ChannelVar> chVar=ch.vars();
			for(String key : chVar.keySet()) {
				ChannelVar v=chVar.get(key);
				vars.put(v.varName(), v.value());
			}
		}
	}
	
	public static String parse(String url,String pUrl,int format) {
		return parse(url,pUrl,format,null,null,null,null,null);
	}
	
	public static String parse(String url,String pUrl,int format,String subFile,Channel ch) {
		return parse(url,pUrl,format,null,null,subFile,ch,null);
	}
	
	public static String parse(String url,String pUrl,int format,ChannelNaviX caller,
							   DLNAResource start,RendererConfiguration render) {
		return parse(url,pUrl,format,caller,start,null,null,render);
	}

	public static String parse(String url,String pUrl,int format,ChannelNaviX caller,
							   DLNAResource start,String subFile,Channel ch,
							   RendererConfiguration render) {
		vars.clear();
		rvars.clear();
		vars.put("subtitle",subFile);
		vars.put("url", url);
		vars.put("__type__", "navix");
		addSpecials(ch);
		if(ch!=null) {
			ChannelAuth auth=ch.prepareCom();
			if((auth!=null)&&(auth.method==ChannelLogin.COOKIE))
				vars.put("s_cookie", auth.authStr);
		}
		if(pUrl==null) // no processor, just return what we got
			return ChannelUtil.createMediaUrl(vars,format,ch,render);
		URL pu=null;
		try {
			pu = new URL(pUrl+"?url="+url);
		} catch (MalformedURLException e) {
			Channels.debug("error fetching page "+e);
			return null;
		}	
		int phase=0;
		boolean loop=true;
		String lastPage="";
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
					res=res+"&"+ChannelUtil.escape(key)+"="+
						ChannelUtil.escape(rvars.get(key));
				}
				Channels.debug("rvars "+res);
				res=res.replaceAll("v\\d+=&","&");
				res=res.replace("nomatch=&", "&");
				res=res.replaceAll("&+","&");
                res=res.replaceAll("^&","");
                Channels.debug("rvars fixed "+res);
                try {
					//res=URLEncoder.encode(res, "UTF-8");
					Channels.debug("res urlified "+res);
					pu=new URL(pUrl+"?"+res);
					Channels.debug("pUrl "+pu.toString());
				} catch (Exception e) {
					Channels.debug("wierd error "+e);
					return null;
				}
			}
			String procPage;
			try {
				procPage = ChannelUtil.fetchPage(pu.openConnection());
			} catch (Exception e1) {
				procPage="";
			}
			Channels.debug("processor page "+procPage);
			if(ChannelUtil.empty(procPage)) 
				return null;
			if(phase>0&&lastPage.equals(procPage)) {
				Channels.debug("processor loop");
				return null;
			}
			lastPage=procPage;
			String[] lines=procPage.split("\n");
			int i=0;
			while(ChannelUtil.ignoreLine(lines[i])) 
				i++;
			try {
				if(lines[i].equalsIgnoreCase("v2"))
					loop=parseV2(lines,i+1,url);
				else if(lines[i].equalsIgnoreCase("v1"))
					loop=parseV1(lines,i+1,pUrl);
				else {
					Channels.debug("weird version "+lines[i]+" guess "+(phase>0?"v2":"v1"));
					if(phase>0)
						loop=parseV2(lines,i,pUrl);
					else
						loop=parseV1(lines,i,pUrl);
				}
				phase++;
				Channels.debug("loop "+loop+" phase "+phase);
			}
			catch (Exception e) {
				Channels.debug("error during NIPL parse "+e);
				return null;
			}
		}
		// We made it construct result
		Channels.debug("createMediaurl");
		if(caller!=null) {
			HashMap<String,String> tmp=new HashMap<String,String>(vars); // subs might use NaviX scripts...
			String sub=caller.subCb(ChannelUtil.backTrack(start, 0));
			vars=tmp;
			vars.put("subtitle", sub);
		}
		// First asx parse (we do this always, since we are not sure here)
		String rUrl=ChannelUtil.parseASX(vars.get("url"), ChannelUtil.ASXTYPE_AUTO);
		vars.put("url", rUrl);
		vars.put("__type__", "navix");
		Channels.debug("type "+vars.get("__type__"));
		rUrl=ChannelUtil.createMediaUrl(vars,format,ch,render);
		Channels.debug("navix return media url "+rUrl);
		return rUrl;
	}
	
	//////////////////////////////////////
	// lite versions 
	//////////////////////////////////////
	
	// lite with ArrayList as line arg
	
	public static HashMap<String,String> lite(String url,ArrayList<String> lines,int asx,
			      HashMap<String,String> initStash,Channel ch) {
		String[] arr=lines.toArray(new String[lines.size()]);
		return lite(url,arr,asx,initStash,ch);
	}
	
	public static HashMap<String,String> lite(String url,ArrayList<String> lines,int asx) {
		return lite(url,lines,asx,null);
	}
	
	public static HashMap<String,String> lite(String url,ArrayList<String> lines,int asx,Channel ch) {
		return lite(url,lines,asx,null,ch);
	}
	
	public static HashMap<String,String> lite(String url,ArrayList<String> lines) {
		return lite(url,lines,ChannelUtil.ASXTYPE_AUTO);
	}
	
	public static HashMap<String,String> lite(String url,ArrayList<String> lines,
			      HashMap<String,String> initStash) {
		return lite(url,lines,ChannelUtil.ASXTYPE_AUTO,initStash,null);
	}
	
	public static HashMap<String,String> lite(String url,ArrayList<String> lines,
		      HashMap<String,String> initStash,Channel ch) {
		return lite(url,lines,ChannelUtil.ASXTYPE_AUTO,initStash,ch);
	}
	
	// Lite with array as line arg
	public static HashMap<String,String> lite(String url,String[] lines) {
		return lite(url,lines,ChannelUtil.ASXTYPE_AUTO,null);
	}
	
	public static HashMap<String,String> lite(String url,String[] lines,int asx,Channel ch) {
		return lite(url,lines,asx,null,ch);
	}
	
	public static HashMap<String,String> lite(String url,String[] lines,
			HashMap<String,String> initStash) {
		return lite(url,lines,ChannelUtil.ASXTYPE_AUTO,initStash,null);
	}
	
	// The actual work of lite is done here
	
	public static HashMap<String,String> lite(String url,String[] lines,int asx,
			                  HashMap<String,String> initStash,Channel ch) {
		try {
			vars.clear();
			addSpecials(ch);
			if(initStash!=null)
				vars.putAll(initStash);
			ChannelAuth a=null;
			if(ch!=null) { 
				a=ch.prepareCom();
				debug("a "+a+" cookie "+a.authStr);
				if(a!=null&&a.method==ChannelLogin.COOKIE)
					vars.put("s_cookie", a.authStr);
				debug("cookie "+vars.get("s_cookie"));
			}
			if(parseV2(lines,0,url,a))
				debug("found report statement in NIPL lite script. Hopefully script worked anyway.");
			String rUrl=ChannelUtil.parseASX(vars.get("url"),asx);
			vars.put("url", rUrl);
			HashMap<String,String> res=new HashMap<String, String>(vars);
			return res;
		}
		catch (Exception e) {
			Channels.debug("error during NIPL lite parse "+e);
			return null;
		}
	}
	
//////////////////////////////////
	// simple versions 
	//////////////////////////////////
	
	public static String simple(String str,ArrayList<String> script) {
		return simple(str,script,null,null);
	}
	
	public static String simple(String str,String script,HashMap<String,String> initStash) {
		ArrayList<String> sData=Channels.getScript(script);
		return simple(str,sData,initStash,null);
	}
	
	public static String simple(String str,String script) {
		ArrayList<String> sData=Channels.getScript(script);
		return simple(str,sData);
	}
	
	public static String simple(String str,ArrayList<String> script,HashMap<String,String> initStash,Channel ch) {
		if(script==null)
			return str;
		HashMap<String,String> res=lite(str,script,initStash,ch);
        if(res == null)
            return null;
		return res.get("url");
	}
}
