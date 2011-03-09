version=0.2
macrodef sopMacro {
	media {
		# Sopcast media
		name=Sopcast
		img=http://www.sopcast.com/images/sopcast-log.gif
		order=url,name+
		prop=name_separator= ,prepend_name,use_conf_thumb
		matcher=<a href=\"(sop:[^\"]*)\" target[^>]+>.*?</a>.*?([0-9 ]+Kbps)
	}
}

macrodef pplMacro {
	media {
		# PPLive media
		name=PPLive
		order=url,name+
		prop=name_separator= ,prepend_name
		matcher=<a href=\"(synacast:[^\"]*)\" target[^>]+>.*?</a>.*?([0-9 ]+Kbps)
	}
}

channel MyP2P {
	img=http://www.myp2p.eu/gfx/logo.gif
	folder {
		# Sports Main
		name=Sports
		url=http://myp2p.eu/index.php?part=sports
		folder {
			# Individual Sports
			matcher=onclick=\"location\.href=\'(competition\.php\?competitionid=&part=sports&discipline=[^\']+)\';\" id=\"[^\"]+\" align=\"center\"><img src=\"([^\"]+)\" /><br><span class=\"subtext\"><b>([^<]+)</b>
			order=url,thumb,name
			url=http://myp2p.eu/
			prop=name_separator=-,peek
			folder {
				# Event list
				matcher=<a href=\"(broadcast.php\?matchid=[^&]+&part=sports)\">.*?<b>([^&]+)&nbsp;<img.*?&nbsp;([^<]*)</b> 
				order=url,name+
				url=http://myp2p.eu/
				prop=name_separator=-
				macro=pplMacro
				macro=sopMacro
			}
		}
	}
	folder {
		# Now playing
		name=Now playing
		url=http://myp2p.eu/index.php?part=sports
		prop=name_separator=-,peek
		folder {
				matcher=<a href=\"(broadcast.php\?matchid=[^&]+&part=sports)\">.*?<b>([^&]+)&nbsp;<img.*?&nbsp;([^<]*)</b> 
				order=url,name+
				url=http://myp2p.eu/
				prop=name_separator=-
				macro=pplMacro
				macro=sopMacro
				media {
					#JustinTv
					name=JustinTV
					matcher=<b>MediaPlayer</b>.*?<a href=\"([^\"]+)\">
				}
			
		}
	}
}
		