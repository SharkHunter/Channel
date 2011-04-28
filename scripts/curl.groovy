script {
	profile ('CURL') {
		pattern {         
			domain 'megaupload.com'
		}
		action {
			def CURL = pmsConf['curl.path']
			def cf = pmsConf['cookie.path']
			$DOWNLOADER = "$CURL -s -S --cookie $cf --location-trusted --output $DOWNLOADER_OUT ${$URI}"       
		}
	}
}