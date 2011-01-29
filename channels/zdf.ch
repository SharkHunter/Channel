version=0.1	
channel ZDF {
	img=http://www.zdf.de/ZDFmediathek/img/logo_mediathek.gif
	folder {
		#name=Sendung verpasst
		#matcher=<a href=\"/ard/servlet/([^\"]+)\">(Sendung verpasst)[^&]*&nbsp
		#matcher=<li class=\"special\"><a href=\"([^\"]*)\">.*;a
		url=http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst?flash=off
		type=empty
		#order=url,name
		folder {
			# Days
			#<li><a href="/ZDFmediathek/hauptnavigation/sendung-verpasst/day1?flash=off">- Gestern, Mi, 12. Jan. 2011</a>
			matcher=<li><a href=\"([^\"]+)\"[^>]*>- ([^<]+)</a>
			url=http://www.zdf.de/
			order=url,name
			folder {
				# Programs
				#a href="/ZDFmediathek/beitrag/video/1233206/drehscheibe-am-13.-Januar-2011?bc=svp;sv0&amp;flash=off">drehscheibe am 13. Januar 2011<br />&nbsp;
				matcher=<b><a href=\"([^\"]+)\">([^<]+)<br />
				url=http://www.zdf.de/
				order=url,name+
				type=empty
				prop=name_separator= ,
				media {
					# We pick the asx files which means we got to do a little extra fetching
					#a href="http://wstreaming.zdf.de/zdf/300/110113_dde.asx" class="play" target="_blank">Abspielen</a>
					matcher=<a href=\"(.*asx)\"[^>]+>Abspielen</a>
					order=url
					type=asx
				}
			}
		}
	}
}


