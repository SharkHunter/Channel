version=0.46

scriptdef furkSubs {
	release='1
	stripExt s_url
	full_url=s_url
	regex='\.*\[.*?\]
	replace s_url '
	url=s_url
	play
}

scriptdef furkSubs1 {
	fname='1
	stripExt s_url
	full_url=s_url
	regex='\.*\[.*?\]
	replace s_url '
	url=s_url
	play
}

scriptdef furkSubs2 {
	# Most likely a tv series
	# These scripts could be combined...
	regex='\.*\[.*?\]
	replace s_url '
	regex='(.*?)[Ss](\d+)[Ee](\d+) 
	full_url=s_url
	stripExt full_url
	match s_url
	url=v1
	season=v2
	episode=v3
	play
}

scriptdef furkSubs3 {
	full_url=s_url
	stripExt full_url
	regex='(.*?)\[(\d+)\]
	match s_url
	year=v2
	url=v1
	if year
		play
	endif
	regex='(.*?)\((\d+)\)
	match s_url
	year=v2
	if year
	  url=v1
	  play
	endif
	regex='(.*?)(\d+) 
	match s_url
	year=v2
	if year
		url=v1
		play
	endif
	regex='(.*?)[_\.](\d+)[_\.]
	match s_url
	year=v2
	if year
		url=v1
		play
	endif
	url=s_url
	play
}

scriptdef furkUploadHash {
	url='info_hash=
	concat url s_url
	play
}

scriptdef furkUploadUrl {
	url='url=
	concat url s_url
	play
}

macrodef furkMacro {
	folder {
		type=exec
		name=Delete
		matcher=id: '([^']+)'
		order=url
		url=http://api.furk.net/api/file/unlink?id=
	}
	folder {
		#<a class="button-large button-play" href="http://icd0q6s14tvhto8tuv82gg9ttb9j0t3ds40r71g.gcdn.biz/pls/JM56JAwhydBTrZo2Kw7FQCJxxfFEEjR9J2gZKiT7myqYD6g42PI7bMWCx49sSKKOyZC5kFkwQVDcckU3yP4SryQcf6eNKqam_CRwEXATzEA/Harry%20Potter%20and%20the%20Deathly%20Hallows%20Part%202%202011%20TS%20UnKnOwN.xspf">Play</a><br /> 
		matcher=a class=\"button-large button-play\" href=\"([^\"]+)\">(Play)<
		order=url
		type=empty
		 media {
			matcher=<title>([^<]+)</title>\s+<location>([^<]+)</location>
			order=name,url
			subtitle=swesub,s4u,ut.se
			prop=name_index=1
		}
	}
}

macrodef furkPlayItem {
	media {
		# <a class="playlist-item" href="http://ie9hajrspg5sg9mgqs4s1tf9nb9j0t3ds40r71g.gcdn.biz/d/R/KNoWaBGevj73PXNXuxaZiISdFFw__hnNo159OhQLI5epxWrSyuW_X1oi88NmdnIZ/01_Enter_Sandman.mp3" class="first" title="Metallica - Metallica (1991)/Metallica - Metallica/01 Enter Sandman.mp3">Metallica - Metallica (1991)/Metallica - Metallica/01 Enter Sandman.mp3</a> 
		matcher=a class=\"playlist-item\" href=\"([^\"]+)\" .*?title=\"([^\"]+)\"
		order=url,name
		subtitle=swesub,s4u,ut.se
		prop=name_index=0
	}
}

macrodef furkFolder {
	folder {
         matcher=a href=\"(/df/[^\"]+)\">([^<]+)<
         order=url,name
         url=https://www.furk.net/
         macro=furkPlayItem
		 macro=furkMacro
	}
	folder {
		type=recurse
		matcher=a href=\"([^\"]+)\" class=\"nextprev\" title=\"Go to (Next Page)\"
		order=url,name
		url=https://www.furk.net/
	}
}

channel Furk {
   img=http://www.furk.net/img/logo.png?249
   subscript=furkSubs,furkSubs1,furkSubs2,furkSubs3
   login {
	  url=http://api.furk.net/api/login/login
      passwd=pwd
      user=login
      type=cookie
      #params=url=&gigya_uid=
	  associate=www.furk.net
   }
   resolve {
		matcher=magnet:[^=]+=urn:btih:([^&]+)&dn=[^&]+&tr
		prop=prepend_url=info_hash=
		action=upload
   }
   folder {
		type=action
		action_name=upload
		url=http://api.furk.net/api/dl/add
		prop=http_method=post
		media {
			# this is qued
			matcher=(\"found_files\":\"0\").*?\"name\":\"([^\"]+)\"
			order=url,name
			prop=bad
		}
		folder {
			matcher=url_page\":\"([^\"]+)\"
			url=https://www.furk.net/
			order=url
			type=empty
			macro=furkPlayItem
			macro=furkMacro
		}
   }
   folder {
		name=Stored
		url=http://www.furk.net/users/files/finished
		prop=continue_name=.*Next Page.*,continue_limit=6
		macro=furkFolder
  }
  folder {
      name=Search
	  type=search
	  url=http://api.furk.net/api/search
	  prop=continue_name=.*Next Page.*,continue_limit=6,prepend_url=format=json;q=
	  folder {
         matcher=url_page\":\"([^\"]+)\".*?name\":\"([^\"]+)\"
         order=url,name
		 macro=furkMacro
         media {
            # <a class="playlist-item" href="http://ie9hajrspg5sg9mgqs4s1tf9nb9j0t3ds40r71g.gcdn.biz/d/R/KNoWaBGevj73PXNXuxaZiISdFFw__hnNo159OhQLI5epxWrSyuW_X1oi88NmdnIZ/01_Enter_Sandman.mp3" class="first" title="Metallica - Metallica (1991)/Metallica - Metallica/01 Enter Sandman.mp3">Metallica - Metallica (1991)/Metallica - Metallica/01 Enter Sandman.mp3</a> 
            matcher=a class=\"playlist-item\" href=\"([^\"]+)\" .*?title=\"([^\"]+)\"
            order=url,name
         }
	}
  }
}
 
