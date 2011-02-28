version = 0.2
channel YouTube Movies {
    img = http://s.ytimg.com/yt/img/pixel-vfl3z5WfW.gif
    folder {
        # Top page
        url = http://www.youtube.com/movies/
        type = empty
        folder {
            # Categories
            # <span class="movie-genre"><a href="/movies/action_adventure">Action och äventyr</a></span>
            matcher = <li>[^<]*<a href=\"(/movies[^\"]+)\">([^<]+)</a>[^<]*</li>
            order = url,name
            url = http://www.youtube.com/
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
    }
}
