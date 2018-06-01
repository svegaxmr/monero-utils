package com.svega.moneroutils.addresses

import com.svega.moneroutils.NetType

open class MainAddress(address: String, net: NetType): MoneroAddress(address, net) {
    override val LENGTH = 95
    override val BYTES = 69
    init{
        validate()
    }
}