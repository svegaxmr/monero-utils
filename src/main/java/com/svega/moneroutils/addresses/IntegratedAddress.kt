package com.svega.moneroutils.addresses

import com.svega.common.math.*
import com.svega.crypto.common.algos.Keccak
import com.svega.moneroutils.*

class IntegratedAddress : MoneroAddress {
    override val LENGTH = 106
    override val BYTES = 77
    var paymentID = ""
        private set
    var mainAddressStr = ""
        private set

    constructor(address: String, net: NetType) : super(address, net) {
        paymentID = bytes.sliceArray(IntRange(65, 72)).asString()
        val mAddrKeys = bytes.sliceArray(IntRange(0, 64))
        val mAddrCSum = Keccak.checksum(mAddrKeys)
        val mAddrBytes = mAddrKeys + mAddrCSum
        validateChecksum(mAddrBytes,"Integrated address splitting")
        mainAddressStr = Base58.encode(BinHexUtils.binaryToHex(mAddrBytes))
        validate()
    }
    constructor(mainAddress: MainAddress, paymentID: String) : super(mainAddress.address, mainAddress.net){
        this.paymentID = paymentID
        this.mainAddressStr = mainAddress.address
        val mAddrKeys = Base58.decode(mainAddressStr).sliceArray(IntRange(0, 64))
        val toHash = mAddrKeys + BinHexUtils.stringToBinary(paymentID)
        val checksum = Keccak.checksum(toHash)
        bytes = toHash + checksum
        MoneroAddress.validateChecksum(bytes, "Integrated address creation")
        address = Base58.encode(BinHexUtils.binaryToHex(bytes))
        validate()
    }

    override fun validate(){
        super.validate()
        if(paymentID.length == 32)
            throw MoneroException("Legacy payment ID's are not supported!")
        if(paymentID.length != 8)
            throw MoneroException("Payment ID's must be 8 bytes long!")
    }
}