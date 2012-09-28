package com.sharkhunter.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualVideoAction;

public class ChannelCrawl implements Comparator {
	
	public final static int CRAWL_FLA = 0;
	public final static int CRAWL_HML = 1;
	
	public final static int CRAWL_HIGH = 0;
	public final static int CRAWL_MED = 1;
	public final static int CRAWL_LOW = 2;
	public final static int CRAWL_FIRST = 3;
	public final static int CRAWL_LAST = 4;
	public final static int CRAWL_ALL = 5;
	
	private boolean allMode;
	private String name;
	
	public ChannelCrawl() {
		allMode=false;
		name=null;
	}
	
	public boolean allSeen() {
		return allMode;
	}
	
	public String goodName() {
		return name;
	}
	
	public DLNAResource crawlOneLevel(ArrayList<DLNAResource> start,int crawlMode) {
		int pos=-1;
		int size=start.size();
		if(size==0)
			return null;
		
		switch(crawlMode) {
		case CRAWL_ALL:
			allMode=true;
		case CRAWL_LOW:
		case CRAWL_FIRST:
			pos=0;
			break;
		case CRAWL_LAST:
		case CRAWL_HIGH:
			pos=size-1;
			break;
		case CRAWL_MED:
			pos=(int)Math.round((double)size/2)-1;
			break;
		default:
			break;
		}
		
		while(pos > -1) {
			DLNAResource tmp=start.get(pos);
			if(filterInternals(tmp)){
				int p1=pos+1;
				if(p1==size)
					pos=pos-1;
				else
					pos=p1;
				continue;
			}
			return tmp;
		}
		return null;
	}
	
	private boolean filterInternals(DLNAResource r) {
		return ((r instanceof VirtualVideoAction)||(r instanceof ChannelPMSAllPlay));
	}
	
	private ArrayList<DLNAResource> sortedList(ArrayList<DLNAResource> list) {
		ArrayList<DLNAResource> res=new ArrayList<DLNAResource>();
		for(DLNAResource r : list) {
			if(filterInternals(r)){
				continue;
			}
			res.add(r);
		}
		Collections.sort(res, this);
		return res;
	}
	
	public DLNAResource crawl(ArrayList<DLNAResource> start,int[] modes) {
		ArrayList<DLNAResource> res=start;
		DLNAResource res1=null;
		for(int i=0;i<modes.length;i++) {
			int crawlMode=-1;
			if(modes[i]==CRAWL_FLA) 
				crawlMode=Channels.cfg().getCrawlFLMode();
			if(modes[i]==CRAWL_HML) {
				crawlMode=Channels.cfg().getCrawlHLMode();
				res=sortedList(res);
			}
			res1=crawlOneLevel(res,crawlMode);
			if(res1==null)
				return null;
			if(ChannelUtil.empty(name))
				name=res1.getName();
			res1.discoverChildren();
			res=(ArrayList<DLNAResource>) res1.getChildren();
		}
		if(Channels.save()) {
			// Compensate for the save folder, We know the last one 
			// in the save folder is the PLAY option so we use that one
			res1=crawlOneLevel((ArrayList<DLNAResource>) res1.getChildren(),CRAWL_LAST);
		}
		return res1;
	}
	
	public DLNAResource crawl(DLNAResource start,int[] modes) {
		return crawl((ArrayList<DLNAResource>) start.getChildren(),modes);
	}

	@Override
	public int compare(Object o1, Object o2) {
		DLNAResource r1=(DLNAResource)o1;
		DLNAResource r2=(DLNAResource)o2;
		if(r1==null||r2==null)
			return 0;
		String s1=r1.getName();
    	String s2=r2.getName();
        if (s2 == null || s1 == null) {
            return 0;
        }
 
        int lengthFirstStr = s1.length();
        int lengthSecondStr = s2.length();
 
        int index1 = 0;
        int index2 = 0;
 
        while (index1 < lengthFirstStr && index2 < lengthSecondStr) {
            char ch1 = s1.charAt(index1);
            char ch2 = s2.charAt(index2);
 
            char[] space1 = new char[lengthFirstStr];
            char[] space2 = new char[lengthSecondStr];
 
            int loc1 = 0;
            int loc2 = 0;
 
            do {
                space1[loc1++] = ch1;
                index1++;
 
                if (index1 < lengthFirstStr) {
                    ch1 = s1.charAt(index1);
                } else {
                    break;
                }
            } while (Character.isDigit(ch1) == Character.isDigit(space1[0]));
 
            do {
                space2[loc2++] = ch2;
                index2++;
 
                if (index2 < lengthSecondStr) {
                    ch2 = s2.charAt(index2);
                } else {
                    break;
                }
            } while (Character.isDigit(ch2) == Character.isDigit(space2[0]));
 
            String str1 = new String(space1);
            String str2 = new String(space2);
 
            int result;
 
            if (Character.isDigit(space1[0]) && Character.isDigit(space2[0])) {
                Integer firstNumberToCompare = new Integer(Integer
                        .parseInt(str1.trim()));
                Integer secondNumberToCompare = new Integer(Integer
                        .parseInt(str2.trim()));
                result = firstNumberToCompare.compareTo(secondNumberToCompare);
            } else {
                result = str1.compareTo(str2);
            }
 
            if (result != 0) {
                return result;
            }
        }
        return lengthFirstStr - lengthSecondStr;
	}
}

