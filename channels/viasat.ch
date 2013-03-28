version=0.40

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

macrodef hlsMedia {
	folder {
		matcher=<ProductId>(\d+)</ProductId>.*?<Title><!\[CDATA\[([^>]+)\]\]></Title>.*?<SamiFile>(.*?)</SamiFile>
		order=url,name,subs
		prop=matcher_dotall,discard_duplicates
		url=http://viastream.viasat.tv/MobileStream/
		macro=viaHLS
	}
	folder {
		matcher=<ProductId>(\d+)</ProductId>.*?<Title><!\[CDATA\[([^>]+)\]\]></Title>
		order=url,name
		url=http://viastream.viasat.tv/MobileStream/
		prop=discard_duplicates
		macro=viaHLS
	}
	
}

macrodef via_ses_epi {
	matcher=<siteMapNode title=\"(.*)\" id=\"(.*)\" children=\"true\".*>
	order=name,url
	folder {
		# Season
		matcher=<siteMapNode title=\"(.*)\" id=\"(.*)\" children=\"false\".*>
		order=name,url
		url=http://viastream.viasat.tv/Products/Category/
		folder {
			#Episodes
			matcher=<ProductId>([^>]+)</ProductId>[^<]+<Title><!\[CDATA\[([^>]+)\]\]></Title>
			url=http://viastream.viasat.tv/Products/
			order=url,name
			type=empty
			macro=hlsMedia
			macro=via_rtmp
		}
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
		url=http://viastream.viasat.tv/siteMapData/se/2se/
		folder {
			# Programs
			url=http://viastream.viasat.tv/siteMapData/se/2se/
			macro=via_ses_epi
		}
	}
}


channel TV6 {
	img=http://www.tv6.se/sites/all/themes/free_tv/css/custom/tv6_se/images/logo.png
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
		url=http://viastream.viasat.tv/siteMapData/se/3se/
		folder {
			# Programs
			url=http://viastream.viasat.tv/siteMapData/se/3se/
			macro=via_ses_epi
		}
	}
}

channel TV8 {
	img=http://www.tv8.se/sites/all/themes/free_tv/css/custom/tv8_se/images/logo.png
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
		url=http://viastream.viasat.tv/siteMapData/se/4se/
		folder {
			# Programs
			url=http://viastream.viasat.tv/siteMapData/se/4se/
			macro=via_ses_epi
		}
	}
}