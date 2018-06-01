package com.svega.moneroutils.addresses

import com.svega.moneroutils.*
import com.svega.moneroutils.crypto.Keccak

abstract class MoneroAddress {
    protected var bytes: Array<UInt8>
    private var address: String
    val net: NetType
    abstract val BYTES: Int
    abstract val LENGTH: Int
    protected constructor(address: String, net: NetType){
        this.net = net
        this.address = address
        bytes = Base58.decode(address)
    }

    open fun validate(){
        if(address.length != LENGTH)
            throw MoneroException("Address $address is not $LENGTH characters long!")
        if(bytes.size != BYTES)
            throw MoneroException("Address $address is not $BYTES bytes long!")
        validateChecksum(bytes, address)
    }

    fun getAddressString() : String{
        return address
    }

    companion object {
        @Throws(MoneroException::class)
        fun stringToAddress(address: String) : MoneroAddress {
            val arr = Base58.decode(address)
            validateChecksum(arr, address)
            return when(arr[0]){
                NetType.MAINNET.MAINADDR -> MainAddress(address, NetType.MAINNET)
                NetType.MAINNET.INTEGRATED -> IntegratedAddress(address, NetType.MAINNET)
                NetType.MAINNET.SUBADDR -> SubAddress(address, NetType.MAINNET)
                NetType.TESTNET.MAINADDR -> MainAddress(address, NetType.TESTNET)
                NetType.TESTNET.INTEGRATED -> IntegratedAddress(address, NetType.TESTNET)
                NetType.TESTNET.SUBADDR -> SubAddress(address, NetType.TESTNET)
                NetType.STAGENET.MAINADDR -> MainAddress(address, NetType.STAGENET)
                NetType.STAGENET.INTEGRATED -> IntegratedAddress(address, NetType.STAGENET)
                NetType.STAGENET.SUBADDR -> SubAddress(address, NetType.STAGENET)
                else -> throw MoneroException("Address prefix ${arr[0]} is not a valid prefix")
            }
        }
        @Throws(MoneroException::class)
        fun validateChecksum(bytes: Array<UInt8>, address: String) {
            val checksum = Keccak.addressChecksum(bytes)
            if (!checksum.contentEquals(bytes.sliceArray(IntRange(bytes.size - 4, bytes.size - 1)))) {
                println(BinHexUtils.binaryToHex(checksum))
                println(BinHexUtils.binaryToHex(bytes.sliceArray(IntRange(bytes.size - 4, bytes.size - 1))))
                throw MoneroException("Invalid address $address fails checksum")
            }
        }
    }
}