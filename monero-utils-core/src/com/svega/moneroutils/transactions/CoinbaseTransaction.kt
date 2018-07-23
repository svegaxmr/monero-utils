package com.svega.moneroutils.transactions

import com.svega.moneroutils.AmountDivision
import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.MoneroException
import com.svega.moneroutils.XMRAmount
import com.svega.moneroutils.blocks.getVarInt
import com.svega.moneroutils.blocks.getVarLong
import java.nio.ByteBuffer

open class CoinbaseTransaction(out: TransactionOutput): MoneroTransaction(arrayListOf(out)){
    companion object {
        fun parseFromBlob(bb: ByteBuffer){
            val ver = bb.getVarInt()
            val unlockTime = bb.getVarInt()
            val numIns = bb.getVarInt()
            val inputType = (bb.get().toInt() and 0xFF)
            val height = bb.getVarInt()
            val numOuts = bb.getVarInt()
            println("Coinbase ver $ver unlocks at $unlockTime, is number of ins $numIns, type $inputType " +
                    "at height $height with $numOuts numOuts")
            if(numIns != 1)
                throw MoneroException("Coinbase tx'es can only have 1 in!")
            if(numOuts != 1)
                throw MoneroException("Coinbase tx'es can only have 1 out!")
            val amt = bb.getVarLong()
            val tver = bb.getVarInt()
            val key = ByteArray(32)
            bb.get(key)
            println("Coinbase TXOut: Amount ${XMRAmount.toOther(XMRAmount(amt), AmountDivision.WHOLE)}, " +
                    "ver $tver, key ${BinHexUtils.binaryToHex(key)}")
        }
    }
}