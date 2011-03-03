version = 0.24
macrodef ytMedia {
	media {
                # Movie list
                # <a href="/watch?v=E_g--mIZnnI" class="ux-thumb-wrap contains-addto">
                # <span class="video-thumb ux-thumb-126 yt-uix-hovercard-target poster-art">
                # <span class="clip">
                # <img src="http://i2.ytimg.com/vi/E_g--mIZnnI/movieposter.jpg?v=9bb1ee" onload="" title="Loved"
				#<a href="/watch?v=EbgmTizq-BE" class="ux-thumb-wrap contains-addto"><span class="video-thumb ux-thumb-128 "><span class="clip"><img src="//i2.ytimg.com/vi/EbgmTizq-BE/default.jpg" alt="Miniatyr" class="" onclick="playnav.playVideo('uploads','0','EbgmTizq-BE');return false;" title="110227bjorn" ></span></span><span class="video-time">2:01</span><span dir="ltr" class="yt-uix-button-group addto-container short video-actions" data-video-ids="EbgmTizq-BE" data-feature="thumbnail"><button type="button" class="start master-sprite  yt-uix-button yt-uix-button-short yt-uix-tooltip" onclick=";return false;" title="" data-button-action="yt.www.addtomenu.add" role="button" aria-pressed="false"><img class="yt-uix-button-icon yt-uix-button-icon-addto" src="//s.ytimg.com/yt/img/pixel-vfl3z5WfW.gif" alt=""><span class="yt-uix-button-content"><span class="addto-label">Lägg till i</span></span></button><button type="button" class="end  yt-uix-button yt-uix-button-short yt-uix-tooltip yt-uix-button-empty" onclick=";return false;" title="" data-button-menu-id="shared-addto-menu" data-button-action="" role="button" aria-pressed="false"><img class="yt-uix-button-arrow" src="//s.ytimg.com/yt/img/pixel-vfl3z5WfW.gif" alt=""></button></span><span class="video-in-quicklist">Har lagts till i kön     </span></a>
                matcher = <a\s+href=\"([^\"]+)\"\s+class=\"ux-thumb-wrap.*?<img\s+src=\"([^\"]+)\"[^>]+?\s+title=\"([^\"]+)\"
                order = url,thumb,name
                prop = prepend_url=http://www.youtube.com,prepend_thumb=http:,
            }
			
			folder {
				# Next folder
				#<a href="?p=2" class="yt-uix-pager-link" data-page="2" >Nästa</a>
				matcher=<a href=\"([^\"]+)\" class=\"yt-uix-pager-link\" data-page=\"[^\"]+\" >([^\<]+)</a>
				order=url,name
				#url=http://www.youtube.com/
				prop=prepend_parenturl
				type=recurse
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
	folder {
				# Next folder
				#<a href="?p=2" class="yt-uix-pager-link" data-page="2" >Nästa</a>
				matcher=<a href=\"([^\"]+)\" class=\"yt-uix-pager-link\" data-page=\"[^\"]+\" >([^\<]+)</a>
				order=url,name
				#url=http://www.youtube.com/
				prop=prepend_parenturl
				type=recurse
			}
}

macrodef ytMedia2 {
	media {
                # Movie list
                # <a href="/watch?v=E_g--mIZnnI" class="ux-thumb-wrap contains-addto">
                # <span class="video-thumb ux-thumb-126 yt-uix-hovercard-target poster-art">
                # <span class="clip">
                # <img src="http://i2.ytimg.com/vi/E_g--mIZnnI/movieposter.jpg?v=9bb1ee" onload="" title="Loved"
				#<a href="/watch?v=EbgmTizq-BE" class="ux-thumb-wrap contains-addto"><span class="video-thumb ux-thumb-128 "><span class="clip"><img src="//i2.ytimg.com/vi/EbgmTizq-BE/default.jpg" alt="Miniatyr" class="" onclick="playnav.playVideo('uploads','0','EbgmTizq-BE');return false;" title="110227bjorn" ></span></span><span class="video-time">2:01</span><span dir="ltr" class="yt-uix-button-group addto-container short video-actions" data-video-ids="EbgmTizq-BE" data-feature="thumbnail"><button type="button" class="start master-sprite  yt-uix-button yt-uix-button-short yt-uix-tooltip" onclick=";return false;" title="" data-button-action="yt.www.addtomenu.add" role="button" aria-pressed="false"><img class="yt-uix-button-icon yt-uix-button-icon-addto" src="//s.ytimg.com/yt/img/pixel-vfl3z5WfW.gif" alt=""><span class="yt-uix-button-content"><span class="addto-label">Lägg till i</span></span></button><button type="button" class="end  yt-uix-button yt-uix-button-short yt-uix-tooltip yt-uix-button-empty" onclick=";return false;" title="" data-button-menu-id="shared-addto-menu" data-button-action="" role="button" aria-pressed="false"><img class="yt-uix-button-arrow" src="//s.ytimg.com/yt/img/pixel-vfl3z5WfW.gif" alt=""></button></span><span class="video-in-quicklist">Har lagts till i kön     </span></a>
                matcher = <a\s+href=\"([^\"]+)\"\s+class=\"ux-thumb-wrap.*?<img\s+src=\"([^\"]+)\"[^>]+?\s+title=\"([^\"]+)\"
                order = url,thumb,name
                prop = prepend_url=http://www.youtube.com,
            }
			folder {
				# Next folder
				#<a href="?p=2" class="yt-uix-pager-link" data-page="2" >Nästa</a>
				matcher=<a href=\"([^\"]+)\" class=\"yt-uix-pager-link\" data-page=\"[^\"]+\" >([^\<]+)</a>
				order=url,name
				#url=http://www.youtube.com/
				prop=prepend_parenturl
				type=recurse
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

macrodef ytSubCategory1 {
	folder {
            # Categories
            # <span class="movie-genre"><a href="/movies/action_adventure">Action och  ventyr</a></span>
            matcher = <li>[^<]*<a href=\"([^\"]+)\">([^<]+)</a>[^<]*</li>
            order = url,name
            url = http://www.youtube.com/
			macro=ytMedia2
        }
}


channel YouTube {
    img = http://www.engr.uky.edu/solarcar/sites/default/files/YouTube_icon.png
	folder {
		name=Favorites
		folder {
			name=Bara Bajen
			url=http://www.youtube.com/user/BaraBajenTV
			macro=ytMedia
		}
	}
    folder {
		name=Categories
		url=http://www.youtube.com/videos/
		folder {
			matcher = <li>[^<]*<a href=\"([^\"]+)\">([^<]+)</a>[^<]*</li>
            order = url,name
            url = http://www.youtube.com/
			macro=ytMedia
			macro=ytMedia1
			macro=ytSubCategory
	    }
    }		
    folder {
        # Top page
        url = http://www.youtube.com/movies/
		name=Movies
        macro=ytSubCategory1
    }
    
}