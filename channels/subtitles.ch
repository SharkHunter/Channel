version=0.1	
subdef s4u {
	# http://s4u.se/?film=Airplane!
	#Http://api.s4u.se/ Version / ApiKey / xml | json | serialize / movie | serie | all / imdb | tmdb | tvdb | title | rls | fname / SearchString / 
	url=http://api.s4u.se/Beta/DemoKey/xml/all/title/
	# <div class="DL_Box"> <a href="dl.php?cat=film&amp;dl=9062">
	matcher=<download_file>([^<]+)</download_file>
	mangle_name=([^\()]+)\(
	best_match=2
}