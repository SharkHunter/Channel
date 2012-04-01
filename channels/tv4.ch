version=0.22
channel TV4 {
	img=http://cdn01.tv4.se/polopoly_fs/2.740!logoImage/807786835.png
	var {
		disp_name=Bitrate
		var_name=br
		values=15,25,8,3
		suffix=00 Kbps
	}
	folder {
		type=ATZ
		name=A-Z
		url=http://www.tv4play.se/alla_program
		folder {
			# Programs
			#matcher=<img alt=\"[^\"]+\" src=\"([^\"]+)\"[^>]*>.*?<h3[^>]*><a href=\"([^\"]+)\" title=\"([^\"]+)\">
			# <h3 class="video-title"><a href="/dokumentarer/112_-_luftens_hjaltar">112 - luftens hjältar</a></h3>
			matcher=<h3 class=\"video-title\">[^<]*<a href=\"([^\"]+)\">([^<]+)</a>
		    #order=thumb,url,name
			order=url,name
			url=http://www.tv4play.se/
			folder {
				# Episodes
				#<a href="/dokumentarer/akutmottagningen?title=akutmottagningen_del_15&amp;videoid=1142950"><img alt="Akutmottagningen del 15" src="http://cdn01.tv4.se/polopoly_fs/1.1916211!picture/440637053.jpg_gen/derivatives/w180/440637053.jpg" /></a>
				matcher=<a href=\"([^\"]+)\"><img alt=\"([^\"]+)\" src=\"([^\"]+)\" />
				order=url,name,thumb
				#url=http://www.tv4play.se/
				#type=empty
				item {
					# Fetch the smil
					#matcher=<a href=\"[^&]+&amp;videoid=([^\"]+)\"
					matcher=<a href=\"[^;]+;videoid=([^\"]+)\"
					#matcher=<link href=\".*videoid=([^\"]+)\" rel
					url=http://anytime.tv4.se/webtv/metafileFlash.smil?p=
					prop=only_first,auto_media,append_url=&bw=1800&emulate=true&sl=true&
					order=url
					media {
						#matcher=<meta base=\"(rtmp[^\"]+)\" /></head><body><switch><video src=\"(mp4:[^\?]+)\?
						#matcher=<meta base=\"(rtmp[^\"]+)\" />[^\"]+\"(mp4.*MP415[^\?]+)\?
						matcher=<meta base=\"(rtmp[^\"]+)\" />[^\"]+\"(mp4:.*MP4@#br@#[^\?]+)\?
						#order=url,url
						order=url,playpath
						put=swfVfy=http://www.tv4play.se/flash/tv4playflashlets.swf
					}
				}
			}
		}
	}
}
