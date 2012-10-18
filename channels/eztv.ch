version=0.12

macrodef ezSwitch {
	folder {
	#<a href="/ep/38340/the-big-bang-theory-s06e02-hdtv-x264-lol/" title="The Big Bang Theory S06E02 HDTV x264-LOL (123.68 MB)"
		matcher=<a href=\"(/ep/[^\"]+)\" title=\"([^\"]+)\" alt=
		order=url,name
		url=http://www.eztv.it
		switch {
				matcher=<a href=\"([^\"]+)\" class=\"download.*?title=\"([^\"]+)\"
				order=url,name
				name=Furk
				action=upload
				script=furkUploadUrl
				prop=name_unescape
		}
	}
	
}


channel EZTV {
	folder {
		url=http://eztv.it/showlist/
		type=ATZ
		name=A-Z
		folder {
		#><a href="/shows/449/10-oclock-live/" class="thread_link">10 O'Clock Live</a></td>
			matcher=<a href=\"([^\"]+)\".*?>([^<]+)</a>
			order=url,name
			url=http://www.eztv.it
			prop=monitor,monitor_type=parent,monitor_templ=tpbMonitor,crawl_mode=FLA+FLA+FLA
			macro=ezSwitch
		}
	}
}