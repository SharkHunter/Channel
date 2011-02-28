version=0.14
channel TV4 {
	img=http://cdn01.tv4.se/polopoly_fs/2.740!logoImage/807786835.png
	folder {
		type=ATZ
		name=A-Z
		url=http://www.tv4play.se/a_till_o_lista
		folder {
			# Programs
#			 <a href="/dokumentarer/112_-_pa_liv_och_dod"><img alt="112 - på liv och död" src="http://cdn01.tv4.se/polopoly_fs/1.1854778.1295530253!image/3060485312.jpg" /></a>
 #             </p>
  #            <h3 class="video-title">
   #             <a href="/dokumentarer/112_-_pa_liv_och_dod" title="112 - på liv och död">112 - på liv och död</a>
			#matcher=<img alt=\"[^\"]+\" src=\"([^\"]+)\"[^>]*>.*?<h3[^>]*><a href=\"([^\"]+)\" title=\"([^\"]+)\">
			matcher=<h3 class=\"video-title\">[^<]*<a href=\"([^\"]+)\" title=\"([^\"]+)\">
		    #order=thumb,url,name
			order=url,name
			url=http://www.tv4play.se/
			folder {
				# Episodes
				#<a href="/dokumentarer/akutmottagningen?title=akutmottagningen_del_15&amp;videoid=1142950"><img alt="Akutmottagningen del 15" src="http://cdn01.tv4.se/polopoly_fs/1.1916211!picture/440637053.jpg_gen/derivatives/w180/440637053.jpg" /></a>
				matcher=<a href=\"([^\"]+)\"><img alt=\"([^\"]+)\" src=\"([^\"]+)\" />
				order=url,name,thumb
				url=http://www.tv4play.se/
				#type=empty
				item {
					# Fetch the smil
					#matcher=<a href=\"[^&]+&amp;videoid=([^\"]+)\"
					#matcher=videoid=([^\"]+)\"
					matcher=<link href=\".*videoid=([^\"]+)\" rel
					url=http://anytime.tv4.se/webtv/metafileFlash.smil?p=
					prop=auto_media,append_url=&bw=1800&emulate=true&sl=true&
					order=url
					media {
						#matcher=<meta base=\"(rtmp[^\"]+)\" /></head><body><switch><video src=\"(mp4:[^\?]+)\?
						matcher=<meta base=\"(rtmp[^\"]+)\" />[^\"]+\"(mp4.*MP415[^\?]+)\?
						#order=url,url
						order=url,playpath
#					prop=url_separator=!!!pms_ch_dash_y!!!,append_url=!!!pms_ch_dash_w!!!http://cdn01.tv4.se/polopoly_fs/1.1615597.1280745068!approot/tv4video.swf
						put=swfVfy=http://cdn01.tv4.se/polopoly_fs/1.1615597.1280745068!approot/tv4video.swf
					}
				}
			}
		}
	}
}
