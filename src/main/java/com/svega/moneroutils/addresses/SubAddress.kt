package com.svega.moneroutils.addresses

import com.svega.moneroutils.AddressType
import com.svega.moneroutils.NetType

@ExperimentalUnsignedTypes
class SubAddress : MoneroAddress {
    constructor(address: String, netType: NetType) : super(address, netType)
    constructor(key: FullKey, net: NetType) : super(key, net, AddressType.SUBADDRESS)

    override val LENGTH = ADDRESS_LENGTH
    override val BYTES = ADDRESS_BYTES

    init {
        validate()
    }

    companion object {
        const val ADDRESS_LENGTH = 95
        const val ADDRESS_BYTES = 69
    }
}
