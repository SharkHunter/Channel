version=0.33

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
#            <a class="button-large button-play" href="http://icd0q6s14tvhto8tuv82gg9ttb9j0t3ds40r71g.gcdn.biz/pls/JM56JAwhydBTrZo2Kw7FQCJxxfFEEjR9J2gZKiT7myqYD6g42PI7bMWCx49sSKKOyZC5kFkwQVDcckU3yP4SryQcf6eNKqam_CRwEXATzEA/Harry%20Potter%20and%20the%20Deathly%20Hallows%20Part%202%202011%20TS%20UnKnOwN.xspf">Play</a><br /> 
		matcher=a class=\"button-large button-play\" href=\"([^\"]+)\">(Play)<
		order=url
		type=empty
		media {
			matcher=<title>([^<]+)</title>\s+<location>([^<]+)</location>
			order=name,url
			subtitle=swesub,s4u
			#,subscene
			prop=name_index=1
		}
	}
}


macrodef furkFolder {
	folder {
         matcher=a href=\"(/df/[^\"]+)\">([^<]+)<
         order=url,name
         url=http://www.furk.net/
		 macro=furkMacro
         media {
            # <a class="playlist-item" href="http://ie9hajrspg5sg9mgqs4s1tf9nb9j0t3ds40r71g.gcdn.biz/d/R/KNoWaBGevj73PXNXuxaZiISdFFw__hnNo159OhQLI5epxWrSyuW_X1oi88NmdnIZ/01_Enter_Sandman.mp3" class="first" title="Metallica - Metallica (1991)/Metallica - Metallica/01 Enter Sandman.mp3">Metallica - Metallica (1991)/Metallica - Metallica/01 Enter Sandman.mp3</a> 
            matcher=a class=\"playlist-item\" href=\"([^\"]+)\" .*?title=\"([^\"]+)\"
            order=url,name
         }
	}
}

channel Furk {
   img=http://www.furk.net/img/logo.png?249
   subscript=furkSubs,furkSubs1,furkSubs2
   login {
	  url=http://api.furk.net/api/login/login
      passwd=pwd
      user=login
      type=cookie      
      params=url=&gigya_uid=
	  associate=www.furk.net
   }
   folder {
		type=action
		action_name=upload
		#url=https://www.furk.net/users/files/add
		url=http://api.furk.net/api/dl/add
		prop=http_method=post
		hdr=Referer=http://www.furk.net/users/files/add
		folder {
			matcher=url_page":"([^"]+)
			order=url
			type=empty
			macro=furkMacro
		}
   }
   folder {
		name=Stored
		url=http://www.furk.net/users/files/finished
		macro=furkFolder
  }
  folder {
      name=Search
	  type=search
	  url=http://api.furk.net/api/search
	  prop=prepend_url=api_key=wxRsJ4uGJ33Tb2f6nysVTNMwdG3hAbOG;format=json;q=
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
