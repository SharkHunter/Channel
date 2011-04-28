script {
	def CURL = 'c:\\curl\\curl.exe'
	def cf = 'c:\\tmp\\inst_tst\\cookies'
	profile ('CURL') {
		pattern {         
			domain 'megaupload.com'
		}
		action {
			$DOWNLOADER = "$CURL -s -S --cookie $cf --location-trusted --output $DOWNLOADER_OUT ${$URI}"       
		}
	}
}