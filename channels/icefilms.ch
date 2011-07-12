version=0.34
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
	print url
	play
}

##############################
## Subtitle name mangle
## script for IceFilms
##############################

scriptdef iceSubs {
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

#############################
## Thumbnail scrape
## script using IMDB
#############################

scriptdef imdbThumb {
#<link rel='image_src' href='http://ia.media-imdb.com/images/M/MV5BNTUwODQyNjM0NF5BMl5BanBnXkFtZTcwNDMwMTU1Mw@@._V1._SX94_SY140_.jpg'
	regex='image_src' href='([^']+)'
	prepend s_url 'http://www.imdb.com/title/tt
	scrape
	url=v1
	play
}

#############################
## The actual scrpaer
#############################

macrodef mediaMacro {
	media {
		name=MegaVideo
		matcher=<a href=\"([^\"]+)\"\s+class=\"down_links_mv\"
		order=url
		subtitle=s4u,allSubs,podnapisiTV
		#nscript=http://navix.turner3d.net/proc/megavideo
		prop=concat_name=rear,name_separator= ,name_index=3+2
	}
	media {
		#<div class="down_butt_pad1" style="display:none;" id="downloadlink"><a href="http://www820.megaupload.com/files/e852a3a714538767347d5866d6ad9d7c/big_bang_theory.1x01.dvdrip_xvid-fov.H2020.dvd4arab.com.avi" class="down_butt1"></a>
		matcher=<div class=\"down_butt_pad1\" style=\"display:none;\" id=\"downloadlink\"><a href="([^\"]+)"
		name=MegaUpload
		order=url
		subtitle=s4u,allSubs,podnapisiTV
		prop=concat_name=rear,name_separator= ,name_index=3+2
	}	
	media {
		# <a href="http://www68.megaupload.com/files/f27f675c5fd22f12f08a4e03fc2d4522/Game.of.Thrones.S01E01.HDTV.XviD-FEVER.avi" class="down_ad_butt1"></a> 
		matcher=<a href="([^\"]+)" class="down_ad_butt1"> 
		name=MegaUpload Premium
		order=url
		subtitle=s4u,allSubs,podnapisiTV
		prop=concat_name=rear,name_separator= ,name_index=3+2
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
					matcher='go\(([0-9]+)\)'>((Source #[0-9]+|PART [0-9]+))
					order=url,name
					macro=mediaMacro
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
					matcher='go\(([0-9]+)\)'>((Source #[0-9]+|PART [0-9]+))
					order=url,name
					macro=mediaMacro
				}
		}
	}
}

channel IceFilms {
	img=http://img.icefilms.info/logo.png
	subscript=iceSubs
	hdr=Referer=http:/www.icefilms.info/index
  login {
		# Login data
		url=http://www.megaupload.com/?c=
		user=username
		passwd=password
		params=login=1&redir=1
		type=cookie
		associate=meagvideo.com,megaporn.com,megalive.com
	}
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
