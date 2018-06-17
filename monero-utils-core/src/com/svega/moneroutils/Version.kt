package com.svega.moneroutils

import com.svega.common.version.Extra

class Version: com.svega.common.version.Version(0,1,0,
        makeExtra(Extra.ALPHA, 1)){
    init {
        com.svega.common.version.Version.requires("com.svega.common", 0, 2)
        com.svega.common.version.Version.requires("com.svega.crypto.common", 0, 1)
        com.svega.common.version.Version.requires("com.svega.crypto.ed25519", 1, 0)
    }
}