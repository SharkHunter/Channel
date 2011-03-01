version = 0.22
macrodef ytMedia {
	media {
                # Movie list
                # <a href="/watch?v=E_g--mIZnnI" class="ux-thumb-wrap contains-addto">
                # <span class="video-thumb ux-thumb-126 yt-uix-hovercard-target poster-art">
                # <span class="clip">
                # <img src="http://i2.ytimg.com/vi/E_g--mIZnnI/movieposter.jpg?v=9bb1ee" onload="" title="Loved"
                matcher = <a\s+href=\"([^\"]+)\"\s+class=\"ux-thumb-wrap.*?<img\s+src=\"([^\"]+)\"[^>]+?\s+title=\"([^\"]+)\"
                order = url,thumb,name
                prop = prepend_url=http://www.youtube.com,
            }


}

macrodef ytMedia1 {
	media {
                # Movie list
                # <a href="/watch?v=E_g--mIZnnI" class="ux-thumb-wrap contains-addto">
                # <span class="video-thumb ux-thumb-126 yt-uix-hovercard-target poster-art">
                # <span class="clip">
                # <img src="http://i2.ytimg.com/vi/E_g--mIZnnI/movieposter.jpg?v=9bb1ee" onload="" title="Loved"
                #matcher = <a href=\"([^\"]+)\" class=\"ux-thumb-wrap.*?<img onload=\"\" title=\"([^\"]+)\" .*? src=\"([^\"]+)\"
				matcher = <a\s+href=\"([^\"]+)\"\s+class=\"ux-thumb-wrap.*?<img .*? title=\"([^\"]+)\" .*? src=\"([^\"]+)\" 
                order = url,name,thumb
                prop = prepend_url=http://www.youtube.com,prepend_thumb=http:,
			}
}

macrodef ytSubCategory {
	folder {
            # Categories
            # <span class="movie-genre"><a href="/movies/action_adventure">Action och  ventyr</a></span>
            matcher = <li>[^<]*<a href=\"([^\"]+)\">([^<]+)</a>[^<]*</li>
            order = url,name
            url = http://www.youtube.com/
			macro=ytMedia
        }
}

channel YouTube {
    img = http://www.engr.uky.edu/solarcar/sites/default/files/YouTube_icon.png
    folder {
	url=http://www.youtube.com/videos/
	type=empty
	folder {
		matcher = <li>[^<]*<a href=\"([^\"]+)\">([^<]+)</a>[^<]*</li>
            order = url,name
            url = http://www.youtube.com/
			macro=ytMedia1
			macro=ytSubCategory
	    }
    }		
    folder {
        # Top page
        url = http://www.youtube.com/movies/
		name=Movies
        #type = empty
        macro=ytSubCategory
    }
    
}