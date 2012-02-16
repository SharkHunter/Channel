/*
    rtmpdump://channel?-v&-r=http%3A//example.com&-y=yvalue&-W=Wvalue

    -o is set automatically
    -r or --rtmp is required
    boolean values can be set without a value e.g. rtmpdump://channel?url=http%3A//example.com&--live&--foo=bar
    values *must* be URL-encoded
    keys can be, but hyphens are not special characters, so they don't need to be
*/

init {
    profile ('rtmpdump://') {
        pattern {
            protocol 'rtmpdump'
         //   match { RTMPDUMP }
        }

        action {
            def ffmpegArgs = []
            def pairs = $HTTP.getNameValuePairs($URI) // uses URLDecoder.decode to decode the name and value
            def seenURL = false
			def args = ''
			def swfUrl=''

            for (pair in pairs) {
                def name = pair.name
                def value = pair.value
				
                switch (name) {
                    case 'url': // deprecated
                    case '-r':
                    case '--rtmp':
					case 'rtmp':
                        if (value) {
                            $URI = value//quoteURI(value)
                            seenURL = true
                        }
                        break
					case '-y':
						args+=' playpath='+value
						break
					case '-s':
						 swfUrl=' swfUrl='+value
						break
					case '-a':
						args+=' app='+value
						break
					case '-W':
					case 'swfVfy':
					    swfUrl=' swfUrl='+value
					case '--swfVfy':
						args+=' swfVfy=1'
						break
					case '-v':
						args+=' live=1'
						break
                    default:
                       // ffmpegArgs << name
                        // not all values are URIs, but quoteURI() is harmless on Windows and a no-op on other platforms
                        if (value)
                            //rtmpdumpArgs << quoteURI(value)
						args+=' '+name+"="+value
                }
            }

            if (seenURL) {
                // rtmpdump doesn't log to stdout, so no need to use -q on Windows
				$PARAMS.waitbeforestart = 2000L
				$URI+=args+swfUrl
               $TRANSCODER = $FFMPEG + ffmpegArgs
			   /*$DOWNLOADER = "$RTMPDUMP -o $DOWNLOADER_OUT -r ${$URI}"
                $DOWNLOADER += rtmpdumpArgs*/
            } else {
                log.error("invalid rtmpdump:// URI: no -r or --rtmp parameter supplied: ${$URI}")
            }
        }
    }
}