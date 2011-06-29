version=0.4
###########################
## s4u
###########################

scriptdef s4uName {
   if imdb
	url='/imdb/
	concat url imdb
   else
    escape s_url
	url='/title/
	concat url s_url
   endif
   concat url '/year=
   concat url year
   concat url '&season=
   concat url season
   concat url '&episode=
   concat url episode
   play
}

subdef s4u {
   # http://s4u.se/?film=Airplane!
   #Http://api.s4u.se/ Version / ApiKey / xml | json | serialize / movie | serie | all / imdb | tmdb | tvdb | title | rls | fname / SearchString / 
   url=http://api.s4u.se/Beta/DemoKey/xml/all/
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
   rurl=s_url
   escape rurl
   concat rurl '+
   concat rurl lang
   concat rurl '/1
   url = rurl
   play
}

subdef allSubs {
   url=http://www.allsubs.org//search-subtitle/
   matcher=download"><a\shref="([^\"]+)"
   best_match=1
   name_script=allSubsName
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
   res = 'http://www.podnapisi.net/en/ppodnapisi/search?
   concat res 'tbsl=2&asdp=0&sK=
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
   regex='0?(\d+)
   match season
   season = v1
   regex='0?(\d+)
   match episode
   episode = v1
   res = 'http://www.podnapisi.net/en/ppodnapisi/search?
   concat res 'tbsl=3&asdp=0&sK=
   concat res rurl
   concat res '&sM=&sJ=
   concat res lang
   concat res '&sY=
   concat res year
   concat res '&sTS=
   concat res season
   concat res '&sTE=
   concat res episode
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


