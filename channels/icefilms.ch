version=0.16
## NOTE!!!
## 
## We match out both the megavideo play link and megaupload link
## here. Both are streamable, but the megaupload is of course subject to
## limits etc. Try for yourself which is best.
## 
## Megavideo link requiers that you have PMSEncoder >=1.4.0
## and that you install the "get_flash_video" script +
## the megavideo script from PMSEncoder
##
macrodef mediaMacro {
	folder {
           # last link
           # Proceed to: <a href="http://www.megaupload.com/?d=cfu23kvq&w=631&h=392"
           matcher=Proceed to: <a href=\"([^\"]+)\"
           order=url
           type=empty
           media {
			name=MegaVideo
		    matcher=<a href=\"([^\"]+)\"\s+class=\"down_links_mv\"
			order=url
			subtitle=s4u
			prop=concat_name=rear,name_separator= ,name_index=2,name_mangle=([^\(]+)\(
           }
		   media {
			#<div class="down_butt_pad1" style="display:none;" id="downloadlink"><a href="http://www820.megaupload.com/files/e852a3a714538767347d5866d6ad9d7c/big_bang_theory.1x01.dvdrip_xvid-fov.H2020.dvd4arab.com.avi" class="down_butt1"></a>
			matcher=<div class=\"down_butt_pad1\" style=\"display:none;\" id=\"downloadlink\"><a href="([^\"]+)"
			name=MegaUpload
			order=url
			subtitle=s4u
			prop=concat_name=rear,name_separator= ,name_index=2,name_mangle=([^\(]+)\(
           }   
      }
}

macrodef tvMedia {
folder {
           # last link
           # Proceed to: <a href="http://www.megaupload.com/?d=cfu23kvq&w=631&h=392"
           matcher=Proceed to: <a href=\"([^\"]+)\"
           order=url
           type=empty
           media {
			name=MegaVideo
		    matcher=<a href=\"([^\"]+)\"\s+class=\"down_links_mv\"
			order=url
			subtitle=s4u
			prop=concat_name=rear,name_separator= ,name_index=3+2,name_mangle=([^\(]+)\([^ ]+ (.*)
           }
		   media {
			#<div class="down_butt_pad1" style="display:none;" id="downloadlink"><a href="http://www820.megaupload.com/files/e852a3a714538767347d5866d6ad9d7c/big_bang_theory.1x01.dvdrip_xvid-fov.H2020.dvd4arab.com.avi" class="down_butt1"></a>
			matcher=<div class=\"down_butt_pad1\" style=\"display:none;\" id=\"downloadlink\"><a href="([^\"]+)"
			name=MegaUpload
			order=url
			subtitle=s4u
			prop=concat_name=rear,name_separator= ,name_index=3+2,name_mangle=([^\(]+)\([^ ]+ (.*)
           }   
      }
}

macrodef tvMacro {
   folder {
      # Series
      #<img class=star><a href=/tv/series/1/565>&#x27;Til Death (2006)</a>
      matcher=<img class=star><a href=([^>]+)>([^<]+)</a>
      order=url,name
      url=http://www.icefilms.info
      folder {
         # Episodes 
         #img class=star><a href=/ip.php?v=124783&>Jan 31. Bill Gates</a>
         matcher=<img class=star><a href=([^>]+)>([^<]+)</a>
         order=url,name
         url=http://www.icefilms.info
         folder {
            # First link fetch
            #<a href="/components/com_iceplayer/video.php?h=374&w=631&vid=21739&img=http://www.icefilms.info/images/vid_images/thesimpsons.jpg&ttl=The+Simpsons+1x01+Simpsons+Roasting+on+an+Open+Fire+%281989%29" 
            matcher=<a href=\"(/comp[^\"]+)\" .*
            order=url
            url=http://www.icefilms.info
            type=empty
            folder {
               # 2nd link
               #href=http://www.icefilms.info/components/com_iceplayer/GMorBMlet.php?vid=21739&img=http://www.icefilms.info/images/vid_images/thesimpsons.jpg&ttl=The+Simpsons+1x01+Simpsons+Roasting+on+an+Open+Fire+%281989%29&sourceid=21739&url=http://www.megaupload.com/?d=cfu23kvq&w=631&h=392>Source #1:
               matcher=href=([^>]+)>Source #
               order=url
              #macro=mediaMacro
			  macro=tvMedia
            }
         }
      }
   }
}

macrodef movieMacro {
	folder {
		# Movies
		#<img class=star><a href=/tv/series/1/565>&#x27;Til Death (2006)</a>
		matcher=<img class=star><a href=([^>]+)>([^<]+)</a>
		order=url,name
		url=http://www.icefilms.info
		folder {
			matcher=<a href=\"(/comp[^\"]+)\" .*
            order=url
            url=http://www.icefilms.info
			type=empty
			folder {
				matcher=;\" href=([^>]+)>([^<]+)<
				order=url,name
				macro=mediaMacro
			}
		}
	}
}

channel IceFilms {
   img=http://img.icefilms.info/logo.png
   folder {
      name=TV Shows
	  folder {
		#Popular
		name=Popular
		url=http://www.icefilms.info/tv/popular/1
		macro=tvMacro
	  }
	  folder {
		name=A-Z
		type=atzlink
		url=http://www.icefilms.info/tv/a-z
		prop=other_string=1,
		macro=tvMacro
	  }
	  folder {
		#Rating
		name=Rating
		url=http://www.icefilms.info/tv/rating/1
		macro=tvMacro
	  }
	  folder {
		#Release
		name=Release
		url=http://www.icefilms.info/tv/release/1
		macro=tvMacro
	  }
   }
   
   folder {
      name=Movies
	  folder {
		name=Popular
		url=http://www.icefilms.info/movies/popular/1
		macro=movieMacro
	  }
      folder {
		name=A-Z
		type=atzlink
		url=http://www.icefilms.info/movies/a-z
		prop=other_string=1,
		macro=movieMacro
	  }
	  folder {
		#Rating
		name=Rating
		url=http://www.icefilms.info/movies/rating/1
		macro=movieMacro
	  }
	  folder {
		#Release
		name=Release
		url=http://www.icefilms.info/movies/release/1
		macro=movieMacro
	  }
	}
} 
