version=0.10

scriptdef fix_quote {
	nodebug='1
	url=s_url
	regex='\\\"
	replace url '\"
	# Specail obscure fix
	regex='^\\?
	replace url '- 
	play
}

channel Kanal9Play {
   img=http://lh3.ggpht.com/mhkHV9eYmmrWW_sU-xt0CBokTk3lHsGZuSjR8YOBJVmcfojSzwjCpSMhLljB-IOq7-OOU3I5O7FVkfBET56-
   var {
	disp_name=Bitrate
	var_name=br
	values=450,900,250
	suffix=Kbps
   }
   sub_conv {
		matcher=\"startMillis\":(\d+),\"endMillis\":(\d+),\"text\":\"(.*?)\",\"posX\"
		order=start,stop,text
		prop=time_ms,matcher_dotall,text_mangle=fix_quote
   }
   resolve {
		matcher=http://www.kanal9play.se/program/[^/]+/video/([^\"]+)
		action=resolved
   }
   resolve {
		matcher=^(\d+)
		action=resolved
		prop=dummy_match
   }
   folder {
	name=A-Z
	type=ATZ
	url=http://www.kanal9play.se/program
	folder {
		matcher=a href=\"(/prog[^\"]+)\"[^>]+>.*?<img src=\"([^\"]+)\".*?alt=\"([^\"]+)\"
		order=url,thumb,name
		url=http://www.kanal9play.se
		prop=matcher_dotall
		folder {
			# Seasons
			matcher=class=\"season-header\">([^<]+)</h2>\s*[^>]+>.*?<a href=\"([^\"]+)\" class=\"ajax sbs-theme-primary\">
			order=name,url
			url=http://www.kanal9play.se
			prop=matcher_dotall
			folder {
				matcher=<a href=\"/program/[^/]+/video/([^\"]+)\"[^>]+>.*?<span [^>]+>([^<]+)</span>([^<]+)</a>.*?</h4>
				order=url,name,name
				url=http://www.kanal9play.se/api/getVideo?format=FLASH&videoId=
				prop=matcher_dotall,name_separator=###0
				action_name=resolved
				media {
					matcher=\"bitrate\":@#br@#000,\"source\":\"([^\"]+)\",\"drmProtected\":false[^}]+}.*?\"streamBaseUrl\":\"([^\"]+)\".*?\"id\":(\d+),\"type\"
					order=playpath,url,subs
					prop=prepend_subs=http://www.kanal9play.se/api/subtitles/,last_play_action=resolved
					put=swfVfy=http://www.kanal9play.se/flash/K9StandardPlayer.swf
				}
			}
		}
	}
}

