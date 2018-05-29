package com.svega.moneroutils.addresses

import com.svega.moneroutils.Base58
import com.svega.moneroutils.MoneroException
import com.svega.moneroutils.NetType
import com.svega.moneroutils.UInt8

abstract class MoneroAddress {
    protected val bytes: Array<UInt8>
    protected var address: String
    protected val net: NetType
    protected constructor(address: String, net: NetType){
        this.net = net
        this.address = address
        bytes = Base58.decode(address)
    }
    companion object {
        @Throws(MoneroException::class)
        fun stringToAddress(address: String) : MoneroAddress {
            val arr = Base58.decode(address)
            return when(arr[0]){
                NetType.MAINNET.MAINADDR -> MainAddress(address, NetType.MAINNET)
                NetType.MAINNET.INTEGRATED -> IntegratedAddress(address, NetType.MAINNET)
                NetType.MAINNET.SUBADDR -> TODO("Subaddresses are not implemented") //SubAddress(address)
                NetType.TESTNET.MAINADDR -> MainAddress(address, NetType.TESTNET)
                NetType.TESTNET.INTEGRATED -> IntegratedAddress(address, NetType.TESTNET)
                NetType.TESTNET.SUBADDR -> TODO("Subaddresses are not implemented") //SubAddress(address)
                NetType.STAGENET.MAINADDR -> MainAddress(address, NetType.STAGENET)
                NetType.STAGENET.INTEGRATED -> IntegratedAddress(address, NetType.STAGENET)
                NetType.STAGENET.SUBADDR -> TODO("Subaddresses are not implemented") //SubAddress(address)
                else -> throw MoneroException("Address prefix ${arr[0]} is not a valid prefix")
            }
        }
    }
}