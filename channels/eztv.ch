version=0.16

macrodef ezSwitch {
	folder {
	#<a href="/ep/38340/the-big-bang-theory-s06e02-hdtv-x264-lol/" title="The Big Bang Theory S06E02 HDTV x264-LOL (123.68 MB)"
		matcher=<a href=\"(/ep/[^\"]+)\"\s*title=\"([^\"]+)\"\s*alt=
		order=url,name
		url=http://eztv.it
		switch {
			matcher=href=\"(magnet:[^\"]+)\" class=\"magnet\" title=\"([^\"]+)\">
			order=url,name
			name=Furk
			action=upload
			script=furkUploadHash
			prop=name_unescape,monitor,monitor_type=parent,monitor_templ=tpbMonitor,crawl_mode=FLA+FLA
		}
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
			url=http://eztv.it
			prop=monitor,monitor_type=parent,monitor_templ=tpbMonitor,crawl_mode=FLA+FLA+FLA
			macro=ezSwitch
		}
	}
	folder {
		name=Added Recently
		url=http://eztv.it/sort/100/
		macro=ezSwitch
		folder {
			type=recurse
			matcher=a href=\"([^\"]+)\"> (next page)
			order=url,name
			url=http://eztv.it
			prop=only_first
		}
	}
}