package com.svega.moneroutils.transactions

import com.svega.moneroutils.blocks.getVarInt
import com.svega.moneroutils.blocks.getVarLong
import java.nio.ByteBuffer

class CoinbaseTransactionHeader {
    var ver = -1
    var ulTime = -1L
    var numIns = -1
    var inputType = -1
    var height  = -1
    var numOuts = -1
    companion object {
        fun parseFromBlob(bb: ByteBuffer): CoinbaseTransactionHeader {
            val ret = CoinbaseTransactionHeader()
            with(ret){
                ver = bb.getVarInt()
                ulTime = bb.getVarLong()
                numIns = bb.getVarInt()
                inputType = (bb.get().toInt() and 0xFF)
                height = bb.getVarInt()
                numOuts = bb.getVarInt()
            }
            return ret
        }
    }

    override fun toString(): String {
        return "CoinbaseTransactionHeader(ver=$ver, ulTime=$ulTime, numIns=$numIns, inputType=$inputType, height=$height, numOuts=$numOuts)"
    }
}