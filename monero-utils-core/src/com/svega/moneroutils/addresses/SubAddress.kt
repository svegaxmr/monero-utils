package com.svega.moneroutils.addresses

import com.svega.moneroutils.NetType

open class SubAddress(address: String, netType: NetType) : MoneroAddress(address, netType) {
    override val LENGTH = 95
    override val BYTES = 69
    init{
        println("Have subaddress $address on net $net")
        validate()
    }
}
