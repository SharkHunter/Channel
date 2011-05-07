version=0.1
channel NZB {
	img=http://nzb.su/views/images/clearlogo.png
	login {
		user=username
		passwd=password
		url=http://nzb.su/login
		matcher=RSSTOKEN = \"([^\"]+)\"
		type=apikey
		authstr=apikey=
		params=redirect=
	}
	folder {
		name=Movies
		url=http://nzb.su?t=movies
		media {
			#<div class="icon icon_nzb"><a title="Download Nzb" href="/getnzb/69d6cf35e73dcff3ddea2e2568cf4038/Elephant.White.2011.DVDRip.XviD-VoMiT"
			matcher=<div class=\"[\"]+"><a title=\"[\"]+\" href=\"([\"]+)\"
			order=url
		}
	}
}

