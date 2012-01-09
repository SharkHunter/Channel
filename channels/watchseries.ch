version=0.21

scriptdef wsTrix {
	tmp='http://www.watchseries.eu/open_link.php?vari=
	concat tmp s_url
	s_url=tmp
	s_action='geturl
	scrape
	url=v1
	play
}

scriptdef wsTrix2 {
	s_action='geturl
	scrape
	url=v1
	play
}


scriptdef wsSubs {
	url=s_url
	regex='(.*?)!!!Season ([\d]+).*?!!!([\d]+)
	match url
	url=v1
	season=v2
	episode=v3
	serie='1
	play
}

scriptdef wsScript {
	s_action='geturl
	scrape
	url=v1
	#url=s_url
	regex='(movshare)
	match url
	if v1 
		call 'http://navix.turner3d.net/proc/movshare
		url=v1
		play
	endif
	regex='(divxstage)
	match url
	if v1
		call 'http://justme4u2c.zymichost.com/DivxStage.php
		url=v1
		play
	endif
	regex='(novamov)
	match url
	if v1
	  call 'http://boseman22.dyndns-server.com/static/novamov
	  url=v1
	  play
	endif
	regex='(viedoweed)
	match url
	if v1
	  call 'http://justme4u2c.zymichost.com/VideoWeed.es.php
	  url=v1
	  play
	endif
	regex='(vixden)
	match url
	if v1
	  call 'http://navix.turner3d.net/proc/vidxden
	  url=v1
	  play
	endif
	regex='(megavideo)
	match url
	if v1
	  call 'http://navix.turner3d.net/proc/megavideo
	  url=v1
	  play
	endif
	regex='(videobb)
	match url
	if v1
	  call 'http://navix.turner3d.net/proc/videobb
	  url=v1
	  play
	endif
	play
}

macrodef wsMacro {
	folder {
		# Series
		#<a href="http://watchseries.eu/serie/a_bit_of_fry_and_laurie" title="A Bit of Fry and Laurie">A Bit of Fry and Laurie<span class="epnum">25 episodes</span></a></li>
		matcher=a href=\"([^\"]+)\" title=\"([^\"]+)\"
		order=url,name
		folder {
			# Seasons
			#href="http://watchseries.eu/season-1/a_gifted_man">Season 1  (3 episodes)</a></h2>
			matcher=href=\"(.*?season[^\"]+)\">\s*([^<]+)
			order=url,name
			folder {
				# Episodes
				#<li><a href="../episode/a_gifted_man_s1_e0-108029.html">0. Episode 0&nbsp;&nbsp;&nbsp;Preview </a></li>
				matcher=href=\"..(/episode[^\"]+)\"><[^>]+>([^<]+)
				order=url,name
				url=http://watchseries.eu/
				folder {
					# Site
					matcher=<div class=\"site\">\s+(\S+)\s+</div>\s+<div .*?>\s+<a href=\"..([^\"]+)
					order=name,url
					url=http://watchseries.eu/
					#post_script=wsTrix
					folder {
						# Trix
						# FlashVars="input=ZWRkaGNlaoSEZA=="
					#	matcher=FlashVars=\"input=([^\"]+)\"
					#<a href="http://watchseries.eu/gateway.php?link=YmZhaGRlY2ZiZ2tpbGFkaGppbGNmaGlqYWFoZGdjZmhhY1NkZmNiaGNrZGVogoKCgoSEaGNiY2RlaGlqa2piamdjZ2ppYmZjZmxra2hjYV9mZGdkbGdhYmNmZ2hpaGlqZWZnY2yCgoKChIRkY2lkaGZoZ2pjaWJjZGVja2RkYV9mZGdlaWeCgoKEhIRkgoKCgoSEZGRiZF9mZGdlamJiaGJkZmVmZ2dmZWhnbGtlYmJiYWhkZ2NoZYKEhISEgmJkY2RqZWtiY2Y=" class="myButton">Click Here to Play</a>
						matcher=a href=\"([^\"]+)\" .*?>Click Here to Play<
						order=url
						type=empty							
						media {
							script=wsScript
							subtitle=s4u,allSubs,podnapisiTV
							prop=name_index=4+3+2,name_separator=!!!
						}
					}
				}
			}
	  	}
	}
}


channel WatchSeries.eu {
	img=http://watchseries.eu/images/logo-hover.png
	subscript=wsSubs
#	hdr=Referer=http:/www.icefilms.info/index
#  login {
		# Login data
		#url=http://www.megaupload.com/?c=
		#user=username
		#passwd=password
		#params=login=1&redir=1
		#type=cookie
		#associate=meagvideo.com,megaporn.com,megalive.com
	#}
	 # folder {
		#Popular
#		name=Popular
#		url=http://watchseries.eu/new
#		macro=wsMacro
#	  }
	  folder {
		name=A-Z
		type=atzlink
		url=http://watchseries.eu/letters/
		prop=other_string=09
		macro=wsMacro
	  }
	  
}