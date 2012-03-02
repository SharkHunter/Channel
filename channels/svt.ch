version=0.35
channel SVTPlay {
	img=http://svtplay.se/img/brand/svt-play.png
	var {
		disp_name=Bitrate 1
		var_name=br1
		values=2400,1400,850,320
		suffix=Kbps
	}
	var {
		disp_name=Bitrate 2
		var_name=br2
		values=850,1400,320
		suffix=Kbps
	}
	folder {
		name=A-Z
		type=ATZ
		url=http://svtplay.se/alfabetisk
		folder {
			 matcher=<a href=\"(/t/[^\"]+)\">([^<]+)</a>
			 order=url,name
			 url=http://svtplay.se
			 item {
				# Regular items
				url=http://svtplay.se
		  		matcher=<a href=\"(/v/[^\?]+)[^\"]+\" title=\"[^\"]*\" [^>]+>[^<]*<img class=\"thumbnail\" src=\"([^\"]+)\"[^>]+>[^>]+>([^<]+)</span>
				order=url,thumb,name
		  		prop=auto_media
				media {
					matcher=url:(rtmp[^,]+),bitrate:[@#br1@#|@#br2@#][^&]+&[^&]+&[^;]+;subtitle=([^&]*)&
					order=url,subs
					prop=only_first,
				}
			}
			item {
				# Live items
				url=http://svtplay.se
		  		matcher=<a href=\"(/v/[^\?]+)[^\"]+\" title=\"[^\"]*\" [^>]+>[^<]*<img class=\"icon-live\" src=\"([^\"]+)\" .*? alt=\"(Live)\" [^>]+>
				order=url,thumb,name
		  		prop=auto_media
				media {
					matcher=url:(rtmp[^,]+),bitrate:(@#br1@#|@#br2@#)[^&]+&[^&]+&[^;]+;subtitle=([^&]*)&
					order=url,subs
					prop=only_first,
				}
			}
			folder {
					name=>>>
					#<li class="next "><a href="?cb,,1,f,-1/pb,a1364150,1,f,-1/pl,v,,1669414/sb,p108524,2,f,-1" class="internal "><img src="/img/button/pagination-next.gif"
					matcher=<li class=\"next \"><a href=\"([^\"]+)\" class=\"internal \"><img src=\"/img/button/pagination-next.gif\"
					order=url
					type=recurse
					prop=prepend_parenturl,ignore_match,only_first,continue_limit=6
			}
		}
	}
}
