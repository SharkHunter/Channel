version=0.11
channel URPlay {
	img=http://urplay.se/images/URPlayLogga.jpg
	folder {
		name=A-Z
		type=ATZ
		url=http://urplay.se/series
		folder {
			 matcher=<a href=\"([^\"]+)\" id=[^>]+>([^<]+)</a>
			 order=url,name
			 url=http://urplay.se
			 folder {
				# Regular items
				url=http://urplay.se
		  		matcher=a href=\"([^\"]+)\" [^>]+>\s*<span><img src=\"([^\"]+)\" .*/></span>\s*.*class="(tv|radio)"></span>([^<]+)</span>
				order=url,thumb,name+
				prop=name_separator= - 
				media {
					# Radio media
					matcher=file=(.*mp3)&.*streamer=([^&]+)&
					order=playpath,url
					prop=only_first,prepend_playpath=mp3:
				}
				media {
					# Tv media
					matcher=file=(.*mp4)&.*streamer=([^&]+)&
					order=playpath,url
					prop=only_first,prepend_playpath=mp4:
				}
			}		
		}
	}
}
