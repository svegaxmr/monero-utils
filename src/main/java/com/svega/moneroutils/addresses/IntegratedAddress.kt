package com.svega.moneroutils.addresses

import com.svega.moneroutils.*
import com.svega.moneroutils.crypto.slowhash.Keccak
import com.svega.moneroutils.exceptions.MoneroException

@ExperimentalUnsignedTypes
class IntegratedAddress : MoneroAddress {
    override val LENGTH = ADDRESS_LENGTH
    override val BYTES = ADDRESS_BYTES
    var paymentID: ByteArray = ByteArray(8)
        set(value) {
            if (value.size != 8)
                throw MoneroException("Payment ID is size ${value.size}, needs to be 8")
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
        val mAddrKeys = UByteArray(1) { net.INTEGRATED } concat bytes.copyOfRange(1, 65)
        val mAddrCSum = Keccak.fullChecksum(mAddrKeys)
        val mAddrBytes = mAddrKeys concat mAddrCSum
        validateChecksum(mAddrBytes, "Integrated address splitting")
        mainAddressStr = Base58.encode(BinHexUtils.ubinaryToHex(mAddrBytes))
        validate()
    }

    constructor(mainAddress: MainAddress, paymentIDStr: String) : super(mainAddress.address, mainAddress.netType) {
        this.paymentIDStr = paymentIDStr
        this.mainAddressStr = mainAddress.address
        val mAddrKeys = Base58.decode(mainAddressStr).copyOfRange(1, 65)
        val toHash = UByteArray(1) { mainAddress.netType.INTEGRATED } concat mAddrKeys concat paymentID.asUByteArray()
        val checksum = Keccak.fullChecksum(toHash)
        bytes = toHash concat checksum
        MoneroAddress.validateChecksum(bytes, "Integrated address creation")
        address = Base58.encode(BinHexUtils.ubinaryToHex(bytes))
        validate()
    }

    override fun validate() {
        super.validate()
        if (paymentID.size == 32)
            throw MoneroException("Legacy payment ID's are not supported!")
        if (paymentID.size != 8)
            throw MoneroException("Payment ID's must be 8 bytes long!")
    }

    companion object {
        const val ADDRESS_LENGTH = 106
        const val ADDRESS_BYTES = 77
    }
}