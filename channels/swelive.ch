version=0.14

macrodef cmHLSMedia {
		media {
			matcher=BANDWIDTH=(\d+)\d###lcbr###3###rcbr###+.*?\n([^\n]+)
			order=name,url
			prop=matcher_dotall,append_name=Kbps,relative_url=path,name_separator=###0,name_index=0
		}
}

channel SweLive {
	folder {
		type=empty		
		media {
			name=TV4 Sport
			img=http://upload.wikimedia.org/wikipedia/en/9/9e/TV4_Sport_logo.png
			url=http://tvhdslive-f.akamaihd.net/i/EDC2XTRAHDS_1@108871/index_4_av-b.m3u8?sd=10&rebase=on
		}		
		folder {
			name=CMore Live 1
			url=http://cmorehlsedc-i.akamaihd.net/hls/live/208150/cmlivehd/cmlivehd.m3u8
			macro=cmHLSMedia
		}
		folder {
			name=CMore Live 2
			url=http://cmorehlsedc-i.akamaihd.net/hls/live/208151/cmlive2/cmlive2.m3u8
			macro=cmHLSMedia
		}
		folder {
			name=CMore Live 3
			url=http://cmorehlsedc-i.akamaihd.net/hls/live/208152/cmlive3/cmlive3.m3u8
			macro=cmHLSMedia
		}
		folder {
			url=http://cmorehlsedc-i.akamaihd.net/hls/live/208153/cmlive4/cmlive4.m3u8
			name=CMore Live 4
			macro=cmHLSMedia
		}
		folder {
			url=http://cmorehlsedc-i.akamaihd.net/hls/live/208145/cmsporthd/cmsporthd.m3u8
			name=CMore Sport
			macro=cmHLSMedia
		}
		folder {
			url=http://cmorehlsedc-i.akamaihd.net/hls/live/208154/cmfotboll/cmfotboll.m3u8
			name=CMore Fotboll
			macro=cmHLSMedia
		}	
	}
}