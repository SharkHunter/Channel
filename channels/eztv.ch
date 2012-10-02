version=0.1

macrodef ezSwitch {
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
			macro=ezSwitch
		}
	}
}