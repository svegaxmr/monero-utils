package com.svega.moneroutils.addresses

import com.svega.moneroutils.MoneroException
import com.svega.moneroutils.NetType

class MainAddress(address: String, net: NetType): MoneroAddress(address, net) {
    private val LENGTH = 95
    private val BYTES = 69
    init{
        if(address.length != LENGTH)
            throw MoneroException("Main address $address is not $LENGTH characters long!")
        if(bytes.size != BYTES)
            throw MoneroException("Main address $address is not $BYTES bytes long!")

    }
}