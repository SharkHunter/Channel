version=0.1
channel YouTube Movies {
	img=http://s.ytimg.com/yt/img/pixel-vfl3z5WfW.gif
	folder {
		# Top page
		url=http://www.youtube.com/movies/
		type=empty
		folder {
			# Categories
			#<span class="movie-genre"><a href="/movies/action_adventure">Action och äventyr</a></span>
			matcher=<li>[^<]*<a href=\"(/movies[^\"]+)\">([^<]+)</a>[^<]*</li>
			order=url,name
			url=http://www.youtube.com/
			media {
				# Movie list
				# <div class="movie-short-title"> <a href="/watch?v=fJIsrb_6uiA" class=" yt-uix-hovercard-target" rel="nofollow" title="The Legend of Marilyn Monroe">The Legend of Marilyn Monroe</a> 
				matcher=<div class=\"movie-short-title\">[^<]*<a href=\"(/watch[^\"]+)\" .*? title=\"([^\"]+)\">
				order=url,name
				prop=prepend_url=http://www.youtube.com,
			}
	 }
  }
}
