version=0.1

scriptdef teliaScript {
	orgUrl=s_url
	regex='content=(.*)
	match s_url
	contId=v1
	regex='(.*)main
	match s_url
	base=v1
	#subscriberobjectid':'625141156865'
	regex='subscriberobjectid':'([^']+)',
	scrape
	subObj=v1
	s_url=base
	s_referer=orgUrl
	s_headers.X-Prototype-Version='1.6.0.2
	s_headers.X-Requested-With='XMLHttpRequest
	s_headers.Accept='text/javascript
	str='ajax_controller.aspx?cmd=play&level=&deliveymethod=stream&contentid=
	concat s_url str
	concat s_url contId
	concat s_url '&istrailer=&priceid=0&machineid=&bitrate=2154&deliverdrm=true&silent=false&format=wmv&subscriberObjectIdForRegisterPlaybackAction=
	concat s_url v1
	concat s_url '&subscriptionpurchase=false
#	s_cookie='82073d11aa8ebfdd89c49b9c405f0989=2154; mainContentListDisplayType=listdisplay
	regex='(.*)
	scrape
}

channel Canal+ {
	img=http://images2.wikia.nocookie.net/__cb20110518152661/yaberolan/sv/images/f/f2/Canal%2B_Logo_2011.PNG
	login {
		url=http://webclient.teliasonerabroadband.frankfurt1.tsicmds.com/main.aspx
		prop=fetch_expr=id=\"(__VIEWSTATE)\"\s+value=\"([^\"]+)\",concat_sep==
		user=username
		passwd=password
		params=cmd=login&recoverPasswordMail=
		type=cookie
	}
		folder {
			# Movie
			url=http://webclient.teliasonerabroadband.frankfurt1.tsicmds.com/main.aspx?level=film
			name=Movie
			folder {
				# <li><a href="main.aspx?level=nyheter">Nyheter</a></li>
				url=http://webclient.teliasonerabroadband.frankfurt1.tsicmds.com/
				matcher=<li><a href=\"([^\"]+)\">([^<]+)</a>
				order=url,name
				folder {
					#Serier
					url=http://webclient.teliasonerabroadband.frankfurt1.tsicmds.com/
					matcher=<li><a href=\"([^\"]+)\">([^<]+)</a>
					order=url,name
					folder {
						#<a class="moreinfo" href="main.aspx?content=614545197057" title="Game of Thrones Del 9, Baelor"
						url=http://webclient.teliasonerabroadband.frankfurt1.tsicmds.com/
						matcher=<a class=\"moreinfo\" href=\"([^\"]+)\" title=\"([^\"]+)\"
						order=url,name
						media {
							script=teliaScript
						}
					}
				}
			}
			
		}
		folder {
			# Sports
			url=http://webclient.teliasonerabroadband.frankfurt1.tsicmds.com/main.aspx?level=sport
			name=Sport
		}
		folder {
			# Extra
			url=http://webclient.teliasonerabroadband.frankfurt1.tsicmds.com/main.aspx?level=ppv
			name=PPV
		}
	}
