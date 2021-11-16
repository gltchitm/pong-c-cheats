package com.github.gltchitm.pongccheatstcpauthserver

import com.xenomachina.argparser.ArgParser

class Args(parser: ArgParser) {
    val disableWebPortal by parser.flagging("--disable-web-portal", help = "Disable the web portal")
    val disableSignups by parser.flagging("--disable-signups", help = "Disable signups")
}
