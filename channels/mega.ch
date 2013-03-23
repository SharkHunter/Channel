version=0.1

scriptdef megaScript {
	url='precoder://mega.py####
	concat url s_url
	play
}

channel Mega {
   img=https://eu.static.mega.co.nz/images/logo.png
   resolve {
	  matcher=(mega\.co\.nz)
	  action=resolved
   }
   folder {
		type=action
		action_name=resolved
		url=dummy_url
		media {
			script=megaScript
			prop=ignore_save
		}
	}
}

