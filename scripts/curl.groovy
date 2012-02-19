script {
	profile ('CURL') {
		pattern {
		domains([  'gcdn.biz','megaupload.com', 'movshare.net', 'stream2k.com', 'bitload.com', 'novamov.com', 'divxstage.eu', 'divxstage.net' ])          
			
		}
		action {
			def CURL = pmsConf['curl.path']
			def cookie = pmsConf['cookie.path']
			
			def tmp = $URI.replaceAll(/\[/,/\\\[/).replaceAll(/\]/,/\\\]/)
			$URI = tmp
			
			//$URI = quoteURI($URI)
			$DOWNLOADER = "$CURL -s -S -b ${cookie} --location-trusted --output $DOWNLOADER_OUT ${$URI}"
		   //set '-cookies-file' : quoteURI(cookie)
		}
	}
}