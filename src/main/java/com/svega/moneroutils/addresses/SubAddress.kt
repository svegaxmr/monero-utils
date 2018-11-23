package com.svega.moneroutils.addresses

import com.svega.moneroutils.AddressType
import com.svega.moneroutils.NetType

@ExperimentalUnsignedTypes
class SubAddress: MoneroAddress{
    constructor(address: String, netType: NetType): super(address, netType)
    constructor(key: FullKey, net: NetType) : super(key, net, AddressType.SUBADDRESS)

    override val LENGTH = 95
    override val BYTES = 69
    init{
        validate()
    }
}
