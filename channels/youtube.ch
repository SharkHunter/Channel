version = 0.52

macrodef apiMedia {
	media {
		#matcher=media:title[^>]+>([^\<]+)</media:title>.*?<media:player url='[^']+' />
		matcher=media:player url=\'([^\']+)\'/><media:thumbnail url='([^']+)'.*?media:title[^>]+>([^\<]+)</media:title>
		order=url,thumb,name
		escript=youtube-dl.bat
		prop=no_fromat,url_unescape
	}
}

channel YouTube {
	img=http://www.youtube.com/yt/brand/media/image/yt-brand-standard-logo-630px.png
	fallback_video=.mp4
	login {
		url=https://www.google.com/youtube/accounts/ClientLogin
		user=Email
		passwd=Passwd
		params=service=youtube&source=pms-ch-plug
		matcher=Auth=(.*)
		authstr=GoogleLogin auth=
	}
	resolve {
		matcher=(.*youtube\.com/.*)
		action=resolved
	}
	folder {
		type=action
		action_name=resolved
		url=dummy_url
		media {
			escript=youtube-dl.bat
			prop=ignore_save
		}
	}
	folder {
		name=MyTube
		folder {
			name=Favorites
			url=https://gdata.youtube.com/feeds/api/users/default/favorites
			macro=apiMedia	
		}
		folder {
			name=Subscriptions
			url=https://gdata.youtube.com/feeds/api/users/default/subscriptions?v=2
			folder {
				#title>Videos published by : BaraBajenTV</title>
				matcher=title>Videos published by:\s*([^<]+)<.*?feed' src='([^']+)'
				order=name,url
				macro=apiMedia
			}
			folder {
				#title>Activity of : TrustNordisk<
				matcher=title>Activity of:\s*([^<]+)<.*? href='([^']+)'
				order=name,url
				folder {
					#uploads' href='https://gdata.youtube.com/feeds/api/users/hammarbyfotboll1/uploads?v=2'
					matcher=uploads' href='([^']+)'
					order=url
					type=empty
					macro=apiMedia
				}
			}
		}
	}
	folder {
		name=Search
		type=search
		url=https://gdata.youtube.com/feeds/api/videos
		prop=prepend_url=?q=,http_method=get
		macro=apiMedia
	}
}