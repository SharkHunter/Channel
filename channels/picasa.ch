version=0.1	
channel Picasa {
	img=https://www.google.com/accounts/lh2/picasaweblogo-sv.gif
	format=image
	login {
		# Login data
		url=https://www.google.com/accounts/ClientLogin
		user=Email
		passwd=Passwd
		params=accountType=HOSTED_OR_GOOGLE&service=lh2&source=pms-ch-plug
		matcher=Auth=(.*)
		authstr=GoogleLogin auth=
	}
	folder {
		url=https://picasaweb.google.com/data/feed/api/user/default
		type=empty
		folder {
			# Album list
			#matcher=media:content url=\'([^\']+)\'.*?media:title[^>]+>([^<]+)</media:title>
			#matcher=<id>([^<]+)</id>.*?<title[^>]+>([^<]+)</title>
			matcher=<title[^>]+>([^<]+)</title>.*?(/user/[^/]+)(/albumid/[^\']+)\'
			order=name,url+
			url=https://picasaweb.google.com/data/feed/api/
			media {
				#Photos
				matcher=media:content url=\'([^\']+)\'.*?media:title[^>]+>([^<]+)<
				order=url,name+
				prop=name_separator=\n,
			}
		}
	}
}


