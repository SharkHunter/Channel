version=0.11

channel Kanal5Play {
   img=http://www.kanal5play.se/themes/kanal5/images/logo.png
   folder {
	name=A-Z
	type=ATZ
	url=http://www.kanal5play.se/program
	folder {
		matcher=a title=\"[^\"]+\" class=\"[^\"]+\"\s+href=\"([^\"]+)\">\s+([^<]+)</a>
		order=url,name
		url=http://www.kanal5play.se
		folder {
			matcher=a href=\"(/program[^\"]+)\" title=\"([^\"]+)\">\s+<img src=\"([^\"]+)\"
			order=url,name,thumb
			url=http://www.kanal5play.se
			media {
			  escript=get_flash_videos.bat
			  prop=script.no_format
			}
		}
	}
}

