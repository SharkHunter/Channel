version=0.21

proxydef p1 {
	addr=82.195.239.19
	port=80
}

#http://tserver.zattoo.com/join?uuid=${uuid}&ticket_id=${ticketid}&f=${fvar}&channel=${channel}
scriptdef zattooScript {
	channel=s_url
	regex='=(.*)
	match s_cookie
	s_url='http://zattoo.com/view
	uuid=v1
	regex='getTime\(\) \- ([0-9]+)
	scrape
	f=v1
	regex='ticket_id = "([^"]+)"
	match htmRaw
	ticket=v1
	regex='setStaticAssets[^"]+"([^"]+)"
	match htmRaw
	swfVfy='http://zattoo.com
	concat swfVfy v1
	url='http://tserver.zattoo.com/join?uuid=
	concat url uuid
	concat url '&ticket_id=
	concat url ticket
	concat url '&f=
	concat url f
	concat url '&channel=
	concat url channel
	s_url=url
	regex='"stream_url": "([^"]+)"
	scrape
	url=v1
	play
}

channel Zattoo {
	proxy=p1
	img=http://zattoo.com/files/Zattoo_RGB_150_neg.png
	login {
		# Login data
		url=https://zattoo.com/login
		user=email
		passwd=pw
		type=cookie
		params=remember=on
		matcher=pzuid
	}
	folder {
         # view
		 url=http://zattoo.com/view
		 #class="name" href="/view/sf-2" 
		 type=empty
		 folder {
			url=dummy_url
			#original="/static/images/channels/b_42x24/BBC1.png" alt="BBC One England" 
			matcher=original=\"/[^\/]+/[^\/]+/[^\/]+/[^\/]+/([^\.]+)\.png\" alt=\"([^\"]+)\"
			order=url,name
			media {
				script=zattooScript
				prop=live,
			}
		}
	}	
}


