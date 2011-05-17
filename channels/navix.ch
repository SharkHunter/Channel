version=0.22
channel NaviX {
	img=http://website.navi-x.org/networks/boxee/navix_icon.png
	folder {
		name=Site scraper
		url=http://navix.turner3d.net/playlist/2229/realtime_scrapers.plx
		prop=continue_name=(.*>>>.*|.*Next [Pp]age.*),continue_limit=6,auto_asx
		type=navix
		subtitle=s4u,allSubs
	}
	
   folder {
      name=Navi-X Media Portal
      url=http://navix.turner3d.net/playlist/50242/navi-xtreme_nxportal_home.plx
      prop=continue_name=(.*>>>.*|.*Next [Pp]age.*),continue_limit=6,auto_asx
      type=navix
      subtitle=s4u,allSubs
   }
	folder {
		name=All
		url=http://navix.turner3d.net/playlist/all.plx
		type=navix
		prop=continue_name=(.*>>>.*|.*Next [Pp]age.*),continue_limit=6,auto_asx
		subtitle=s4u,allSubs
	}
}