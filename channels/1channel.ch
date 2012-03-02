version=0.1

scriptdef ch1Subs {
	url=s_url
	regex='(.*?)\((\d+)\)!!!Season ([\d]+).*?!!!Episode ([\d]+)
	match url
	year=v2
	season=v3
	episode=v4
	if v1
		url=v1
		serie='1
		play
	endif
	regex='(.*?)(\(\d+\))
	match url
	url=v1
	year=v2
	play
}

macrodef tvMacro {
	folder {
		matcher=div class=\"index_item index_item_ie\"><a href=\"([^\"]+)\" title=\"Watch ([^\"]+)\"><img .*?\s*src=\"([^\"]+)\"
		order=url,name,thumb
		url=http://www.1channel.ch
		folder {
			#<h2><a href="/tv-6147-How-I-Met-Your-Mother/season-1">Season 1</a></h2>
			matcher=<h2><a href=\"([^\"]+)\">([^<]+)<
			order=url,name
			url=http://www.1channel.ch
			folder {
				#div class="tv_episode_item"> <a href="/tv-6147-How-I-Met-Your-Mother/season-1-episode-1">Episode 1                                <span class="tv_episode_name"> - Pilot</span>
				matcher=div class=\"[^\"]+\">\s*<a href=\"([^\"]+)\">(\S+ \d+)\s*<span [^>]+>([^<]+)
				order=url,name+
				url=http://www.1channel.ch
				folder {
					#<a href="/external.php?title=The+Walking+Dead&url=aHR0cDovL3d3dy5wdXRsb2NrZXIuY29tL2ZpbGUvMEY2RUY0NUIyRkE2MjI2MQ==&domain=cHV0bG9ja2VyLmNvbQ==&loggedin=0" onClick="return  addHit('1889616332', '1')" rel="nofollow" title="Watch Version 1 of The Walking Dead" target="_blank">Version 1</a>
					matcher=href=\"([^\"]+)\"[^>]+>(Version[^<]+)<
					order=url,name
					url=http://www.1channel.ch
					type=empty
					media {
						script=lockerScript
						subtitle=swesub,s4u,allSubs,podnapisiTV
						prop=name_index=3+2+1,name_separator=!!!
					}
				}
			}
		}
	}
}

macrodef movieMacro {
	folder {
		#<div class="index_item index_item_ie"><a href="/watch-2280191-Hangover-2" title="Watch Hangover 2 (2011)"><img src="http://images.1channel.ch/thumbs/2280191_Hangover_2_2011.jpg"
		matcher=<div class=\"index_item index_item_ie\"><a href=\"([^\"]+)\" title=\"Watch ([^\"]+)\"[^>]*><img src=\"([^\"]+)\"
		order=url,name,thumb
		url=http://www.1channel.ch
		folder {
			#<a href="/external.php?title=The+Walking+Dead&url=aHR0cDovL3d3dy5wdXRsb2NrZXIuY29tL2ZpbGUvMEY2RUY0NUIyRkE2MjI2MQ==&domain=cHV0bG9ja2VyLmNvbQ==&loggedin=0" onClick="return  addHit('1889616332', '1')" rel="nofollow" title="Watch Version 1 of The Walking Dead" target="_blank">Version 1</a>
			matcher=href=\"([^\"]+)\"[^>]+>(Version[^<]+)<
			order=url,name
			url=http://www.1channel.ch
			type=empty
			media {
				script=lockerScript
				subtitle=swesub,s4u,allSubs,podnapisiTV
				prop=name_index=1
			}
		}
	}
}

channel 1Channel {
	subscript=ch1Subs,
	folder {
	  name=TV Shows
	  folder {
		#Popular
		name=Popular
		url=http://www.1channel.ch/?tv=&sort=views
		macro=tvMacro
	  }
	  folder {
		name=A-Z
		type=atzlink
		url=http://www.1channel.ch/?tv=&sort=alphabet&letter=
		prop=other_string=0
		macro=tvMacro
	  }
	  folder {
		#Rating
		name=Rating
		url=http://www.1channel.ch/?tv=&sort=ratings
		macro=tvMacro
	  }
	  folder {
		#Release
		name=Release
		url=http://www.1channel.ch/?tv=&sort=release
		macro=tvMacro
	  }
	}
	
	folder {
	  name=Movies
	  folder {
		name=Popular
		url=http://www.1channel.ch/?sort=views
		macro=movieMacro
	  }
	  folder {
		name=A-Z
		type=atzlink
		url=http://www.1channel.ch/?sort=alphabet&letter=
		macro=movieMacro
	  }
	  folder {
		#Rating
		name=Rating
		url=http://www.1channel.ch/?&sort=ratings
		macro=movieMacro
	  }
	  folder {
		#Release
		name=Release
		url=http://www.1channel.ch/?&sort=release
		macro=movieMacro
	  }
	}
} 
