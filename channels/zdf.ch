version=0.2	
macrodef  asxAbspielen {
             media {
                 # We pick the asx files which means we got to do a little extra fetching
                 #a href="http://wstreaming.zdf.de/zdf/300/110113_dde.asx" class="play" target="_blank">Abspielen</a>
                 matcher=<a href=\"(.*asx)\"[^>]+>Abspielen</a>
                 order=url
                 type=asx
            }
	    type=empty
}

macrodef bMatcher {
           # http://www.zdf.de/ZDFmediathek/kanaluebersicht/aktuellste/330?bc=saz;saz6&flash=off
           matcher=<b><a href=\"([^\"]+)\"[^>]*>([^<]+)<br.*</a>
           url=http://www.zdf.de/
           order=url,name+
}

macrodef liMatcher {
           # http://www.zdf.de/ZDFmediathek/kanaluebersicht/aktuellste/330?bc=saz;saz6&flash=off
           matcher=<li><a href=\"([^\"]+)\"[^>]*>- ([^<]+)</a>
           url=http://www.zdf.de/
           order=url,name+
}

macrodef oneLayer {
       folder {
           macro=bMatcher
           macro=asxAbspielen
       }
}

macrodef twoLayer {
         folder {
            macro=bMatcher
            macro=oneLayer
         }
}

macrodef threeLayer {
         folder {
            macro=bMatcher
            macro=twoLayer
         }
}

channel ZDF {
	img=http://www.zdf.de/ZDFmediathek/img/logo_mediathek.gif
        folder {
                url=http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst?flash=off
                name=Sendung verpasst ?
                folder {
                        macro=liMatcher
                        macro=oneLayer
                }
        }
        folder {
                url=http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-a-bis-z?flash=off
                name=Sendungen A-Z
                folder {
                        macro=liMatcher
			macro=twoLayer
                }
        }

        folder {
                url=http://www.zdf.de/ZDFmediathek/hauptnavigation/nachrichten?flash=off
                name=Nachrichten
                macro=oneLayer
		folder {
			name=Ganze Sendungen
			url=http://www.zdf.de/ZDFmediathek/hauptnavigation/nachrichten/ganze-sendungen?flash=off
			macro=twoLayer
		}
        }
        folder {
                url=http://www.zdf.de/ZDFmediathek/hauptnavigation/rubriken?flash=off
                name=Rubriken
		macro=threeLayer
        }

        folder {
                url=http://www.zdf.de/ZDFmediathek/hauptnavigation/themen?flash=off
                name=Themen
		macro=twoLayer
        }
        folder {
                url=http://www.zdf.de/ZDFmediathek/hauptnavigation/sender?flash=off
                name=Sender - Aktuellste Sendungen
		macro=twoLayer
        }
}


