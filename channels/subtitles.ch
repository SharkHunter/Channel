version=0.52

###########################
## s4u
###########################

scriptdef s4uName {
   url='/all
   if serie
      url='/serie
   elseif movie
      url='/movie
   endif
   if imdb
	concat url '/imdb/
	concat url imdb
   elseif release
	   concat url '/rls/
	   escape s_url
	   concat url s_url
	elseif fname
		concat url '/fname/
		escape s_url
		concat url s_url
	else
	   concat url '/title/
	   escape s_url
	   concat url s_url
   endif
   if release=='
	concat url '/year=
	concat url year
	concat url '&season=
	concat url season
	concat url '&episode=
	concat url episode
   endif
   play
}

subdef s4u {
   # http://s4u.se/?film=Airplane!
   #Http://api.s4u.se/ Version / ApiKey / xml | json | serialize / movie | serie | all / imdb | tmdb | tvdb | title | rls | fname / SearchString / 
   url=http://api.s4u.se/Beta/LL3ift66Sddwo1e/xml/
   # <div class="DL_Box"> <a href="dl.php?cat=film&amp;dl=9062">
   matcher=<download_zip>([^<]+)</download_zip>
   best_match=1
   name_script=s4uName
   lang=swe
   prop=zip_force
}

###################################
## AllSubs
###################################

scriptdef allSubsName {   
   url=s_url
   escape url
   if season
	concat url '+
	concat url season
   elsif year
    concat url '+
	concat url year
   endif
   concat url '&language=
   concat url lang
   concat url '&limit=100
   play
}

subdef allSubs {
   #http://api.allsubs.org/index.php?search=heroes+season+4&language=en&limit=3
   url=http://api.allsubs.org/index.php?search=
   matcher=download"><a\shref="([^\"]+)"
   best_match=1
   name_script=allSubsName
#   script=allSubsMatcher
   lang=all
   prop=zip_force,iso2

}

###################################
## PodnapisiMovies
###################################

scriptdef podnapisinameMov {
   #lang ='28
   podurl='http://www.podnapisi.net
   rurl = s_url
   escape rurl
   res = 'http://www.podnapisi.net/en/ppodnapisi/search?tbsl=2&asdp=0&sK=
   concat res rurl
   concat res '&sM=&sJ=
   concat res lang
   concat res '&sY=
   concat res year
   concat res '&sTS=
   concat res '&sTE=
   concat res '&sS=downloads&sO=desc
   s_url = res
   regex ='<tbody>[^\*]+?<a\shref="([^\"]+)"
   scrape
   rurl = v1
   res2 = podurl
   concat res2 rurl
   s_url = res2
   regex ='podnapis_tabele_download[^\*]+?href="([^\"]+)"
   rurl = v1
   concat podurl rurl
   url = podurl
   play
}

subdef podnapisiMovie {   
    #matcher=podnapis_tabele_download[^\*]+?href="([^\"]+)"
   order=url
   best_match=1
   name_script=podnapisinameMov
   lang=all
   prop=zip_force,lang_stash=podnapisis
   script=podnapisis

}

###################################
## PodnapisiTV
###################################

scriptdef podnapisinameTV {
   #lang ='28
   podurl='http://www.podnapisi.net
   rurl = s_url
   escape rurl
   #regex='0?(\d+)
   #match season
   #season = v1
   #regex='0?(\d+)
   #match episode
   #episode = v1
   res = 'http://www.podnapisi.net/en/ppodnapisi/search?
   concat res 'tbsl=3&asdp=0&sK=
   concat res rurl
   concat res '&sM=&sJ=
   concat res lang
   if year
	concat res '&sY=
	concat res year
   endif
   if season
	concat res '&sTS=
	concat res season
   endif
   if episode
	concat res '&sTE=
	concat res episode
   endif
   concat res '&sS=downloads&sO=desc
   s_url = res
   regex ='<tbody>[^\*]+?<a\shref="([^\"]+)"
   scrape
   rurl = v1
   res2 = podurl
   concat res2 rurl
   s_url = res2
   regex ='podnapis_tabele_download[^\*]+?href="([^\"]+)"
   rurl = v1
   concat podurl rurl
   url = podurl
   play
}

subdef podnapisiTV {

   
   order=url
   best_match=1
   name_script=podnapisinameTV
   lang=all
   prop=zip_force,lang_stash=podnapisis
   script=podnapisis
      

}

scriptdef podnapisis {
   regex='podnapis_tabele_download[^\*]+?href="([^\"]+)"
   match s_url
   url='http://www.podnapisi.net
   concat url v1
   play
}

stash podnapisis {
	alb,29
	ara,12
	bel,50
	bos,10
	pob,48
	bul,33
	cat,53
	chi,17
	hrv,38
	cze,7
	dan,24
	dut,23
	eng,2
	est,20
	fao,52
	fin,31
	fre,8
	ger,5
	heb,22
	hin,42
	hun,15
	ice,6
	ind,54
	gle,49
	ita,9
	jpn,11
	kor,4
	lav,21
	lit,19
	mac,35
	may,55
	mdr,40
	nor,3
	pol,26
	por,32
	rum,13
	rus,27
	srp,36
	slo,37
	slv,1
	spa,28
	swe,25
	tha,44
	tur,30
	ukr,46
	vie,51
}

## podnapisi language selection - replace the number in the lang = field
########################################################################
#Argentino   14
#Serb.(Cyrillic)47

#####################################################
## Subscene
#####################################################

scriptdef subscene_matcher {
	regex='<span [^\>]+>/s*English/s*</span>/s*<span id="r([^\"]+)"
	match s_url
	id=v1
	#<small>Download problems?<a href='/downloadissue.aspx?subtitleId=525340&contentType=zip'>Click here</a>
	s_url='http://subscene.com/downloadissue.aspx?subtitleId=
	concat s_url id
	concat s_url '&contentType=zip
	#	<a id="s_lc_bcr_downloadlink" href="/Downloads/Temporary/Subtitles/tinker-tailor-soldier-spy-2011-dvdrip-xvid-bhrg_3323781.zip">
	regex='id="s_lc_bcr_downloadlink" href="([^\"]+)"
	scrape
	url='http://subscene.com
	concat url v1
	play
}

scriptdef subsceneName {
	if release
		url='s.aspx?q=
	else
		url='filmsearch.aspx?q=
	endif
	escape s_url
	concat url s_url
	play
}

subdef subscene {
   url=http://subscene.com/
   # <a class="a1" href="/english/Tinker-Tailor-Solider-Spy/subtitle-525340.aspx" title="Subtitle - Tinker Tailor Solider Spy - English"><span class="r0" >English</span>   
   name_script=subsceneName
   script=subscene_matcher
   lang=eng
   prop=zip_force
}

##################################
## SweSub
##################################

scriptdef swesubName {
	if imdb
	  url='/title/
	  concat url imdb
	else
		rurl='http://swesub.nu/?s=
		escape s_url
		concat rurl s_url
		if year
		  yurl='+(
		  concat yurl year
		  concat yurl ')
		  concatl s_url yurl
		endif
		s_url=rurl
		#a href="/title/tt1478338/"
		regex='a href="(/title[^\"]+)"
		scrape
		url=v1
	endif
	play
}

scriptdef swesubMatch {
	#<a href="/download/26483/" rel="nofollow">Boardwalk.Empire.S01E01.720p.HDTV.x264-IMMERSE (1 cd)</a>
	regex='a href="([^\"]+)" [^>]+>
	concat regex full_url
	match s_url
	s_url='http://swesub.nu/
	concat s_url v1
	s_action='geturl
	scrape
	url=v1
	play
}

subdef swesub {
	url=http://swesub.nu/
	name_script=swesubName
	lang=swe
	script=swesubMatch
}