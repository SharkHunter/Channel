version=0.21
macrodef ses_epi {
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
			#prop=auto_media
			type=empty
			item {
				matcher=<Url>(http.*)</Url>
				order=url
				prop=auto_media
				media {
					matcher=<Url>(rtmp.*)</Url>
				}
			}
			media {
				matcher=<Url>(rtmp.*)</Url>
			}
		}
	}
}

channel TV3 {
	img=http://tv3.se/sites/all/themes/free_tv/css/custom/tv3_se/images/logo.png
	folder {
		name=A-Z
		type=ATZ
		url=http://viastream.viasat.tv/siteMapData/se/2se/
		folder {
			# Programs
			url=http://viastream.viasat.tv/siteMapData/se/2se/
			macro=ses_epi
		}
	}
}


channel TV6 {
	img=http://www.tv6.se/sites/all/themes/free_tv/css/custom/tv6_se/images/logo.png
	folder {
		name=A-Z
		type=ATZ
		url=http://viastream.viasat.tv/siteMapData/se/3se/
		folder {
			# Programs
			url=http://viastream.viasat.tv/siteMapData/se/3se/
			macro=ses_epi
		}
	}
}

channel TV8 {
	img=http://www.tv8.se/sites/all/themes/free_tv/css/custom/tv8_se/images/logo.png
	folder {
		name=A-Z
		type=ATZ
		url=http://viastream.viasat.tv/siteMapData/se/4se/
		folder {
			# Programs
			url=http://viastream.viasat.tv/siteMapData/se/4se/
			macro=ses_epi
		}
	}
}