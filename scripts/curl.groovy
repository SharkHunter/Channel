script {
	profile ('CURL') {
		pattern {
		domains([ 'megaupload.com', 'movshare.net', 'stream2k.com', 'bitload.com', 'novamov.com', 'divxstage.eu', 'divxstage.net' ])          
			
		}
		action {
			def CURL = pmsConf['curl.path']
			def cookie = pmsConf['cookie.path']
			
			$URI = quoteURI($URI)
			$DOWNLOADER = "$CURL -s -S -b ${cookie} --location-trusted --output $DOWNLOADER_OUT ${$URI}"       
		}
	}
}