// By. S.leop -v1.1 -03/06/2011

script {
	profile ('Cuevana') {
		pattern {   
		match $URI: [      
			 '(?<URI>http://www.megaupload.com/[^\\/]+)/(?<serie>series)/(?<movcode>[^\\/]+)/(?<movname>[^\\/]+)/(?<movname2>[^\\/]+)/?',
			 '(?<URI>http://www.megaupload.com/[^\\/]+)/(?<pelicula>peliculas)/(?<movcode>[^\\/]+)/(?<movname>[^\\/]+)/?'			
			]
			}
		
		action {


// kills mencoder.exe before starting to assure that no double download is performed - not to be applied to a standard groovy				
			def killoldproc = "TaskKill /IM mencoder.exe /F /T"
			def proc = killoldproc.execute()  
			proc.waitForOrKill(2500) 
/////////////////////////////////////////////////////////////////////////////////////////////////
			                         
			def CURL = pmsConf['curl.path']
			def chandir = pmsConf['channels.path']		
			def cookie = pmsConf['cookie.path']
			def cuecookie = new File("${cookie}")
			def tmpfile = new File("${chandir}\\data\\tmpfile")
			def credfile = pmsConf['cred.path']
			def credentials = new File("${credfile}")
			
			if (serie) {
			movname = "${movname}_${movname2}" 
			}
			

// MEGAUPLOAD COOKIE - Check if cookie has a megaupload account, or create cookie if needed

			if (cuecookie.exists())	{
         		cuecookie = new File("${cookie}").text		
			scrape	"(?<megaok>megaupload\\.com)", [ source: cuecookie]

			}


			if (!megaok) {

			credentials = new File("${credfile}").text
			scrape "[i|I]ce[f|F]ilms[^\\=]*?\\=(?<megausername>[^\\,]+),(?<megapassword>[^\\s\\n]+)\\s?\\n?", [ source: credentials ]

			if (megausername) {
			def megadata = quoteURI("username=${megausername}&password=${megapassword}&login=1&redir=1")
			def megalogin = quoteURI('http://www.megaupload.com/?c=')
			def getmegacookie = "curl -4 -b ${cookie} -c ${cookie} --location-trusted --no-buffer --tcp-nodelay --no-keepalive -d ${megadata} --url ${megalogin} -o ${tmpfile}"
			def procmegacookie = getmegacookie.execute()
			Thread.start { System.err << procmegacookie.err }
			procmegacookie.waitForOrKill(5000)
			}
			}

// COOKIE SECTION - If exists reads uuid from current - otherwise creates a new cookie in order to have the user's uuid



			def cuesubfile = "${chandir}\\data\\cue_${movname}.srt"
			def cuesubs = new File("${cuesubfile}")
			
			if (!cuesubs.exists()) {			

			credentials = new File("${credfile}").text
			scrape "[\\n](?:channels?\\.cuevana[^\\=]*?\\=(?<usuario>[^\\d]+?),(?<password>[\\d]+))", [ source: credentials ]			 
			
			def cuedata = quoteURI("usuario=${usuario}&password=${password}&ingresar=true&recordarme=si")
						
			def getcookie = "curl -4 -b ${cookie} -c ${cookie} --location-trusted --no-buffer --tcp-nodelay --no-keepalive -d ${cuedata} http://www.cuevana.tv/login_get.php -o ${tmpfile}"
			def proccookie = getcookie.execute()
			Thread.start { System.err << proccookie.err }
			proccookie.waitForOrKill(4000)
			
         		cuecookie = new File("${cookie}").text
			scrape	"cuevana\\.tv[^\\*]+?(?<cookieok>cue_id)", [ source: cuecookie]

			if (!cookieok) {
	log.error("CUEVANA: Need to define cred file with your cuevana id and pwd")
			}



// Get the subtitles file and store it in Channels data folder
			
			def suburl = quoteURI("http://www.cuevana.tv/botlink_des.php?id=${movcode}")
			def getsublink = "$CURL -4 -b ${cookie} --location-trusted --no-buffer --tcp-nodelay --no-keepalive --url ${suburl} -o ${tmpfile}"
			
			if (serie) {
			suburl = quoteURI("http://www.cuevana.tv/botlink_des.php?id=${movcode}&serie=true")
			getsublink = "$CURL -4 -b ${cookie} --location-trusted --no-buffer --tcp-nodelay --no-keepalive --url ${suburl} -o ${tmpfile}"
			} 
		
			def procsublink = getsublink.execute()
			Thread.start { System.err << procsublink.err }
			procsublink.waitForOrKill(3000)

			def cuetmpfile = new File("${tmpfile}").text
		 	scrape "href=\\'(?<sublink>/[^\\']+)\\'", [ source: cuetmpfile ]
		 	scrape "href=\\'(?<login>\\#loginblock)\\'", [ source: cuetmpfile ]
	
			if (sublink)	{
			sublink = quoteURI("http://www.cuevana.tv${sublink}")
			def getsubfile = "$CURL -4 -b ${cookie} --location-trusted --no-buffer --tcp-nodelay --no-keepalive --url ${sublink} -o ${cuesubfile}"
			def procsubfile = getsubfile.execute()
			Thread.start { System.err << procsubfile.err }
			procsubfile.waitForOrKill(5000)
				}
			else {			
	log.error("Invalid Cuevana:  could not download subtitles for movie: ${movname} - trying to regenerate cookie")
            		}		
            		if (login) {
            		credentials = new File("${credfile}").text
			scrape "[\\n](?:channels?\\.cuevana[^\\=]*?\\=(?<usuario>[^\\d]+?),(?<password>[\\d]+))", [ source: credentials ]			 
			def cuedata2 = quoteURI("usuario=${usuario}&password=${password}&ingresar=true&recordarme=si")
						
			def getcookie2 = "curl -b ${cookie} -c ${cookie} --location-trusted --no-buffer --tcp-nodelay --no-keepalive -d ${cuedata2} http://www.cuevana.tv/login_get.php -o ${tmpfile}"
			def proccookie2 = getcookie2.execute()

            		}
            
		}

			
// define  mencoder args for subs
if (cuesubs.exists()) {
			
			cuesubfile = quoteURI("${cuesubfile}")
           def mencoderArgs = []
           
		        mencoderArgs << '-mc' << '0.1'
			mencoderArgs << '-channels' << '2'
			mencoderArgs << '-sub' << "${cuesubfile}"
			mencoderArgs << '-subcp' << '"ISO-8859-1"'
			mencoderArgs << '-subfont-text-scale' << '"2"'
		//	mencoderArgs << '-subfont-outline' <<
			mencoderArgs << '-subfont-blur' << '"1"'
			mencoderArgs << '-subpos' << '"95"'
		//	mencoderArgs << '-subdelay' <<
				
				//$FFMPEG = $FFMPEG + mencoderArgs
				$TRANSCODER = $MENCODER + mencoderArgs
				//$DOWNLOADER = $MPLAYER + mencoderArgs
				//$TRANSCODER = ["win32\\mencoder_mt.exe"] + mencoderArgs + "-o $TRANSCODER_OUT $DOWNLOADER_OUT"
				//$DOWNLOADER = $MENCODER + mencoderArgs
				//$TRANSCODER = $FFMPEG + mencoderArgs
			} else {
			
		//	$TRANSCODER = $FFMPEG
			log.info("Cuevana:  there seems to be no subtitle for movie: ${movname} - ")
			}
			
			
	}
}
}