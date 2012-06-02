version=0.12

channel RTMPGui {
   folder {
		name=A-Z
		type=ATZ
		url=http://apps.ohlulz.com/rtmpgui/list.xml
		prop=cache,cache_age=0
		media {
			matcher=<title><!\[CDATA\[([^\]]+)\]\]></title>\s*.*?\s*<swfUrl><!\[CDATA\[([^\]]+)\]\]></swfUrl>\s*<link><!\[CDATA\[([^\]]+)\]\]></link>\s*<pageUrl><!\[CDATA\[([^\]]+)\]\]></pageUrl>\s*<playpath><!\[CDATA\[([^\]]+)\]\]></playpath>\s*.*?\s*<advanced><!\[CDATA\[\]\]></advanced>
			order=name,swfUrl,url,pageUrl,playpath
		}
		media {
			matcher=<title><!\[CDATA\[([^\]]+)\]\]></title>\s*.*?\s*<swfUrl><!\[CDATA\[([^\]]+)\]\]></swfUrl>\s*<link><!\[CDATA\[([^\]]+)\]\]></link>\s*<pageUrl><!\[CDATA\[([^\]]+)\]\]></pageUrl>\s*<playpath><!\[CDATA\[([^\]]+)\]\]></playpath>\s*.*?\s*<advanced><!\[CDATA\[live=1\]\]></advanced>
			order=name,swfUrl,url,pageUrl,playpath
			prop=live
		}
		media {
			matcher=<title><!\[CDATA\[([^\]]+)\]\]></title>\s*.*?\s*<swfUrl><!\[CDATA\[([^\]]+)\]\]></swfUrl>\s*<link><!\[CDATA\[([^\]]+)\]\]></link>\s*<pageUrl><!\[CDATA\[([^\]]+)\]\]></pageUrl>\s*<playpath><!\[CDATA\[([^\]]+)\]\]></playpath>\s*.*?\s*<advanced><!\[CDATA\[-a ([^\]]+)\]\]></advanced>
			order=name,swfUrl,url,pageUrl,playpath,app
		}
		media {
			#matcher=<title><!\[CDATA\[([^\]]+)\]\]></title>\s*.*?\s*<swfUrl><!\[CDATA\[([^\]]+)\]\]></swfUrl>\s*<link><!\[CDATA\[([^\]]+)\]\]></link>\s*<pageUrl><!\[CDATA\[([^\]]+)\]\]></pageUrl>\s*<playpath><!\[CDATA\[([^\]]+)\]\]></playpath>\s*.*?\s*<advanced><!\[CDATA\[-v -a ([^\]]+)\]\]></advanced>
			matcher=<title><!\[CDATA\[([^>]+)\]\]></title>\s*.*?\s*<swfUrl><!\[CDATA\[([^>]+)\]\]></swfUrl>\s*<link><!\[CDATA\[([^>]+)\]\]></link>\s*<pageUrl><!\[CDATA\[([^>]+)\]\]></pageUrl>.*\s<playpath><!\[CDATA\[([^>]+)></playpath>.*\s*.*\s*.*<advanced><!\[CDATA\[-v -a ([^>]+)\]\]></advanced>
			order=name,swfUrl,url,pageUrl,playpath,app
			prop=live
		}
  }
}
