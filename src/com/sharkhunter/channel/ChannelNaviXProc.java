package com.sharkhunter.channel;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelNaviXProc {
	
	private static String escapeChars(String str) {
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<str.length();i++) {
			char ch;
			switch((ch=str.charAt(i))) {
				case '\"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
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
	
	private static boolean boolOp(String var,String op,String comp) {
		if(op.equals("<")) // less then
			return var.compareTo(comp)<0;
		if(op.equals("<=")) // lte
			return var.compareTo(comp)<=0;
		if(op.equals("=")||op.equals("=="))
			return var.compareTo(comp)==0;
		if(op.equals("!=")||op.equals("<>"))
			return var.compareTo(comp)!=0;
		if(op.equals(">"))
			return var.compareTo(comp)>0;
		if(op.equals(">="))
			return var.compareTo(comp)>=0;
		return false;
	}
	
	private static String parseV2(Channel parent,String[] lines,int start,String url) {
		HashMap<String,String> vars=new HashMap<String,String>();
		Pattern ifparse=Pattern.compile("^([^<>=!]+)\\s*([!<>=]+)\\s*(.+)$");
		boolean if_skip=false;
		boolean if_true=false;
		parent.debug("parse v2 ");
		vars.put("s_url", url);
		for(int i=start;i<lines.length;i++) {
			String line=lines[i].trim();
			if(ChannelUtil.ignoreLine(line))
				continue;
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
				URL u=null;
				try {
					u=new URL(sUrl);
				} catch (MalformedURLException e) {
					return null;
				}
				String sPage=ChannelUtil.fetchPage(u);
				if(sPage==null||sPage.length()==0) {
					parent.debug("bad page from proc");
					return null;
				}
				parent.debug("scrape page "+sPage);
				vars.put("htmRaw", sPage);
				// apply regexp
				Pattern re=Pattern.compile(escapeChars(vars.get("regex")));
				Matcher m=re.matcher(sPage);
				if(m.find()) {
					for(int j=1;j<=m.groupCount();j++) {
						vars.put("v"+String.valueOf(j), m.group(j));
					}
				}
				continue;
			}
			if(line.startsWith("endif"))
				if_true=false;
			if(line.startsWith("if ")) { // if block
				String cond=line.substring(3);
				parent.debug("if "+cond+" pattern "+ifparse.pattern());
				Matcher im=ifparse.matcher(cond);
				if(!im.find()) {
					parent.debug("no match");
					continue;
				}
				String var=im.group(1);
				String op=null;
				String comp=null;
				if(im.groupCount()>1)
					op=im.group(2);
				if(im.groupCount()>2)
					comp=fixVar(im.group(3).trim(),vars.get(im.group(3).trim()));
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
				continue;
			}
			if(line.startsWith("elseif ")) {
				String cond=line.substring(7);
				Matcher im=ifparse.matcher(cond);
				if(!im.find())
					continue;
				String var=im.group(1);
				String op=null;
				String comp=null;
				if(im.groupCount()>1)
					op=im.group(2);
				if(im.groupCount()>2)
					comp=fixVar(im.group(3).trim(),vars.get(im.group(3)));
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
				continue;
			}
			if(line.startsWith("else ")) {
				if_true=true;
				continue;
			}
			
			if(line.startsWith("error ")) {
				parent.debug("Error "+line.substring(6));
				return null;
			}
			
			String[] vLine=line.split("=",2);
			if(vLine.length==2) { // variable
				String key=vLine[0].trim();
				String val=vLine[1].trim();
				vars.put(key, fixVar(val,vars.get(val)));
				parent.debug("var ass "+key+"="+fixVar(val,vars.get(val)));
				continue;
			}
			
			if(line.startsWith("concat ")) {
				String[] ops=line.substring(7).split(" ",2);
				String res=vars.get(ops[0].trim())+fixVar(ops[1],vars.get(ops[1]));
				vars.put(ops[0].trim(), res);
			}
			
			if(line.startsWith("match ")) {
				String var=line.substring(6).trim();
				Pattern re=Pattern.compile(escapeChars(vars.get("regex")));
				Matcher m=re.matcher(vars.get(var));
				if(!m.find()) {
					vars.put("nomatch","1");
				}
				else {
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
				String res;
				try {
					res = URLDecoder.decode(vars.get(var),"UTF-8");
				} catch (UnsupportedEncodingException e) {
					continue;
				}
				vars.put(var, res);
				continue;
			}
			
			if(line.startsWith("play"))
				break;
		}
		// All done return what we got
		String rUrl=vars.get("url");
		rUrl=ChannelUtil.append(rUrl, "!!!pms_ch_dash_y!!!", vars.get("playpath"));
		rUrl=ChannelUtil.append(rUrl, "!!!pms_ch_dash_w!!!", vars.get("swfplayer"));
		return rUrl;
	}
	
	private static String parseV1(Channel parent,String[] lines,int start,String url) {
		return parseV2(parent,lines,start,url);
	}
	
	public static String parse(Channel parent,String url,String pUrl) {
		if(pUrl==null) // no processor, just return what we got
			return url;
		URL pu=null;
		try {
			pu = new URL(pUrl+"?url="+url);
		} catch (MalformedURLException e) {
			parent.debug("error fetching page "+e);
			return null;
		}
		String procPage=ChannelUtil.fetchPage(pu);
		parent.debug("processor page "+procPage);
		if(procPage==null||procPage.length()==0) 
			return null;
		String[] lines=procPage.split("\n");
		int i=0;
		while(ChannelUtil.ignoreLine(lines[i])) 
			i++;
		try {
			if(lines[i].equalsIgnoreCase("v2"))
				return parseV2(parent,lines,i+1,url);
			if(lines[i].equalsIgnoreCase("v1"))
				return parseV1(parent,lines,i+1,url);
			return parseV2(parent,lines,i,url);
		}
		catch (Exception e) {
			parent.debug("error during NIPL parse "+e);
			return null;
		}
	}
}
