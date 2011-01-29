version=0.1
channel SVTPlay {
	img=http://svtplay.se/img/brand/svt-play.png
	folder {
		name=A-Z
		type=ATZ
		url=http://svtplay.se/alfabetisk
		folder {
			 matcher=<a href=\"(/t/.*)\">(.*)</a>
			 order=url,name
			 url=http://svtplay.se
			  item {
				url=http://svtplay.se
		  		matcher=<a href=\"(/v/.*)\?.*\" title=\"(.*)\" .*?>[^<]*<img class=\"thumbnail\" src=\"([^\"]+)\" 
			  	order=url,name,thumb
		  		prop=auto_media
				media {
		  			matcher=url:(rtmp.*),bitrate:2400
				}
			}
		}
	}
}