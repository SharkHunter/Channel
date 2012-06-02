version=0.20

channel Kanal5Play {
   img=http://www.kanal5play.se/themes/kanal5/images/logo.png
   var {
	disp_name=Bitrate
	var_name=br
	values=450,900,250
	suffix=Kbps
   }
   folder {
	name=A-Z
	type=ATZ
	url=http://www.kanal5play.se/program
	folder {
		matcher=a href=\"(/prog[^\"]+)\"[^>]+>([^<]+)</a>
		order=url,name
		url=http://www.kanal5play.se
		folder {
			# Seasons
			matcher=class=\"season-header\">([^<]+)</h2>\s*[^>]+>\s*<a href=\"([^\"]+)\" class=\"ajax sbs-button sbs-button-list\">
			order=name,url
			url=http://www.kanal5play.se
			folder {
				#<h4 class="title"><a href="/program/223047/video/280395" class="ajax">Storslagen final hägrar i 100 höjdare</a></h4>
				matcher=/video/([^\"]+)\"[^>]+>([^<]+)</a></h4>
				order=url,name
				url=http://www.kanal5play.se/api/getVideo?format=FLASH&videoId=
				media {
					matcher=\"bitrate\":@#br@#000,\"source\":\"([^\"]+)\",\"drmProtected\":false}],\"streamBaseUrl\":\"([^\"]+)\"
					order=playpath,url
					put=swfVfy=http://www.kanal5play.se/flash/StandardPlayer.swf
				}
			}
		}
	}
}

