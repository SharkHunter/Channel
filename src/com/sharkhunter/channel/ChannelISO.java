package com.sharkhunter.channel;

import java.util.HashMap;
import java.util.Locale;

public class ChannelISO {
	private static HashMap<String,String> special=new HashMap<String,String>();

	static {
		special.put("alb", "sq");
		special.put("arm", "hy");
		special.put("baq", "eu");
		special.put("bur", "my");
		special.put("cze", "cs");
		special.put("chi", "zh");
		special.put("wel", "cy");
		special.put("ger", "de");
		special.put("dut", "nl");
		special.put("gre", "el");
		special.put("per", "fa");
		special.put("fre", "fr");
		special.put("geo", "ka");
		special.put("ice", "is");
		special.put("mac", "mk");
		special.put("mao", "mi");
		special.put("may", "ms");
		special.put("rum", "ro");
		special.put("slo", "sk");
		special.put("tib", "bo");
		Locale[] locs=Locale.getAvailableLocales();
		for(int i=0;i<locs.length;i++) {
			String iso2=locs[i].getLanguage();
			String iso3=locs[i].getISO3Language();
			special.put(iso2,iso3);
			special.put(iso3, iso2);
		}
	}

	public static String iso(String someIso,int wantedIso) {
		if(someIso.length()==wantedIso)
			return someIso;
		return special.get(someIso);
	}

	public static boolean equal(String a,String b) {
		return a.equals(iso(b,a.length()));
	}


}
