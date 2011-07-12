version=0.12
channel Veetle {
   folder {
      url=http://www.veetle.com/index.php/listing/index/sports/popular/0
      type=empty
      folder {
         # Categories
         #<a href="/index.php/listing/index/all/popular/0" onclick="return VEETLE.listing.changeCategory(0);">all<i>
         matcher=a href=\"([^\"]+)\" onclick[^>]+>([^<]+)<i>
         order=url,name
         url=http://www.veetle.com
         media {
            #Real Channels
#            <a 
#            class="channelLogo" 
#           href="/index.php/channel/view/4cc8d0747ccac" 
##            onclick="VEETLE.ChannelList.flatRedirect('4cc8d0747ccac'); return false;"
#           title="Sin The Movie">
#           <img class="channelLogoImg" src="http://veetle.com/channel_logos/4cc8d0747ccac/logo_0_20101028123245_4cc9cfdde7fbb.jpg" />
  #      </a>
            matcher=a[^c]*class=\"channelLogo\"[^h]*href=\"([^\"]+)\"[^o]*onclick=\"[^\"]+\"[^t]*title=\"([^\"]+)\"
            order=url,name
            url=http://www.veetle.com
            script=veetleScript
         }
         folder {
            # Next
            #<a href="/index.php/listing/index/all/popular/9" onclick="return VEETLE.listing.traversePage(2);" class="spriteGradient gradientShine">Next
            matcher=<a href=\"([^\"]+)\" [^>]+>(Next) 
            order=url,name
            url=http://www.veetle.com
            type=recurse
         }
      }
   }
}

