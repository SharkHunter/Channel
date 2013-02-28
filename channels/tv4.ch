version=0.31

channel TV4 {
	img=http://cdn01.tv4.se/polopoly_fs/2.740!logoImage/807786835.png
	login {
		url=https://www.tv4play.se/session?method=post
		passwd=password
		user=user_name
		pre_fetch {
			url=https://www.tv4play.se/session/new?https=
			matcher=name=\"(authenticity_token)\" type=\"hidden\" value=\"([^\"]+)\"
			order=name,url
		}
		params=https=&my_page=true
		type=cookie
	}
	folder {
		type=ATZ
		name=A-Z
		url=http://www.tv4play.se/program?per_page=999&per_row=4&page=1&content-type=a-o
		folder {
			# Programs
			# <h3 class="video-title"><a href="/dokumentarer/112_-_luftens_hjaltar">112 - luftens hjältar</a></h3>
			matcher=<li><a href=\"([^\"]+)\">([^<]+)</a>
			order=url,name
			url=http://www.tv4play.se/
			folder {
				# Episodes
				#<h3 class="video-title"><a href="/program/112-p%C3%A5-liv-och-d%C3%B6d?video_id=2286322">112 - på liv och död del 3</a>
				matcher=<h3 class=\"video-title\">\s*<a href=\"([^\"]+)\">([^<]+)</a>
				order=url,name,thumb
				prop=matcher_dotall,discard_duplicates
				url=http://www.tv4play.se/
				folder {
					matcher=tv4play://play/([^\"]+)\" 
					order=url
					prop=append_url=/play?protocol=hls
					url=http://premium.tv4play.se/api/web/asset/
					type=empty
					folder {
						matcher=<url>(.*?m3u8)</url>
						order=url
						type=empty
						media {
							matcher=BANDWIDTH=(\d+)\d###lcbr###3###rcbr###+.*?(index.*?)\n
							order=name,url
							prop=matcher_dotall,append_name=Kbps,relative_url=path,name_separator=###0,name_index=1
						}
					}
				}
			}
		}
	}
}
