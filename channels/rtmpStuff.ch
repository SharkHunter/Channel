version=0.11

macrodef myMedia {
	media {
			#title<span class="re2">&gt;</span></span></span>BBC HD<span class="sc3"><span class="re1">&lt;/title<span class="re2">&gt;</span></span></span></div></li>
#<li class="li2"><div class="de2">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <span class="sc3"><span class="re1">&lt;link<span class="re2">&gt;</span></span></span>rtmp://s6.webport.tv/live/ playpath=z010201.stream swfUrl=&quot;http://www.tvsector.com/wp-content/uploads/jw-player-plugin-for-wordpress/player/player.swf&quot; pageUrl=&quot;http://www.tvsector.com/national-geographic-nat-geo-hd/&quot;<span class="sc3"><span class="re1">&lt;/link<span class="re2">&gt;</span></span></span></div></li>
#<li class="li1"><div class="de1">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; <span class="sc3"><span class="re1">&lt;thumbnail<span class="re2">&gt;</span></span></span>http://www.lyngsat-logo.com/logo/tv/bb/bbc_hd_scan.jpg
			matcher=title<span[^>]+>&gt;</span></span></span>([^<]+)<.*?\s*.*?link<span [^>]+>&gt;</span></span></span>([^ ]+) playpath=([^ ]+) swfUrl=&quot;([^&]+)&quot;\s*(&nbsp;)*pageUrl=([^<]+)<.*?thumbnail<span class=\"re2\">&gt;</span></span></span>([^<]+)<
			order=name,url,playpath,swfurl,dummy,pageurl,thumb
			prop=matcher_dotall
		}
}

channel RTMPStuff - Fetch {
   folder {
		name=A-Z
		type=ATZ
		url=http://pastebin.com/k7uavgZR
		macro=myMedia
  }
}

channel RTMPStuff - Cached {
	folder {
		name=A-Z
		type=ATZ
		url=file:///tmp/rtmpstuff
		macro=myMedia
  }
}
