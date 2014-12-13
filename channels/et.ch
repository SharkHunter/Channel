version=0.11

scriptdef etMonitor {
	regex='\.*\[.*?\]
	replace s_url '
	regex='[\._]
	replace s_url '###0
	regex='(.*?)[Ss]0*(\d+)[Ee]0*(\d+) 
	match s_url
	if nomatch
		regex='(.*?)0*(\d+)x0*(\d+)
		match s_url
	endif
	if __wash__
		url=v1		
		play
	else
		name=v1
		season=v2
		episode=v3
		regex='\.*\[.*?\]
		replace entry '
		regex='[\._]
		replace entry '###0
		regex='(.*?)[Ss]0*(\d+)[Ee]0*(\d+) 
		match entry
		if nomatch
			regex='(.*?)0*(\d+)x0*(\d+)
			match entry
		endif
		url='
		if name!=v1
			play
		endif
		if season!=v2
			play
		endif
		if episode!=v3
			url=entry
			play
		else
			url='__EXACT_MATCH__
			play
		endif
	endif
	play
}

macrodef etSwitch {
	folder {
			#a href="/torrent/3915691/The+Equalizer+2014+V2+720p+BRRip+x264+AC3-EVO.html" title="view The Equalizer 2014 V2 720p BRRip x264 AC3-EVO torrent">The Equalizer 2014 V2 720p BRRip x264 AC3-EVO</a>
			matcher=a href=\"(/torrent[^\"]+)\"[^>]+>([^<]+)</a>
			order=url,name
			url=http://extratorrent.cc			
			switch {
				#<a href="magnet:?xt=urn:btih:7be37ac7f65dc78bf8b115629e60220c637fe0d1&dn=
				matcher=<a href=\"magnet:[^=]+=urn:btih:([^&]+)&amp;dn=([^&]+)&amp;tr
				order=url,name
				name=Furk
				action=upload
				script=furkUploadHash
				prop=name_unescape,monitor,monitor_try_search,monitor_type=parent,monitor_templ=etMonitor,crawl_mode=FLA+FLA
			}
	}
}

macrodef etNext {
	folder {
		#<a href="/view/popular/TV.html?page=4&amp;srt=added&amp;order=desc&amp;pp=50" title="4" class="pager_link">4</a>
		type=recurse
		matcher=a href=\"([^\"]+)\" title=\"[^\"]+\" class=\"pager_link\">(&gt;)</a>
		order=url,name
		url=http://extratorrent.cc
		prop=continue_name=.*gt;,continue_limit=3
	}
}

channel ExtraTorrent {
	img=http://static.extratorrent.cc/images/logo.gif
	folder {
		name=Popular
		folder {
			name=Movies
			url=http://extratorrent.cc/view/popular/Movies.html
			macro=etSwitch
			macro=etNext
		}
		folder {
			name=TV
			url=http://extratorrent.cc/view/popular/TV.html
			prop=continue_name=.*gt;,continue_limit=3
			macro=etSwitch
			macro=etNext
		}
		folder {
			name=Music
			url=http://extratorrent.cc/view/popular/Music.html
			format=audio
			macro=etSwitch
			macro=etNext
		}
	}		
	folder {
		name=Browse
		folder {
			name=Movies
			url=http://extratorrent.cc/category/4/Movies+Torrents.html
			prop=continue_name=.*gt;,continue_limit=3
			folder {
				matcher=a href=\"(/category[^\"]+)\"[^>]+>([^<]+)</a>
				order=url,name
				url=http://extratorrent.cc/
				macro=etSwitch
			}
			macro=etNext
		}
		folder {
			name=Tv
			url=http://extratorrent.cc/category/8/TV+Torrents.html
			type=ATZ
			folder {
				matcher=a href=\"(/category[^\"]+)\"[^>]+>([^<]+)</a></td>
				order=url,name				
				url=http://extratorrent.cc/
				macro=etSwitch
			}			
			macro=etNext
		}
		folder {
			name=Music
			url=http://extratorrent.cc/category/5/Music+Torrents.html
			folder {
				matcher=a href=\"(/category[^\"]+)\"[^>]+>([^<]+)</a></td>
				order=url,name
				url=http://extratorrent.cc/
				format=audio
				macro=etSwitch
			}			
			macro=etNext
		}
	}
	
	folder {
      name=Search
	  type=search
	  url=http://extratorrent.cc/search/
	  prop=http_method=get
	  macro=etSwitch
	}
}
