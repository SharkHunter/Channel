version=1.32
channel Movie2K {

	login {
   
	url=http://www.movie2k.to
   	type=simple_cookie
	}

folder {

	name=Cinema Movies - English

	folder {
		name=Top
		url=http://www.movie2k.to/index.php?lang=en
		macro=cinetopmacro
		}
	folder {
		name=Featured
		url=http://www.movie2k.to/index.php?lang=en
		macro=cinefeaturedmacro
		}
	folder {
		name=Latest Updates
		url=http://www.movie2k.to/index.php?lang=en
		macro=cinelatestmacro
		}
}

folder {

	name=Cinema Movies - German

	folder {
		name=Top
		url=http://www.movie2k.to/index.php?lang=de
		macro=cinetopmacro
		}
	folder {
		name=Featured
		url=http://www.movie2k.to/index.php?lang=de
		macro=cinefeaturedmacro
		}
	folder {
		name=Latest Updates
		url=http://www.movie2k.to/index.php?lang=de
		macro=cinelatestmacro
		}
}



folder {
	name=Movies in other languages

	folder {
	name=Movies in Italian
	url=http://www.movie2k.to/index.php?lang=it
	macro=langlistmacro
	}

	folder {
	name=Movies in Spanish
	url=http://www.movie2k.to/index.php?lang=es
	macro=langlistmacro
	}
	folder {
	name=Movies in French
	url=http://www.movie2k.to/index.php?lang=fr
	macro=langlistmacro
	}
	folder {
	name=Movies in Japanese
	url=http://www.movie2k.to/index.php?lang=jp
	macro=langlistmacro
	}
	folder {
	name=Movies in Russian
	url=http://www.movie2k.to/index.php?lang=ru
	macro=langlistmacro
	}
}

folder {

	name=Movies A-Z (requires long time)

	url=http://www.movie2k.to/movies-all.html

	folder {
	name=#
	url=http://www.movie2k.to/movies-all-1.html
	macro=listallmacro
	macro=pagesmacro
	}

	folder {
	matcher=div id=\"boxgrey\"><a href=\"([^\"]+)\">([\w])<
	order=url,name
	macro=listallmacro
	macro=pagesmacro

	}
}

folder {
	name=TV Shows - Featured
	url=http://www.movie2k.to/tvshows_featured.php
	folder {
		matcher=a href="([^\"]+)\"><img src=\"([^\"]+)\" .*?title=\"([^\"]+)\">
		order=url,thumb,name
		url=http://www.movie2k.to/
		macro=mediamacro
	}
}


}

macrodef cinetopmacro {
	folder {
	matcher=div\sstyle=\"float\:left\">[^\*]+?href=\"([^\"]+)\"[^\*]+?color[^\>]+?>([^\<]+)<[^\*]+?tt([0-9]+)\"
	order=url,name,imdb
	url=http://www.movie2k.to/
	macro=sourcesmacro
	prop=movieinfo
	}
}

macrodef cinefeaturedmacro {
	folder {
	matcher=div\sid=\"maincontent2\">[^\*]+?href=\"([^\"]+)\"[^\*]+?title=\"([^\"]+)\"
	order=url,name
	url=http://www.movie2k.to/
	macro=sourcesmacro
	prop=movieinfo
	}
}
macrodef cinelatestmacro {
	folder {
	matcher=td valign=\"top\" rowspan=\"2\"[^\*]+?href=\"([^\"]+)\"[^\*]+?<strong>([^\<]+)<
	order=url,name
	url=http://www.movie2k.to/
	macro=sourcesmacro
	prop=movieinfo
	}
}


macrodef langlistmacro {
	folder {
	matcher=550\" id=\"tdmovies\">[^\*]+?href=\"([^\"]+)\">([^\<]+)<
	order=url,name
	url=http://www.movie2k.to/
	macro=sourcesmacro
	prop=movieinfo
	}
}


macrodef listallmacro {

	folder {

	matcher=width=\"550\" id=\"tdmovies\">[^\*]+?href=\"([^\"]+)\">([^\*]+?)\s\s\s<?[^\*]+?align=\"right\"[^\*]+?img/f?l?a?g?_?([^\*]+?_?g?e?r?)[_flag]
	order=url,name,name

	macro=sourcesmacro
	prop=name_separator=  - ,
		}

}

macrodef pagesmacro {
	folder {
	matcher=id=\"boxgrey\"><a href=\"([^\"]+)\">[0-9]
	type=empty
	macro=listallmacro
	}
}


macrodef sourcesmacro {

	folder {
	matcher=nbsp;(Movshare|MegaVideo|Stream2k|Bitload|Novamov|Putlocker|Divxstage)<[^\*]+?href[^\*]*?\"([^\*]+?html)[^\*]+?(>Quality:)[^\*]+?img/smileys/([0-9])
	order=name,url,name+
	url=http://www.movie2k.to/
	prop=name_separator=###0
	macro=mediamacro
	macro=partsmacro
	}
}


macrodef partsmacro {
	folder {
	matcher=href=\"([^\"]+?part=[0-9])\"[^\*]+?img/parts/([^\W]+)_
	order=url,name
	prop=movieinfo
	url=http://www.movie2k.to/
	macro=mediamacro
		}
}


scriptdef stream2k {
	s_referer=s_url
	regex='file=([^']+)'
	scrape
	url=v1
	play
}

macrodef mediamacro {
	
media {
#Movshare - OK with curl
	matcher=img src="(http://img.movie2k.to/thumbs/[^\"]+)\"[^\*]+?(http://www\.movshare\.net/[^\/]+/[^\/\"]+)/?\"?
	order=thumb,url
	script=movshares
	prop=concat_name=rear,name_separator=  - ,
	}
item {
#Bitload
	matcher=img src="(http://img.movie2k.to/thumbs/[^\"]+)\"[^\*]+?(http://www\.bitload\.com/d/[^\"]+?)\"
	order=thumb,url
	prop=auto_media,append_url=?m=def&c=free,concat_name=rear,name_separator=  - ,
 media {
	matcher=var url[^\*]+?(http://[^\*]+?)\'
	prop=concat_name=rear,name_separator=  - ,
	}	
	}

	media {
		matcher=src=\"(http://.*?stream2k.com[^\"]+)\"
		order=url
		script=stream2k		
	}

item {
#Novamov - OK with Curl	
	matcher=img src="(http://img.movie2k.to/thumbs/[^\"]+)\"[^\*]+?(http://www\.novamov\.com/video[^\"]+?)\"
	order=thumb,url
	prop=auto_media,concat_name=rear,name_separator=  - ,
 media {
	matcher=flashvars\.file=\"([^\"]+?)\"
	prop=concat_name=rear,name_separator=  - ,
	}
	}


media {
#Putlocker - OK with curl - needs putlocker.groovy
	matcher=img src="(http://img.movie2k.to/thumbs/[^\"]+)\"[^\*]+?href=\"(http://www\.putlocker\.com/file/[^\"]+)\"
	order=thumb,url
	prop=concat_name=rear,name_separator=  - ,
	}

item {
#Divxstage - OK with Curl (uses movshare links)
	matcher=img src="(http://img.movie2k.to/thumbs/[^\"]+)\"[^\*]+?(http://www.divxstage.[^\*]+?/video[^\"]+?)\"
	order=thumb,url
	prop=auto_media,concat_name=rear,name_separator=  - ,
 media {
	matcher=embed type=[^\*]+?src=\"([^\"]+)\"
	prop=concat_name=rear,name_separator=  - ,
	}
	}
}
