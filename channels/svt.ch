version=0.58

scriptdef svtFilter{
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

channel SVTPlay {
	img=http://www.svtplay.se/public/2012.53/images/svt-play.png
	var {
		disp_name=HLS Only
		var_name=hls_only
		default=true
		type=bool
		action=null
	}
	folder {
		name=Live
		url=http://www.svtplay.se/?live=1
		folder {
			matcher=a href=\"(/live[^\"]+)\" class=\"svtXColorWhite [^\"]+\">.*?<img  class=\"svtMediaBlockFig-L playBroadcastThumbnail\" alt=\"([^\"]+)\" src=\"([^\"]+)\"/>
			order=url,name,thumb
			prop=matcher_dotall
			url=http://www.svtplay.se/
			folder {
				type=empty
				matcher=data-json-href=\"([^\"]+)\"
				url=http://www.svtplay.se/
				prop=append_url=?output=json
				action_name=crawl
				media {
					matcher=\"url\":\"(rtmp[^\"]+)\",\"bitrate\":([^,]+),
					order=url,name
					prop=live,append_name=Kbps,name_separator=###0,url_mangle=svtFilter
					put=swfVfy=http://www.svtplay.se/public/swf/video/svtplayer-2012.34.swf
				}
				folder {
					matcher=\"url\":\"(http[^\"]+)\",\"bitrate\":\d+,\"playerType\":\"ios\"
					order=url
					type=empty
					prop=no_case,discard_duplicates
					media {
						matcher=BANDWIDTH=(\d+)\d###lcbr###3###rcbr###+.*?\n([^\n]+)
						order=name,url
						prop=matcher_dotall,append_name=Kbps,relative_url=path,name_separator=###0
					}
				}
			}
		}
	}
	folder {
		name=A-Z
		type=ATZ
		url=http://www.svtplay.se/program
		folder {
			 matcher=<li class=\"playListItem\"><a href=\"([^\"]+)\" class=\"playLetterLink\">([^<]+)</a></li>
			 order=url,name
			 url=http://www.svtplay.se/
			 prop=monitor
			 folder {
				url=http://www.svtplay.se/
		  		matcher=<a href=\"([^\"]+)\" class=\"playLink playFloatLeft playBox-Padded"\>.*?<img class=\"playGridThumbnail\" alt=\"([^\"]+)\" src=\"([^\"]+)\"/>
				order=url,name,thumb
				action_name=crawl
				prop=matcher_dotall,monitor,crawl_mode=FLA+HML
				folder {
					type=empty
					matcher=data-json-href=\"([^\"]+)\"
					url=http://www.svtplay.se/
					prop=append_url=?output=json,monitor
					media {
						matcher=\"url\":\"(rtmp[^\"]+)\",\"bitrate\":([^,]+),
						order=url,name
						prop=append_name=Kbps,name_separator=###0,url_mangle=svtFilter
						put=swfVfy=http://www.svtplay.se/public/swf/video/svtplayer-2012.34.swf
					}
					folder {
						matcher=\"url\":\"(http[^\"]+)\",\"bitrate\":\d+,\"playerType\":\"ios\"
						order=url
						type=empty
						prop=no_case
						media {
							matcher=BANDWIDTH=(\d+)\d###lcbr###3###rcbr###+.*?\n([^\n]+)
							order=name,url
							prop=matcher_dotall,append_name=Kbps,relative_url=path,name_separator=###0,name_index=1
						}
					}
				}
			}
		}
	}
}
			