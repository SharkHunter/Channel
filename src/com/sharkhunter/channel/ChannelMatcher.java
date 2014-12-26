package com.sharkhunter.channel;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelMatcher implements ChannelProps{
	 private Pattern regexp;
	 private String regStr;
	 private String order[];
	 private ChannelProps properties;
	 private Matcher matcher;
	 private Channel parent;
	 private int flags;
	 private String pageReg;
	 private HashMap<String,ChannelMatcher> embed;
	 
	 private static final String lcbr="###lcbr###";
	 private static final String rcbr="###rcbr###";
	 
	 ChannelMatcher(String reg,
			 		String order,
			 		ChannelProps prop) {
		 if(ChannelUtil.empty(order)) //  no order configured, use default
			 order="url,name,thumb";
		 if(reg!=null)
			 regStr=reg;
		 this.order=order.split(",");
		 this.properties=prop;
		 regexp=null;
		 parent=null;
		 pageReg = null;
		 flags=Pattern.MULTILINE;
		 embed=new HashMap<String,ChannelMatcher>();
	  }
	 
	 public void setChannel(Channel ch) {
		 parent=ch;
	 }
	 
	 public void processProps(String[] props) {
		 if(ChannelUtil.getProperty(props, "matcher_dotall"))
			 flags|=Pattern.DOTALL;
		 if(ChannelUtil.getProperty(props, "no_case"))
			 flags|=Pattern.CASE_INSENSITIVE;
		 pageReg = ChannelUtil.getPropertyValue(props, "page_split");
		 for(String key : embed.keySet()) {
			 ChannelMatcher m=embed.get(key);
			 m.processProps(props);
		 }
	 }

	 public Pattern getRegexp() {
		 return Pattern.compile(fixReg(regStr),flags);
	 }

	 public void setMatcher(String reg) {
		 if(!ChannelUtil.empty(reg))
			 regStr=reg;
	 }
	 
	 private String fixReg(String str) {
		 if(parent!=null)
			 str=parent.resolveVars(str);
		 return str.replaceAll(lcbr, "{")
		 			.replaceAll(rcbr, "}");
	 }
	      
	 public void setOrder(String o) {
		 if(!ChannelUtil.empty(o))
			 order=o.trim().split(",");
	 }
	      
	 public void startMatch(String str) {
		 if(ChannelUtil.empty(regStr))
			 return;
		 regexp=Pattern.compile(fixReg(regStr),flags);
		 if(!ChannelUtil.empty(pageReg)) {
			 // inherit flags
			 Pattern re = Pattern.compile(fixReg(pageReg), flags);
			 Matcher m1 = re.matcher(str);
			 if(m1.find()) {
				 str = m1.group(1);
			 }
		 }
		 this.matcher=this.regexp.matcher(str);
	 }

	 public void reStartMatch() {
		 this.matcher.reset();
	 }
	 
	 public void addEmbed(String name,ChannelMatcher m) {
		 m.flags=this.flags;
		 embed.put(name, m);
	 }

	 public boolean match() {
		 if(this.regexp==null) // no regexp, of course no match
			 return false;
	  	return this.matcher.find();
	 }

	 public String getMatch(String field) {
		 return this.getMatch(field,false,"");
	 }
	      
	 public String getMatch(String field,boolean fallbackFirst) {
		 return getMatch(field,fallbackFirst,"");
	 }
	 
	 public String getMatch(String field,boolean fallbackFirst,String def) {
		 return pend(getMatch_i(field,fallbackFirst,def),field);
	 }

	 private String getMatch_i(String field,boolean fallbackFirst,String def) {
		 String res="";
		 if(properties==null)
			 properties=this;
		 for(int i=0;i<this.order.length;i++) {
			 if(ChannelUtil.empty(field))
				 break;
			 //if(this.order[i].compareToIgnoreCase(field)==0) {
			 if(this.order[i].startsWith(field)) {
				 if((i+1)>this.matcher.groupCount()) { // to few matches
					 if(res.length()!=0) // we got some already use it
						 return res;
					 if(fallbackFirst) // we return the first match here
						 return this.matcher.group(1);
					 else // otherwise default value (="")
						 return def;
				 }
				 else {
					 if(order[i].charAt(order[i].length()-1)=='+') { 
						 // special hack here. if it ends with a '+' then we 
						 // add all matches left to one big chunk
						 return concatAll(i+1,res,fixSep(properties.separator(field)));
					 }
					 String matched=matcher.group(i+1);
					 if(order[i].endsWith("_embed")) {
						 // embed matching
						 ChannelMatcher m1=embed.get(field);
						 if(m1!=null) {
							 m1.startMatch(matched);
							 String tmp="";
							 while(m1.match())
								 tmp=ChannelUtil.append(tmp,fixSep(properties.separator(field)),
										 				m1.getMatch(field, true, tmp));
							 if(!ChannelUtil.empty(tmp))
								 matched=tmp;
						 }
					 }
					 res=ChannelUtil.append(res,fixSep(properties.separator(field)),
	    						  					   matched);
				 }
			 }
		 }
		 if(res.length()!=0)
			 return res;
		 if(fallbackFirst)
			 return this.matcher.group(1);
		 else
			 return def;
	 }
	 
	 public String pend(String str,String field) {
		 if(ChannelUtil.empty(str))
			 return str;
		 str=esc(unesc(str,field),field);
		 //str=StringEscapeUtils.unescapeHtml(str); // do this always
		 String r1=ChannelUtil.append(str, fixSep(properties.separator(field)), properties.append(field));
		 return doMangle(ChannelUtil.append(properties.prepend(field), fixSep(properties.separator(field)), r1),field);
	 }
	 
	 private String doMangle(String str,String field) {
		 String script=properties.mangle(field);
		 if(ChannelUtil.empty(script)) // no script return what we got
			 return str;
		 String res=ChannelScriptMgr.runScript(script, str, parent);
/*		 if(ChannelUtil.empty(res)) // no funny result from mangle script, leave it
			 return str;*/
		 return res;
	 }
	 
	 private String esc(String str,String field) {
		 if(properties.escape(field))
			 return ChannelUtil.escape(str);
		 return str;
	 }
	 
	 private String unesc(String str,String field) {
		 if(properties.unescape(field))
			 return ChannelUtil.unescape(str);
		 return str;
	 }
	 
	 private String fixSep(String s) {
		 return ChannelUtil.separatorToken(s);
	 }
	      
	 private String concatAll(int start,String res,String sep) {
		 for(int i=start;i<=matcher.groupCount();i++) {
			 res=ChannelUtil.append(res,sep,this.matcher.group(i));
		 }
		 return res;
	 }
	 
	 public void setProperties(ChannelProps prop) {
		 this.properties=prop;
	 }
	      
	 public String separator(String base) { // used if no props are configured...
		 return null;
	 }   	  
	 
	 public boolean onlyFirst() {
		 return false;
	 }
	 
	 public String orderString() {
		 StringBuilder sb=new StringBuilder();
		 orderString(sb);
		 return sb.toString();
	 }
	 
	 public void orderString(StringBuilder sb) {
		 if(order==null)
			 return;
		 sb.append("order=");
		 ChannelUtil.list2file(sb, order);
	 }
	 
	 public String regString() {
		 return getRegexp().toString().replaceAll("\\{",lcbr)
		 		.replaceAll("}", rcbr);
	 }

	@Override
	public String append(String base) {
		return null;
	}

	@Override
	public String prepend(String base) {
		return null;
	}

	@Override
	public boolean escape(String base) {
		return false;
	}

	@Override
	public boolean unescape(String base) {
		return false;
	}
	 
	public String mangle(String base) {
		return null;
	}
}
