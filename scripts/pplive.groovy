script {
    profile ('PPLive') {
        pattern {
           protocol 'synacast'
        }

        action {
            $HOOK = "C:\\Program Files (x86)\\PPLive\\PPTV\\PPLive.exe ${$URI}"
            $URI = 'http://127.0.0.1:8888'
        }
    }
}
