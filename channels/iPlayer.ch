version=0.12

channel BBC iPlayer {
	img=https://encrypted-tbn0.google.com/images?q=tbn:ANd9GcSnChPOaWGDEVS_2lvcvHwFJkbwQ10RiInrkJwwaEatkBkFtrTz
	folder {
		pre_script=get_iplayer_update.bat
		url=dummy_url
		type=empty
		folder {
			name=A-Z
			type=ATZ
			#the url below has to be absolute; relative physical (file) url are known not to work with URLConnection class which is used in channels
			url=file:///tmp/list
			media {
				#the url below must match the url in channel_bbciplayer.groovy file and must be valid as per RFC 1738 (though pmsencoder allows for RFC 2396)
				matcher=<Name>([^<]+)</Name>\s*<index>[^<]*</index>\s*<type>[^<]*</type>\s*<Url>(.........mov)</Url>\s*<StreamImage>([^<]+)</StreamImage>\s*<Subtitle>[^<]*</Subtitle>\s*<Synopsis>[^<]*</Synopsis>
				order=name,url,thumb
				escript=get_iplayer2.bat
			}
		}
	}
}