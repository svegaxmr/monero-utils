package com.svega.moneroutils.addresses

import com.svega.moneroutils.NetType
import com.svega.moneroutils.asString

class IntegratedAddress : MoneroAddress {
    private var paymentID = ""
    constructor(address: String, net: NetType) : super(address, net) {
        paymentID = bytes.sliceArray(IntRange(65, 72)).asString()
        this.address = address
        TODO("Cannot extract main address from integrated addresses yet")
    }
}