package com.svega.moneroutils.transactions

import com.svega.moneroutils.AmountDivision
import com.svega.moneroutils.MoneroException
import com.svega.moneroutils.XMRAmount
import com.svega.moneroutils.blocks.getVarInt
import com.svega.moneroutils.blocks.getVarLong
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

open class CoinbaseTransaction: MoneroTransaction(arrayListOf()){
    lateinit var header: CoinbaseTransactionHeader
    var amt = -1L
    var txVer = -1
    var extra = ByteArray(0)
    val keys = ArrayList<ByteArray>()
    companion object {
        fun parseFromBlob(bb: ByteBuffer): CoinbaseTransaction {
            val ret = CoinbaseTransaction()
            with(ret){
                header = CoinbaseTransactionHeader.parseFromBlob(bb)
                if(header.numIns != 1)
                    throw MoneroException("Coinbase tx'es can only have 1 in, has ${header.numIns}!")
                amt = bb.getVarLong()
                txVer = bb.getVarInt()
                val txid = ByteArray(32)
                for(i in 0 until header.numOuts){
                    bb.get(txid)
                    keys.add(Arrays.copyOf(txid, 32))
                }
                println(header.toString())
                println(toString())
            }
            return ret
        }
    }

    override fun toString(): String {
        return "CoinbaseTransaction(amt=${XMRAmount.toOther(XMRAmount(amt), AmountDivision.WHOLE)}, txVer=$txVer)"
    }
}