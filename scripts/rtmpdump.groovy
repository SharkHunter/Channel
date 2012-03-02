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
            match { RTMPDUMP }
        }

        action {
            def rtmpdumpArgs = []
            def pairs = $HTTP.getNameValuePairs($URI) // uses URLDecoder.decode to decode the name and value
            def seenURL = false
			def mencoderArgs =[]
			def seenSub = false

			mencoderArgs << '-mc' << '0.1'
			mencoderArgs << '-channels' << '6'

            for (pair in pairs) {
                def name = pair.name
                def value = pair.value

                switch (name) {
                    case 'url': // deprecated
                    case '-r':
                    case '--rtmp':
                        if (value) {
                            $URI = quoteURI(value)
                            seenURL = true
                        }
                        break
					case 'subs':
						if(value) {
							mencoderArgs << '-sub' << quoteURI(value)
							seenSub = true
						}
						break
					case 'subcp':
						if(value)
							mencoderArgs << '-subcp' << quoteURI(value)
						break
					case 'subtext':
						if(value)
							mencoderArgs << '-subfont-text-scale' << quoteURI(value)
						break
					case 'subtext':
						if(value)
							mencoderArgs << '-subfont-outline' << quoteURI(value)
						break
					case 'subblur':
						if(value)
							mencoderArgs << '-subfont-blur' << quoteURI(value)
						break
					case 'subpos':
						if(value)
							mencoderArgs << '-subpos' << quoteURI(value)
						break
                    default:
                        rtmpdumpArgs << name
                        // not all values are URIs, but quoteURI() is harmless on Windows and a no-op on other platforms
                        if (value)
                            rtmpdumpArgs << quoteURI(value)
                }
            }

            if (seenURL) {
                // rtmpdump doesn't log to stdout, so no need to use -q on Windows
                $DOWNLOADER = "$RTMPDUMP -o $DOWNLOADER_OUT -r ${$URI}"
                $DOWNLOADER += rtmpdumpArgs
				if(seenSub) {
					$TRANSCODER = $MENCODER + mencoderArgs
				}
            } else {
                log.error("invalid rtmpdump:// URI: no -r or --rtmp parameter supplied: ${$URI}")
            }
        }
    }
}
