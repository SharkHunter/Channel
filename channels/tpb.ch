version=0.31

macrodef tpbSwitch {
	switch {
		#<a href="magnet:?xt=urn:btih:7be37ac7f65dc78bf8b115629e60220c637fe0d1&dn=
			matcher=<a href=\"magnet:[^=]+=urn:btih:([^&]+)&dn=([^&]+)&tr
			order=url,name
			name=Furk
			action=upload
			script=furkUploadHash
			prop=name_unescape
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
