version=0.50

scriptdef viaFilter{
	url=s_url
	if @#hls_only@# !='true
		regex='(rtmp.*://)
		match s_url
		if v1
		  url='
		endif
	endif
	play
}

macrodef via_media {
	media {
		matcher=<Url>(rtmp.*)</Url>
		prop=url_mangle=viaFilter
		put=swfVfy=http://flvplayer.viastream.viasat.tv/play/swf/player120328.swf
	}
}

macrodef via_rtmp {
	folder {
		matcher=<SamiFile>(.*?)</SamiFile>.*?<Url>.*?<Url>(.*?extraN.*?)</Url>
		type=empty
		order=subs,url
		prop=matcher_dotall,append_name=- Subs,name_separator=###0
		macro=via_media
	}
	folder {
		matcher=<Url>(.*?extraN.*?)</Url>
		type=empty
		order=url
		macro=via_media
	}
	macro=via_media
}

scriptdef viaMangle {
	regex='\\/
	replace s_url '/
	url=s_url
	play
}

macrodef viaHLS {
	folder {
		matcher=\"(.*?m3u8)\"
		type=empty
		order=url
		prop=url_mangle=viaMangle
		media {
			matcher=BANDWIDTH=(\d+)\d###lcbr###3###rcbr###+.*?\n([^\n]+)
			order=name,url
			prop=matcher_dotall,append_name=Kbps,relative_url=path,name_separator=###0
		}
	}
	media {
		matcher=\"(.*?mp4)\"
		order=url
		prop=url_mangle=viaMangle
	}
}

channel TV3 {
	img=http://www.mynewsdesk.com/se/view/Image/download/resource_image/95624
	var {
		disp_name=HLS Only
		var_name=hls_only
		default=true
		type=bool
		action=null
	}
	sub_conv {
		matcher=<Subtitle .*?TimeIn=\"([^\"]+)\" TimeOut=\"([^\"]+)\"[^>]+>(.*?)</Subtitle>
		order=start,stop,text_embed
		prop=matcher_dotall,text_separator=###n
		emb_matcher_text=<Text [^>]+>(.*?)</Text>
	}
	folder {
		name=A-Z
		type=ATZ
		url=http://www.tv3play.se/program
		folder {
			# Programs
			#<a href="http://www.tv3play.se/program/101-satt-att-aka-ur-en-gameshow" class="list-item" data-channel-id="1209">101 sätt att åka ur en gameshow</a><br/>
			matcher=a href=\"([^\"]+)\" class=\"list-item\"[^>]+>(.*)</a><br/>
			order=url,name
			prop=discard_duplicates
			folder {
				# Season
				#<li class="selector-item is-current" data-id="6302" data-title="Säsong 2"
				matcher=<li class=\"selector-item[^\"]*\" data-id=\"([^\"]+)\" data-title=\"([^\"]+)\"
				order=url,name
				prop=prepend_parenturl,prepend_url=/avsnitt?id=			
				folder {
					# Episode
					#<a href="http://www.tv6play.se/program/112-aina/325517?autostart=true">		
			#<div class="clip-image">
		#		<img class="lazyload" src="http://cdn.playstatic.mtgx.tv/static/ui/img/clip-small-placeholder.png" data-src="http://cdn.playapi.mtgx.tv/imagecache/230x150/seasons/6302/325517/5aaa4bd3ccc98d302dedaf9738dc32aa-3jpg.jpg"/>
		#	</div>
		#	<div class="clip-description-wrapper">
		#										<div class="clip-description">
		#			<h3 class="clip-title">Avsnitt 8</h3>
		#		</div>
					matcher=<a href=\".*?/program/[^/]+/([0-9]+)\?autostart=true\">.*?<div class=\"clip-image\">.*?<img class=\"lazyload\" src=\"[^\"]+\" data-src=\"([^\"]+)\"/>.*?<h3 class=\"clip-title\">([^<]+)</h3>
					order=url,thumb,name
					prop=matcher_dotall
					url=http://viastream.viasat.tv/MobileStream/
					macro=viaHLS
				}
			}
		}
	}
}


