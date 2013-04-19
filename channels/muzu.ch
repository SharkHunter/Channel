version=0.2

scriptdef muzuScript {
	regex='([^\_]+)_(\d+)_(\d+)
	match s_url
	url='http://player.muzu.tv/player/requestVideo?cn=5&device=web%2Eoffsite&playlistId=0&areaName=&ni=
	concat url v2
	concat url '&qv=360&ai=
	concat url v3
	concat url '&networkVersion=2013%2D04%2D05%2D18%3A31%3A17%2E0&hostName=http%3A%2F%2Fplayer%2Emuzu%2Etv&country=
	concat url v1
	concat url '&vt=y&tm=0&viewhash=6xjPCTwMbnjS9uWugZ7QEjJ4MU
	play
}

channel MuZu {
	img=http://www.muzu.tv/MUZU_114x114.png
	folder {
		name=A-Z
		type=atzlink
		url=http://www.muzu.tv/se/browse/artistBrowse/?af=
		folder {
			matcher=li class=\"seoBrowseItem\"><a title=\"([^\"]+)\" href=\"([^\"]+)\">
			order=name,url
			url=http://www.muzu.tv/se/
			folder {
				# <a itemprop="url" href="/abba/ring-ring-musikvideo/1037728/" title="Abba - Ring, Ring"><img width="74" height="44" alt="Abba - Ring, Ring" src="http://static.muzu.tv/media/images/001/037/728/001/1037728-thb1.jpg"/></a>
				matcher=itemprop=\"url\" href=\"([^\"]+)\" title=\"([^\"]+)\"><img .*?src=\"([^\"]+)"
				order=url,name,thumb
				url=http://www.muzu.tv
				prop=matcher_dotall
				post_script=muzuScript
				folder {
					type=empty
					#video_flashVars.country = "se";video_flashVars.networkId = 32791;video_flashVars.serverURL = "http://player.muzu.tv";video_flashVars.inskinAds = "n";video_flashVars.vidId = "1037728"
					matcher=video_flashVars.country = \"([^\"]+)\";video_flashVars.networkId = (\d+);[^;]+;[^;]+;video_flashVars.vidId = \"([^\"]+)\";
					order=url,url,url
					prop=url_separator=_
					media {
						matcher=url\":\"([^\"]+)\"
						order=url
						script=fixSlash
					}
				}
			}
		}
	}
	
}


