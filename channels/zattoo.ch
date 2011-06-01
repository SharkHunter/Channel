version=0.23

#http://tserver.zattoo.com/join?uuid=${uuid}&ticket_id=${ticketid}&f=${fvar}&channel=${channel}
scriptdef zattooScript {
   channel=s_url
   regex='=(.*)
   match s_cookie
   s_url='http://pro.gaebu.ch/index.php?q=aHR0cDovL3phdHRvby5jb20vdmlldw%3D%3D&hl=2e9
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
   url='http://pro.gaebu.ch/index.php?q=http%3A%2F%2Ftserver.zattoo.com%2Fjoin%3Fuuid%3D
   concat url uuid
   concat url '%26ticket_id%3D
   concat url ticket
   concat url '%26f%3D
   concat url f
   concat url '%26channel%3D
   concat url channel
   concat url '&hl=2e1
   s_url=url
   regex='"stream_url": "([^"]+)"
   scrape
   url=v1
   play
}

channel Zattoo {
   img=http://zattoo.com/files/Zattoo_RGB_150_neg.png
   login {
      # Login data
      url=http://pro.gaebu.ch/index.php?q=aHR0cHM6Ly96YXR0b28uY29tL2xvZ2lu
      user=email
      passwd=pw
      type=cookie
      params=remember=on
      matcher=pzuid
   }
   folder {
         # view
       #url=http://zattoo.com/view
	   url=http://pro.gaebu.ch/index.php?q=aHR0cDovL3phdHRvby5jb20vdmlldw%3D%3D&hl=2e9
       #class="name" href="/view/sf-2" 
       type=empty
       folder {
         url=dummy_url
         #original="/static/images/channels/b_42x24/BBC1.png" alt="BBC One England" 
         matcher=original=\"/[^\/]+/[^\/]+/[^\/]+/[^\/]+/([^\.]+)\.png\" alt=\"([^\"]+)\"[^\*]+?\s[^\*]+?(now-showing)\">([^\<]+)<         
         order=url,name+
         prop=name_separator=###0,
         media {
            script=zattooScript
            prop=live,
         }
      }
   }   
}


