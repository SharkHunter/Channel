package com.sharkhunter.channel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelMatcher implements ChannelProps{
	 private Pattern regexp;
	 private String order[];
	 private ChannelProps properties;
	 private Matcher matcher;
	 
	 ChannelMatcher(String reg,
			 		String order,
			 		ChannelProps prop) {
		 if(ChannelUtil.empty(order)) //  no order configured, use default
			 order="url,name,thumb";
		 if(reg!=null)
			 this.regexp=Pattern.compile(reg,Pattern.MULTILINE|Pattern.DOTALL);
		 this.order=order.split(",");
		 this.properties=prop;
	  }

	 public Pattern getRegexp() {
		 return this.regexp;
	 }

	 public void setMatcher(String reg) {
		 if(reg!=null&&reg.length()!=0)
			 regexp=Pattern.compile(reg,Pattern.MULTILINE);
	 }
	      
	 public void setOrder(String o) {
		 if(o!=null&&o.length()!=0)
			 order=o.trim().split(",");
	 }
	      
	 public void startMatch(String str) {
		 if(this.regexp==null)
			 return;
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
						 return concatAll(i+1,res,properties.separator(field));
					 }
					 res=ChannelUtil.append(res,properties.separator(field),
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
}
