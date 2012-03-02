package com.sharkhunter.channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelMatcher implements ChannelProps{
	 private Pattern regexp;
	 private String regStr;
	 private String order[];
	 private ChannelProps properties;
	 private Matcher matcher;
	 private Channel parent;
	 
	 private static final String lcbr="###lcbr###";
	 private static final String rcbr="###rcbr###";
	 
	 ChannelMatcher(String reg,
			 		String order,
			 		ChannelProps prop) {
		 if(ChannelUtil.empty(order)) //  no order configured, use default
			 order="url,name,thumb";
		 if(reg!=null)
			 regStr=reg;//this.regexp=Pattern.compile(fixReg(reg),Pattern.MULTILINE);//|Pattern.DOTALL);
		 this.order=order.split(",");
		 this.properties=prop;
		 regexp=null;
		 parent=null;
	  }
	 
	 public void setChannel(Channel ch) {
		 parent=ch;
	 }

	 public Pattern getRegexp() {
		 //return this.regexp;
		 return Pattern.compile(fixReg(regStr),Pattern.MULTILINE);
	 }

	 public void setMatcher(String reg) {
		 if(!ChannelUtil.empty(reg))
			 regStr=reg;//regexp=Pattern.compile(fixReg(reg),Pattern.MULTILINE);
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
		 regexp=Pattern.compile(fixReg(regStr),Pattern.MULTILINE);
		 this.matcher=this.regexp.matcher(str);
	 }

	 public void reStartMatch() {
		 this.matcher.reset();
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
			 if(field==null||field.length()==0)
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
					 res=ChannelUtil.append(res,fixSep(properties.separator(field)),
	    						  				matcher.group(i+1));
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
	 
	 private String pend(String str,String field) {
		 str=esc(unesc(str,field),field);
		 String r1=ChannelUtil.append(str, null, properties.append(field));
		 return ChannelUtil.append(properties.prepend(field), null, r1);
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
	 
}
