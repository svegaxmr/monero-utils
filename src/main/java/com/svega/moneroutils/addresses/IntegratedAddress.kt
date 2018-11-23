package com.svega.moneroutils.addresses

import com.svega.moneroutils.*
import com.svega.moneroutils.crypto.slowhash.Keccak

@ExperimentalUnsignedTypes
class IntegratedAddress : MoneroAddress {
    override val LENGTH = 106
    override val BYTES = 77
    var paymentID: ByteArray = ByteArray(8)
        set(value){
            if(value.size != 8)
                throw MoneroException("Value is size ${value.size}, needs to be 8")
            field = value
        }
    var paymentIDStr: String
        get() = BinHexUtils.binaryToHex(paymentID)
        set(value) {
            paymentID = BinHexUtils.hexToByteArray(value)
        }
    var mainAddressStr = ""
        private set

    constructor(address: String, net: NetType) : super(address, net) {
        paymentID = bytes.asByteArray().sliceArray(IntRange(65, 72))
        val mAddrKeys = UByteArray(1) {net.INTEGRATED} concat bytes.copyOfRange(1, 65)
        val mAddrCSum = Keccak.fullChecksum(mAddrKeys)
        val mAddrBytes = mAddrKeys concat mAddrCSum
        validateChecksum(mAddrBytes,"Integrated address splitting")
        mainAddressStr = Base58.encode(BinHexUtils.binaryToHex(mAddrBytes))
        validate()
    }

    constructor(mainAddress: MainAddress, paymentIDStr: String) : super(mainAddress.address, mainAddress.net){
        this.paymentIDStr = paymentIDStr
        this.mainAddressStr = mainAddress.address
        val mAddrKeys = Base58.decode(mainAddressStr).copyOfRange(1, 65)
        val toHash = UByteArray(1) {mainAddress.net.INTEGRATED} concat mAddrKeys concat paymentID.asUByteArray()
        val checksum = Keccak.fullChecksum(toHash)
        bytes = toHash concat checksum
        MoneroAddress.validateChecksum(bytes, "Integrated address creation")
        address = Base58.encode(BinHexUtils.binaryToHex(bytes))
        validate()
    }

    override fun validate(){
        super.validate()
        if(paymentID.size == 32)
            throw MoneroException("Legacy payment ID's are not supported!")
        if(paymentID.size != 8)
            throw MoneroException("Payment ID's must be 8 bytes long!")
    }
}