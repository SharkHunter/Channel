version=0.2

###########################
## s4u
###########################

scriptdef s4uName {
	url=s_url
	escape url
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
	url=http://api.s4u.se/Beta/DemoKey/xml/all/title/
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
   lang=swe,ger,eng,esp,fre,ice,cze,dan,nor,por,
   prop=zip_force,iso2

}
