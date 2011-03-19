/*
    navix://channel?url=http%3A//example.com&referer=http%3A//example.com&agent=Mozilla

    This protocol uses MEncoder as the downloader/transcoder
    Only the following Navi-X output fields are supported:

        url     // required: media URL
        agent   // optional: HTTP user-agent
        referer // optional: HTTP referrer
        player  // optional: currently ignored

    Although most fields are optional, there is no point using this protocol unless
    at least one optional field is supplied.

    boolean values (none currently) can be set without a value e.g. navix://channel?url=http%3A//example.com&foo
    values *must* be URL-encoded
    keys are just alphanumeric, so don't need to be
*/

init {
    profile ('subs://') {
        pattern {
            protocol 'subs'
        }

        action {
            def mencoderArgs = []
            def pairs = $HTTP.getNameValuePairs($URI) // uses URLDecoder.decode to decode the name and value
            def seenURL = false
			
			mencoderArgs << '-mc' << '0.1'
			mencoderArgs << '-channels' << '6'

            for (pair in pairs) {
                def name = pair.name
                def value = pair.value

                switch (name) {
                    case 'url':
                        if (value) {
                            // quote handling is built in for MEncoder
                            $URI = value
                            seenURL = true
                        }
                        break
						
					
					case 'subs':
						if(value)
							mencoderArgs << '-sub' << quoteURI(value)
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
				/*	case 'subdelay':
						if(value)
							mencoderArgs << '-subdelay' << quoteURI(value)
						break*/
                    default:
                        log.warn("unsupported subs:// option: ${name}=${value}")
                }
            }
            if (seenURL) {
				//mencoderArgs << '-scodec' << 'copy'
				//$FFMPEG = $FFMPEG + mencoderArgs
				$TRANSCODER = $MENCODER + mencoderArgs
				//$TRANSCODER = ["C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe"] + mencoderArgs
				//$DOWNLOADER = $MENCODER + mencoderArgs
				//$TRANSCODER = $FFMPEG + mencoderArgs
            } else {
                log.error("invalid navix:// URI: no url parameter supplied: ${$URI}")
            }
        }
    }
}
