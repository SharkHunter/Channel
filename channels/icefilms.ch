version=0.49
## NOTE!!
## 
## We match out both the megavideo play link and megaupload link
## here. Both are streamable, but the megaupload is of course subject to
## limits etc. Try for yourself which is best.
## 

###############################
## IceFilms new method of
## fecting url's
## Thx to infidel for big parts
## of this script
#################################

scriptdef iceGo {
	print s_url
	id=s_url

	# get postdata vals
	regex='f\.lastChild\.value="(.+?)"
	match htmRaw
	sec=v1
	print sec
	regex='&t=(\d+)
	match htmRaw
	t=v1

	# build postdata string
	# e.g. id=189537&s=16&iqs=&url=&m=-99&cap=&sec=37fn8Oklq&t=4524
	# note: faking s,m (s:elapsed seconds, m:'onmousemove="m-=1"')
	s_postdata='id=
	concat s_postdata id
	concat s_postdata '&s=11&iqs=
	concat s_postdata '&url=&m=-77&cap=
	concat s_postdata '&sec=
	concat s_postdata sec
	concat s_postdata '&t=
	concat s_postdata t
	print s_postdata

	# set request method, header, cookie and send (using xbmc icefilms addon as model)
	s_method='post
	s_referer='http://www.icefilms.info
	s_url='http://www.icefilms.info/membersonly/components/com_iceplayer/video.phpAjaxResp.php

	#e.g /membersonly/components/com_iceplayer/GMorBMlet.php?url=http%3A%2F%2Fwww.megaupload.com%2F%3Fd%3DS6CLEDC2&
	regex='url=(.+)
	scrape
	url=v1
	unescape url
#	concat url '&login=1
	print url
	play
}

##############################
## Subtitle name mangle
## script for IceFilms
##############################

scriptdef iceSubs {
	full_url=s_url
	url=s_url
	regex='\((\d+)\) 
	match s_url
	year=v1
	v1='
	replace url '
	s_url=url
	regex='(\d+)x(\d+) 
	match s_url
	season=v1
	episode=v2
	regex='(\d+x\d+) .*
	replace url '
	play
}

#################################
## Rapidshare script
#################################

scriptdef rsScript {
    print s_url
	regex='\/files\/([^\/]+)\/(.*)
	match s_url
	s_url='https://api.rapidshare.com/cgi-bin/rsapi.cgi?sub=download&try=1&fileid=
	id=v1
	name=v2
	concat s_url v1
	concat s_url '&filename=
	concat s_url v2
	if user
		escape user
		concat s_url '&login=
		concat s_url user
		escape pwd
		concat s_url '&password=
		concat s_url pwd
	endif
	regex='DL:([^,]+),([^,]+),([^,]+)
	scrape 
	if v2 == '0
		url='https://
	else
		url='http://
	endif
	concat url v1
	concat url '/cgi-bin/rsapi.cgi?sub=download&fileid=
	concat url id
	concat url '&filename=
	concat url name
	concat url '&dlauth=
	concat url v2
	if user
		concat url '&login=
		concat url user
		concat url '&password=
		concat url pwd
	endif
	pms_stash.sleep=v3
	play
}

#############################
## The actual scrpaer
#############################

macrodef iceTvmacro {
    media {
		# Rapidshare
		#https://rapidshare.com/files/2029276453/The.Big.Bang.Theory.S05E14.HDTV.XviD-LOL.avi"
		script=rsScript
		prop=name_index=3+2,delay=dynamic
		subtitle=s4u
	}
}

macrodef mediaMacro {
	media {
		script=rsScript
		prop=name_index=2,delay=dynamic
		subtitle=s4u,allSubs,podnapisiMovie
	}
}

macrodef tvMacro {
	folder {
		# Series
		#<img class=star><a href=/tv/series/1/565>&#x27;Til Death (2006)</a>
		matcher=<a name=i id=([^>]+)></a><img class=star><a href=([^>]+)>([^<]+)</a>
		order=imdb,url,name
		#matcher=<img class=star><a href=([^>]+)>([^<]+)</a>
		#order=url,name
		url=http://www.icefilms.info
#		thumb_script=imdbThumb
        prop=movieinfo
		folder {
			# Episodes 
			#img class=star><a href=/ip.php?v=124783&>Jan 31. Bill Gates</a>
			matcher=<img class=star><a href=([^>]+)>([^<]+)</a>
			order=url,name
			url=http://www.icefilms.info
			folder {
				# <iframe id="videoframe" src="/membersonly/components/com_iceplayer/video.php?h=374&w=631&vid=4524&img=&ttl=30+Rock+1x01+Pilot+%282006%29" width="631" height="392" frameborder="0" marginwidth="0" marginheight="0" scrolling="no">
				matcher=src=\"(/membersonly/comp[^\"]+)\" 
				order=url,thumb
				url=http://www.icefilms.info
				type=empty
				post_script=iceGo
				folder {
					# onclick='go(247108)'>Source #1|PART 1
					matcher='go\(([0-9]+)\)'>(Source #[0-9]+|PART [0-9]+)[^<]+<span title='[^R]+(RapidShare)
					order=url,name,name
					prop=name_separator=###0
					type=empty
					macro=iceTvmacro
				}
			}
		}
	}
}

macrodef movieMacro {
	folder {
		# Movies
		#<img class=star><a href=/tv/series/1/565>&#x27;Til Death (2006)</a>
		matcher=<a name=i id=([^>]+)></a><img class=star><a href=([^>]+)>([^<]+)</a>
		order=imdb,url,name
		url=http://www.icefilms.info
		#thumb_script=imdbThumb
		prop=movieinfo
		folder {
			# <iframe id="videoframe" src="/membersonly/components/com_iceplayer/video.php?h=374&w=631&vid=4524&img=&ttl=30+Rock+1x01+Pilot+%282006%29" width="631" height="392" frameborder="0" marginwidth="0" marginheight="0" scrolling="no">
			matcher=src=\"(/membersonly/comp[^\"]+)\" 
			order=url
			url=http://www.icefilms.info
			type=empty
			post_script=iceGo
			folder {
					# onclick='go(247108)'>Source #1|PART 1
					matcher='go\(([0-9]+)\)'>(Source #[0-9]+|PART [0-9]+)[^<]+<span title='.* (RapidShare)
					order=url,name,name
					prop=name_separator=###0
					macro=mediaMacro
				}
		}
	}
}

channel IceFilms {
	img=http://img.icefilms.info/logo.png
	subscript=iceSubs
#	hdr=Referer=http://www.megaupload.com/?c=login
	folder {
	  name=TV Shows
	  folder {
		#Popular
		name=Popular
		url=http://www.icefilms.info/tv/popular/1
		macro=tvMacro
	  }
	  folder {
		name=A-Z
		type=atzlink
		url=http://www.icefilms.info/tv/a-z
		prop=other_string=1
		macro=tvMacro
	  }
	  folder {
		#Rating
		name=Rating
		url=http://www.icefilms.info/tv/rating/1
		macro=tvMacro
	  }
	  folder {
		#Release
		name=Release
		url=http://www.icefilms.info/tv/release/1
		macro=tvMacro
	  }
	}
	
	folder {
	  name=Movies
	  folder {
		name=Popular
		url=http://www.icefilms.info/movies/popular/1
		macro=movieMacro
	  }
	  folder {
		name=A-Z
		type=atzlink
		url=http://www.icefilms.info/movies/a-z
		prop=other_string=1
		macro=movieMacro
	  }
	  folder {
		#Rating
		name=Rating
		url=http://www.icefilms.info/movies/rating/1
		macro=movieMacro
	  }
	  folder {
		#Release
		name=Release
		url=http://www.icefilms.info/movies/release/1
		macro=movieMacro
	  }
	}
} 
