version = 0.31

macrodef apiMedia {
	media {
		#matcher=media:title[^>]+>([^\<]+)</media:title>.*?<media:player url='[^']+' />
		matcher=media:player url=\'([^\']+)\'/><media:thumbnail url='([^']+)'.*?media:title[^>]+>([^\<]+)</media:title>
		order=url,thumb,name
	}
}

macrodef ytMedia {
	media {
		#<a href="/watch?v=8hMsNRMIHZQ&amp;feature=tn" title="Shay Given&#39;s tears over Gary Speed&#39;s death." class="yt-uix-tile-link"  >Shay Given&#39;s tears over Gary Speed&#39;s death.</a>
		matcher = <a href=\"([^\"]+)\" title=\"([^\"]+)\"
		order = url,name
		prop = prepend_url=http://www.youtube.com,prepend_thumb=http:,
	}
}

channel YouTube {
	img = http://www.engr.uky.edu/solarcar/sites/default/files/YouTube_icon.png
	login {
		url=https://www.google.com/youtube/accounts/ClientLogin
		user=Email
		passwd=Passwd
		params=service=youtube&source=pms-ch-plug
		matcher=Auth=(.*)
		authstr=GoogleLogin auth=
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
		name=Categories
		url=http://www.youtube.com/videos?feature=mh
		folder {
			# <li><a href="/news" >Nyheter och politik</a>
			matcher = <li[^>]*>\s+<a href=\"([^\"]+)\"\s*>([^<]+)</a>[^<]*</li>
            order = url,name
            url = http://www.youtube.com/
			macro=ytMedia
		}
		folder {
			#<a class="" href="/autos">Bilar och fordon</a>
			matcher=<a class=\"\" href=\"([^\"]+)\">([^<]+)<
			order=url,name
			url=http://www.youtube.com/
			macro=ytMedia
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