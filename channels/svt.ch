version=0.76

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

macrodef svtHLSMedia {
	folder {
		matcher=\"url\":\"(http[^\"]+)\",\"bitrate\":\d+,\"playerType\":\"ios\"
		order=url
		type=empty
		prop=no_case,discard_duplicates
		media {
			matcher=BANDWIDTH=(\d+)\d###lcbr###3###rcbr###+.*?\n([^\n]+)
			order=name,url
			prop=matcher_dotall,append_name=Kbps,relative_url=path,name_separator=###0,name_index=0
		}
	}
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
		url=http://www.svtplay.se/kanaler
		type=empty
		folder {
			#data-jsonhref="/kanaler/svt1" data-channel="svt1" title="SVT1 |
			matcher=data-thumbnail=\"([^\"]+)\" data-jsonhref=\"(/kanaler/[^\"]+)\" data-channel=\"[^\"]+\" title=\"([^\|]+) \|
			order=thumb,url,name
			url=http://www.svtplay.se/
			prop=append_url=?output=json
			macro=svtHLSMedia
		}
	}
	folder {
		name=Live
		url=http://www.svtplay.se/?live=1
		folder {
			matcher=a href=\"(/video[^\"]+)\" class=\"[^\"]+\">.*?<img *class=\"svtMediaBlockFig-L playBroadcastThumbnail\" alt=\"([^\"]+)\" src=\"([^\"]+)\"
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
				macro=svtHLSMedia
			}
		}
	}
	folder {
		name=Öppet Arkiv
		url=http://www.oppetarkiv.se/kategori/titel
		type=ATZ
		folder {
			#<li class="svtoa-anchor-list-item"><a class="svt-text-default" href="http://www.oppetarkiv.se/etikett/titel/Vilse%20i%20pannkakan/">Vilse i pannkakan</a></li>
			matcher=<li class=\"svtoa-anchor-list-item\"><a class=\"svt-text-default\" href=\"([^\"]+)\">([^<]+)</a>
			order=url,name
			folder {
				matcher=data-imagename=\"([^\"]+)\" alt=\"([^\"]+)\".*?class=\"svtLink-Discreet-THEMED svtJsLoadHref\" href=\"([^\"]+)\"
				order=thumb,name,url
				prop=matcher_dotall
				folder {
					type=empty
					matcher=data-json-href=\"([^\"]+)\"
					url=http://www.oppetarkiv.se/
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
							prop=matcher_dotall,append_name=Kbps,relative_url=path,name_separator=###0,name_index=0
						}
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
			 matcher=<a href=\"([^\"]+)\" class=\"playAlphabeticLetterLink\".*?>([^<]+)</a>
			 order=url,name
			 url=http://www.svtplay.se/
			 prop=matcher_dotall,monitor
			 folder {
				url=http://www.svtplay.se/
		  		matcher=data-title=\"([^\"]+)\".*?<a href=\"([^\"]+)\" class=\"playLink [^\"]+\">.*?<img class=\"playGridThumbnail\" alt=\"[^\"]*\" src=\"([^\"]+)\"
				order=name,url,thumb
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
							prop=matcher_dotall,append_name=Kbps,relative_url=path,name_separator=###0,name_index=0
						}
					}
				}
			}
		}
	}
}
			