script {
	profile ('Putlocker') {
		pattern {
		match $URI: 'putlocker\\.com/file/(?<putcode>[^\\W]+)'
		
		}
		
		action {
			def CURL = pmsConf['curl.path']
			def cookiepath = pmsConf['cookie.path']
			def chandir = pmsConf['channels.path']
			def tmpfile = new File("${chandir}\\data\\tmpfile")

// kills mencoder.exe and ffmpeg.exe before starting to assure that no double download is performed - not to be applied to a standard groovy	
			def killoldproc = "TaskKill /IM mencoder.exe /IM ffmpeg.exe /F /T"
			def proc = killoldproc.execute()  
			proc.waitForOrKill(2500)	
			
			scrape 'type=\\"hidden\\" value=\\"(?<puthash>[^\"]+)\\"'
			def putdata = quoteURI("hash=${puthash}&confirm=Continue+as+Free+User")
			def putsession = "curl -4 -b ${cookiepath} -c ${cookiepath} --location-trusted --no-buffer --tcp-nodelay -d ${putdata} --url ${$URI} -o ${tmpfile}"
			def procputsess = putsession.execute()
			Thread.start { System.err << procputsess.err }
			procputsess.waitForOrKill(7000)
			
			def puttmpfile = new File("${tmpfile}").text
			scrape "playlist: \\'(?<geturi>[^\\']+)\\'", [ source: puttmpfile ]
			
			def putget = quoteURI("http://www.putlocker.com${geturi}")
			def putvideofile = "curl -4 -b ${cookiepath} --location-trusted --no-buffer --tcp-nodelay --url ${putget} -o ${tmpfile}"
			def procputget = putvideofile.execute()
			Thread.start { System.err << procputget.err }
			procputget.waitForOrKill(6000)


			puttmpfile = new File("${tmpfile}").text
			scrape 'url=\\"(?<URI>http://[^\\"]+)"', [ source: puttmpfile ]
			
			$URI = quoteURI($URI)
			$DOWNLOADER = "$CURL -s -S --raw -b ${cookiepath} --location-trusted --output $DOWNLOADER_OUT ${$URI}"
		}
	}
}


