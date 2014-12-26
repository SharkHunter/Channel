version=1.00

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
	resolve {
		matcher=http://www.svtplay.se/(video/.*)
		action=resolved
	}
	resolve {
		matcher=(http://www.oppetarkiv.se/video/.*)
		action=resolved_open
	}
	resolve {
		matcher=http://www.svt.se/(wd\?.*)
		prop=append_url=&output=json&format=json,prepend_url=http://www.svt.se/
		action=svt_klipp_resolved		
	}
	resolve {
		matcher=(.*svt.se/.*)
		action=svt_klipp
	}
	folder {
		type=action
		action_name=svt_klipp
		folder {
			type=empty
			# <iframe src="http://www.svt.se/wd?widgetId=23991&sectionId=1752&articleId=1634593&type=embed&contextSectionId=1752&autostart=false
			matcher=<p class=\"svtplayembed svtWpVideo\"><iframe src=\"([^\"]+)\"[^>]+
			prop=append_url=&output=json&format=json
			macro=svtHLSMedia
			action_name=svt_klipp_resolved 
		}
		
	}
	folder {
		url=http://www.svtplay.se/kanaler
		type=empty
		folder {
			#data-jsonhref="/kanaler/svt1" data-channel="svt1" title="SVT1 |
			matcher=data-thumbnail=\"([^\"]+)\" data-jsonhref=\"(/kanaler/[^\"]+)\" data-channel=\"([^\"]+)\"
			order=thumb,url,name
			url=http://www.svtplay.se/
			prop=append_url=?output=json
			macro=svtHLSMedia
		}
	}
	folder {
		name=Live
		url=http://www.svtplay.se/?tab=live&sida=1
		folder {
			matcher=data-title=\"([^\"]+).*?a href=\"(/video[^\"]+)\" class=\"[^\"]+\">.*?<img *class=\"playGridThumbnail\" alt=\"\" src=\"([^\"]+)\"
			order=name,url,thumb
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
			#<li class="svtoa-anchor-list-item svt-column-avoid-break-inside"><a class="svt-text-default" href="http://www.oppetarkiv.se/etikett/titel/Abba%20dabba%20dooo/">Abba dabba dooo</a></li>
			matcher=<li class=\"svtoa-anchor-list-item[^\"]+\"><a class=\"svt-text-default\" href=\"([^\"]+)\">([^<]+)</a>
			order=url,name
			folder {
				matcher=data-imagename=\"([^\"]+)\" alt=\"([^\"]+)\".*?class=\"svtLink-Discreet-THEMED svtJsLoadHref\" href=\"([^\"]+)\"
				order=thumb,name,url
				prop=matcher_dotall
				action_name=resolved_open
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
			 matcher=<a href=\"(/[^\"]+)\" title.*?class=\".*?play_alphabetic-li[^\"]+\">([^<]+)</a>
			 order=url,name
			 url=http://www.svtplay.se/
			 prop=matcher_dotall,monitor
			 action_name=resolved
			 folder {
				url=http://www.svtplay.se/
		  		matcher=<a title=\"[^\"]+\".*?href=\"(/video/[^\"]+)\".*?class=\"[^\"]+\".*?<img.*?alt=\"([^\"]+)\".*?src=\"([^\"]+)\"
				order=url,name,thumb
				action_name=crawl
				prop=matcher_dotall,monitor,crawl_mode=FLA+HML,page_split=(.*)Mer fr.n <a href=\"
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
			
channel SVTFlow {
	img=http://www.svtflow.se/public/version_ae0812cc31cdecf53ab8e39a17675d9b08909372/images/favicon.ico
	folder {
		name=Flow
		url=http://www.svtflow.se
		folder {
			matcher=data-fulltitle=\"([^\"]+)\".*?data-json-href=\"(/video[^\"]+)\".*?data-thumbnailxlimax=\"([^\"]+)\"			
			order=name,url,thumb
			url=http://www.svtplay.se
			action_name=crawl
			prop=matcher_dotall,monitor,crawl_mode=FLA+HML,append_url=?output=json
			media {
				matcher=\"url\":\"(rtmp[^\"]+)\",\"bitrate\":([^,]+),
				order=url,name
				prop=append_name=Kbps,name_separator=###0,url_mangle=svtFilter
				put=swfVfy=http://www.svtplay.se/public/swf/video/svtplayer-2012.34.swf
			}
			macro=svtHLSMedia	
		}
	}
	folder {
		name=Program
		type=ATZ
		url=http://www.svtflow.se/program
		folder {
			matcher=<article class=\"svt_mediablock\">.*?<a href=\"([^\"]+)\">.*?<img src=\"([^\"]+)\" alt=\"([^\"]+)\"[^>]+>
			order=url,thumb,name
			url=http://www.svtflow.se
			prop=matcher_dotall
			folder {
				matcher=data-fulltitle=\"([^\"]+)\".*?data-json-href=\"(/video[^\"]+)\".*?data-thumbnailxl=\"([^\"]+)\"					
				order=name,url,thumb
				url=http://www.svtplay.se
				action_name=crawl
				prop=matcher_dotall,monitor,crawl_mode=FLA+HML,append_url=?output=json
				media {
					matcher=\"url\":\"(rtmp[^\"]+)\",\"bitrate\":([^,]+),
					order=url,name
					prop=append_name=Kbps,name_separator=###0,url_mangle=svtFilter
					put=swfVfy=http://www.svtplay.se/public/swf/video/svtplayer-2012.34.swf
				}
				macro=svtHLSMedia				
			}
		}
	}
}