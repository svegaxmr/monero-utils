package com.svega.moneroutils.transactions

import com.svega.moneroutils.AmountDivision
import com.svega.moneroutils.MoneroException
import com.svega.moneroutils.XMRAmount
import com.svega.moneroutils.blocks.getVarLong
import java.nio.ByteBuffer

open class CoinbaseTransaction: MoneroTransaction(arrayListOf()){
    lateinit var header: CoinbaseTransactionHeader
    var totalOut = XMRAmount(0)
        private set
    val outs = ArrayList<CoinbaseTXOut>()
        get() = ArrayList(field)
    companion object {
        fun parseFromBlob(bb: ByteBuffer): CoinbaseTransaction {
            val ret = CoinbaseTransaction()
            with(ret){
                header = CoinbaseTransactionHeader.parseFromBlob(bb)
                if(header.numIns != 1)
                    throw MoneroException("Coinbase tx'es can only have 1 in, has ${header.numIns}!")
                for(i in 0 until header.numOuts) {
                    val txo = CoinbaseTXOut.parseFromBlob(bb)
                    outs.add(txo)
                    totalOut += XMRAmount(txo.amt)
                }
            }
            return ret
        }
    }

    override fun toString(): String {
        var amt = 0L
        for (tx in outs){
            amt += tx.amt
        }
        return "CoinbaseTransaction(header=$header, amt=${XMRAmount(amt).toOther(AmountDivision.WHOLE)})"
    }
}

class CoinbaseTXOut{
    var amt = -1L
    var outType = (-1).toByte()
    val key = ByteArray(32)
    companion object {
        fun parseFromBlob(bb: ByteBuffer): CoinbaseTXOut{
            val ret = CoinbaseTXOut()
            with(ret){
                amt = bb.getVarLong()
                outType = bb.get()
                bb.get(key)
            }
            return ret
        }
    }
}