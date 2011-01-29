script {
	profile ('RTMP(E)') {
		pattern {
			protocol ([ 'rtmp', 'rtmpe' ])
		}

		action {
			$URI = "\""+$URI+"\""
			def $X=$URI.split("!!!pms_ch_dash_y!!!") as String[]
			if($X.length>1)
				$URI=$X[0]+"\""+" -y \""+$X[1]
			$DOWNLOADER = "C:\\rtmpdump.exe -o $DOWNLOADER_OUT -r ${$URI}"
		}
	}
}
