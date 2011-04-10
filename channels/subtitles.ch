version=0.1	
scriptdef s4uName {
	url=s_url
	regex=' (\(\d+\)) 
	match s_url
	year=v1
	replace url '
	s_url=url
	regex='(\d+)x(\d+) 
	match s_url
	res='/year=
	concat res year
	concat res '&season=
	concat res v1
	concat res '&episode=
	concat res v2
	regex='(\d+x\d+) .*
	replace url '
	escape url
	concat url res
	play
}

subdef s4u {
	# http://s4u.se/?film=Airplane!
	#Http://api.s4u.se/ Version / ApiKey / xml | json | serialize / movie | serie | all / imdb | tmdb | tvdb | title | rls | fname / SearchString / 
	url=http://api.s4u.se/Beta/DemoKey/xml/all/title/
	# <div class="DL_Box"> <a href="dl.php?cat=film&amp;dl=9062">
	matcher=<download_file>([^<]+)</download_file>
	best_match=1
	name_script=s4uName
}