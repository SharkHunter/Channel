version=0.12

channel Furk {
	login {
		url=http://www.furk.net/login/login/
		passwd=pwd
		user=login
		type=cookie		
		params=url=&gigya_uid=
	}
	folder {
		url=http://www.furk.net/users/files/finished
		type=empty
		folder {
			matcher=a href=\"(/df/[^\"]+)\">([^<]+)<
			order=url,name
			url=http://www.furk.net/
			media {
				# <a class="playlist-item" href="http://ie9hajrspg5sg9mgqs4s1tf9nb9j0t3ds40r71g.gcdn.biz/d/R/KNoWaBGevj73PXNXuxaZiISdFFw__hnNo159OhQLI5epxWrSyuW_X1oi88NmdnIZ/01_Enter_Sandman.mp3" class="first" title="Metallica - Metallica (1991)/Metallica - Metallica/01 Enter Sandman.mp3">Metallica - Metallica (1991)/Metallica - Metallica/01 Enter Sandman.mp3</a> 
				matcher=a class=\"playlist-item\" href=\"([^\"]+)\" .*?title=\"([^\"]+)\"
				order=url,name
			}
			media {
				# <a class="dl_link" href="http://gk40cbrpv46mvlvul6r49rea3b9j0t3ds40r71g.gcdn.biz:30084/d/r/P6MBnvpenhLgDZWAjA_nRO-gwKdVuJRdAKY8FGjkM_Xzss9zYirWoFqERGUwpSXjdGaFW7_r4uaDGzEQ38Gkm7I_u5mzteC7WKo8HMBAqKE/Rise.of.the.Planet.of.the.Apes.2011.TS.XviD-NOVA.avi" title="http, port# 30084">1</a>,
				matcher=a class=\"dl[^\"]+\" href=\"([^\"]+)\" .*?>([^<]+)<
				order=url,name
- Hide quoted text -
			}
		}
	}
}