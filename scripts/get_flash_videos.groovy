script {
    profile ('Get Flash Videos') {
        pattern {
            match { PERL && GET_FLASH_VIDEOS }
            domains([ 'wimp.com', 'megavideo.com' ]) // &c.
        }

        action {
			if(!$URI.contains("megavideo.com/files")) {
				// only if we haven't done this yet
				$URI = quoteURI($URI)
				$DOWNLOADER = "$PERL $GET_FLASH_VIDEOS --quality high --quiet --yes --filename $DOWNLOADER_OUT ${$URI}"
			}
        }
    }
}
