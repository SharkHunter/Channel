// XXX this script needs to be loaded before get_flash_videos.groovy (currently a check script)

script {
    // redirect Megaupload links to Megavideo
    /*
    profile ('Megaupload') {
        pattern {
            domain 'megaupload.com'
        }

        action {
            $URI = browse (uri: $HTTP.target($URI)) { $('a.mvlink').@href }
        }
    }
    */	
    profile ('Megavideo') {
        pattern {
			log.info("even cooler stuff")
            domain 'megavideo.com'
        }
        action {
			log.info("cool stuff")
			$URI = browse (uri: $HTTP.target($URI)) { $('flashvars.v\s*=\s*"([^"]+)').@script }
			log.stuff("megav $URI")
 //           set '-r': '24'
        }
    }
}