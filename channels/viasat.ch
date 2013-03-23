version=0.36

macrodef via_media {
	media {
		matcher=<Url>(rtmp.*)</Url>
		put=swfVfy=http://flvplayer.viastream.viasat.tv/play/swf/player120328.swf
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
	}
}

channel TV3 {
	img=http://www.mynewsdesk.com/se/view/Image/download/resource_image/95624
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