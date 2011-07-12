version=1.0
scriptdef movshares {
	rurl=s_url
	regex='form (id=[^\*]+)" method="post
	scrape
	datatest=v1
	data2='id=watch&name=watch
	s_method='post
	s_postdata=data2
	s_url=rurl
	regex='src" value="([^\"]+)"
	scrape
	url=v1
	play
}


