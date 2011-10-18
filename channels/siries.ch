version=0.1


scriptdef serSubs {
	url=s_url
	regex='(.*?) S(\d+)E(\d+)
	match url
	url=v1
	season=v2
	episode=v3
	serie='1
	play
}

channel Siries.me {
	  subscript=serSubs
	  login {
		pre_fetch {
			#name="authenticity_token" type="hidden" value="iWOgKMak93u7Cz9Zy0zcJbjIF+xV6z1fwrW0xyOW6dI="
			url=http://siries.me/users/sign_in
			matcher=name=\"(authenticity_token)\" type=\"hidden\" value=\"([^\"]+)\"
			order=name,url
		}
		url=http://siries.me/users/sign_in
		user=user%5Bemail%5D
		passwd=user%5Bpassword%5D
		params=user%5Bremember_me%5D=0&commit=Sign+in
		type=cookie
	  }
	  folder {
		url=http://siries.me/shows
		type=empty
		folder {
			#<a href="/90210/" title="90210">
			#<div class='cover'>
			#<img src="http://img.siries.me/pictures/covers/90210_list.jpg" width='130' height='130'/>
			matcher=a href=\"([^\"]+)\" title=\"([^\"]+)\">\s*<[^>]+>\s*<img src=\"([^\"]+)\"
			order=url,name,thumb
			url=http://siries.me/
			prop=append_url=seasons
			folder {
				#<a class='invisible' href='/90210/season4'><li>Season 4<div class='tip'></div></li></a>
				matcher=href='([^']+)'><li>([^<]+)<div
				order=url,name
				url=http://siries.me
				folder {
					#<a class='invisible' title='90210 S04E03 Greek Tragedy' href='/90210/season4/episode3'>
					#<div class='episode-box'>
					#<div class='image'>
					#<img src='http://img.siries.me/pictures/episodes/90210/s04e03.jpg'>
					matcher=class='invisible' title='([^']+)' href='([^']+)'>\s*<[^>]+>\s*<[^>]+>\s*<img src='([^']+)'>
					order=name,url,thumb
					url=http://siries.me
					media {
						#<source src="http://wolverine.mirrors.videostreamserver.net/video/0/8ab3b0a87c719af2c377f1f654009342/90210/s04e03.sq.mp4" type="video/mp4" />
						matcher=<source src=\"([^\"]+)\" 
						order=url
						subtitle=s4u,allSubs,podnapisiTV
						prop=name_index=1
					}
				}
			}
		}
	  }
}