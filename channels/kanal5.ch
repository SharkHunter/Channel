version=0.30

channel Kanal5Play {
   img=http://www.kanal5play.se/themes/kanal5/images/logo.png
   var {
	disp_name=Bitrate
	var_name=br
	values=450,900,250
	suffix=Kbps
   }
   sub_conv {
		matcher=\"startMillis\":(\d+),\"endMillis\":(\d+),\"text\":\"(.*?)\",
		order=start,stop,text
		prop=time_ms,matcher_dotall
   }
   folder {
	name=A-Z
	type=ATZ
	url=http://www.kanal5play.se/program
	folder {
		matcher=a href=\"(/prog[^\"]+)\"[^>]+>.*?<img src=\"([^\"]+)\".*?alt=\"([^\"]+)\"
		order=url,thumb,name
		url=http://www.kanal5play.se
		prop=matcher_dotall
		folder {
			# Seasons
			matcher=class=\"season-header\">([^<]+)</h2>\s*[^>]+>.*?<a href=\"([^\"]+)\" class=\"ajax sbs-theme-primary\">
			order=name,url
			url=http://www.kanal5play.se
			prop=matcher_dotall
			folder {
				matcher=<a href=\"/program/[^/]+/video/([^\"]+)\"[^>]+>.*?<span [^>]+>([^<]+)</span>([^<]+)</a>.*?</h4>
				order=url,name,name
				url=http://www.kanal5play.se/api/getVideo?format=FLASH&videoId=
				prop=matcher_dotall,name_separator=###0
				media {
					matcher=\"bitrate\":@#br@#000,\"source\":\"([^\"]+)\",\"drmProtected\":false}.*?\"streamBaseUrl\":\"([^\"]+)\".*?\"id\":(\d+),\"type\"
					order=playpath,url,subs
					prop=prepend_subs=http://www.kanal5play.se/api/subtitles/
					put=swfVfy=http://www.kanal5play.se/flash/StandardPlayer.swf
				}
			}
		}
	}
}

