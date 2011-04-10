version=0.20
channel NaviX {
	img=http://website.navi-x.org/networks/boxee/navix_icon.png
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
      name=Navi-X Media Portal
      url=http://navix.turner3d.net/playlist/50242/navi-xtreme_nxportal_home.plx
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
	folder {
		name=Bosemans lists
		url=http://navix.turner3d.net/playlist/17830/bosemans_playlist.plx
		prop=continue_name=(.*>>>.*|.*Next [Pp]age.*),continue_limit=6,auto_asx
		type=navix
		subtitle=s4u
	}
}