version=0.20
macrodef ardItem {
	item {
		#<h3 class="mt-title"><a href="/ard/servlet/content/3517136?documentId=6067530" class="mt-fo_source" rel="/ard/servlet/ajax-cache/6067530/view=ajax/index.html">Europa zecht, Deutschland blecht - Steht die Europäische ...</a></h3>
		matcher=<h3 class=\"[^\"]+\"><a href=\"([^\"]+)\"[^>]+>([^<]+)</a></h3>
		order=url,name
		prop=name_separator= ,auto_media
		url=http://www.ardmediathek.de
		media {
			# mediaCollection.addMediaStream(0,0,"","http:/xxxxx") or mms://
			matcher=mediaCollection\.addMediaStream\([^,]*, [^,]*, \"\", \"([^\"]+)\"\);
		}
		media {
			# mediaCollection.addMediaStream(0, 2, "rtmp://vod.daserste.de/ardfs/", "mp4:videoportal/mediathek/Verbotene+Liebe/c_160000/160995/format174660.f4v?sen=Verbotene+Liebe&amp;for=Web-L&amp;clip=Vorschau+auf+Folge+3763");
			matcher=mediaCollection\.addMediaStream\([^,]*, [^,]*, \"(rtmp:[^\"]+)\", \"(mp4:[^\"]+)\"\);
			order=url,playpath
			#prop=url_separator=!!!pms_ch_dash_y!!!,
		}
	}
}

macrodef ardMacro {
	folder {
		# Time
		matcher=<a href=\"([^\"]+)\" title=\"\" class=\"[^\"]+\">.*?</span>(Ganzer Tag)</a>
		type=empty
		#order=url,name+
		#prop=name_separator= ,
		url=http://www.ardmediathek.de
		macro=ardItem
	}
}

channel ARD {
	img=http://www.ardmediathek.de/ard/static/pics/logos/logo_mediathek.gif
	folder {
		#name=Sendung verpasst
		#matcher=<a href=\"/ard/servlet/([^\"]+)\">(Sendung verpasst)[^&]*&nbsp
		#matcher=<li class=\"special\"><a href=\"([^\"]*)\">.*;a
		url=http://www.ardmediathek.de/ard/servlet/
		name=Top
		type=empty
		#order=url,name
		folder {
			matcher=<li class=\"special\"><a href=\"([^\"]+)\">([^&]*)&
			url=http://www.ardmediathek.de
			order=url,name
			folder {
				# Days
				#matcher=<li.*?><a href=\"(/ard/servlet/content/[^\"]+)\" title=\"\"><span>([^<]+)</span> <strong>([^<]+)</strong>
				matcher=<li.*?><a href=\"([^\"]+)\" title=\"\"><span>([^<]+)</span> <strong>([^<]+)</strong>
				url=http://www.ardmediathek.de
				order=url,name+
				prop=name_separator= ,
				macro=ardMacro
			}
			folder {
				type=ATZ
				name=A-Z
				url=http://www.ardmediathek.de
				order=name,name,url
				matcher=\"titel\": \"([^\"]+)\", \"kanal\": \"([^\"]+)\", \"link\": \"([^\"]+)\"
				folder {
						# Other chanels
						#matcher=<a href=\"([^\"]+)" class=\"[^\"]+\">.*?<strong>([^<]+)</strong> ([^<]+)</a>
						matcher=\"titel\": \"([^\"]+)\", \"kanal\": \"([^\"]+)\", \"link\": \"([^\"]+)\"
						order=name,name,url
						prop=name_separator= ,
						url=http://www.ardmediathek.de
						folder {
							matcher=<a class=\"[^\"]+\" href=\"([^\"]+)\">
							url=http://www.ardmediathek.de
							type=empty
							folder {
								matcher=<a class=\"[^\"]+\" href=\"([^\"]+)\">
								url=http://www.ardmediathek.de
								type=empty
								macro=ardItem
							}
						}
					}
				}
			}
		}
	}
