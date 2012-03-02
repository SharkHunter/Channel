version=0.1

#############################################################
## This file contains some useful scripts used
## by variuos channels
##############################################################

#########################################################
## Simple trix script, just get the redirect 
## URL which probably is just the locker URL
##########################################################

scriptdef trixScript {
	s_action='geturl
	scrape
	url=v1
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

#################################################
## Script to analyze the various 
## cyberlockers around. Have a central one to 
## make changes hit all over
#################################################

scriptdef lockerScript {
	s_action='geturl
	scrape
	url=v1
	regex='(movshare)
	match url
	if v1 
		call 'http://navix.turner3d.net/proc/movshare
		url=v1
		play
	endif
	regex='(divxstage)
	match url
	if v1
		call 'http://justme4u2c.zymichost.com/DivxStage.php
		url=v1
		play
	endif
	regex='(novamov)
	match url
	if v1
	  call 'http://boseman22.dyndns-server.com/static/novamov
	  url=v1
	  play
	endif
	regex='(videoweed)
	match url
	if v1
	  call 'http://justme4u2c.zymichost.com/VideoWeed.es.php
	  url=v1
	  play
	endif
	regex='(vixden)
	match url
	if v1
	  call 'http://navix.turner3d.net/proc/vidxden
	  url=v1
	  play
	endif
	regex='(megavideo)
	match url
	if v1
	  call 'http://navix.turner3d.net/proc/megavideo
	  url=v1
	  play
	endif
	regex='(videobb)
	match url
	if v1
	  call 'http://navix.turner3d.net/proc/videobb
	  url=v1
	  play
	endif
	regex='(putlocker)
	match url
	if v1
		call 'http://navix.turner3d.net/sproc/putlocker
		url=v1
		play
	endif
	regex='(sockshare)
	match url
	if v1
		call 'http://navix.turner3d.net/sproc/sockshare
		url=v1
		play
	endif
	play
}