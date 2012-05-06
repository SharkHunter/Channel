version=0.14

scriptdef fix_ampand {
	regex='&nbsp;
	replace s_url '###0
	regex='&quot;
	replace s_url '
	regex='&amp;
	replace s_url '&
	url=s_url
	play
}

macrodef media2 {
media {
			matcher=title&gt;([^&]+)&lt;.*?link&gt;([^&]+)&quot;([^&]+)&quot;\s*(&nbsp;)([^&]+)&lt;.*?thumbnail&gt;([^&]+)&lt;
			order=name,url,url,url,url,thumb
			prop=matcher_dotall,live
			script=fix_ampand
		}
		
		media {
			matcher=title<span[^>]+>&gt;</span></span></span>([^<]+)<.*?link<span [^>]+>&gt;</span></span></span>(.*?)<span class="sc3">
			#.*?thumbnail<span class=\"re2\">&gt;</span></span></span>([^<]+)<span 
			order=name,url,thumb
			prop=matcher_dotall,live
			script=fix_ampand
		}
		media {
			matcher=title<span[^>]+>&gt;</span></span></span>([^<&]+)(<span class=\"sc1\">)*&amp;([^&]+)&lt;.*?link&gt;([^&]+)&lt;
			#.*?thumbnail<span class=\"re2\">&gt;</span></span></span>([^<]+)<span 
			order=name,dummy,name,url,thumb
			prop=matcher_dotall,name_separator=&,live
			script=fix_ampand
		}
		media {
			matcher=title&gt;([^<&]+)(<span class=\"sc1\">)*&amp;([^&]+)&lt;.*?link&gt;([^&]+)&lt;
			#.*?thumbnail<span class=\"re2\">&gt;</span></span></span>([^<]+)<span 
			order=name,dummy,name,url,thumb
			prop=matcher_dotall,name_separator=&,live
			script=fix_ampand
		}
		
		media {
			matcher=title&gt;([^&]+)&lt;.*?link&gt;(.*?)&lt;.*?thumbnail.*?(http[^<&]+).*?/thumbnail
			order=name,url,thumb
			prop=matcher_dotall,live
			script=fix_ampand
		}
}

macrodef myMedia {
	media {
			matcher=title<span[^>]+>&gt;</span></span></span>([^<]+)<.*?link<span [^>]+>&gt;</span></span></span>([^\s]+)\s*playpath=([^\s]+)\s*swfUrl=&quot;([^&]+)&quot;\s*(&nbsp;)*\s*pageUrl=([^<]+)<span .*?thumbnail<span class=\"re2\">&gt;</span></span></span>([^<]+)<
			order=name,url,playpath,swfUrl,dummy,pageUrl,thumb
			prop=matcher_dotall
		}
}

channel RTMPLists {
   folder {
		name=List 1
		folder {
			name=A-Z
			type=ATZ
			url=http://pastebin.com/k7uavgZR
			prop=cache
			macro=myMedia
		}
   }
   folder {
		name=Jonny Bravo's list
		folder {
			name=A-Z
			type=ATZ
			url=http://pastebin.com/Lu83Px2S
			prop=cache,discard_duplicates
			macro=myMedia
			macro=media2
		}
   }
   folder {
		name=BlackoutWorm's list
		folder {
			name=A-Z
			type=ATZ
			url=http://home.no/chj191/LiveTV.xml
			prop=cache
			media {
				matcher=title>([^<]+)</title>\s*<link>([^<]+)</link>\s*<thumbnail>([^<]+)</thumbnail>
				order=name,url,thumb
			}
		}
   }
}

