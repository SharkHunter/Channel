version=0.1
scriptdef cuevanascript {
	regex='http://www.cuevana.tv(/[^\/]+)(/[^\/]+)(/[^\*]+)
	match s_url
	mode=v1
	movcode=v2
	movname=v3
	regex='videoi"\sclass="hide">([^\;]+);
	scrape
	cuelink=v1
	s_url='http://www.cuevana.tv/player/source?	
	concat s_url cuelink
	regex='goSource\('([^\W]+)[^\*]+?megaupload
	scrape
	megacode=v1
	movdata='key=
	concat movdata megacode
	concat movdata '&host=megaupload&vars=&
	concat movdata cuelink
	s_method='post
	s_postdata=movdata
	s_url='http://www.cuevana.tv/player/source_get
	regex='(.+)
	scrape
	url=v1
	concat url mode
	concat url movcode
	concat url movname
	play
}