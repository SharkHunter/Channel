version=0.14
channel NaviX {
	img=http://navix.turner3d.net/images/navi-x_sm.gif
	folder {
		name=Site scraper
		url=http://navix.turner3d.net/playlist/2229/realtime_scrapers.plx
		prop=continue_name=(.*>>>.*|.*Next [Pp]age.*),continue_limit=6,auto_asx
		type=navix
		subtitle=s4u
	}
	folder {
		name=Sample playlist
		url=http://navix.turner3d.net/playlist/19195/ironbills_playlist.plx
		type=navix
		subtitle=s4u
	}
	folder {
		name=Top
		url=http://www.navi-x.org/playlists/med_port.plx
		prop=continue_name=(.*>>>.*|.*Next [Pp]age.*),continue_limit=6,auto_asx
		type=navix
		subtitle=s4u
	}
	folder {
		name=All
		url=http://navix.turner3d.net/playlist/all.plx
		type=navix
		prop=continue_name=(.*>>>.*|.*Next [Pp]age.*),continue_limit=6,auto_asx
		subtitle=s4u
	}
	folder {
		name=Img test
		url=http://navi-x.googlecode.com/svn/trunk/Navi-X/examples/example%202.plx
		type=navix
	}
	folder {
		name=House
		url=http://navix.turner3d.net/playlist/20203/house_m.d.plx
		prop=continue_name=(.*>>>.*|.*Next [Pp]age.*),continue_limit=6,auto_asx
		type=navix
		subtitle=s4u
	}
}