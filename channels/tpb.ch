version=0.2

macrodef tpbSwitch {
	switch {
		#<a href="http://torrents.thepiratebay.org/6614245/Fast_and_Furious_5_Fast_Five_(2011)_DVDRip_XviD-MAX.6614245.TPB.torrent" title="Download this torrent"><img src="http://static.thepiratebay.org/img/dl.gif" class="dl" alt="Download" /></a><a href="magnet:?xt=urn:btih:ce1fc50bffb09962be8f3c49478cbeb65e2afe0f&dn=Fast+and+Furious+5+Fast+Five+%282011%29+DVDRip+XviD-MAX&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80&tr=udp%3A%2F%2Ftracker.publicbt.com%3A80&tr=udp%3A%2F%2Ftracker.ccc.de%3A80"
			matcher=a href=\"[^\"]+\" [^>]+>([^<]+)</a></div>\s*<[^>]+>\s*<[^>]+></a>\s*<a href=\"([^\"]+)\"
			order=name,url
			name=Furk
			action=upload
			script=furkUpload
	}	
}

channel TPB {
	img=http://static.thepiratebay.org/img/tpblogo_sm_ny.gif
	folder {
		name=Top 100 - Video
		url=http://thepiratebay.org/top/201
		macro=tpbSwitch
	}
	folder {
		name=Top 100 - TV
		url=http://thepiratebay.org/top/205
		macro=tpbSwitch
	}
	folder {
		name=Top 100 - Music
		url=http://thepiratebay.org/top/101
		format=audio
		macro=tpbSwitch
	}
	folder {
		name=Top 100 - Books
		url=http://thepiratebay.org/top/102
		format=audio
		macro=tpbSwitch
	}
}
