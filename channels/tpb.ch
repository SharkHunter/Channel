version=0.41

scriptdef tpbMonitor {
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
		concat url 'Season
		concat url '###0 
		concat url v2
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
		if name==v1
			if season==v2
			   if episode!=v3
				   url=entry
				endif
			endif
		endif				   
	endif
	play
}

macrodef tpbSwitch {
	switch {
		#<a href="magnet:?xt=urn:btih:7be37ac7f65dc78bf8b115629e60220c637fe0d1&dn=
			matcher=<a href=\"magnet:[^=]+=urn:btih:([^&]+)&dn=([^&]+)&tr
			order=url,name
			name=Furk
			action=upload
			script=furkUploadHash
			prop=name_unescape,monitor,monitor_type=parent,monitor_templ=tpbMonitor,crawl_mode=FLA+FLA
	}
}

channel TPB {
	img=http://static.thepiratebay.se/img/tpblogo_sm_ny.gif
	folder {
		name=Top 100 - Video
		url=http://thepiratebay.se/top/201
		macro=tpbSwitch
	}
	folder {
		name=Top 100 - TV
		url=http://thepiratebay.se/top/205
		macro=tpbSwitch
	}
	folder {
		name=Top 100 - Music
		url=http://thepiratebay.se/top/101
		format=audio
		macro=tpbSwitch
	}
	folder {
		name=Top 100 - Books
		url=http://thepiratebay.se/top/102
		format=audio
		macro=tpbSwitch
	}
	folder {
      name=Search
	  type=search
	  url=https://thepiratebay.se/search/
	  prop=http_method=get
	  macro=tpbSwitch
	}
}
