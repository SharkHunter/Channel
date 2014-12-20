version=0.41
macrodef sopMacro {
	media {
		# Sopcast media
		name=Sopcast
		img=http://www.sopcast.com/images/sopcast-log.gif
		order=url,name+
		prop=name_separator= ,prepend_name,use_conf_thumb,do_resolve
		matcher=href=\"(sop:[^\"]+)\" [^>]+>[^<]+</a>\s*<a[^>]+>[^<]+</a>\s*</td>\s*<td>([^<]+)<
	}
}


macrodef aceMacro {
	media {
		name=AceStream
		img=http://static.torrentstream.org/sites/acestream/img/ACE-logo.png
		matcher=href=\"acestream://([^\"]+)\".*?<td>([^<]+)</td>
		order=url,name		
		prop=matcher_dotall,append_url=/stream.mp4,prepend_url=http://127.0.0.1:8200/pid/,prepend_name=Ace,name_separator=###0,use_conf_thumb
	}
}

scriptdef myp2p_un {
	regex='&amp;
	replace s_url '&
	url=s_url
	play
}

channel Wiziwig {
	img=http://www.wiziwig.tv/gfx/Wiziwig_logo.jpg	
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
				#macro=sopMacro
				macro=aceMacro
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
				#macro=sopMacro
				macro=aceMacro
			
		}
	}
}
		