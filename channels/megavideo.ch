version=0.11

channel MegaLinks {
   img=http://www.redditstatic.com/icon-touch.png
   subscript=megaSubs
   folder {
	  name=MegaVideos
	  url=http://www.reddit.com/r/megavideos
	  media {
		#<a class="title " href="https://mega.co.nz/#!wQIkkDoQ!CrULFFQsydkWezxNhNTIpg8jjCUKwI_bRVPDzMXTjt4" >[TV Series] Police Squad! All Episodes of the Original Naked Gun Series 1982 (1.51 GB)</a>&
		matcher=a class=\"title[^\"]+\" href=\"(https://mega.co.nz/[^\"]+)\"[^>]+>([^<]+)</a>
		order=url,name
		prop=do_resolve
	  }
   }
}

