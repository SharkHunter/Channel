version=0.1

scriptdef tubeSub1 {
	url=s_url
	regex='(.*?) S(\d+)E(\d+)
	match url
	if v1
		url=v1
		season=v2
		episode=v3
		serie='1
		play
	endif
	url=s_url
	regex='[^-]-
	match url
	url=v1
	movie='1
	play
}

macrodef tubeMedia {
		folder {
			#<a class="none" href="http://www.putlocker.com/file/21A1D7CAD6806210#">A Beautiful Mind - putlocker.com - http://www.putlocker.com/file/21A1D7CAD6806210#</a>
			matcher=class=\"none\" href=\"([^\"]+)\">(.*?) - http://
			order=url,name
			media {
				script=lockerScript
				subtitle=swesub,s4u,allSubs,podnapisiTV
				prop=name_index=1
			}
		}
		folder {
			#<a class="torrents" href="/torrent/7523/A_Beautiful_Mind/">
			matcher=a class=\"torrents\" href=\"([^\"]+)\"
			order=url
			url=http://www.tubeplus.me/
			prop=append_name=Torrents
			switch {
				#href='magnet:?xt=urn:btih:e93ade6b7b67632b1c2a490069effb4aae079cd2&dn=A+Beautiful+Mind+2001+dvdrip&tr=http%3A%2F%2Ftracker.publicbt.com%2Fannounce'
				matcher=href='magnet:[^=]+=urn:btih:([^&]+)&dn=([^&]+)&tr
				order=url,name
				name=Furk
				action=upload
				script=furkUploadHash
				prop=name_unescape
			}
		}
}

macrodef tvMacro {
	folder {
		#a target="_blank" title="Watch online: A Beautiful Mind" href="/player/7523/A_Beautiful_Mind/"><img border="0" alt="A Beautiful Mind" src="/resources/thumbs/7523.jpg"></a>
		matcher=a target=\"_blank\" title=\"[^:]+: ([^\"]+)\" href=\"([^\"]+)\"><img .*?src=\"([^\"]+)\"
		order=name,url,thumb
		url=http://www.tubeplus.me/
		prop=prepend_thumb=http://www.tubeplus.me/
		#post_script=tubeTvScript
		folder {
			#href=/player/1972239/The_Big_Bang_Theory/season_5/episode_18/The_Werewolf_Transformation/">Episode 18 - The Werewolf Transformation<
			matcher=href=(/player[^\"]+)\">([^<]+)<
			order=url,name
			url=http://www.tubeplus.me/
			macro=tubeMedia
			prop=name_separator=-
		}
	}
}

macrodef movieMacro {
	folder {
		#a target="_blank" title="Watch online: A Beautiful Mind" href="/player/7523/A_Beautiful_Mind/"><img border="0" alt="A Beautiful Mind" src="/resources/thumbs/7523.jpg"></a>
		matcher=a target=\"_blank\" title=\"[^:]+: ([^\"]+)\" href=\"([^\"]+)\"><img .*?src=\"([^\"]+)\"
		order=name,url,thumb
		url=http://www.tubeplus.me/
		prop=prepend_thumb=http://www.tubeplus.me/
		macro=tubeMedia
	}
	folder {
		#<li title="Next Page"><a href="/browse/movies/All_Genres/A/page/2/">
		matcher=title=\"(Next Page)\"><a href=\"([^\"]+)\"
		order=name,url
		url=http://www.tubeplus.me/
		prop=continue_name=(*Next [Pp]age.*),continue_limit=6
		type=recurse
	}
}

channel Tube+ {
	img=http://www.tubeplus.me/resources/img/tubeplus_logo.jpg
	subscript=tubeSub1
	folder {
	  name=TV Shows
	  folder {
		name=A-Z
		type=atzlink
		url=http://www.tubeplus.me/browse/tv-shows/All_Genres/
		prop=other_string=-
		macro=tvMacro
	  }
	}
	
	folder {
	  name=Movies
	  folder {
		name=A-Z
		type=atzlink
		url=http://www.tubeplus.me/browse/movies/All_Genres/
		prop=other_string=-
		macro=movieMacro
	  }
	}
} 
