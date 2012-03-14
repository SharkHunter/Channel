version=0.31
macrodef sopMacro {
	media {
		# Sopcast media
		name=Sopcast
		img=http://www.sopcast.com/images/sopcast-log.gif
		order=url,name+
		prop=name_separator= ,prepend_name,use_conf_thumb
		matcher=href=\"(sop:[^\"]+)\" [^>]+>[^<]+</a>\s*<a[^>]+>[^<]+</a>\s*</td>\s*<td>([^<]+)<
	}
}

macrodef pplMacro {
	media {
		# PPLive media
		name=PPLive
		order=url,name+
		prop=name_separator= ,prepend_name
		matcher=href=\"(synacast:[^\"]+)\" [^>]+>[^<]+</a>\s*</td>\s*<td>([^<]+)<
	}
}

scriptdef myp2p_un {
	regex='&amp;
	replace s_url '&
	url=s_url
	play
}

channel MyP2P {
	img=http://www.myp2p.eu/gfx/logo.gif
	folder {
		# Sports Main
		name=Sports
		url=http://www.wiziwig.tv/index.php?part=sports
		post_script=myp2p_un
		folder {
			# Individual Sports
			matcher=href=\"(/competition\.php\?part=sports&amp;dis[^\"]+)\">([^<]+)<
			order=url,name
			url=http://www.wiziwig.tv
			prop=name_separator=-,peek
			post_script=myp2p_un
			folder {
				# Event list
				matcher=<td class=\"home\"><[^>]+>([^<]+)<[^>]+></td>\s*<td>vs\.</td>\s*<td class="away"><[^>]+>([^<]+)<[^>]+></td>\s*<[^>]+><a .*? href=\"(/broadcast[^\"]+)\"
				#matcher=href=\"(/broadcast[^\"]+)\"
				order=name,name,url
				url=http://www.wiziwig.tv
				prop=name_separator=-
				macro=pplMacro
				macro=sopMacro
			}
		}
	}
	folder {
		# Now playing
		name=Now playing
		url=http://www.wiziwig.tv/index.php?part=sports
		prop=name_separator=-,peek
		folder {
				matcher=<td class=\"home\"><[^>]+>([^<]+)<[^>]+></td>\s*<td>vs\.</td>\s*<td class="away"><[^>]+>([^<]+)<[^>]+></td>\s*<[^>]+><a .*? href=\"(/broadcast[^\"]+)\"
				order=name,name,url
				url=http://www.wiziwig.tv/
				prop=name_separator=-
				macro=pplMacro
				macro=sopMacro
			
		}
	}
}
		