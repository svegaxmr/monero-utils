package com.svega.moneroutils.addresses

import com.svega.moneroutils.NetType

class SubAddress(address: String, netType: NetType) : MoneroAddress(address, netType) {
    override val LENGTH = 95
    override val BYTES = 69
    init{
        validate()
    }
}
