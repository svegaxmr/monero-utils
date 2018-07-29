package com.svega.moneroutils.addresses

import com.svega.moneroutils.AddressType
import com.svega.moneroutils.NetType

open class MainAddress: MoneroAddress {
    constructor(address: String, net: NetType): super(address, net)
    constructor(key: FullKey, net: NetType, addrType: AddressType): super(key, net, addrType)
    constructor(seed: ByteArray? = null, secretSpendKey: SecretKey? = null, net: NetType):
            super(seed, secretSpendKey, net, AddressType.MAIN)
    override val LENGTH = 95
    override val BYTES = 69
    init{
        validate()
    }
}