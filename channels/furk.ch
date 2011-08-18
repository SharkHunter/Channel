version=0.14

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
         folder {
#            <a class="button-large button-play" href="http://icd0q6s14tvhto8tuv82gg9ttb9j0t3ds40r71g.gcdn.biz/pls/JM56JAwhydBTrZo2Kw7FQCJxxfFEEjR9J2gZKiT7myqYD6g42PI7bMWCx49sSKKOyZC5kFkwQVDcckU3yP4SryQcf6eNKqam_CRwEXATzEA/Harry%20Potter%20and%20the%20Deathly%20Hallows%20Part%202%202011%20TS%20UnKnOwN.xspf">Play</a><br /> 
            matcher=a class=\"button-large button-play\" href=\"([^\"]+)\">(Play)<
            order=url
            type=empty
            media {
               matcher=<title>([^<]+)</title>\s+<location>([^<]+)</location>
               order=name,url
            }
         }
         media {
            # <a class="playlist-item" href="http://ie9hajrspg5sg9mgqs4s1tf9nb9j0t3ds40r71g.gcdn.biz/d/R/KNoWaBGevj73PXNXuxaZiISdFFw__hnNo159OhQLI5epxWrSyuW_X1oi88NmdnIZ/01_Enter_Sandman.mp3" class="first" title="Metallica - Metallica (1991)/Metallica - Metallica/01 Enter Sandman.mp3">Metallica - Metallica (1991)/Metallica - Metallica/01 Enter Sandman.mp3</a> 
            matcher=a class=\"playlist-item\" href=\"([^\"]+)\" .*?title=\"([^\"]+)\"
            order=url,name
         }
	}
  }
}
