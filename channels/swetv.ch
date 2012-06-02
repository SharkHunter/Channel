version=0.12

macrodef sweswitch {
switch {
		matcher=href=\"magnet:[^=]+=urn:btih:([^&]+)&amp;dn=([^&]+)&
		order=url,name
		name=Furk
		action=upload
		script=furkUploadHash
		prop=name_unescape
	}
	switch {
		#<a href="download.php?id=10480&name=Game.of.Thrones.S02E02.SWESUB.WEBRip.XviD-%5Btankafilm.com%5D.torrent">
		matcher=a href=\"(download\.php[\"]+)\"
		order=url
		name=Furk
		action=upload
		script=furkUploadUrl
		prop=name_unescape			
	}
}

macrodef swemacro {
folder {
	matcher=<a href=\'(http://swesub\.tv/torrents-details[^\']+)\'.*?src=([^ ]+) .*?title=\'([^\']+)\'.*?<b>([^<]+)</b>.*?imdb\.com/title/tt(\d+)
	order=url,thumb,name,imdb
	prop=movieinfo,matcher_dotall
	macro=sweswitch
 }
}

channel Swesub.tv {
	folder {
	  name=TV Serier
	  url=http://swesub.tv/torrents.php?parent_cat=Tv-serie
	  macro=swemacro
	}
	folder {
	  name=Filmer
	  url=http://swesub.tv/torrents.php?parent_cat=Film
	  macro=swemacro
	}
	folder {
		name=A-Z
		type=atzlink
		url=http://swesub.tv/catalogue.php?letter=
		prop=atz_noslash,atz_lowercase
		folder {
			#<a href="torrents-details.php?id=3980&amp;hit=1"><b>A Christmas Carol 2009 Blu-ray Swesub</b></a>
			matcher=href=\"(torrents-details[^\"]+)\"><b>([^<]+)</b></a>
			order=url,name
			url=http://swesub.tv/
			macro=sweswitch
		}
		folder {
			#<a href="/catalogue.php?letter=b&amp;page=1"><b>Nästa sida
			matcher=href=\"([^\"]+)\"><b>(Nästa sida)
			order=url,name
			url=http://swesub.tv
			type=recurse
			prop=only_first,continue_name=Nästa sida,continue_limit=20
		}
	}
} 
