script {
    PPLIVE = 'C:\\Program Files (x86)\\PPLive\\PPTV\\PPLive.exe'

    profile ('PPLive') {
        pattern {
           protocol 'synacast'
        }

        action {
            $HOOK = "$PPLIVE \"${$URI}\""
            $URI = 'http://127.0.0.1:8888'
        }
    }
}
