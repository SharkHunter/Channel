version=0.10

channel RTMPGui {
   #img=http://www.furk.net/img/logo.png?249
   folder {
		name=A-Z
		type=ATZ
		url=http://apps.ohlulz.com/rtmpgui/list.xml
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
			matcher=<title><!\[CDATA\[([^\]]+)\]\]></title>\s*.*?\s*<swfUrl><!\[CDATA\[([^\]]+)\]\]></swfUrl>\s*<link><!\[CDATA\[([^\]]+)\]\]></link>\s*<pageUrl><!\[CDATA\[([^\]]+)\]\]></pageUrl>\s*<playpath><!\[CDATA\[([^\]]+)\]\]></playpath>\s*.*?\s*<advanced><!\[CDATA\[-v -a ([^\]]+)\]\]></advanced>
			order=name,swfUrl,url,pageUrl,playpath,app
			prop=live
		}
  }
}
