version=0.1
scriptdef veetleScript {
	#rurl='http://www.veetle.com/channelHostPort/
	rurl='http://www.veetle.com//index.php/channel/ajaxStreamLocation/
	regex='/index.php/channel/view/(.*)
	match s_url
	concat rurl v1
	concat rurl '/flash
	s_url=rurl
#	regex='ok.([^:]+:[^,]+),
	regex='"payload":"(.*)"
	scrape
#	url='http://
#	concat url v1
	url=v1
	regex='\\
	replace url '
	play
}