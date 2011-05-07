version=0.1	
channel MuZu {
	folder {
		name=Popular
		url=http://www.muzu.tv/ww/music-videos?locale=en
		folder {
			#<a href='/brunomars/the-lazy-song-music-video/930420?country=ww&locale=en'>The Lazy Song</a></div>
			matcher=<a href=\'[^/d]+([/d]+)\?[^\>]+>([^<]+)</a></div>
			order=url,name
			media {
				url=http://www.muzu.tv/player/playAsset?vt=2&assetId=
				matcher=src=\"([^\"]+)"
				order=url
			}
		}
	}
	
}


