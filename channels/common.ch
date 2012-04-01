version=0.2

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
	url=s_url
	regex='(movshare)
	match url
	if v1 
		call 'http://www.navixtreme.com/movshare
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
	  call 'http://www.navixtreme.com/vidxden
	  url=v1
	  play
	endif
	regex='(megavideo)
	match url
	if v1
	  call 'http://www.navixtreme.com/proc/megavideo
	  url=v1
	  play
	endif
	regex='(videobb)
	match url
	if v1
	  call 'http://www.navixtreme.com/proc/videobb
	  url=v1
	  play
	endif
	regex='(putlocker)
	match url
	if v1
		call 'putlocker
		url=v1
		play
	endif
	regex='(sockshare)
	match url
	if v1
		call 'http://www.navixtreme.com/sproc/sockshare
		url=v1
		play
	endif
	play
}

scriptdef lockerScriptScrape {
	s_action='geturl
	scrape
	url=v1
	call 'lockerScript
	url=v1
	play
}

################################
## Putlocker script copied
## form navix but it needed
## to be updated with the 
## 5 sec sleep there
#################################

scriptdef putlocker {
url_ori=s_url

# Retrieve video id, hash, and PHPSESSID values
regex='([^#/]+)#?$
match s_url
vidid=v1
regex='value="([^"]+)" name="hash"
scrape
hash=v1

regex='(/\?404)$
match geturl
if v1
	# report removed file
	s_url='http://www.navixtreme.comn/proc_check/putlocker_report.cgi?url=
	escape url_ori
	concat s_url url_ori
	scrape
	error 'This file does not exist on the server
endif

#Check for a hash
if hash='
	error 'Missing Hash for File Access, Most Likely Link Does Not Exist
	v1='
else

#Cookie Setup
cookieval='usender=
concat cookieval cookies.usender
concat cookieval '; ad_cookie1=1; PHPSESSID=
concat cookieval cookies.PHPSESSID

# Sleep some seconds
sleep '7000

# "Click" the "Continue as Free User" button
s_cookie=cookieval
s_referer=url_ori
s_method='post
s_postdata='confirm=Continue%20as%20Free%20User&hash=
concat s_postdata hash
scrape

# Get the Stream ID
s_cookie=cookieval
s_referer=url_ori
s_url=url_ori
s_method='get
regex='get_file\.php\?stream=([^']+)
scrape
streamid=v1

# Pull final media URL from XML
s_method='get
s_cookie=cookieval
s_referer=url_ori
s_url='http://www.putlocker.com/get_file.php?stream=
concat s_url streamid
regex='url="([^"]+)
scrape
endif
if v1
	url=v1
	play
endif

error 'Could not retrieve source
}